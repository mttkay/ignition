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

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;

import java.util.HashMap;
import java.util.Map;

class HttpPost extends IgnitedHttpRequestBase {

    HttpPost(IgnitedHttp ignitedHttp, String url, HashMap<String, String> defaultHeaders) {
        super(ignitedHttp);
        this.request = new org.apache.http.client.methods.HttpPost(url);
        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()){
            request.setHeader(entry.getKey(), entry.getValue());
        }
    }

    HttpPost(IgnitedHttp ignitedHttp, String url, HttpEntity payload,
            HashMap<String, String> defaultHeaders) {
        super(ignitedHttp);
        this.request = new org.apache.http.client.methods.HttpPost(url);
        ((HttpEntityEnclosingRequest) request).setEntity(payload);

        request.setHeader(HTTP_CONTENT_TYPE_HEADER, payload.getContentType().getValue());
        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()){
            request.setHeader(entry.getKey(), entry.getValue());
        }
    }

}
