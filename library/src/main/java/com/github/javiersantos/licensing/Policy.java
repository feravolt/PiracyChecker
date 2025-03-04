/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.github.javiersantos.licensing;

/**
 * Policy used by {@link LibraryChecker} to determine whether a user should have access to the
 * application.
 */
public interface Policy {

    /**
     * LICENSED means that the server returned back a valid license response
     */
    int LICENSED = 0x0B8A;
    /**
     * NOT_LICENSED means that the server returned back a valid license response that indicated that
     * the user definitively is not licensed
     */
    int NOT_LICENSED = 0x01B3;
    /**
     * RETRY means that the license response was unable to be determined --- perhaps as a result of
     * faulty networking
     */
    int RETRY = 0x0C48;

    /**
     * Provide results from contact with the license server. Retry counts are incremented if the
     * current value of response is RETRY. Results will be used for any future policy decisions.
     *
     * @param response the result from validating the server response
     * @param rawData  the raw server response data, can be null for RETRY
     */
    void processServerResponse(int response, ResponseData rawData);

    /**
     * Check if the user should be allowed access to the application.
     */
    boolean allowAccess();
}
