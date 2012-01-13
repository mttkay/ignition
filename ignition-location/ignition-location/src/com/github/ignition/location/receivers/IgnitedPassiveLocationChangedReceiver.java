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

package com.github.ignition.location.receivers;

import static com.github.ignition.location.IgnitedLocationConstants.SHARED_PREFERENCE_FILE;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.github.ignition.location.IgnitedLocationConstants;
import com.github.ignition.location.annotations.IgnitedLocation;
import com.github.ignition.location.utils.IgnitedLegacyLastLocationFinder;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce that a location change
 * has occurred while this application isn't visible.
 * 
 * Where possible, this is triggered by a Passive Location listener.
 */
public class IgnitedPassiveLocationChangedReceiver extends BroadcastReceiver {
    protected static String LOG_TAG = IgnitedPassiveLocationChangedReceiver.class.getSimpleName();

    @IgnitedLocation
    private Location currentLocation;

    /**
     * When a new location is received, extract it from the Intent and update the current location.
     * 
     * This is the Passive receiver, used to receive Location updates from third party apps when the
     * Activity is not visible.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String key = LocationManager.KEY_LOCATION_CHANGED;
        Location location = null;

        if (intent.hasExtra(key)) {
            // This update came from Passive provider, so we can extract the
            // location directly.
            location = (Location) intent.getExtras().get(key);

        } else {
            // This update came from a recurring alarm. We need to determine if
            // there has been a more recent Location received than the last
            // location we used.

            SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCE_FILE,
                    Context.MODE_PRIVATE);
            long locationUpdateInterval = prefs.getLong(
                    IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_INTERVAL,
                    IgnitedLocationConstants.LOCATION_UPDATES_INTERVAL_DEFAULT);
            int locationUpdateDistanceDiff = prefs.getInt(
                    IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_DISTANCE_DIFF,
                    IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT);

            // Get the best last location detected from the providers.
            IgnitedLegacyLastLocationFinder lastLocationFinder = new IgnitedLegacyLastLocationFinder(
                    context);
            location = lastLocationFinder.getLastBestLocation(context, locationUpdateDistanceDiff,
                    System.currentTimeMillis() - locationUpdateInterval);

            // Check if the last location detected from the providers is either
            // too soon, or too close to the last
            // value we used. If it is within those thresholds we set the
            // location to null to prevent the update
            // Service being run unnecessarily (and spending battery on data
            // transfers).
            if (currentLocation != null
                    && (currentLocation.getTime() > System.currentTimeMillis()
                            - locationUpdateInterval || currentLocation.distanceTo(location) < locationUpdateDistanceDiff)) {
                location = null;
            }
        }

        if (location != null) {
            Log.d(LOG_TAG, "Passively updating location...");
            currentLocation = location;
        }
    }
}