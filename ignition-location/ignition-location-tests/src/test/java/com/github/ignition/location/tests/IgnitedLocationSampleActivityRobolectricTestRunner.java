package com.github.ignition.location.tests;

import java.io.File;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.RobolectricTestRunner;

public class IgnitedLocationSampleActivityRobolectricTestRunner extends RobolectricTestRunner {

    public IgnitedLocationSampleActivityRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new File("../ignition-location-samples"));
    }
}
