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

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.github.ignition.location.annotations.IgnitedLocation;
import com.github.ignition.location.templates.ILastLocationFinder;

/**
 * Legacy implementation of Last Location Finder for all Android platforms down to Android 1.6.
 * 
 * This class let's you find the "best" (most accurate and timely) previously detected location
 * using whatever providers are available.
 * 
 * Where a timely / accurate previous location is not detected it will return the newest location
 * (where one exists) and setup a one-off location update to find the current location.
 */
public class IgnitedLegacyLastLocationFinder implements ILastLocationFinder {
    protected static String LOG_TAG = IgnitedLegacyLastLocationFinder.class.getSimpleName();

    @SuppressWarnings("unused")
    @IgnitedLocation
    private Location currentLocation;

    protected LocationManager locationManager;
    protected Criteria criteria;
    protected Context context;

    /**
     * Construct a new Legacy Last Location Finder.
     * 
     * @param context
     *            Context
     */
    public IgnitedLegacyLastLocationFinder(Context appContext) {
        this.context = appContext;
        this.locationManager = (LocationManager) appContext
                .getSystemService(Context.LOCATION_SERVICE);
        this.criteria = new Criteria();
        // Coarse accuracy is specified here to get the fastest possible result.
        // The calling Activity will likely (or have already) request ongoing
        // updates using the Fine location provider.
        this.criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    }

    /**
     * Returns the most accurate and timely previously detected location. Where the last result is
     * beyond the specified maximum distance or latency a one-off location update is returned via
     * the {@link LocationListener} specified in {@link setChangedLocationListener}.
     * 
     * @param minDistance
     *            Minimum distance before we require a location update.
     * @param minTime
     *            Minimum time required between location updates.
     * @return The most accurate and / or timely previously detected location.
     */
    @Override
    public Location getLastBestLocation(Context context, int minDistance, long minTime) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MAX_VALUE;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = this.locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = this.locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if (((time < minTime) && (accuracy < bestAccuracy))) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if ((time > minTime) && (bestAccuracy == Float.MAX_VALUE)
                        && (time < bestTime)) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        // If the best result is beyond the allowed time limit, or the accuracy
        // of the best result is wider than the acceptable maximum distance,
        // request a single update.
        // This check simply implements the same conditions we set when
        // requesting regular location updates every [minTime] and
        // [minDistance].
        // Prior to Gingerbread "one-shot" updates weren't available, so we need
        // to implement this manually.
        if ((bestTime > minTime) || (bestAccuracy > minDistance)) {
            String provider = this.locationManager.getBestProvider(this.criteria, true);
            if (provider != null) {
                this.locationManager.requestLocationUpdates(provider, 0, 0,
                        this.singeUpdateListener, context.getMainLooper());
            }

            bestResult.getExtras().putBoolean(LAST_LOCATION_TOO_OLD_EXTRA, true);
        }

        return bestResult;
    }

    /**
     * This one-off {@link LocationListener} simply listens for a single location update before
     * unregistering itself. The one-off location update is returned via the
     * {@link LocationListener} specified in {@link setChangedLocationListener}.
     */
    protected LocationListener singeUpdateListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d(LOG_TAG,
                        "Single Location Update Received from " + location.getProvider()
                                + " (lat, long): " + location.getLatitude() + ", "
                                + location.getLongitude());
                setCurrentLocation(location);
                // if (context instanceof OnIgnitedLocationChangedListener) {
                // ((OnIgnitedLocationChangedListener) context).onIgnitedLocationChanged(location);
                // }
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
        locationManager.removeUpdates(this.singeUpdateListener);
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
