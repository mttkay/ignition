package com.github.ignition.location.utils;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.github.ignition.location.R;
import com.github.ignition.support.IgnitedDiagnostics;

public class IgnitedLocationSupport {
    /**
     * Gets all enabled "physical" providers (so don't include passive provider).
     * 
     * @param context
     * @return a list of all enabled providers except the passive provider.
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

    /**
     * Creates a wait for location dialog. By default dismissing the dialog will finish the
     * {@link Activity} that created it.
     * 
     * @param context
     * @return a wait for location dialog
     */
    public static Dialog createWaitForLocationDialog(final Context context) {
        return buildWaitForLocationDialog(context).create();
    }

    /**
     * Creates a builder for the wait for location dialog. By default dismissing the dialog will
     * finish the {@link Activity} that created it.
     * 
     * @param context
     * @return a builder for the wait for location dialog
     */
    public static Builder buildWaitForLocationDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ((Activity) context).finish();
            }
        });
        builder.setMessage(context.getString(R.string.ign_loc_dialog_wait_for_fix));
        return builder;
    }

    public static Builder createNoProvidersEnabledDialog(final Activity activity,
            final boolean finishActivityOnDismiss) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                } else {
                    dialog.cancel();
                    if (finishActivityOnDismiss) {
                        activity.finish();
                    }
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setPositiveButton(R.string.ign_loc_dialog_location_unavailable_change_settings,
                listener);
        builder.setNegativeButton(android.R.string.no, listener);
        builder.setCancelable(false);
        builder.setTitle(R.string.ign_loc_dialog_location_unavailable_title);
        builder.setMessage(R.string.ign_loc_dialog_location_unavailable_message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        return builder;
    }
}
