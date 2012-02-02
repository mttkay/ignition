package com.github.ignition.core.test;

import android.graphics.drawable.Drawable;
import com.github.ignition.core.test.shadows.RemoteImageLoaderMock;
import com.github.ignition.core.test.shadows.TestShadowProgressBar;
import com.github.ignition.core.widgets.RemoteImageView;
import com.github.ignition.samples.remoteimageview.R;
import com.github.ignition.samples.remoteimageview.RemoteImageViewActivity;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(IgnitionCoreTestRunner.class)
public class RemoteImageViewTest {

    private RemoteImageViewActivity activity;
    private RemoteImageLoaderMock imageLoader;

    @Before
    public void before() {
        imageLoader = new RemoteImageLoaderMock();
        RemoteImageView.setSharedImageLoader(imageLoader);

        activity = new RemoteImageViewActivity();
        activity.onCreate(null);
    }

    @Test
    public void testCorrectAttributeInflationAndDefaulting() {
        RemoteImageView imageView = (RemoteImageView) activity.findViewById(R.id.image1);

        // default values for unsupplied attributes
        assertTrue(imageView.isAutoLoad());

        Drawable expectedErrorDrawable = Robolectric.application.getResources().getDrawable(
                RemoteImageView.DEFAULT_ERROR_DRAWABLE_RES_ID);
        assertEquals(expectedErrorDrawable, imageView.getErrorDrawable());

        Drawable expectedDefaultDrawable = Robolectric.application.getResources().getDrawable(
                RemoteImageView.DEFAULT_DRAWABLE_RES_ID);
        assertEquals(expectedDefaultDrawable, imageView.getDefaultDrawable());

        Drawable expectedProgressDrawable = new TestShadowProgressBar().getIndeterminateDrawable();
        assertEquals(expectedProgressDrawable, imageView.getProgressDrawable());

        assertEquals("http://developer.android.com/images/home/android-design.png",
                imageView.getImageUrl());
    }

    @Test
    public void canCustomizeTheProgressIndicator() {
        RemoteImageView imageView = (RemoteImageView) activity.findViewById(R.id.image2);

        Drawable expectedProgressDrawable = Robolectric.application.getResources().getDrawable(
                android.R.drawable.progress_indeterminate_horizontal);
        assertEquals(expectedProgressDrawable, imageView.getProgressDrawable());
    }

    @Test
    public void canCustomizeTheErrorDrawable() {
        RemoteImageView imageView = (RemoteImageView) activity.findViewById(R.id.image4);

        Drawable expectedErrorDrawable = Robolectric.application.getResources().getDrawable(
                android.R.drawable.stat_notify_error);
        assertEquals(expectedErrorDrawable, imageView.getErrorDrawable());
    }

    @Test
    public void canCustomizeTheDefaultDrawable() {
        RemoteImageView imageView = (RemoteImageView) activity.findViewById(R.id.image5);

        Drawable expectedDefaultDrawable = Robolectric.application.getResources().getDrawable(
                android.R.drawable.dialog_frame);
        assertEquals(expectedDefaultDrawable, imageView.getDefaultDrawable());
    }

    @Test
    public void testAutoLoadingOfImages() {
        RemoteImageView view1 = (RemoteImageView) activity.findViewById(R.id.image1);
        RemoteImageView view2 = (RemoteImageView) activity.findViewById(R.id.image2);
        RemoteImageView view3 = (RemoteImageView) activity.findViewById(R.id.image3);
        RemoteImageView view4 = (RemoteImageView) activity.findViewById(R.id.image4);
        RemoteImageView view5 = (RemoteImageView) activity.findViewById(R.id.image5);

        assertTrue(view1.isAutoLoad());
        assertTrue(imageLoader.isLoadImageCalled(view1));

        assertTrue(view2.isAutoLoad());
        assertTrue(imageLoader.isLoadImageCalled(view2));

        assertTrue(view3.isAutoLoad());
        assertTrue(imageLoader.isLoadImageCalled(view3));

        assertTrue(view4.isAutoLoad());
        assertTrue(imageLoader.isLoadImageCalled(view4));

        assertFalse(view5.isAutoLoad());
        assertFalse(imageLoader.isLoadImageCalled(view5));

        Robolectric.clickOn(activity.findViewById(R.id.load_image_button));
        assertTrue(imageLoader.isLoadImageCalled(view5));
    }

}
