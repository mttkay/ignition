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

package com.github.ignition.location.utils.lastlocationfinders;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.github.ignition.location.templates.IgnitedAbstractLastLocationFinder;

/**
 * Legacy implementation of Last Location Finder for all Android platforms down to Android 1.6.
 *
 * This class let's you find the "best" (most accurate and timely) previously detected location
 * using whatever providers are available.
 *
 * Where a timely / accurate previous location is not detected it will return the newest location
 * (where one exists) and setup a one-off location update to find the current location.
 */
public class IgnitedLegacyLastLocationFinder extends IgnitedAbstractLastLocationFinder {
    protected static String LOG_TAG = IgnitedLegacyLastLocationFinder.class.getSimpleName();

    /**
     * Construct a new Legacy Last Location Finder.
     *
     * @param context
     *            {@link android.content.Context}
     */
    public IgnitedLegacyLastLocationFinder(Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void retrieveSingleLocationUpdate() {
        Log.d(LOG_TAG, "Requesting Single Location Update...");
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 0, 0, singeUpdateListener,
                    context.getMainLooper());
        }
    }

    /**
     * This one-off {@link android.location.LocationListener} simply listens for a single location
     * update before unregistering itself. The one-off location update is returned via the
     * {@link android.location.LocationListener} specified in {@link setChangedLocationListener}.
     */
    protected LocationListener singeUpdateListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d(LOG_TAG,
                        "Single Location Update Received from " + location.getProvider()
                                + " (lat, long/acc): " + location.getLatitude() + ", "
                                + location.getLongitude() + "/" + location.getAccuracy());
                setCurrentLocation(location);
            }
            locationManager.removeUpdates(IgnitedLegacyLastLocationFinder.this.singeUpdateListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        Log.d(LOG_TAG, "Remove single update request");
        locationManager.removeUpdates(singeUpdateListener);
    }
}
