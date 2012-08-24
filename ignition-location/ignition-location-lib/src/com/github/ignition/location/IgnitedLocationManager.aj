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
 * 
 * This code has been modified by Stefano Dacchille.
 */

package com.github.ignition.location;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.github.ignition.location.annotations.IgnitedLocation;
import com.github.ignition.location.annotations.IgnitedLocationActivity;
import com.github.ignition.location.receivers.IgnitedLocationChangedReceiver;
import com.github.ignition.location.receivers.IgnitedPassiveLocationChangedReceiver;
import com.github.ignition.location.tasks.IgnitedLastKnownLocationAsyncTask;
import com.github.ignition.location.templates.ILastLocationFinder;
import com.github.ignition.location.templates.IgnitedAbstractLastLocationFinder;
import com.github.ignition.location.templates.IgnitedAbstractLocationUpdateRequester;
import com.github.ignition.location.templates.OnIgnitedLocationChangedListener;
import com.github.ignition.location.utils.IgnitedLocationSupport;
import com.github.ignition.location.utils.PlatformSpecificImplementationFactory;
import com.github.ignition.support.IgnitedDiagnostics;

@SuppressAjWarnings
public aspect IgnitedLocationManager {
    public static final String LOG_TAG = IgnitedLocationManager.class.getSimpleName();

    declare parents : (@IgnitedLocationActivity *) implements OnIgnitedLocationChangedListener;

    private Criteria defaultCriteria, lowPowerCriteria;
    protected IgnitedAbstractLocationUpdateRequester locationUpdateRequester;
    protected PendingIntent locationListenerPendingIntent, locationListenerPassivePendingIntent;
    protected LocationManager locationManager;
    protected IgnitedLocationListener bestInactiveLocationProviderListener;

    private Context context;
    private volatile Location currentLocation;

    private IgnitedLastKnownLocationAsyncTask ignitedLastKnownLocationTask;
    private SharedPreferences prefs;
    private Handler handler;

    private long locationUpdatesInterval, passiveLocationUpdatesInterval;
    private int locationUpdatesDistanceDiff;
    private int passiveLocationUpdatesDistanceDiff;
    private boolean requestLocationUpdates = false;
    private boolean locationUpdatesDisabled = true;
    private boolean waitForFixDialogShown = false;
    private boolean noProvidersEnabledDialogShown = false;
    private boolean locationProviderDisabledReceiverRegistered = false;
    private boolean refreshLocationUpdatesReceiverRegistered = false;

    // Switch to another provider if gps doesn't return a location quickly enough.
    private Runnable removeGpsUpdates = new Runnable() {
        @Override
        public void run() {
            Log.d(LOG_TAG,
                    "It looks like GPS isn't available at this time (i.e.: maybe you're indoors). Removing GPS location updates and requesting network updates.");

            disableLocationUpdates(false);
            requestLocationUpdates(context, lowPowerCriteria());
        }
    };

    /**
     * If the Location Provider we're using to receive location updates is disabled while the app is
     * running, this Receiver will be notified, allowing us to re-register our Location Receivers
     * using the best available Location Provider is still available.
     */
    protected BroadcastReceiver locationProviderDisabledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean providerDisabled = !intent.getBooleanExtra(
                    LocationManager.KEY_PROVIDER_ENABLED, false);
            // Re-register the location listeners using the best available
            // Location Provider.
            if (providerDisabled) {
                disableLocationUpdates(false);
                requestLocationUpdates(context, null);
            }
        }
    };

    /**
     * If the battery state is low disable the passive location update receiver.
     */
    protected BroadcastReceiver refreshLocationUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            disableLocationUpdates(false);
            requestLocationUpdates(context, null);
        }
    };

    after(Context context, IgnitedLocationActivity ignitedAnnotation) : 
        execution(* Activity.onCreate(..)) && this(context)
        && @this(ignitedAnnotation) && within(@IgnitedLocationActivity *) {

        // Get a reference to the Context
        this.context = context;
        // Set pref file
        prefs = context.getSharedPreferences(IgnitedLocationConstants.SHARED_PREFERENCE_FILE,
                Context.MODE_PRIVATE);
        // Get references to the managers
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        handler = new Handler();

        // Specify the Criteria to use when requesting location updates while
        // the application is Active
        if (defaultCriteria == null) {
            defaultCriteria = new Criteria();
        }
        // Use gps if it's enabled and if battery level is at least 15%
        boolean useGps = prefs.getBoolean(IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_USE_GPS,
                IgnitedLocationConstants.USE_GPS_DEFAULT);
        if (useGps) {
            defaultCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            defaultCriteria.setPowerRequirement(Criteria.POWER_LOW);
        }

        // Setup the location update Pending Intent
        Intent activeIntent = new Intent(IgnitedLocationConstants.ACTIVE_LOCATION_UPDATE_ACTION);
        locationListenerPendingIntent = PendingIntent.getBroadcast(context, 0, activeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup the passive location update Pending Intent
        Intent passiveIntent = new Intent(context, IgnitedPassiveLocationChangedReceiver.class);
        locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0,
                passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Instantiate a Location Update Requester class based on the available
        // platform version. This will be used to request location updates.
        locationUpdateRequester = PlatformSpecificImplementationFactory
                .getLocationUpdateRequester(context);
    }

    private boolean isBatteryOk() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, filter);
        double currentLevel = 100.0;
        if (intent != null) {
            currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }

        return currentLevel >= prefs.getInt(IgnitedLocationConstants.SP_KEY_MIN_BATTERY_LEVEL,
                IgnitedLocationConstants.MIN_BATTERY_LEVEL_FOR_GPS_DEFAULT);
    }

    before(Context context, IgnitedLocationActivity ignitedAnnotation) : 
        execution(* Activity.onResume(..)) && this(context)
        && @this(ignitedAnnotation) && within(@IgnitedLocationActivity *) {
        // Get a reference to the Context if this context is null
        if (this.context == null) {
            this.context = context;
        }

        saveToPreferences(context, ignitedAnnotation);

        Log.d(LOG_TAG, "Retrieving last known location...");
        // Get the last known location. This isn't directly affecting the UI, so put it on a
        // worker thread.
        ignitedLastKnownLocationTask = new IgnitedLastKnownLocationAsyncTask(context,
                locationUpdatesDistanceDiff, locationUpdatesInterval);
        ignitedLastKnownLocationTask.execute();
    }

    /**
     * Save last settings to preferences.
     * 
     * @param context
     * @param locationAnnotation
     */
    private void saveToPreferences(Context context, IgnitedLocationActivity locationAnnotation) {
        requestLocationUpdates = locationAnnotation.requestLocationUpdates();
        locationUpdatesDistanceDiff = locationAnnotation.locationUpdatesDistanceDiff();
        locationUpdatesInterval = locationAnnotation.locationUpdatesInterval();
        passiveLocationUpdatesDistanceDiff = locationAnnotation
                .passiveLocationUpdatesDistanceDiff();
        passiveLocationUpdatesInterval = locationAnnotation.passiveLocationUpdatesInterval();
        boolean enablePassiveLocationUpdates = locationAnnotation.enablePassiveUpdates();
        boolean useGps = locationAnnotation.useGps();

        Editor editor = prefs.edit();
        editor.putBoolean(IgnitedLocationConstants.SP_KEY_ENABLE_LOCATION_UPDATES,
                requestLocationUpdates);
        editor.putBoolean(IgnitedLocationConstants.SP_KEY_ENABLE_PASSIVE_LOCATION_UPDATES,
                enablePassiveLocationUpdates);
        editor.putBoolean(IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_USE_GPS, useGps);
        editor.putInt(IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_DISTANCE_DIFF,
                locationUpdatesDistanceDiff);
        editor.putLong(IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_INTERVAL,
                locationUpdatesInterval);
        editor.putInt(IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF,
                passiveLocationUpdatesDistanceDiff);
        editor.putLong(IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_INTERVAL,
                passiveLocationUpdatesInterval);
        editor.putBoolean(IgnitedLocationConstants.SP_KEY_RUN_ONCE, true);
        editor.putInt(IgnitedLocationConstants.SP_KEY_MIN_BATTERY_LEVEL,
                locationAnnotation.minBatteryLevelForGps());
        editor.putLong(IgnitedLocationConstants.SP_KEY_WAIT_FOR_GPS_FIX_INTERVAL,
                locationAnnotation.waitForGpsFixInterval());
        editor.putBoolean(IgnitedLocationConstants.SP_KEY_SHOW_WAIT_FOR_LOCATION_DIALOG,
                locationAnnotation.showWaitForLocationDialog());
        editor.commit();

    }

    before(Activity activity, IgnitedLocationActivity ignitedAnnotation) : execution(* Activity.onPause(..)) 
        && @this(ignitedAnnotation) && this(activity) && within(@IgnitedLocationActivity *) {

        if (ignitedAnnotation.requestLocationUpdates()) {
            disableLocationUpdates(true);
            handler.removeCallbacks(removeGpsUpdates);
        }

        if (ignitedLastKnownLocationTask != null) {
            if (ignitedLastKnownLocationTask.getStatus() != AsyncTask.Status.FINISHED) {
                Log.d(LOG_TAG, "Cancel last location task");
                ignitedLastKnownLocationTask.cancel(true);
            }
            ignitedLastKnownLocationTask.getLastLocationFinder().cancel();
        }

        boolean finishing = activity.isFinishing();
        if (finishing) {
            context = null;
            // Be sure to reset every flag previously set (since an aspect is a
            // singleton by default, so its state will be shared across all Activities that
            // reference it).
            requestLocationUpdates = false;
            locationUpdatesDisabled = true;
            waitForFixDialogShown = false;
            noProvidersEnabledDialogShown = false;
            locationProviderDisabledReceiverRegistered = false;
            refreshLocationUpdatesReceiverRegistered = false;
        }
    }

    Location around() : get(@IgnitedLocation Location *) {
        return currentLocation;
    }

    void around(Location freshLocation) : set(@IgnitedLocation Location *) && args(freshLocation) 
        && within(IgnitedPassiveLocationChangedReceiver) && !adviceexecution() {

        currentLocation = freshLocation;
        Log.d(LOG_TAG, "New location from " + currentLocation.getProvider() + " (lat, lng/acc): "
                + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + "/"
                + currentLocation.getAccuracy());
    }

    void around(Location freshLocation) : set(@IgnitedLocation Location *) && args(freshLocation) 
        && (within(IgnitedLocationChangedReceiver) || within(IgnitedAbstractLastLocationFinder)
                || within(IgnitedLastKnownLocationAsyncTask)) && !adviceexecution() {

        if (context == null) {
            return;
        }

        final Activity activity = (Activity) context;
        if (IgnitedLocationSupport.getEnabledProviders(context).isEmpty()) {
            // On some older versions of Android if a dialog is not shown by the activity after
            // calling showDialog an IllegalArgunmentException is raised. Catch this exception since
            // the docs returning a null dialog should be allowed.
            try {
                activity.showDialog(R.id.ign_loc_dialog_no_providers_enabled);
                noProvidersEnabledDialogShown = true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return;
        }

        if (freshLocation == null) {
            // TODO Migrate this to DialogFragment at some point
            boolean showWaitForLocationDialog = prefs.getBoolean(
                    IgnitedLocationConstants.SP_KEY_SHOW_WAIT_FOR_LOCATION_DIALOG,
                    IgnitedLocationConstants.SHOW_WAIT_FOR_LOCATION_DIALOG_DEFAULT);
            if (showWaitForLocationDialog && !activity.isFinishing()) {
                // On some older versions of Android if a dialog is not shown by the activity after
                // calling showDialog an IllegalArgunmentException is raised. Catch this exception since
                // the docs returning a null dialog should be allowed.
                try {
                    activity.showDialog(R.id.ign_loc_dialog_wait_for_fix);
                    waitForFixDialogShown = true;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        currentLocation = freshLocation;
        Log.d(LOG_TAG, "New location from " + currentLocation.getProvider() + " (lat, lng/acc): "
                + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + "/" + currentLocation.getAccuracy());
        // TODO Migrate this to DialogFragment at some point
        if (noProvidersEnabledDialogShown) {
            activity.removeDialog(R.id.ign_loc_dialog_no_providers_enabled);
            noProvidersEnabledDialogShown = false;
        } else if (waitForFixDialogShown) {
            activity.removeDialog(R.id.ign_loc_dialog_wait_for_fix);
            waitForFixDialogShown = false;
        }
        boolean keepRequestingLocationUpdates = ((OnIgnitedLocationChangedListener) context)
                .onIgnitedLocationChanged(currentLocation);
        Bundle extras = freshLocation.getExtras();
        // a) If location updates shouldn't be requested anymore turn them off.
        // b) Don't turn location updates on if last location is too old, since the single location
        // update is running.
        if (!keepRequestingLocationUpdates) {
            disableLocationUpdates(true);
            // PlatformSpecificImplementationFactory.getLastLocationFinder(context).cancel();
            return;
        } else if (requestLocationUpdates
                && (extras == null || !extras.containsKey(ILastLocationFinder.LAST_LOCATION_TOO_OLD_OR_INACCURATE_EXTRA))) {
            // If we requested location updates, turn them on here.
            requestLocationUpdates(context, null);
        }

        // If gps is enabled location comes from gps, remove runnable that removes gps updates
        boolean lastLocation = extras != null && extras.containsKey(IgnitedLocationConstants.IGNITED_LAST_LOCATION_EXTRA);
        if (!lastLocation && defaultCriteria.getAccuracy() == Criteria.ACCURACY_FINE
                && currentLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            handler.removeCallbacks(removeGpsUpdates);
        }
    }

    /**
     * Start listening for location updates.
     */
    protected void requestLocationUpdates(Context context, Criteria criteria) {
        if (!locationUpdatesDisabled) {
            return;
        }
        Criteria locationUpdateCriteria = criteria;
        if (criteria == null) {
            if (isBatteryOk()) {
                locationUpdateCriteria = defaultCriteria;
            } else {
                locationUpdateCriteria = lowPowerCriteria();
            }
        }
        Log.d(LOG_TAG, "Disabling passive location updates");
        locationManager.removeUpdates(locationListenerPassivePendingIntent);

        Log.d(LOG_TAG, "Requesting location updates");
        // Normal updates while activity is visible.
        locationUpdateRequester.requestLocationUpdates(locationUpdatesInterval,
                locationUpdatesDistanceDiff, locationUpdateCriteria, locationListenerPendingIntent);

        // Register a receiver that listens for when the provider I'm using has
        // been disabled.
        IntentFilter locationProviderDisabledIntentFilter = new IntentFilter(
                IgnitedLocationConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED_ACTION);
        context.registerReceiver(locationProviderDisabledReceiver,
                locationProviderDisabledIntentFilter);
        locationProviderDisabledReceiverRegistered = true;

        IntentFilter refreshLocationUpdatesIntentFilter = new IntentFilter(
                IgnitedLocationConstants.UPDATE_LOCATION_UPDATES_CRITERIA_ACTION);
        context.registerReceiver(refreshLocationUpdatesReceiver, refreshLocationUpdatesIntentFilter);
        refreshLocationUpdatesReceiverRegistered = true;

        // Register a receiver that listens for when a better provider than I'm
        // using becomes available.
        String bestProvider = locationManager.getBestProvider(locationUpdateCriteria, false);
        String bestAvailableProvider = locationManager.getBestProvider(locationUpdateCriteria, true);
        if (bestProvider != null && !bestProvider.equals(bestAvailableProvider)) {
            bestInactiveLocationProviderListener = new IgnitedLocationListener(context);
            locationManager.requestLocationUpdates(bestProvider, 0, 0,
                    bestInactiveLocationProviderListener, context.getMainLooper());
        }

        if (bestAvailableProvider.equals(LocationManager.GPS_PROVIDER)) {
            Log.d(LOG_TAG, "Posting delayed remove GPS updates message");
            // Post a runnable that will remove gps updates if no gps location is returned after 1
            // minute in order to avoid draining the battery.
            handler.postDelayed(removeGpsUpdates, prefs.getLong(
                    IgnitedLocationConstants.SP_KEY_WAIT_FOR_GPS_FIX_INTERVAL,
                    IgnitedLocationConstants.WAIT_FOR_GPS_FIX_INTERVAL_DEFAULT));
        }

        locationUpdatesDisabled = false;
    }

    /**
     * Stop listening for location updates
     * 
     * @param enablePassiveLocationUpdates
     */
    protected void disableLocationUpdates(boolean requestPassiveLocationUpdates) {
        if (locationUpdatesDisabled) {
            return;
        }

        Log.d(LOG_TAG, "Disabling location updates");
        if (locationProviderDisabledReceiverRegistered) {
            context.unregisterReceiver(locationProviderDisabledReceiver);
            locationProviderDisabledReceiverRegistered = false;
        }
        if (refreshLocationUpdatesReceiverRegistered) {
            context.unregisterReceiver(refreshLocationUpdatesReceiver);
            refreshLocationUpdatesReceiverRegistered = false;
        }
        locationUpdateRequester.removeLocationUpdates();
        if (bestInactiveLocationProviderListener != null) {
            locationManager.removeUpdates(bestInactiveLocationProviderListener);
        }

        boolean finishing = ((Activity) context).isFinishing();

        if (requestPassiveLocationUpdates && !finishing) {
            requestPassiveLocationUpdates();
        }

        locationUpdatesDisabled = true;
    }

    private void requestPassiveLocationUpdates() {
        if (IgnitedDiagnostics.SUPPORTS_FROYO
                && prefs.getBoolean(
                        IgnitedLocationConstants.SP_KEY_ENABLE_PASSIVE_LOCATION_UPDATES,
                        IgnitedLocationConstants.ENABLE_PASSIVE_LOCATION_UPDATES_DEFAULT)) {
            Log.d(LOG_TAG, "Requesting passive location updates");
            // Passive location updates from 3rd party apps when the Activity isn't
            // visible. Only for Android 2.2+.
            locationUpdateRequester.requestPassiveLocationUpdates(passiveLocationUpdatesInterval,
                    passiveLocationUpdatesDistanceDiff, locationListenerPassivePendingIntent);
        }
    }

    public boolean isLocationUpdatesDisabled() {
        return locationUpdatesDisabled;
    }

    public Criteria lowPowerCriteria() {
        if (lowPowerCriteria == null) {
            lowPowerCriteria = new Criteria();
            lowPowerCriteria.setPowerRequirement(Criteria.POWER_LOW);
            lowPowerCriteria.setAccuracy(Criteria.NO_REQUIREMENT);
        }
        return lowPowerCriteria;
    }

    /**
     * If the best Location Provider (usually GPS) is not available when we request location
     * updates, this listener will be notified if / when it becomes available. It calls
     * requestLocationUpdates to re-register the location listeners using the better Location
     * Provider.
     */
    private class IgnitedLocationListener implements LocationListener {
        private Context context;

        public IgnitedLocationListener(Context appContext) {
            this.context = appContext;
        }

        @Override
        public void onLocationChanged(Location l) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Re-register the location listeners using the better Location
            // Provider.
            disableLocationUpdates(false);
            requestLocationUpdates(context, null);
        }
    }

}
