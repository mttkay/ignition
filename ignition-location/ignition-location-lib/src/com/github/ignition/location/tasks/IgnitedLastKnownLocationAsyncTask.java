package com.github.ignition.location.tasks;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.ignition.location.IgnitedLocationConstants;
import com.github.ignition.location.annotations.IgnitedLocation;
import com.github.ignition.location.templates.ILastLocationFinder;
import com.github.ignition.location.utils.PlatformSpecificImplementationFactory;

public class IgnitedLastKnownLocationAsyncTask extends AsyncTask<Void, Void, Location> {
    private final int locationUpdateDistanceDiff;
    private final long locationUpdateInterval;
    @SuppressWarnings("unused")
    @IgnitedLocation
    private Location currentLocation;
    private ILastLocationFinder lastLocationFinder;

    /**
     * 
     * @param appContext
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

    @Override
    protected Location doInBackground(Void... params) {
        return getLastKnownLocation();
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
     */
    protected Location getLastKnownLocation() {
        // Find the last known location, specifying a required accuracy
        // of within the min distance between updates
        // and a required latency of the minimum time required between
        // updates.
        Location lastKnownLocation = lastLocationFinder.getLastBestLocation(
                locationUpdateDistanceDiff, System.currentTimeMillis() - locationUpdateInterval,
                false);

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
