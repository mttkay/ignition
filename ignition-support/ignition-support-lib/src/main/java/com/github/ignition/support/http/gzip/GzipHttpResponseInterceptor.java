package com.github.ignition.support.http.gzip;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import com.github.ignition.support.http.IgnitedHttp;

/**
 * Simple {@link HttpResponseInterceptor} that inflates response if GZIP encoding header.
 */
public class GzipHttpResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(final HttpResponse response, final HttpContext context) {
        // Inflate any responses compressed with gzip
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            final Header encoding = entity.getContentEncoding();
            if (encoding != null) {
                for (HeaderElement element : encoding.getElements()) {
                    if (element.getName().equalsIgnoreCase(IgnitedHttp.ENCODING_GZIP)) {
                        response.setEntity(new GzipInflatingEntity(response.getEntity()));
                        break;
                    }
                }
            }
        }
    }

}
