package com.github.ignition.location.tasks;

import android.app.IntentService;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.ignition.location.IgnitedLocationConstants;
import com.github.ignition.location.annotations.IgnitedLocation;
import com.github.ignition.location.templates.ILastLocationFinder;
import com.github.ignition.location.utils.PlatformSpecificImplementationFactory;

public class IgnitedLastKnownLocationAsyncTask extends AsyncTask<Boolean, Void, Location> {
    private final int locationUpdateDistanceDiff;
    private final long locationUpdateInterval;
    @SuppressWarnings("unused")
    @IgnitedLocation
    private Location currentLocation;
    private ILastLocationFinder lastLocationFinder;

    /**
     * 
     * @param context
     * @param locationUpdateDistanceDiff
     * @param locationUpdateInterval
     */
    public IgnitedLastKnownLocationAsyncTask(Context context, int locationUpdateDistanceDiff,
            long locationUpdateInterval) {
        this.locationUpdateDistanceDiff = locationUpdateDistanceDiff;
        this.locationUpdateInterval = locationUpdateInterval;
        this.lastLocationFinder = PlatformSpecificImplementationFactory
                .getLastLocationFinder(context);
    }

    /**
     * Creates a new {@link IgnitedLastKnownLocationAsyncTask} object using
     * {@link IgnitedLocationConstants#LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT} and
     * {@link IgnitedLocationConstants#LOCATION_UPDATES_INTERVAL_DEFAULT}
     * 
     * @param context
     */
    public IgnitedLastKnownLocationAsyncTask(Context context) {
        this(context, IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT,
                IgnitedLocationConstants.LOCATION_UPDATES_INTERVAL_DEFAULT);
    }

    /**
     * @param refreshLocationIfLastLocationIsTooOld
     * @return the last known location
     */
    @Override
    protected Location doInBackground(Boolean... params) {
        return run(params);
    }

    /**
     * Typically you do not call this method directly; instead it's called by {@link #execute()} and
     * run on a worker thread. If however you want to execute the task synchronously - i.e.: from a
     * {@link IntentService} - you can invoke this method directly.
     * 
     * @param refreshLocationIfLastLocationIsTooOld
     * @return the last known location
     */
    public Location run(Boolean... refreshLocationIfLastLocationIsTooOld) {
        return getLastKnownLocation(refreshLocationIfLastLocationIsTooOld.length > 0 ? refreshLocationIfLastLocationIsTooOld[0]
                : Boolean.TRUE);
    }

    @Override
    protected void onPostExecute(Location lastKnownLocation) {
        if (lastKnownLocation != null) {
            Bundle extras = lastKnownLocation.getExtras();
            if (extras == null) {
                extras = new Bundle();
                lastKnownLocation.setExtras(extras);
            }
            extras.putBoolean(IgnitedLocationConstants.IGNITED_LAST_LOCATION_EXTRA, true);
        }
        currentLocation = lastKnownLocation;
    }

    /**
     * Find the last known location (using a {@link LastLocationFinder}) and updates the place list
     * accordingly.
     * 
     * @param refreshLocationIfLastIsTooOld
     * 
     */
    protected Location getLastKnownLocation(Boolean refreshLocationIfLastIsTooOld) {
        // Find the last known location, specifying a required accuracy
        // of within the min distance between updates
        // and a required latency of the minimum time required between
        // updates.
        Location lastKnownLocation = lastLocationFinder.getLastBestLocation(
                locationUpdateDistanceDiff, System.currentTimeMillis() - locationUpdateInterval,
                refreshLocationIfLastIsTooOld);

        return lastKnownLocation;
    }

    public ILastLocationFinder getLastLocationFinder() {
        return lastLocationFinder;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        lastLocationFinder.cancel();
    }
}
