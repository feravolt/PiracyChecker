/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javiersantos.licensing.util;

import android.os.Build;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

public class URIQueryDecoder {

    /**
     * Decodes the query portion of the passed-in URI.
     *
     * @param encodedURI the URI containing the query to decode
     * @param results    a map containing all query parameters. Query parameters that do not have a value will
     *                   map to a null string
     */
    static public void DecodeQuery(URI encodedURI, Map<String, String> results) {
        Scanner scanner = new Scanner(encodedURI.getRawQuery());
        scanner.useDelimiter("&");
        while (scanner.hasNext()) {
            String param = scanner.next();
            String[] valuePair = param.split("=");
            String name = null, value = null;
            if (valuePair.length == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    value = URLDecoder.decode(valuePair[1], StandardCharsets.UTF_8);
                }
            } else {
                throw new IllegalArgumentException("query parameter invalid");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                name = URLDecoder.decode(valuePair[0], StandardCharsets.UTF_8);
            }
            results.put(name, value);
        }
    }
}