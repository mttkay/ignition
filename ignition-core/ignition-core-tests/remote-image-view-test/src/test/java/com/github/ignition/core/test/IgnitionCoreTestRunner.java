package com.github.ignition.core.test;

import com.github.ignition.core.test.shadows.TestShadowProgressBar;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.lang.reflect.Method;

public class IgnitionCoreTestRunner extends RobolectricTestRunner {

    public IgnitionCoreTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new File("../../ignition-core-samples/remote-image-view-sample"));
    }
    
    @Override
    public void beforeTest(Method method) {
        Robolectric.bindShadowClass(TestShadowProgressBar.class);
        //Robolectric.bindShadowClass(RemoteImageLoaderShadow.class);
    }
}
