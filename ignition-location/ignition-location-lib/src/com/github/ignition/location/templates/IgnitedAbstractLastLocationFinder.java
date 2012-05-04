package com.github.ignition.location.templates;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.github.ignition.location.annotations.IgnitedLocation;

public abstract class IgnitedAbstractLastLocationFinder implements ILastLocationFinder {
    protected Context context;
    protected LocationManager locationManager;
    protected Criteria criteria;

    @IgnitedLocation
    protected Location currentLocation;

    public IgnitedAbstractLastLocationFinder(Context context) {
        this.context = context.getApplicationContext();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        // Coarse accuracy is specified here to get the fastest possible result.
        // The calling Activity will likely (or have already) request ongoing
        // updates using the Fine location provider.
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLastBestLocation(int minDistance, long minTime,
            boolean refreshLocationIfLastIsTooOld) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
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
        // of the best result is wider than the acceptable maximum distance, request a
        // single update. This check simply implements the same conditions we set when
        // requesting regular location updates every [minTime] and [minDistance].
        if (refreshLocationIfLastIsTooOld && ((bestTime < minTime) || (bestAccuracy > minDistance))) {
            Log.d(LOG_TAG, "Last location is too old or too inaccurate. Retrieving a new one...");
            retrieveSingleLocationUpdate();
            if (bestResult != null) {
                Bundle extras = bestResult.getExtras();
                if (extras == null) {
                    extras = new Bundle();
                    bestResult.setExtras(extras);
                }
                extras.putBoolean(LAST_LOCATION_TOO_OLD_OR_INACCURATE_EXTRA, true);
            }
        }

        return bestResult;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}