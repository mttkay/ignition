/*
 * Copyright 2011 Google Inc.
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

package com.github.ignition.location.utils;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;

/**
 * Provides support for initiating active and passive location updates optimized for the Gingerbread
 * release. Includes use of the Passive Location Provider.
 * 
 * Uses broadcast Intents to notify the app of location changes.
 */
public class GingerbreadLocationUpdateRequester extends FroyoLocationUpdateRequester {

    public GingerbreadLocationUpdateRequester(LocationManager locationManager) {
        super(locationManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestLocationUpdates(long minTime, long minDistance, Criteria criteria,
            PendingIntent pendingIntent) {
        super.requestLocationUpdates(minTime, minDistance, criteria, pendingIntent);
        // Gingerbread supports a location update request that accepts criteria
        // directly.
        // Note that we aren't monitoring this provider to check if it becomes
        // disabled - this is handled by the calling Activity.
        this.locationManager.requestLocationUpdates(minTime, minDistance, criteria, pendingIntent);
    }
}
