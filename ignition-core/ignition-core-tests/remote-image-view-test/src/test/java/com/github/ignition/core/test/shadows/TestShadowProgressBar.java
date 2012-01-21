package com.github.ignition.core.test.shadows;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ProgressBar;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowProgressBar;

@Implements(ProgressBar.class)
public class TestShadowProgressBar extends ShadowProgressBar {
    
    @Implementation
    public Drawable getIndeterminateDrawable() {
        return new BitmapDrawable();
    }
    
}
