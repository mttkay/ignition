package com.github.ignition.support.http.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;

import com.github.ignition.support.http.IgnitedHttpResponse;

/**
 * A response proxy returning data from a {@link HttpResponseCache}
 * 
 * @author Matthias Kaeppler
 */
public class CachedHttpResponse implements IgnitedHttpResponse {

    public static final class ResponseData {
        public ResponseData(int statusCode, byte[] responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        private int statusCode;
        private byte[] responseBody;

        public int getStatusCode() {
            return statusCode;
        }

        public byte[] getResponseBody() {
            return responseBody;
        }
    }

    private ResponseData cachedData;

    public CachedHttpResponse(ResponseData cachedData) {
        this.cachedData = cachedData;
    }

    @Override
    public String getHeader(String header) {
        return null;
    }

    @Override
    public InputStream getResponseBody() throws IOException {
        return new ByteArrayInputStream(cachedData.responseBody);
    }

    @Override
    public byte[] getResponseBodyAsBytes() throws IOException {
        return cachedData.responseBody;
    }

    @Override
    public String getResponseBodyAsString() throws IOException {
        return new String(cachedData.responseBody);
    }

    @Override
    public int getStatusCode() {
        return cachedData.statusCode;
    }

    @Override
    public HttpResponse unwrap() {
        return null;
    }

}
