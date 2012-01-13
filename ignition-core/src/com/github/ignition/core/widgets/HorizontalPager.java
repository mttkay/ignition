package com.github.ignition.core.widgets;

/*
 * Copyright (C) 2010 Deez Apps!
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 * 
 * Based on http://android.git.kernel.org/?p=platform/packages/apps/Launcher.git;a=blob;f=src/com/android/launcher/Workspace.java
 *
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

import com.github.ignition.core.Ignition;
import com.github.ignition.core.R;

/**
 * User: jeanguy@gmail.com Date: Aug 8, 2010
 */
public class HorizontalPager extends ViewGroup {
    private static final int INVALID_SCREEN = -1;
    public static final int SPEC_UNDEFINED = -1;

    /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */
    private static final int SNAP_VELOCITY = 1000;

    private int pageWidthSpec, pageWidth;

    private boolean mFirstLayout = true;
    private boolean mHasPagerControl = true;

    private int mCurrentPage;
    private int mNextPage = INVALID_SCREEN;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private HorizontalPagerControl pagerControl;

    private int mTouchSlop;
    private int mMaximumVelocity;

    private float mLastMotionX;
    private float mLastMotionY;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private boolean mAllowLongPress;

    private Set<OnScrollListener> mListeners = new HashSet<OnScrollListener>();

    /**
     * Used to inflate the Workspace from XML.
     * 
     * @param context
     *            The application's context.
     * @param attrs
     *            The attribtues set containing the Workspace's customization values.
     */
    public HorizontalPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Used to inflate the Workspace from XML.
     * 
     * @param context
     *            The application's context.
     * @param attrs
     *            The attribtues set containing the Workspace's customization values.
     * @param defStyle
     *            Unused.
     */
    public HorizontalPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // TODO: cannot use styled attributes until issue #9656 is fixed
        // TypedArray a = context.obtainStyledAttributes(attrs,
        // R.styleable.com_deezapps_widget_HorizontalPager);
        // pageWidthSpec =
        // a.getDimensionPixelSize(R.styleable.com_deezapps_widget_HorizontalPager_pageWidth,
        // SPEC_UNDEFINED);
        // a.recycle();
        pageWidthSpec = attrs.getAttributeIntValue(Ignition.XMLNS, "pageWidth",
                SPEC_UNDEFINED);

        init();
    }

    /**
     * Initializes various states for this workspace.
     */
    private void init() {
        mScroller = new Scroller(getContext());
        mCurrentPage = 0;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    /**
     * Returns the index of the currently displayed page.
     * 
     * @return The index of the currently displayed page.
     */
    int getCurrentPage() {
        return mCurrentPage;
    }

    /**
     * Sets the current page.
     * 
     * @param currentPage
     */
    public void setCurrentPage(int currentPage) {
        mCurrentPage = Math.max(0, Math.min(currentPage, getChildCount()));
        scrollTo(getScrollXForPage(mCurrentPage), 0);
        invalidate();
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidthSpec = pageWidth;
    }

    /**
     * Gets the value that getScrollX() should return if the specified page is the current page (and
     * no other scrolling is occurring). Use this to pass a value to scrollTo(), for example.
     * 
     * @param whichPage
     * @return
     */
    private int getScrollXForPage(int whichPage) {
        return (whichPage * pageWidth) - pageWidthPadding();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else if (mNextPage != INVALID_SCREEN) {
            mCurrentPage = mNextPage;
            mNextPage = INVALID_SCREEN;
            clearChildrenCache();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        // ViewGroup.dispatchDraw() supports many features we don't need:
        // clip to padding, layout animation, animation listener, disappearing
        // children, etc. The following implementation attempts to fast-track
        // the drawing dispatch by drawing only what we know needs to be drawn.

        final long drawingTime = getDrawingTime();
        // todo be smarter about which children need drawing
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawChild(canvas, getChildAt(i), drawingTime);
        }

        for (OnScrollListener mListener : mListeners) {
            int adjustedScrollX = getScrollX() + pageWidthPadding();
            mListener.onScroll(adjustedScrollX);
            if (adjustedScrollX % pageWidth == 0) {
                mListener.onViewScrollFinished(adjustedScrollX / pageWidth);
            }
        }
    }

    int pageWidthPadding() {
        return ((getMeasuredWidth() - pageWidth) / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        pageWidth = pageWidthSpec == SPEC_UNDEFINED ? getMeasuredWidth() : pageWidthSpec;
        pageWidth = Math.min(pageWidth, getMeasuredWidth());

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY),
                    heightMeasureSpec);
        }

        if (mFirstLayout) {
            scrollTo(getScrollXForPage(mCurrentPage), 0);
            mFirstLayout = false;
        }
    }

    private void attachPagerControl() {
        if (mHasPagerControl && pagerControl == null) {
            Activity activity = (Activity) getContext();
            pagerControl = (HorizontalPagerControl) activity
                    .findViewById(R.id.ign_horizontal_pager_control);
            if (pagerControl != null) {
                int childCount = getChildCount();
                if (childCount > 0) {
                    pagerControl.setNumPages(childCount);
                }
                addOnScrollListener(new OnScrollListener());
            } else {
                mHasPagerControl = false;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        attachPagerControl();

        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        if (screen != mCurrentPage || !mScroller.isFinished()) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int focusableScreen;
        if (mNextPage != INVALID_SCREEN) {
            focusableScreen = mNextPage;
        } else {
            focusableScreen = mCurrentPage;
        }
        getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentPage() > 0) {
                snapToPage(getCurrentPage() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentPage() < getChildCount() - 1) {
                snapToPage(getCurrentPage() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction) {
        getChildAt(mCurrentPage).addFocusables(views, direction);
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentPage > 0) {
                getChildAt(mCurrentPage - 1).addFocusables(views, direction);
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (mCurrentPage < getChildCount() - 1) {
                getChildAt(mCurrentPage + 1).addFocusables(views, direction);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Log.d(TAG, "onInterceptTouchEvent::action=" + ev.getAction());

        /*
         * This method JUST determines whether we want to intercept the motion. If we return true,
         * onTouchEvent will be called and we do the actual scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging state and he is moving his
         * finger. We want to intercept this motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            /*
             * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check whether
             * the user has moved far enough from his original down touch.
             */
            if (mTouchState == TOUCH_STATE_REST) {
                checkStartScroll(x, y);
            }

            break;

        case MotionEvent.ACTION_DOWN:
            // Remember location of down touch
            mLastMotionX = x;
            mLastMotionY = y;
            mAllowLongPress = true;

            /*
             * If being flinged and user touches the screen, initiate drag; otherwise don't.
             * mScroller.isFinished should be false when being flinged.
             */
            mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
            break;

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            // Release the drag
            clearChildrenCache();
            mTouchState = TOUCH_STATE_REST;
            break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }

    private void checkStartScroll(float x, float y) {
        /*
         * Locally do absolute value. mLastMotionX is set to the y value of the down event.
         */
        final int xDiff = (int) Math.abs(x - mLastMotionX);
        final int yDiff = (int) Math.abs(y - mLastMotionY);

        boolean xMoved = xDiff > mTouchSlop;
        boolean yMoved = yDiff > mTouchSlop;

        if (xMoved || yMoved) {

            if (xMoved) {
                // Scroll if the user moved far enough along the X axis
                mTouchState = TOUCH_STATE_SCROLLING;
                enableChildrenCache();
            }
            // Either way, cancel any pending longpress
            if (mAllowLongPress) {
                mAllowLongPress = false;
                // Try canceling the long press. It could also have been scheduled
                // by a distant descendant, so use the mAllowLongPress flag to block
                // everything
                final View currentScreen = getChildAt(mCurrentPage);
                currentScreen.cancelLongPress();
            }
        }
    }

    void enableChildrenCache() {
        setChildrenDrawingCacheEnabled(true);
        setChildrenDrawnWithCacheEnabled(true);
    }

    void clearChildrenCache() {
        setChildrenDrawnWithCacheEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished will be false if being
             * flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mTouchState == TOUCH_STATE_REST) {
                checkStartScroll(x, y);
            } else if (mTouchState == TOUCH_STATE_SCROLLING) {
                // Scroll to follow the motion event
                int deltaX = (int) (mLastMotionX - x);
                mLastMotionX = x;

                // Apply friction to scrolling past boundaries.
                if (getScrollX() < 0 || getScrollX() > getChildAt(getChildCount() - 1).getLeft()) {
                    deltaX /= 2;
                }

                scrollBy(deltaX, 0);
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();

                if (velocityX > SNAP_VELOCITY && mCurrentPage > 0) {
                    // Fling hard enough to move left
                    snapToPage(mCurrentPage - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurrentPage < getChildCount() - 1) {
                    // Fling hard enough to move right
                    snapToPage(mCurrentPage + 1);
                } else {
                    snapToDestination();
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
        }

        return true;
    }

    private void snapToDestination() {
        final int startX = getScrollXForPage(mCurrentPage);
        int whichPage = mCurrentPage;
        if (getScrollX() < startX - getWidth() / 8) {
            whichPage = Math.max(0, whichPage - 1);
        } else if (getScrollX() > startX + getWidth() / 8) {
            whichPage = Math.min(getChildCount() - 1, whichPage + 1);
        }

        snapToPage(whichPage);
    }

    void snapToPage(int whichPage) {
        enableChildrenCache();

        boolean changingPages = whichPage != mCurrentPage;

        mNextPage = whichPage;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && changingPages && focusedChild == getChildAt(mCurrentPage)) {
            focusedChild.clearFocus();
        }

        final int newX = getScrollXForPage(whichPage);
        final int delta = newX - getScrollX();
        mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentPage;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != INVALID_SCREEN) {
            mCurrentPage = savedState.currentScreen;
        }
    }

    public void scrollLeft() {
        if (mNextPage == INVALID_SCREEN && mCurrentPage > 0 && mScroller.isFinished()) {
            snapToPage(mCurrentPage - 1);
        }
    }

    public void scrollRight() {
        if (mNextPage == INVALID_SCREEN && mCurrentPage < getChildCount() - 1
                && mScroller.isFinished()) {
            snapToPage(mCurrentPage + 1);
        }
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }

    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void addOnScrollListener(OnScrollListener listener) {
        mListeners.add(listener);
    }

    public void removeOnScrollListener(OnScrollListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Implement to receive events on scroll position and page snaps.
     */
    public class OnScrollListener {
        /**
         * Receives the current scroll X value. This value will be adjusted to assume the left edge
         * of the first page has a scroll position of 0. Note that values less than 0 and greater
         * than the right edge of the last page are possible due to touch events scrolling beyond
         * the edges.
         * 
         * @param scrollX
         *            Scroll X value
         */
        public void onScroll(int scrollX) {
            float scale = (float) (pageWidth * getChildCount()) / (float) pagerControl.getWidth();
            pagerControl.setPosition((int) (scrollX / scale));
        }

        /**
         * Invoked when scrolling is finished (settled on a page, centered).
         * 
         * @param currentPage
         *            The current page
         */
        public void onViewScrollFinished(int currentPage) {
            pagerControl.setCurrentPage(currentPage);
        }
    }
}
