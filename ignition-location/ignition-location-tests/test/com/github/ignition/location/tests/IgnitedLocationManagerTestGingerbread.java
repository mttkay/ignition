package com.github.ignition.location.tests;

import org.junit.runner.RunWith;

import com.github.ignition.support.IgnitedDiagnostics;

@RunWith(LocationTestsRobolectricTestRunner.class)
public class IgnitedLocationManagerTestGingerbread extends IgnitedLocationManagerTest {

    @Override
    public void setUp() throws Exception {
        IgnitedDiagnostics.setTestApiLevel(IgnitedDiagnostics.GINGERBREAD);

        super.setUp();
    }
}
