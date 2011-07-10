package com.github.ignition.http.cache;

import java.net.ConnectException;

import org.apache.http.client.methods.HttpUriRequest;

import com.github.ignition.http.IgnitedHttpRequest;
import com.github.ignition.http.IgnitedHttpResponse;

public class CachedHttpRequest implements IgnitedHttpRequest {

    private String url;

    public CachedHttpRequest(String url) {
        this.url = url;
    }

    public String getRequestUrl() {
        return url;
    }

    public IgnitedHttpRequest expecting(Integer... statusCodes) {
        return this;
    }

    public IgnitedHttpRequest retries(int retries) {
        return this;
    }

    public IgnitedHttpResponse send() throws ConnectException {
        return new CachedHttpResponse(url);
    }

    public HttpUriRequest unwrap() {
        return null;
    }

    public IgnitedHttpRequest withTimeout(int timeout) {
        return this;
    }
}
