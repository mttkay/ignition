/* Copyright (c) 2009-2011 Matthias Kaeppler
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

package com.github.ignition.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;

public class IgnitedDiagnostics {
    public static final int ANDROID_API_LEVEL;

    public static final int ICS = 14;
    public static final int HONEYCOMB = 11;
    public static final int GINGERBREAD = 9;
    public static final int FROYO = 8;
    public static final int ECLAIR = 7;
    public static final int DONUT = 4;
    public static final int CUPCAKE = 3;

    public static final boolean SUPPORTS_ICS, SUPPORTS_HONEYCOMB, SUPPORTS_GINGERBREAD,
            SUPPORTS_FROYO, SUPPORTS_ECLAIR, SUPPORTS_DONUT, SUPPORTS_CUPCAKE;

    private static boolean test = false;
    private static int testAndroidApiLevel;

    static {
        int apiLevel = -1;
        try {
            apiLevel = Build.VERSION.class.getField("SDK_INT").getInt(null);
        } catch (Exception e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        }
        ANDROID_API_LEVEL = apiLevel;
        SUPPORTS_ICS = ANDROID_API_LEVEL >= ICS;
        SUPPORTS_HONEYCOMB = ANDROID_API_LEVEL >= HONEYCOMB;
        SUPPORTS_GINGERBREAD = ANDROID_API_LEVEL >= GINGERBREAD;
        SUPPORTS_FROYO = ANDROID_API_LEVEL >= FROYO;
        SUPPORTS_ECLAIR = ANDROID_API_LEVEL >= ECLAIR;
        SUPPORTS_DONUT = ANDROID_API_LEVEL >= DONUT;
        SUPPORTS_CUPCAKE = ANDROID_API_LEVEL >= CUPCAKE;
    }

    public static boolean isTest() {
        return test;
    }

    public static void setTestApiLevel(int androidApiLevel) {
        testAndroidApiLevel = androidApiLevel;
        test = true;
    }

    public static boolean supportsApiLevel(int apiLevel) {
        if (test) {
            return testAndroidApiLevel >= apiLevel;
        } else {
            return ANDROID_API_LEVEL >= apiLevel;
        }
    }

    /**
     * Returns the ANDROID_ID unique device ID for the current device. Reading that ID has changed
     * between platform versions, so this method takes care of attempting to read it in different
     * ways, if one failed.
     * 
     * @param context
     *            the context
     * @return the device's ANDROID_ID, or null if it could not be determined
     * @see Secure#ANDROID_ID
     */
    public static String getAndroidId(Context context) {
        String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (androidId == null) {
            // this happens on 1.6 and older
            androidId = android.provider.Settings.System.getString(context.getContentResolver(),
                    android.provider.Settings.System.ANDROID_ID);
        }
        return androidId;
    }

    /**
     * Same as {@link #getAndroidId(Context)}, but never returns null.
     * 
     * @param context
     *            the context
     * @param fallbackValue
     *            the fallback value
     * @return the device's ANDROID_ID, or the fallback value if it could not be determined
     * @see Secure#ANDROID_ID
     */
    public static String getAndroidId(Context context, String fallbackValue) {
        String androidId = getAndroidId(context);
        if (androidId == null) {
            androidId = fallbackValue;
        }
        return androidId;
    }

    public static String getApplicationVersionString(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            return "v" + info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createDiagnosis(Activity context, Exception error) {
        StringBuilder sb = new StringBuilder();

        sb.append("Application version: " + getApplicationVersionString(context) + "\n");
        sb.append("Device locale: " + Locale.getDefault().toString() + "\n\n");
        sb.append("Android ID: " + getAndroidId(context, "n/a"));

        // phone information
        sb.append("PHONE SPECS\n");
        sb.append("model: " + Build.MODEL + "\n");
        sb.append("brand: " + Build.BRAND + "\n");
        sb.append("product: " + Build.PRODUCT + "\n");
        sb.append("device: " + Build.DEVICE + "\n\n");

        // android information
        sb.append("PLATFORM INFO\n");
        sb.append("Android " + Build.VERSION.RELEASE + " " + Build.ID + " (build "
                + Build.VERSION.INCREMENTAL + ")\n");
        sb.append("build tags: " + Build.TAGS + "\n");
        sb.append("build type: " + Build.TYPE + "\n\n");

        // settings
        sb.append("SYSTEM SETTINGS\n");
        String networkMode = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            if (Settings.Secure.getInt(resolver, Settings.Secure.WIFI_ON) == 0) {
                networkMode = "DATA";
            } else {
                networkMode = "WIFI";
            }
            sb.append("network mode: " + networkMode + "\n");
            sb.append("HTTP proxy: "
                    + Settings.Secure.getString(resolver, Settings.Secure.HTTP_PROXY) + "\n\n");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }

        sb.append("STACK TRACE FOLLOWS\n\n");

        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));

        sb.append(stackTrace.toString());

        return sb.toString();
    }

}
