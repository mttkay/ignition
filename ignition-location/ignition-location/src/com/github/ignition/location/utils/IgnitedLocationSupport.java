package com.github.ignition.location.utils;

import java.util.List;

import android.content.Context;
import android.location.LocationManager;

import com.github.ignition.support.IgnitedDiagnostics;

public class IgnitedLocationSupport {

    /*
     * Get all enabled "physical" providers (so don't include passive provider).
     */
    public static List<String> getEnabledProviders(Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        if (IgnitedDiagnostics.SUPPORTS_FROYO) {
            providers.remove(LocationManager.PASSIVE_PROVIDER);
        }

        return providers;
    }
}
