package com.github.ignition.location.tests;

import org.junit.runner.RunWith;

import com.github.ignition.support.IgnitedDiagnostics;

@RunWith(LocationTestsRobolectricTestRunner.class)
public class IgnitedLocationManagerTestLegacy extends IgnitedLocationManagerTest {

    @Override
    public void setUp() throws Exception {
        IgnitedDiagnostics.setTestApiLevel(IgnitedDiagnostics.DONUT);

        super.setUp();
    }
}
