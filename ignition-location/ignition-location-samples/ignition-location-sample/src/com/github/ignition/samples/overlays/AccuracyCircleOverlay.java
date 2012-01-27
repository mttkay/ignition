/*
 * Copyright 2011 Novoda Ltd.
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

package com.github.ignition.samples.overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class AccuracyCircleOverlay extends Overlay {
    private final String LOG_TAG = AccuracyCircleOverlay.class.getSimpleName();

    private final float accuracy;
    private final GeoPoint geoPoint;

    public AccuracyCircleOverlay(GeoPoint geoPoint, float accuracy) {
        this.accuracy = accuracy;
        this.geoPoint = geoPoint;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        Projection projection = mapView.getProjection();
        if (shadow && projection == null) {
            Log.v(LOG_TAG, "drawing not done because shadow and projection are null");
            return;
        }
        Point pt = new Point();
        projection.toPixels(geoPoint, pt);
        float circleRadius = metersToRadius(accuracy, projection, geoPoint.getLatitudeE6());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0x186666ff);
        paint.setStyle(Style.FILL_AND_STROKE);
        canvas.drawCircle(pt.x, pt.y, circleRadius, paint);

        paint.setColor(0xff6666ff);
        paint.setStyle(Style.STROKE);
        canvas.drawCircle(pt.x, pt.y, circleRadius, paint);

        paint.setColor(Color.RED);
        paint.setStyle(Style.FILL_AND_STROKE);
        canvas.drawCircle(pt.x, pt.y, 3, paint);

        super.draw(canvas, mapView, shadow);
    }

    private int metersToRadius(float m, Projection p, double latitude) {
        return Math
                .abs((int) (p.metersToEquatorPixels(m) * (1 / Math.cos(Math.toRadians(latitude)))));
    }
}
