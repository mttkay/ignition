package com.github.ignition.location.tests;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;

public class IgnitedLocationSampleActivityRobolectricTestRunner extends RobolectricTestRunner {

    public IgnitedLocationSampleActivityRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new File("../ignition-location-samples/ignition-location-sample/"));
    }
}
