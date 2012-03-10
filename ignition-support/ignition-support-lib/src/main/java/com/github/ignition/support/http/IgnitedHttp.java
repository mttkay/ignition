/* Copyright (c) 2009-2011 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ignition.support.http;

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
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.util.Log;

import com.github.ignition.support.IgnitedDiagnostics;
import com.github.ignition.support.cache.AbstractCache;
import com.github.ignition.support.http.cache.CachedHttpRequest;
import com.github.ignition.support.http.cache.HttpResponseCache;
import com.github.ignition.support.http.gzip.GzipHttpRequestInterceptor;
import com.github.ignition.support.http.gzip.GzipHttpResponseInterceptor;
import com.github.ignition.support.http.ssl.EasySSLSocketFactory;

public class IgnitedHttp {

    static final String LOG_TAG = IgnitedHttp.class.getSimpleName();

    public static final int DEFAULT_MAX_CONNECTIONS = 4;
    public static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;
    public static final int DEFAULT_WAIT_FOR_CONNECTION_TIMEOUT = 30 * 1000;
    public static final String DEFAULT_HTTP_USER_AGENT = "Android/Ignition";

    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ENCODING_GZIP = "gzip";

    private HashMap<String, String> defaultHeaders = new HashMap<String, String>();
    private AbstractHttpClient httpClient;

    private HttpResponseCache responseCache;

    public IgnitedHttp() {
        setupHttpClient();
    }

    public IgnitedHttp(Context context) {
        setupHttpClient();
        listenForConnectivityChanges(context);
    }

    protected void setupHttpClient() {
        BasicHttpParams httpParams = new BasicHttpParams();

        ConnManagerParams.setTimeout(httpParams, DEFAULT_WAIT_FOR_CONNECTION_TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(
                DEFAULT_MAX_CONNECTIONS));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);
        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, DEFAULT_HTTP_USER_AGENT);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        if (IgnitedDiagnostics.ANDROID_API_LEVEL >= 7) {
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
     * Registers a broadcast receiver with the application context that will take care of updating
     * proxy settings when failing over between 3G and Wi-Fi. <b>This requires the
     * {@link Manifest.permission#ACCESS_NETWORK_STATE} permission</b>.
     * 
     * @param context
     *            the context used to retrieve the app context
     */
    public void listenForConnectivityChanges(Context context) {
        context.getApplicationContext().registerReceiver(
                new ConnectionChangedBroadcastReceiver(this),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void setGzipEncodingEnabled(boolean enabled) {
        if (enabled) {
            httpClient.addRequestInterceptor(new GzipHttpRequestInterceptor());
            httpClient.addResponseInterceptor(new GzipHttpResponseInterceptor());
        } else {
            httpClient.removeRequestInterceptorByClass(GzipHttpRequestInterceptor.class);
            httpClient.removeResponseInterceptorByClass(GzipHttpResponseInterceptor.class);
        }
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
    public void enableResponseCache(int initialCapacity, long expirationInMinutes,
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
    public void enableResponseCache(Context context, int initialCapacity, long expirationInMinutes,
            int maxConcurrentThreads, int diskCacheStorageDevice) {
        enableResponseCache(initialCapacity, expirationInMinutes, maxConcurrentThreads);
        responseCache.enableDiskCache(context, diskCacheStorageDevice);
    }

    /**
     * Disables caching of HTTP responses. You may also choose to wipe any files that may have been
     * written to disk.
     */
    public void disableResponseCache(boolean wipe) {
        if (responseCache != null && wipe) {
            responseCache.clear();
        }
        responseCache = null;
    }

    /**
     * @return the response cache, if enabled, otherwise null
     */
    public synchronized HttpResponseCache getResponseCache() {
        return responseCache;
    }

    public void setHttpClient(AbstractHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public AbstractHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Updates the underlying HTTP client's proxy settings with what the user has entered in the APN
     * settings. This will be called automatically if {@link #listenForConnectivityChanges(Context)}
     * has been called. <b>This requires the {@link Manifest.permission#ACCESS_NETWORK_STATE}
     * permission</b>.
     * 
     * @param context
     *            the current context
     */
    public void updateProxySettings(Context context) {
        if (context == null) {
            return;
        }
        HttpParams httpParams = httpClient.getParams();
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = connectivity.getActiveNetworkInfo();
        if (nwInfo == null) {
            return;
        }
        Log.i(LOG_TAG, nwInfo.toString());
        if (nwInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            String proxyHost = Proxy.getHost(context);
            if (proxyHost == null) {
                proxyHost = Proxy.getDefaultHost();
            }
            int proxyPort = Proxy.getPort(context);
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

    public IgnitedHttpRequest get(String url) {
        return get(url, false);
    }

    public IgnitedHttpRequest get(String url, boolean cached) {
        if (cached && responseCache != null && responseCache.containsKey(url)) {
            return new CachedHttpRequest(responseCache, url);
        }
        return new HttpGet(this, url, defaultHeaders);
    }

    public IgnitedHttpRequest post(String url) {
        return new HttpPost(this, url, defaultHeaders);
    }

    public IgnitedHttpRequest post(String url, HttpEntity payload) {
        return new HttpPost(this, url, payload, defaultHeaders);
    }

    public IgnitedHttpRequest put(String url) {
        return new HttpPut(this, url, defaultHeaders);
    }

    public IgnitedHttpRequest put(String url, HttpEntity payload) {
        return new HttpPut(this, url, payload, defaultHeaders);
    }

    public IgnitedHttpRequest delete(String url) {
        return new HttpDelete(this, url, defaultHeaders);
    }

    public void setMaximumConnections(int maxConnections) {
        ConnManagerParams.setMaxTotalConnections(httpClient.getParams(), maxConnections);
    }

    /**
     * Adjust the connection timeout, i.e. the amount of time that may pass in order to establish a
     * connection with the server. Time unit is milliseconds.
     * 
     * @param connectionTimeout
     *            the timeout in milliseconds
     * @see CoreConnectionPNames#CONNECTION_TIMEOUT
     */
    public void setConnectionTimeout(int connectionTimeout) {
        ConnManagerParams.setTimeout(httpClient.getParams(), connectionTimeout);
    }

    /**
     * Adjust the socket timeout, i.e. the amount of time that may pass when waiting for data coming
     * in from the server. Time unit is milliseconds.
     * 
     * @param socketTimeout
     *            the timeout in milliseconds
     * @see CoreConnectionPNames#SO_TIMEOUT
     */
    public void setSocketTimeout(int socketTimeout) {
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), socketTimeout);
    }

    public void setDefaultHeader(String header, String value) {
        defaultHeaders.put(header, value);
    }

    public HashMap<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setPortForScheme(String scheme, int port) {
        Scheme _scheme = new Scheme(scheme, PlainSocketFactory.getSocketFactory(), port);
        httpClient.getConnectionManager().getSchemeRegistry().register(_scheme);
    }

}
