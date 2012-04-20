package com.github.ignition.location.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;

import com.github.ignition.location.utils.lastlocationfinders.IgnitedGingerbreadLastLocationFinder;
import com.github.ignition.samples.location.R;
import com.github.ignition.support.IgnitedDiagnostics;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;

@RunWith(IgnitedLocationSampleActivityRobolectricTestRunner.class)
public class IgnitedLocationManagerGingerbreadTest extends AbstractIgnitedLocationManagerTest {

    @Override
    public void setUp() throws Exception {
        IgnitedDiagnostics.setTestApiLevel(IgnitedDiagnostics.GINGERBREAD);

        super.setUp();
    }

    @Test
    public void shouldRequestUpdatesFromGpsIfBatteryOkay() {
        sendBatteryLevelChangedBroadcast(10);

        resume();

        Map<PendingIntent, Criteria> locationPendingIntents = shadowLocationManager
                .getRequestLocationUdpateCriteriaPendingIntents();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        sendBatteryLevelChangedBroadcast(100);

        Intent intent = new Intent(Intent.ACTION_BATTERY_OKAY);
        shadowApp.sendBroadcast(intent);

        assertThat("Updates from " + LocationManager.GPS_PROVIDER
                + " provider should be requested when battery power is okay!",
                locationPendingIntents.containsValue(criteria));
    }

    @Test
    public void shouldDismissWaitForFixDialogWhenLocationIsAvailable() {
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, null);
        resume();

        sendMockLocationBroadcast(LocationManager.GPS_PROVIDER,
                IgnitedGingerbreadLastLocationFinder.SINGLE_LOCATION_UPDATE_ACTION);

        // boolean showing = Robolectric.shadowOf(ShadowDialog.getLatestDialog()).isShowing();
        // assertThat("Wait for fix dialog should be dismissed", showing, equalTo(false));
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        assertThat("Wait for fix dialog should be dismissed",
                shadowActivity.getDialogById(R.id.ign_loc_dialog_wait_for_fix), equalTo(null));
    }
}
