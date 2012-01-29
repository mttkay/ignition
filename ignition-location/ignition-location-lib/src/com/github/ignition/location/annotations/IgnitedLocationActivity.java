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

    /**
     * Determines whether to use GPS when requesting location updates or not. Default value is
     * {@link IgnitedLocationConstants.USE_GPS_DEFAULT}).
     *
     * @return true if GPS is used when requesting location updates, false otherwise.
     */
    boolean useGps() default IgnitedLocationConstants.USE_GPS_DEFAULT;

    /**
     * Determines whether location updates should be requested or not. Default value is
     * {@link IgnitedLocationConstants.REQUEST_LOCATION_UPDATES_DEFAULT}).
     *
     * @return true if location updates should be requested, false otherwise.
     */
    boolean requestLocationUpdates() default IgnitedLocationConstants.REQUEST_LOCATION_UPDATES_DEFAULT;

    /**
     * Determines the minimum difference (in meters) between two location updates. Default value is
     * {@link IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT}).
     *
     * @return the minimum difference (in meters) between two location updates.
     */
    int locationUpdatesDistanceDiff() default IgnitedLocationConstants.LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT;

    /**
     * Determines the minimum interval (in milliseconds) between two location updates. Default value is
     * {@link IgnitedLocationConstants.LOCATION_UPDATES_INTERVAL_DEFAULT}).
     *
     * @return the minimum interval (in milliseconds) between two location updates.
     */
    long locationUpdatesInterval() default IgnitedLocationConstants.LOCATION_UPDATES_INTERVAL_DEFAULT;

    /**
     * Determines the minimum difference (in meters) between two location updates. Default value is
     * {@link IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT}).
     *
     * @return the minimum difference (in meters) between two location updates
     */
    int passiveLocationUpdatesDistanceDiff() default IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_DISTANCE_DIFF_DEFAULT;

    /**
     * Determines the minimum interval (in milliseconds) between two location updates. Default value is
     * {@link IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT}).
     *
     * @return the minimum interval (in milliseconds) between two location updates.
     */
    long passiveLocationUpdatesInterval() default IgnitedLocationConstants.PASSIVE_LOCATION_UPDATES_INTERVAL_DEFAULT;

    /**
     * Determines whether passive location updates should be enabled or not. Default value is
     * {@link IgnitedLocationConstants.ENABLE_PASSIVE_LOCATION_UPDATES_DEFAULT}).
     *
     * @return true if passive location updates should be enabled, false otherwise.
     */
    boolean enablePassiveUpdates() default IgnitedLocationConstants.ENABLE_PASSIVE_LOCATION_UPDATES_DEFAULT;

    /**
     * Determines the interval (in milliseconds) to wait for a GPS fix. If a fix is not returned
     * within this interval GPS updates will be disabled and updates from the network will be
     * requested instead. Default value is
     * {@link IgnitedLocationConstants.WAIT_FOR_GPS_FIX_INTERVAL_DEFAULT}).
     *
     * @return the interval (in milliseconds) to wait for a GPS fix.
     */
    long waitForGpsFix() default IgnitedLocationConstants.WAIT_FOR_GPS_FIX_INTERVAL_DEFAULT;

    /**
     * Determines the minimum battery level to enable location updates using GPS. Default value is
     * {@link IgnitedLocationConstants.MIN_BATTERY_LEVEL_DEFAULT}).
     *
     * @return the minimum battery level to enable location updates using GPS.
     */
    int minBatteryLevel() default IgnitedLocationConstants.MIN_BATTERY_LEVEL_DEFAULT;

    /**
     * Determines whether the activity should display a "wait for location" dialog while the
     * location manager is trying to get a location. Default default value is
     * {@link IgnitedLocationConstants.SHOW_WAIT_FOR_LOCATION_DIALOG_DEFAULT}).
     *
     * NB: if this is true, the Activity MUST create a dialog in
     * {@link android.app.Activity#onCreateDialog(int, android.os.Bundle)}
     *
     * @return true if the activity should display a "wait for location" dialog while the location
     *         manager is trying to get a location, false otherwise.
     */
    boolean showWaitForLocationDialog() default IgnitedLocationConstants.SHOW_WAIT_FOR_LOCATION_DIALOG_DEFAULT;
}
