package com.github.ignition.location.tests;

import java.io.File;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.RobolectricTestRunner;

public class LocationTestsRobolectricTestRunner extends RobolectricTestRunner {

    public LocationTestsRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new File("../location-sample/"));
    }
}
