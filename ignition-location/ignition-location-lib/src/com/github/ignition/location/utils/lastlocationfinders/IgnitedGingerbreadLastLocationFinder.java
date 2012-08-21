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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import com.github.ignition.location.templates.IgnitedAbstractLastLocationFinder;

/**
 * Optimized implementation of Last Location Finder for devices running Gingerbread and above.
 * <p/>
 * This class let's you find the "best" (most accurate and timely) previously detected location
 * using whatever providers are available.
 * <p/>
 * Where a timely / accurate previous location is not detected it will return the newest location
 * (where one exists) and setup a oneshot location update to find the current location.
 */
public class IgnitedGingerbreadLastLocationFinder extends IgnitedAbstractLastLocationFinder {
    public static String SINGLE_LOCATION_UPDATE_ACTION = "com.github.ignition.location.SINGLE_LOCATION_UPDATE_ACTION";

    private PendingIntent singleUpatePI;

    private boolean singleUpdateReceiverRegistered = false;

    /**
     * Construct a new Gingerbread Last Location Finder.
     * 
     * @param context
     *            Context
     */
    public IgnitedGingerbreadLastLocationFinder(Context context) {
        super(context);

        // Construct the Pending Intent that will be broadcast by the oneshot
        // location update.
        Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
        singleUpatePI = PendingIntent.getBroadcast(context, 0, updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void retrieveSingleLocationUpdate() {
        Log.d(LOG_TAG, "Requesting Single Location Update...");
        IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
        context.registerReceiver(singleUpdateReceiver, locIntentFilter);
        singleUpdateReceiverRegistered = true;
        try {
            locationManager.requestSingleUpdate(criteria, singleUpatePI);
        } catch (IllegalArgumentException e) {
            // Fold back to the network provider, since we know it's the quickest and less battery
            // draining location provider.
            Log.e(LOG_TAG,
                    "no provider found for the supplied criteria, try requesting network update");
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager
                        .requestSingleUpdate(LocationManager.NETWORK_PROVIDER, singleUpatePI);
            }
        }
    }

    /**
     * This {@link BroadcastReceiver} listens for a single location update before unregistering
     * itself. The oneshot location update is returned via the {@link LocationListener} specified in
     * {@link setChangedLocationListener}.
     */
    protected BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterSingleUpdateReceiver();

            String key = LocationManager.KEY_LOCATION_CHANGED;
            Location location = (Location) intent.getExtras().get(key);

            if (location != null) {
                Log.d(LOG_TAG,
                        "Single Location Update Received from " + location.getProvider()
                                + " (lat, long/acc): " + location.getLatitude() + ", "
                                + location.getLongitude() + "/" + location.getAccuracy());
                setCurrentLocation(location);
            }

            IgnitedGingerbreadLastLocationFinder.this.locationManager.removeUpdates(singleUpatePI);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        Log.d(LOG_TAG, "Remove single update request");
        locationManager.removeUpdates(singleUpatePI);
        unregisterSingleUpdateReceiver();
    }

    private void unregisterSingleUpdateReceiver() {
        if (singleUpdateReceiverRegistered) {
            context.unregisterReceiver(singleUpdateReceiver);
            singleUpdateReceiverRegistered = false;
        }
    }
}
