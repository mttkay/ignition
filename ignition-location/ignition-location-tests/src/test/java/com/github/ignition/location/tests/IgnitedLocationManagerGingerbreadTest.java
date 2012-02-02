package com.github.ignition.location.tests;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import com.github.ignition.support.IgnitedDiagnostics;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

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
}
