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
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.github.ignition.core.Ignition;

/**
 * User: jeanguy@gmail.com Date: Aug 11, 2010
 */
public class HorizontalPagerControl extends View {
    private static final int DEFAULT_BAR_COLOR = 0xaa777777;
    private static final int DEFAULT_HIGHLIGHT_COLOR = 0xaa999999;
    private static final int DEFAULT_FADE_DELAY = 2000;
    private static final int DEFAULT_FADE_DURATION = 500;

    private int numPages, currentPage, position;
    private Paint barPaint, highlightPaint;
    private int fadeDelay, fadeDuration;
    private float roundRectRadius;

    private Animation fadeOutAnimation;

    public HorizontalPagerControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalPagerControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // TODO: cannot use styled attributes until issue #9656 is fixed
        // TypedArray a = context.obtainStyledAttributes(attrs,
        // R.styleable.com_deezapps_widget_PagerControl);
        // int barColor =
        // a.getColor(R.styleable.com_deezapps_widget_PagerControl_barColor,
        // DEFAULT_BAR_COLOR);
        // int highlightColor =
        // a.getColor(R.styleable.com_deezapps_widget_PagerControl_highlightColor,
        // DEFAULT_HIGHLIGHT_COLOR);
        // fadeDelay =
        // a.getInteger(R.styleable.com_deezapps_widget_PagerControl_fadeDelay,
        // DEFAULT_FADE_DELAY);
        // fadeDuration =
        // a.getInteger(R.styleable.com_deezapps_widget_PagerControl_fadeDuration,
        // DEFAULT_FADE_DURATION);
        // ovalRadius =
        // a.getDimension(R.styleable.com_deezapps_widget_PagerControl_roundRectRadius,
        // 0f);
        // a.recycle();

        int barColor = attrs.getAttributeIntValue(Ignition.XMLNS, "barColor",
                DEFAULT_BAR_COLOR);
        int highlightColor = attrs.getAttributeIntValue(Ignition.XMLNS,
                "highlightColor", DEFAULT_HIGHLIGHT_COLOR);
        fadeDelay = attrs.getAttributeIntValue(Ignition.XMLNS, "fadeDelay",
                DEFAULT_FADE_DELAY);
        fadeDuration = attrs.getAttributeIntValue(Ignition.XMLNS,
                "fadeDuration", DEFAULT_FADE_DURATION);
        roundRectRadius = attrs.getAttributeFloatValue(Ignition.XMLNS,
                "roundRectRadius", 0);

        barPaint = new Paint();
        barPaint.setColor(barColor);

        highlightPaint = new Paint();
        highlightPaint.setColor(highlightColor);

        fadeOutAnimation = new AlphaAnimation(1f, 0f);
        fadeOutAnimation.setDuration(fadeDuration);
        fadeOutAnimation.setRepeatCount(0);
        fadeOutAnimation.setInterpolator(new LinearInterpolator());
        fadeOutAnimation.setFillEnabled(true);
        fadeOutAnimation.setFillAfter(true);
    }

    /**
     * 
     * @return current number of pages
     */
    public int getNumPages() {
        return numPages;
    }

    /**
     * 
     * @param numPages
     *            must be positive number
     */
    public void setNumPages(int numPages) {
        if (numPages <= 0) {
            throw new IllegalArgumentException("numPages must be positive");
        }
        this.numPages = numPages;
        invalidate();
        fadeOut();
    }

    private void fadeOut() {
        if (fadeDuration > 0) {
            clearAnimation();
            fadeOutAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis() + fadeDelay);
            setAnimation(fadeOutAnimation);
        }
    }

    /**
     * 0 to numPages-1
     * 
     * @return
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * 
     * @param currentPage
     *            0 to numPages-1
     */
    public void setCurrentPage(int currentPage) {
        if (currentPage < 0 || currentPage >= numPages) {
            throw new IllegalArgumentException("currentPage parameter out of bounds");
        }
        if (this.currentPage != currentPage) {
            this.currentPage = currentPage;
            this.position = currentPage * getPageWidth();
            invalidate();
            fadeOut();
        }
    }

    /**
     * Equivalent to the width of the view divided by the current number of pages.
     * 
     * @return page width, in pixels
     */
    public int getPageWidth() {
        return getWidth() / numPages;
    }

    /**
     * 
     * @param position
     *            can be -pageWidth to pageWidth*(numPages+1)
     */
    public void setPosition(int position) {
        if (this.position != position) {
            this.position = position;
            invalidate();
            fadeOut();
        }
    }

    /**
     * 
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), roundRectRadius,
                roundRectRadius,
                barPaint);
        canvas.drawRoundRect(
                new RectF(position, 0, position + (getWidth() / numPages), getHeight()),
                roundRectRadius, roundRectRadius, highlightPaint);
    }
}
