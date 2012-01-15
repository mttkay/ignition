package com.github.ignition.core.test;

import java.io.File;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.RobolectricTestRunner;

public class IgnitionCoreTestRunner extends RobolectricTestRunner {

    public IgnitionCoreTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new File("../../ignition-core-samples/RemoteImageViewSampleApp"));
    }
}
