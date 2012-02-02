/* Copyright (c) 2009 Matthias Kaeppler
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

public class IgnitedHttpResponseImpl implements IgnitedHttpResponse {

    private HttpResponse response;
    private HttpEntity entity;

    public IgnitedHttpResponseImpl(HttpResponse response) throws IOException {
        this.response = response;
        HttpEntity temp = response.getEntity();
        if (temp != null) {
            entity = new BufferedHttpEntity(temp);
        }
    }

    public HttpResponse unwrap() {
        return response;
    }

    public InputStream getResponseBody() throws IOException {
        return entity.getContent();
    }

    public byte[] getResponseBodyAsBytes() throws IOException {
        return EntityUtils.toByteArray(entity);
    }

    public String getResponseBodyAsString() throws IOException {
        return EntityUtils.toString(entity);
    }

    public int getStatusCode() {
        return this.response.getStatusLine().getStatusCode();
    }

    public String getHeader(String header) {
        if (!response.containsHeader(header)) {
            return null;
        }
        return response.getFirstHeader(header).getValue();
    }
}
