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

import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import com.github.ignition.location.annotations.IgnitedLocation;
import com.github.ignition.location.templates.ILastLocationFinder;

/**
 * Optimized implementation of Last Location Finder for devices running Gingerbread and above.
 * <p/>
 * This class let's you find the "best" (most accurate and timely) previously detected location
 * using whatever providers are available.
 * <p/>
 * Where a timely / accurate previous location is not detected it will return the newest location
 * (where one exists) and setup a oneshot location update to find the current location.
 */
public class IgnitedGingerbreadLastLocationFinder implements ILastLocationFinder {
    protected static String SINGLE_LOCATION_UPDATE_ACTION = "com.github.ignition.location.SINGLE_LOCATION_UPDATE_ACTION";

    @SuppressWarnings("unused")
    @IgnitedLocation
    private Location currentLocation;

    protected PendingIntent singleUpatePI;
    protected LocationManager locationManager;
    protected Criteria criteria;

    /**
     * Construct a new Gingerbread Last Location Finder.
     * 
     * @param Appc
     *            Context
     */
    public IgnitedGingerbreadLastLocationFinder(Context appContext) {
        this.locationManager = (LocationManager) appContext
                .getSystemService(Context.LOCATION_SERVICE);
        // Coarse accuracy is specified here to get the fastest possible result.
        // The calling Activity will likely (or have already) request ongoing
        // updates using the Fine location provider.
        this.criteria = new Criteria();
        this.criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        // Construct the Pending Intent that will be broadcast by the oneshot
        // location update.
        Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
        this.singleUpatePI = PendingIntent.getBroadcast(appContext, 0, updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
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
        long bestTime = Long.MIN_VALUE;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = this.locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = this.locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                // Workaround to this bug: http://code.google.com/p/android/issues/detail?id=23937
                if (new Date(time).after(new Date(System.currentTimeMillis()))) {
                    time -= 1000 * 60 * 60 * 24;
                }

                if (((time > minTime) && (accuracy < bestAccuracy))) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if ((time < minTime) && (bestAccuracy == Float.MAX_VALUE)
                        && (time > bestTime)) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        // If the best result is beyond the allowed time limit, or the accuracy
        // of the
        // best result is wider than the acceptable maximum distance, request a
        // single update.
        // This check simply implements the same conditions we set when
        // requesting regular
        // location updates every [minTime] and [minDistance].
        if ((bestTime < minTime) || (bestAccuracy > minDistance)) {
            Log.d(LOG_TAG, "Last location is too old. Retrieving a new one...");
            IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
            context.registerReceiver(this.singleUpdateReceiver, locIntentFilter);
            this.locationManager.requestSingleUpdate(this.criteria, this.singleUpatePI);

            bestResult.getExtras().putBoolean(LAST_LOCATION_TOO_OLD_EXTRA, true);
        }

        return bestResult;
    }

    /**
     * This {@link BroadcastReceiver} listens for a single location update before unregistering
     * itself. The oneshot location update is returned via the {@link LocationListener} specified in
     * {@link setChangedLocationListener}.
     */
    protected BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(IgnitedGingerbreadLastLocationFinder.this.singleUpdateReceiver);

            String key = LocationManager.KEY_LOCATION_CHANGED;
            Location location = (Location) intent.getExtras().get(key);

            if (location != null) {
                Log.d(LOG_TAG,
                        "Single Location Update Received from " + location.getProvider()
                                + " (lat, long): " + location.getLatitude() + ", "
                                + location.getLongitude());
                setCurrentLocation(location);
            }

            IgnitedGingerbreadLastLocationFinder.this.locationManager
                    .removeUpdates(IgnitedGingerbreadLastLocationFinder.this.singleUpatePI);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        this.locationManager.removeUpdates(this.singleUpatePI);
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
