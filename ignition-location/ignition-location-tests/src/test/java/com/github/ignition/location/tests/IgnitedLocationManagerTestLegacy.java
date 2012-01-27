package com.github.ignition.location.tests;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.runner.RunWith;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;

import com.github.ignition.support.IgnitedDiagnostics;

@RunWith(IgnitedLocationSampleActivityRobolectricTestRunner.class)
public class IgnitedLocationManagerTestLegacy extends IgnitedLocationManagerTest {

    @Override
    public void setUp() throws Exception {
        IgnitedDiagnostics.setTestApiLevel(IgnitedDiagnostics.DONUT);

        super.setUp();
    }

    @Override
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
}
