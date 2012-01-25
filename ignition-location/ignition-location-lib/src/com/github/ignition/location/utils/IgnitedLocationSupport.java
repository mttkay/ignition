package com.github.ignition.location.utils;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;

import com.github.ignition.location.R;
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

    public static boolean isFromGps(Location location) {
        return location.getProvider().equals(LocationManager.GPS_PROVIDER);
    }

    public static boolean isFromNetwork(Location location) {
        return location.getProvider().equals(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isGpsProviderEnabled(Context context) {
        return getEnabledProviders(context.getApplicationContext()).contains(
                LocationManager.GPS_PROVIDER);
    }

    public static boolean isNetworkProviderEnabled(Context context) {
        return getEnabledProviders(context.getApplicationContext()).contains(
                LocationManager.NETWORK_PROVIDER);
    }

    public static Dialog createWaitForLocationDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ((Activity) context).finish();
            }
        });
        dialog.setMessage(context.getString(R.string.ign_loc_dialog_wait_for_fix));
        return dialog;
    }
}
