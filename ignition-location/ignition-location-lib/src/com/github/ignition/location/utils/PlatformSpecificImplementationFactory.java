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

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static com.github.ignition.support.IgnitedDiagnostics.FROYO;
import static com.github.ignition.support.IgnitedDiagnostics.GINGERBREAD;
import android.app.AlarmManager;
import android.content.Context;
import android.location.LocationManager;

import com.github.ignition.location.templates.ILastLocationFinder;
import com.github.ignition.location.templates.IgnitedAbstractLocationUpdateRequester;
import com.github.ignition.location.utils.lastlocationfinders.IgnitedGingerbreadLastLocationFinder;
import com.github.ignition.location.utils.lastlocationfinders.IgnitedLegacyLastLocationFinder;
import com.github.ignition.location.utils.locationupdaterequesters.IgnitedFroyoLocationUpdateRequester;
import com.github.ignition.location.utils.locationupdaterequesters.IgnitedGingerbreadLocationUpdateRequester;
import com.github.ignition.location.utils.locationupdaterequesters.IgnitedLegacyLocationUpdateRequester;
import com.github.ignition.support.IgnitedDiagnostics;

/**
 * Factory class to create the correct instances of a variety of classes with platform specific
 * implementations.
 */
public class PlatformSpecificImplementationFactory {

    /**
     * Create a new LastLocationFinder instance
     * 
     * @param context
     *            {@link android.content.Context}
     * @return LastLocationFinder
     */
    public static ILastLocationFinder getLastLocationFinder(Context context) {
        return IgnitedDiagnostics.supportsApiLevel(GINGERBREAD) ? new IgnitedGingerbreadLastLocationFinder(
                context) : new IgnitedLegacyLastLocationFinder(context);
    }

    /**
     * Create a new LocationUpdateRequester
     * 
     * @param locationManager
     *            {@link android.location.LocationManager}
     * @return LocationUpdateRequester
     */
    public static IgnitedAbstractLocationUpdateRequester getLocationUpdateRequester(Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
        if (IgnitedDiagnostics.supportsApiLevel(GINGERBREAD)) {
            return new IgnitedGingerbreadLocationUpdateRequester(locationManager);
        } else if (IgnitedDiagnostics.supportsApiLevel(FROYO)) {
            return new IgnitedFroyoLocationUpdateRequester(locationManager);
        } else {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            return new IgnitedLegacyLocationUpdateRequester(locationManager, alarmManager);
        }
    }
}
