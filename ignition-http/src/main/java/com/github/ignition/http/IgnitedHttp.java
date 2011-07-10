package com.github.ignition.http;

import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.util.Log;

import com.github.ignition.cache.AbstractCache;
import com.github.ignition.http.cache.CachedHttpRequest;
import com.github.ignition.http.cache.HttpResponseCache;
import com.github.ignition.http.ssl.EasySSLSocketFactory;
import com.github.ignition.support.DiagnosticSupport;

public class IgnitedHttp {

    static final String LOG_TAG = "BetterHttp";

    public static final int DEFAULT_MAX_CONNECTIONS = 4;
    public static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;
    public static final String DEFAULT_HTTP_USER_AGENT = "Android/DroidFu";

    private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    private static HashMap<String, String> defaultHeaders = new HashMap<String, String>();
    private static AbstractHttpClient httpClient;
    private static Context appContext;

    private static HttpResponseCache responseCache;

    public static void setupHttpClient() {
        BasicHttpParams httpParams = new BasicHttpParams();

        ConnManagerParams.setTimeout(httpParams, socketTimeout);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
                new ConnPerRouteBean(maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, DEFAULT_HTTP_USER_AGENT);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        if (DiagnosticSupport.ANDROID_API_LEVEL >= 7) {
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        } else {
            // used to work around a bug in Android 1.6:
            // http://code.google.com/p/android/issues/detail?id=1946
            // TODO: is there a less rigorous workaround for this?
            schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
        }

        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, httpParams);
    }

    /**
     * Enables caching of HTTP responses. This will only enable the in-memory cache. If you also
     * want to enable the disk cache, see {@link #enableResponseCache(Context, int, long, int, int)}
     * .
     * 
     * @param initialCapacity
     *            the initial element size of the cache
     * @param expirationInMinutes
     *            time in minutes after which elements will be purged from the cache
     * @param maxConcurrentThreads
     *            how many threads you think may at once access the cache; this need not be an exact
     *            number, but it helps in fragmenting the cache properly
     * @see HttpResponseCache
     */
    public static void enableResponseCache(int initialCapacity, long expirationInMinutes,
            int maxConcurrentThreads) {
        responseCache = new HttpResponseCache(initialCapacity, expirationInMinutes,
                maxConcurrentThreads);
    }

    /**
     * Enables caching of HTTP responses. This will also enable the disk cache.
     * 
     * @param context
     *            the current context
     * @param initialCapacity
     *            the initial element size of the cache
     * @param expirationInMinutes
     *            time in minutes after which elements will be purged from the cache (NOTE: this
     *            only affects the memory cache, the disk cache does currently NOT handle element
     *            TTLs!)
     * @param maxConcurrentThreads
     *            how many threads you think may at once access the cache; this need not be an exact
     *            number, but it helps in fragmenting the cache properly
     * @param diskCacheStorageDevice
     *            where files should be cached persistently (
     *            {@link AbstractCache#DISK_CACHE_INTERNAL}, {@link AbstractCache#DISK_CACHE_SDCARD}
     *            )
     * @see HttpResponseCache
     */
    public static void enableResponseCache(Context context, int initialCapacity,
            long expirationInMinutes, int maxConcurrentThreads, int diskCacheStorageDevice) {
        enableResponseCache(initialCapacity, expirationInMinutes, maxConcurrentThreads);
        responseCache.enableDiskCache(context, diskCacheStorageDevice);
    }

    /**
     * @return the response cache, if enabled, otherwise null
     */
    public static HttpResponseCache getResponseCache() {
        return responseCache;
    }

    public static void setHttpClient(AbstractHttpClient httpClient) {
        IgnitedHttp.httpClient = httpClient;
    }

    public static AbstractHttpClient getHttpClient() {
        return httpClient;
    }

    public static void updateProxySettings() {
        if (appContext == null) {
            return;
        }
        HttpParams httpParams = httpClient.getParams();
        ConnectivityManager connectivity = (ConnectivityManager) appContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = connectivity.getActiveNetworkInfo();
        if (nwInfo == null) {
            return;
        }
        Log.i(LOG_TAG, nwInfo.toString());
        if (nwInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            String proxyHost = Proxy.getHost(appContext);
            if (proxyHost == null) {
                proxyHost = Proxy.getDefaultHost();
            }
            int proxyPort = Proxy.getPort(appContext);
            if (proxyPort == -1) {
                proxyPort = Proxy.getDefaultPort();
            }
            if (proxyHost != null && proxyPort > -1) {
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            } else {
                httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, null);
            }
        } else {
            httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, null);
        }
    }

    public static IgnitedHttpRequest get(String url) {
        return get(url, false);
    }

    public static IgnitedHttpRequest get(String url, boolean cached) {
        if (cached && responseCache != null && responseCache.containsKey(url)) {
            return new CachedHttpRequest(url);
        }
        return new HttpGet(httpClient, url, defaultHeaders);
    }

    public static IgnitedHttpRequest post(String url) {
        return new HttpPost(httpClient, url, defaultHeaders);
    }

    public static IgnitedHttpRequest post(String url, HttpEntity payload) {
        return new HttpPost(httpClient, url, payload, defaultHeaders);
    }

    public static IgnitedHttpRequest put(String url) {
        return new HttpPut(httpClient, url, defaultHeaders);
    }

    public static IgnitedHttpRequest put(String url, HttpEntity payload) {
        return new HttpPut(httpClient, url, payload, defaultHeaders);
    }

    public static IgnitedHttpRequest delete(String url) {
        return new HttpDelete(httpClient, url, defaultHeaders);
    }

    public static void setMaximumConnections(int maxConnections) {
        IgnitedHttp.maxConnections = maxConnections;
    }

    /**
     * Adjust the socket timeout, i.e. the amount of time that may pass when waiting for a server
     * response. Time unit is milliseconds.
     * 
     * @param socketTimeout
     *            the timeout in milliseconds
     */
    public static void setSocketTimeout(int socketTimeout) {
        IgnitedHttp.socketTimeout = socketTimeout;
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), socketTimeout);
    }

    public static int getSocketTimeout() {
        return socketTimeout;
    }

    public static void setDefaultHeader(String header, String value) {
        defaultHeaders.put(header, value);
    }

    public static HashMap<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public static void setContext(Context context) {
        if (appContext != null) {
            return;
        }
        appContext = context.getApplicationContext();
        appContext.registerReceiver(new ConnectionChangedBroadcastReceiver(), new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public static void setPortForScheme(String scheme, int port) {
        Scheme _scheme = new Scheme(scheme, PlainSocketFactory.getSocketFactory(), port);
        httpClient.getConnectionManager().getSchemeRegistry().register(_scheme);
    }

}
