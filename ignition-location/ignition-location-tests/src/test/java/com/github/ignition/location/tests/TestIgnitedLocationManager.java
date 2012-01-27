package com.github.ignition.location.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;

import com.github.ignition.location.IgnitedLocationConstants;
import com.github.ignition.samples.ui.IgnitedLocationSampleActivity;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import com.xtremelabs.robolectric.shadows.ShadowApplication.Wrapper;
import com.xtremelabs.robolectric.shadows.ShadowLocationManager;

public abstract class TestIgnitedLocationManager {
    protected ShadowApplication shadowApp;
    protected ShadowLocationManager shadowLocationManager;

    private IgnitedLocationSampleActivity activity;
    private Location lastKnownLocation;

    @Before
    public void setUp() throws Exception {
        activity = new IgnitedLocationSampleActivity();

        shadowApp = Robolectric.getShadowApplication();
        shadowLocationManager = Robolectric.shadowOf((LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE));

        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, true);
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);

        lastKnownLocation = getMockLocation();
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, lastKnownLocation);

        sendBatteryLevelChangedBroadcast(100);

        activity.onCreate(null);
    }

    @After
    public void tearDown() throws Exception {
        if (!activity.isFinishing()) {
            finish();
        }
    }

    protected Location getMockLocation() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(1.0);
        location.setLongitude(1.0);
        location.setAccuracy(50);
        return location;
    }

    protected Location sendMockLocationBroadcast(String provider) {
        return sendMockLocationBroadcast(provider, 50f);
    }

    protected Location sendMockLocationBroadcast(String provider, float accuracy) {
        Intent intent = new Intent(IgnitedLocationConstants.ACTIVE_LOCATION_UPDATE_ACTION);
        Location location = new Location(provider);
        location.setLatitude(2.0);
        location.setLongitude(2.0);
        location.setAccuracy(accuracy);
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        shadowApp.sendBroadcast(intent);

        return location;
    }

    protected void sendBatteryLevelChangedBroadcast(int level) {
        Intent intent = new Intent(Intent.ACTION_BATTERY_CHANGED);
        intent.putExtra(BatteryManager.EXTRA_LEVEL, level);
        intent.putExtra(BatteryManager.EXTRA_SCALE, 100);
        shadowApp.sendStickyBroadcast(intent);
    }

    @Test
    public void ignitedLocationIsCurrentLocation() {
        resume();

        assertThat(lastKnownLocation, equalTo(activity.getCurrentLocation()));
        Location newLocation = sendMockLocationBroadcast(LocationManager.GPS_PROVIDER);
        assertThat(newLocation, equalTo(activity.getCurrentLocation()));
    }

    @Test
    public void shouldActivelyRequestLocationUpdatesOnResume() {
        resume();

        List<Wrapper> receivers = shadowApp.getRegisteredReceivers();
        assertThat(receivers, notNullValue());
        boolean receiverRegistered = false;
        for (Wrapper receiver : receivers) {
            if (receiver.intentFilter.getAction(0).equals(
                    IgnitedLocationConstants.ACTIVE_LOCATION_UPDATE_ACTION)) {
                receiverRegistered = true;
                break;
            }
        }
        assertThat(receiverRegistered, is(true));
    }

    // TODO: find a better way to test this. Now the activity must be resumed twice or an Exception
    // will be thrown because one of the receivers is not registered.
    @Test
    public void shouldNotHaveRegisteredReceiverOnPause() throws Exception {
        resume();
        activity.onPause();

        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        shadowActivity.assertNoBroadcastListenersRegistered();

        resume();
    }

    // @Test
    // public void noTaskRunningOnFinish() {
    // // shadowApp.getBackgroundScheduler().pause();
    // ActivityManager activityManager = (ActivityManager) activity
    // .getSystemService(Context.ACTIVITY_SERVICE);
    // ShadowActivityManager shadowActivityManager = Robolectric.shadowOf(activityManager);
    // resume();
    // assertThat(shadowActivityManager.getRunningTasks(0).size(), equalTo(1));
    // }

    @Test
    public void shouldSaveSettingsToPreferences() {
        resume();

        SharedPreferences pref = activity.getSharedPreferences(
                IgnitedLocationConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        boolean followLocationChanges = pref.getBoolean(
                IgnitedLocationConstants.SP_KEY_ENABLE_PASSIVE_LOCATION_UPDATES, true);
        boolean useGps = pref.getBoolean(IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_USE_GPS,
                IgnitedLocationConstants.USE_GPS_DEFAULT);
        boolean runOnce = pref.getBoolean(IgnitedLocationConstants.SP_KEY_RUN_ONCE, true);
        int locUpdatesDistDiff = pref.getInt(
                IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_DISTANCE_DIFF,
                IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT);
        long locUpdatesInterval = pref.getLong(
                IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_INTERVAL,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT);
        int passiveLocUpdatesDistDiff = pref.getInt(
                IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT);
        long passiveLocUpdatesInterval = pref.getLong(
                IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_INTERVAL,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT);

        assertThat(followLocationChanges, is(true));
        assertThat(useGps, is(true));
        assertThat(runOnce, is(true));

        assertThat(IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT,
                equalTo(locUpdatesDistDiff));
        assertThat(IgnitedLocationConstants.LOCATION_UPDATES_INTERVAL_DEFAULT,
                equalTo(locUpdatesInterval));
        assertThat(IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT,
                equalTo(passiveLocUpdatesDistDiff));
        assertThat(IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT,
                equalTo(passiveLocUpdatesInterval));
    }

    @Test
    public void shouldRegisterListenerIfBestProviderDisabled() throws Exception {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, false);
        shadowLocationManager.setBestProvider(LocationManager.NETWORK_PROVIDER, true);

        resume();

        List<LocationListener> listeners = shadowLocationManager
                .getRequestLocationUpdateListeners();
        Assert.assertFalse(listeners.isEmpty());
    }

    @Test
    public void shouldNotRegisterListenerIfBestProviderEnabled() throws Exception {
        resume();

        List<LocationListener> listeners = shadowLocationManager
                .getRequestLocationUpdateListeners();
        assertThat("No listener must be registered, the best provider is enabled!",
                listeners.isEmpty());
    }

    @Test
    public void shouldRegisterLocationProviderDisabledReceiver() {
        resume();

        List<Wrapper> receivers = shadowApp.getRegisteredReceivers();
        assertThat(receivers, notNullValue());
        boolean receiverRegistered = false;
        for (Wrapper receiver : receivers) {
            if (receiver.intentFilter.getAction(0).equals(
                    IgnitedLocationConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED_ACTION)) {
                receiverRegistered = true;
                break;
            }
        }
        assertThat(receiverRegistered, is(true));
    }

    @Test
    public void shouldNotRequestUpdatesFromGpsIfBatteryLow() {
        sendBatteryLevelChangedBroadcast(10);

        resume();

        Map<PendingIntent, Criteria> locationPendingIntents = shadowLocationManager
                .getRequestLocationUdpateCriteriaPendingIntents();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        assertThat("Updates from " + LocationManager.GPS_PROVIDER
                + " provider shouldn't be requested when battery power is low!",
                !locationPendingIntents.containsValue(criteria));
    }

    @Test
    public void shouldSwitchToNetworkProviderIfBatteryLow() {
        resume();

        Map<PendingIntent, Criteria> locationPendingIntents = shadowLocationManager
                .getRequestLocationUdpateCriteriaPendingIntents();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        sendBatteryLevelChangedBroadcast(10);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_BATTERY_LOW);
        shadowApp.sendBroadcast(intent);

        sendBatteryLevelChangedBroadcast(100);

        assertThat("Updates from " + LocationManager.GPS_PROVIDER
                + " provider shouldn't be requested when battery power is low!",
                !locationPendingIntents.containsValue(criteria));
    }

    // @Test
    // public void shouldDisableLocationUpdatesIfOnIgnitedLocationChangedReturnsFalse() {
    // resume();
    //
    // assertThat("Location updates shouldn't be disabled at this point", !IgnitedLocationManager
    // .aspectOf().isLocationUpdatesDisabled());
    //
    // sendMockLocationBroadcast(LocationManager.GPS_PROVIDER, 10f);
    //
    // assertThat("Location updates should be disabled at this point", IgnitedLocationManager
    // .aspectOf().isLocationUpdatesDisabled());
    // }

    // @Test
    // public void requestLocationUpdatesFromAnotherProviderIfCurrentOneIsDisabled() {
    // // TODO
    // }

    @Test
    public void shouldUpdateDataOnNewLocation() {
        resume();

        int countBefore = activity.getLocationCount();
        sendMockLocationBroadcast(LocationManager.GPS_PROVIDER);
        int countAfter = activity.getLocationCount();
        assertThat(countAfter, equalTo(++countBefore));
    }

    // Helper methods
    protected void finish() {
        activity.finish();
        activity.onPause();
        activity.onStop();
        activity.onDestroy();
    }

    protected void resume() {
        activity.onStart();
        activity.onResume();
    }
}