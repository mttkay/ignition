package com.github.ignition.support.http.gzip;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import com.github.ignition.support.http.IgnitedHttp;

/**
 * Simple {@link HttpRequestInterceptor} that adds GZIP accept encoding header.
 */
public class GzipHttpRequestInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(final HttpRequest request, final HttpContext context) {
        // Add header to accept gzip content
        if (!request.containsHeader(IgnitedHttp.HEADER_ACCEPT_ENCODING)) {
            request.addHeader(IgnitedHttp.HEADER_ACCEPT_ENCODING, IgnitedHttp.ENCODING_GZIP);
        }
    }

}
