/*
 * Copyright 2011 Novoda Ltd.
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
 * Ignition-ized by Stefano Dacchille
 */

package com.github.ignition.samples.ui;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.github.ignition.location.IgnitedLocationConstants;
import com.github.ignition.location.annotations.IgnitedLocation;
import com.github.ignition.location.annotations.IgnitedLocationActivity;
import com.github.ignition.samples.R;
import com.github.ignition.samples.overlays.AccuracyCircleOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

@IgnitedLocationActivity
public class IgnitedLocationSampleActivity extends MapActivity {

    private TextView useGps;
    private TextView updates;
    private TextView passive;
    private TextView interval;
    private TextView distance;
    private TextView passiveInterval;
    private TextView passiveDistance;
    private MapView mapView;

    // private static final int FEEDBACK_DIALOG = 1;

    private List<Overlay> mapOverlays;
    private MapController mapController;
    // private long time;
    @IgnitedLocation
    private Location currentLocation;
    private LayoutInflater inflater;
    private TextView waitForGpsFix;
    private TextView minBatteryLevel;
    private ViewGroup viewGroup;

    // MUST BE OVERRIDDEN OR IGNITION LOCATION WON'T WORK!
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_update_list_activity);

        BugSenseHandler.setup(this, "930aa554");

        inflater = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));

        useGps = (TextView) findViewById(R.id.val_use_gps);
        updates = (TextView) findViewById(R.id.val_updates);
        interval = (TextView) findViewById(R.id.val_update_interval);
        passive = (TextView) findViewById(R.id.val_passive_updates);
        distance = (TextView) findViewById(R.id.val_update_distance);
        passiveInterval = (TextView) findViewById(R.id.val_passive_interval);
        passiveDistance = (TextView) findViewById(R.id.val_passive_distance);
        waitForGpsFix = (TextView) findViewById(R.id.val_wait_for_gps_fix);
        minBatteryLevel = (TextView) findViewById(R.id.val_min_battery_level);
        mapView = (MapView) findViewById(R.id.mapView);

        displayLocationSettings();
        mapView.setBuiltInZoomControls(false);
        mapController = mapView.getController();
        mapController.setZoom(17);
        mapOverlays = mapView.getOverlays();
    }

    // MUST BE OVERRIDDEN OR IGNITION LOCATION WON'T WORK!
    @Override
    public void onResume() {
        super.onResume();
    }

    // MUST BE OVERRIDDEN OR IGNITION LOCATION WON'T WORK!
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onIgnitedLocationChanged(Location newLocation) {
        displayNewLocation();
        update(newLocation);

        return true;
    }

    // protected Dialog onCreateDialog(int id) {
    // if(FEEDBACK_DIALOG == id) {
    // return new AlertDialog.Builder(this)
    // .setTitle("Help us getting better")
    // .setMessage("Did you get a good location quickly?")
    // .setCancelable(true)
    // .setPositiveButton("Yes", new Dialog.OnClickListener() {
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // analytics.trackPositiveFeedback();
    // LocationUpdateList.this.finish();
    // }
    // })
    // .setNegativeButton("No", new Dialog.OnClickListener() {
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // analytics.trackNegativeFeedback();
    // LocationUpdateList.this.finish();
    // }
    // }).create();
    // }
    // return super.onCreateDialog(id);
    // };

    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if ((keyCode == KeyEvent.KEYCODE_BACK)) {
    // showDialog(FEEDBACK_DIALOG);
    // return false;
    // }
    // return super.onKeyDown(keyCode, event);
    // }

    private void update(Location location) {
        mapOverlays.clear();
        int lat = (int) (location.getLatitude() * 1E6);
        int lon = (int) (location.getLongitude() * 1E6);
        final float accuracy = location.getAccuracy();
        GeoPoint point = new GeoPoint(lat, lon);
        mapController.setCenter(point);
        mapOverlays.add(new AccuracyCircleOverlay(point, accuracy));
    }

    private void displayLocationSettings() {
        SharedPreferences settings = getSharedPreferences(
                IgnitedLocationConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

        useGps.setText(getBooleanText(settings.getBoolean(
                IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_USE_GPS,
                IgnitedLocationConstants.USE_GPS_DEFAULT)));
        updates.setText(getBooleanText(settings.getBoolean(
                IgnitedLocationConstants.SP_KEY_ENABLE_LOCATION_UPDATES,
                IgnitedLocationConstants.ENABLE_LOCATION_UPDATES_DEFAULT)));
        passive.setText(getBooleanText(settings.getBoolean(
                IgnitedLocationConstants.SP_KEY_ENABLE_PASSIVE_LOCATION_UPDATES,
                IgnitedLocationConstants.ENABLE_PASSIVE_LOCATION_UPDATES_DEFAULT)));
        interval.setText(String.valueOf(settings.getLong(
                IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_INTERVAL,
                IgnitedLocationConstants.LOCATION_UPDATES_INTERVAL_DEFAULT) / 60000)
                + " mins");
        distance.setText(String.valueOf(settings.getInt(
                IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_DISTANCE_DIFF,
                IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT))
                + " m");
        passiveInterval.setText(String.valueOf(settings.getLong(
                IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_INTERVAL,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT) / 60000)
                + " mins");
        passiveDistance.setText(String.valueOf(settings.getInt(
                IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT))
                + " m");
        waitForGpsFix.setText(String.valueOf(settings.getLong(
                IgnitedLocationConstants.SP_KEY_WAIT_FOR_GPS_FIX_INTERVAL,
                IgnitedLocationConstants.WAIT_FOR_GPS_FIX_INTERVAL_DEFAULT) / 1000)
                + " secs");
        minBatteryLevel.setText(String.valueOf(settings.getInt(
                IgnitedLocationConstants.SP_KEY_MIN_BATTERY_LEVEL,
                IgnitedLocationConstants.MIN_BATTERY_LEVEL_DEFAULT))
                + "%");
    }

    private void displayNewLocation() {
        View block = inflater.inflate(R.layout.location_view, null);
        block.setTag(currentLocation);

        TextView time = (TextView) block.findViewById(R.id.val_time);
        TextView accuracy = (TextView) block.findViewById(R.id.val_acc);
        TextView provider = (TextView) block.findViewById(R.id.val_prov);
        TextView latitude = (TextView) block.findViewById(R.id.val_lat);
        TextView longitude = (TextView) block.findViewById(R.id.val_lon);

        time.setText(DateFormat.format("hh:mm:ss", new Date(currentLocation.getTime())));
        accuracy.setText(currentLocation.getAccuracy() + "m");
        provider.setText(currentLocation.getProvider());
        latitude.setText(String.valueOf(currentLocation.getLatitude()));
        longitude.setText(String.valueOf(currentLocation.getLongitude()));

        String providerName = currentLocation.getProvider();
        if (providerName.equalsIgnoreCase("network")) {
            block.setBackgroundResource(R.color.network);
        } else if (providerName.equalsIgnoreCase("gps")) {
            block.setBackgroundResource(R.color.gps);
        }

        block.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                update((Location) v.getTag());
            }
        });
        if (viewGroup == null) {
            viewGroup = (ViewGroup) findViewById(R.id.content);
        }
        viewGroup.addView(block, 0);
    }

    private String getBooleanText(boolean bool) {
        return bool ? "ON" : "OFF";
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public int getLocationCount() {
        return viewGroup.getChildCount();
    }
}
