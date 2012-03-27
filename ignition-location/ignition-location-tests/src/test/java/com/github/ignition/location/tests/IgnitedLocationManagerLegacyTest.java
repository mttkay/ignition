package com.github.ignition.location.tests;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;

import com.github.ignition.support.IgnitedDiagnostics;

@RunWith(IgnitedLocationSampleActivityRobolectricTestRunner.class)
public class IgnitedLocationManagerLegacyTest extends AbstractIgnitedLocationManagerTest {

    @Override
    @Before
    public void setUp() throws Exception {
        IgnitedDiagnostics.setTestApiLevel(IgnitedDiagnostics.DONUT);

        super.setUp();
    }

    @Test
    public void shouldRequestUpdatesFromGpsIfBatteryOkay() {
        sendBatteryLevelChangedBroadcast(10);

        resume();

        Map<PendingIntent, String> locationPendingIntents = shadowLocationManager
                .getRequestLocationUdpateProviderPendingIntents();

        sendBatteryLevelChangedBroadcast(100);

        Intent intent = new Intent(Intent.ACTION_BATTERY_OKAY);
        shadowApp.sendBroadcast(intent);

        assertThat("Updates from " + LocationManager.GPS_PROVIDER
                + " provider should be requested when battery power is okay!",
                locationPendingIntents.containsValue(LocationManager.GPS_PROVIDER));
    }

    // TODO don't see an easy way to test it, maybe this requires patching robolectric
    // @Test
    // public void shouldDismissWaitForFixDialogWhenLocationIsAvailable() {
    // shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, null);
    // resume();
    // }
}
