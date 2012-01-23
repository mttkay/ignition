/* Copyright (c) 2011 Stefano Dacchille
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ignition.location.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ignition.location.IgnitedLocationConstants;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface IgnitedLocationActivity {

    boolean useGps() default IgnitedLocationConstants.USE_GPS_DEFAULT;

    boolean requestLocationUpdates() default IgnitedLocationConstants.ENABLE_LOCATION_UPDATES_DEFAULT;

    int locationUpdatesDistanceDiff() default IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT;

    long locationUpdatesInterval() default IgnitedLocationConstants.LOCATION_UPDATES_INTERVAL_DEFAULT;

    int passiveLocationUpdatesDistanceDiff() default IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT;

    long passiveLocationUpdatesInterval() default IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT;

    boolean enablePassiveUpdates() default IgnitedLocationConstants.ENABLE_PASSIVE_LOCATION_UPDATES_DEFAULT;

    long waitForGpsFix() default IgnitedLocationConstants.WAIT_FOR_GPS_FIX_INTERVAL_DEFAULT;

    int minBatteryLevel() default IgnitedLocationConstants.MIN_BATTERY_LEVEL_DEFAULT;
}
