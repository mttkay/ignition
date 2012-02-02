package com.github.ignition.location.tests;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;
import com.github.ignition.support.IgnitedDiagnostics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

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
}
