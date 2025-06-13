package com.example.taskercalendarplugin.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CalendarPermissionHelper {

    public static final String[] CALENDAR_PERMISSIONS = {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
    };

    public static final int REQUEST_CODE_CALENDAR = 101; // Example request code

    /**
     * Checks if calendar permissions are granted. If not, requests them.
     * This version doesn't immediately show rationale UI but signals if it's needed.
     *
     * @param activity The activity requesting the permissions.
     * @return true if permissions are already granted.
     *         false if permissions are not granted and a request has been made.
     */
    public static boolean checkAndRequestCalendarPermissions(Activity activity) {
        if (areCalendarPermissionsGranted(activity)) {
            return true;
        }
        ActivityCompat.requestPermissions(activity, CALENDAR_PERMISSIONS, REQUEST_CODE_CALENDAR);
        return false;
    }

    /**
     * Checks if the user has previously denied the calendar permission request and selected "Don't ask again".
     * This can be used to decide whether to show a custom explanation UI.
     *
     * @param activity The activity.
     * @return true if rationale should be shown for READ_CALENDAR or WRITE_CALENDAR, false otherwise.
     */
    public static boolean shouldShowRationale(Activity activity) {
        boolean shouldShowRead = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALENDAR);
        boolean shouldShowWrite = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALENDAR);
        // If either requires rationale, we should explain for both.
        return shouldShowRead || shouldShowWrite;
    }


    /**
     * Checks if both READ_CALENDAR and WRITE_CALENDAR permissions are granted.
     *
     * @param activity The context to use for checking permissions.
     * @return true if both permissions are granted, false otherwise.
     */
    public static boolean areCalendarPermissionsGranted(Activity activity) {
        boolean readPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        boolean writePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        return readPermission && writePermission;
    }

    /**
     * Handles the result of a permission request. This method should be called from the
     * Activity's onRequestPermissionsResult method.
     *
     * @param requestCode  The request code passed in requestPermissions().
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions.
     * @return true if all CALENDAR_PERMISSIONS were granted, false otherwise.
     */
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_CALENDAR) {
            if (grantResults.length == CALENDAR_PERMISSIONS.length) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        return false; // At least one permission was denied
                    }
                }
                return true; // All permissions were granted
            } else if (grantResults.length > 0) {
                // This case handles if for some reason only a subset of permissions were processed.
                // Or if our CALENDAR_PERMISSIONS array changes and we have partial results.
                // For strict checking against CALENDAR_PERMISSIONS, this might also be false.
                boolean allGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                // Additionally, verify that all expected permissions are covered.
                // This part is more complex if permissions array in callback isn't guaranteed to match CALENDAR_PERMISSIONS.
                // For simplicity, we assume if any requested permission is denied, it's a failure.
                return allGranted && (grantResults.length >= CALENDAR_PERMISSIONS.length);
            }
        }
        return false; // Request code doesn't match or no results / partial results not fully granted
    }
}
