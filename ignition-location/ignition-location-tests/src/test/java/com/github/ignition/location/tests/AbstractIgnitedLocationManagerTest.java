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
import com.github.ignition.samples.location.R;
import com.github.ignition.samples.location.ui.IgnitedLocationSampleActivity;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import com.xtremelabs.robolectric.shadows.ShadowApplication.Wrapper;
import com.xtremelabs.robolectric.shadows.ShadowLocationManager;

public abstract class AbstractIgnitedLocationManagerTest {
    private static final float DEFAULT_ACCURACY = 50f;

    protected ShadowApplication shadowApp;
    protected ShadowLocationManager shadowLocationManager;
    protected IgnitedLocationSampleActivity activity;

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
        return getMockLocation(1.0, 1.0);
    }

    protected Location getMockLocation(double lat, double lon) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAccuracy(DEFAULT_ACCURACY);
        return location;
    }

    protected Location sendMockLocationBroadcast(String provider) {
        return sendMockLocationBroadcast(provider, DEFAULT_ACCURACY,
                IgnitedLocationConstants.ACTIVE_LOCATION_UPDATE_ACTION);
    }

    protected Location sendMockLocationBroadcast(String provider, String action) {
        return sendMockLocationBroadcast(provider, DEFAULT_ACCURACY, action);
    }

    protected Location sendMockLocationBroadcast(String provider, float accuracy, String action) {
        Intent intent = new Intent(action);
        Location location = getMockLocation(2.0, 2.0);
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
    public void shouldShowWaitForFixDialogIfNoLocationAvailable() {
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, null);
        resume();

        assertThat("Wait for fix dialog should be shown", Robolectric.shadowOf(activity)
                .getLastShownDialogId(), equalTo(R.id.ign_loc_dialog_wait_for_fix));
    }

    @Test
    public void shouldShowNoEnabledProvidersDialogIfNoProviderAvailable()
            throws InterruptedException {
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
        resume();

        assertThat("No enabled providers dialog should be shown", Robolectric.shadowOf(activity)
                .getLastShownDialogId(), equalTo(R.id.ign_loc_dialog_no_providers_enabled));
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
                IgnitedLocationConstants.SP_KEY_ENABLE_PASSIVE_LOCATION_UPDATES, false);
        boolean useGps = pref.getBoolean(IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_USE_GPS,
                !IgnitedLocationConstants.USE_GPS_DEFAULT);
        boolean runOnce = pref.getBoolean(IgnitedLocationConstants.SP_KEY_RUN_ONCE, false);
        int locUpdatesDistDiff = pref.getInt(
                IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_DISTANCE_DIFF,
                IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT + 1);
        long locUpdatesInterval = pref.getLong(
                IgnitedLocationConstants.SP_KEY_LOCATION_UPDATES_INTERVAL,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT + 1);
        int passiveLocUpdatesDistDiff = pref.getInt(
                IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT + 1);
        long passiveLocUpdatesInterval = pref.getLong(
                IgnitedLocationConstants.SP_KEY_PASSIVE_LOCATION_UPDATES_INTERVAL,
                IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT + 1);
        long waitForGpsFixInterval = pref.getLong(
                IgnitedLocationConstants.SP_KEY_WAIT_FOR_GPS_FIX_INTERVAL,
                IgnitedLocationConstants.WAIT_FOR_GPS_FIX_INTERVAL_DEFAULT + 1);
        int minBatteryLevelToUseGps = pref.getInt(
                IgnitedLocationConstants.SP_KEY_MIN_BATTERY_LEVEL,
                IgnitedLocationConstants.MIN_BATTERY_LEVEL_FOR_GPS_DEFAULT + 1);
        boolean showWaitForLocationDialog = pref.getBoolean(
                IgnitedLocationConstants.SP_KEY_SHOW_WAIT_FOR_LOCATION_DIALOG,
                !IgnitedLocationConstants.SHOW_WAIT_FOR_LOCATION_DIALOG_DEFAULT);

        assertThat(followLocationChanges, is(true));
        assertThat(useGps, is(true));
        assertThat(runOnce, is(true));
        assertThat(showWaitForLocationDialog, is(true));
        assertThat(minBatteryLevelToUseGps,
                equalTo(IgnitedLocationConstants.MIN_BATTERY_LEVEL_FOR_GPS_DEFAULT));
        assertThat(waitForGpsFixInterval,
                equalTo(IgnitedLocationConstants.WAIT_FOR_GPS_FIX_INTERVAL_DEFAULT));
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