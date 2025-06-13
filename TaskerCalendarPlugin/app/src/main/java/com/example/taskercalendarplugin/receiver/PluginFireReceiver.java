package com.example.taskercalendarplugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.taskercalendarplugin.util.CalendarPermissionHelper;
import com.example.taskercalendarplugin.util.CalendarResolverHelper;

import java.util.TimeZone;

// Standard Tasker plugin API constants
final class LocaleIntentFire {
    public static final String EXTRA_BUNDLE = "com.twofortyfouram.locale.api.intent.extra.BUNDLE";
    public static final String ACTION_FIRE_SETTING = "com.twofortyfouram.locale.api.intent.action.FIRE_SETTING";
}

public class PluginFireReceiver extends BroadcastReceiver {

    private static final String TAG = "PluginFireReceiver";
    // Using constants from PluginEditActivity for bundle keys
    // private static final String BUNDLE_KEY_CONFIG_DATA = "config_data_key"; // Old key

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!LocaleIntentFire.ACTION_FIRE_SETTING.equals(intent.getAction())) {
            Log.e(TAG, "Received incorrect Intent action: " + intent.getAction());
            return;
        }

        Log.d(TAG, "Fire Receiver invoked for action: " + intent.getAction());
        final Bundle configuration = intent.getBundleExtra(LocaleIntentFire.EXTRA_BUNDLE);

        if (configuration == null) {
            Log.e(TAG, "No configuration bundle received. Cannot perform action.");
            return;
        }

        final String actionType = configuration.getString(PluginEditActivity.ACTION_TYPE_KEY);
        Log.d(TAG, "Received configuration. Action type: " + actionType);

        final PendingResult pendingResult = goAsync(); // Ensure receiver stays alive for background work

        new Thread(new Runnable() {
            @Override
            public void run() {
                CalendarResolverHelper resolverHelper = new CalendarResolverHelper();
                try {
                    if (PluginEditActivity.ACTION_ADD_EVENT.equals(actionType)) {
                        handleAddEventAction(context, configuration, resolverHelper);
                    } else if ("add_default_event".equalsIgnoreCase(configuration.getString("config_data_key"))) {
                        // Handle legacy "add_default_event" if necessary
                         Log.d(TAG, "Handling legacy 'add_default_event'");
                        handleAddEventAction(context, getDefaultEventBundle(), resolverHelper); // Use a default bundle
                    }
                    else {
                        Log.w(TAG, "Unknown or unsupported action type in FireReceiver: " + actionType);
                    }
                } catch (SecurityException se) {
                    Log.e(TAG, "SecurityException during calendar operation in FireReceiver. Permissions may be missing.", se);
                    // Cannot request permissions from a receiver. User must grant them via Activity.
                    // Tasker usually doesn't expect a detailed failure response for FIRE_SETTING.
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error during FireReceiver processing.", e);
                } finally {
                    pendingResult.finish(); // Crucial to call this to allow receiver to be recycled.
                    Log.d(TAG, "PendingResult finished.");
                }
            }
        }).start();
    }

    private void handleAddEventAction(Context context, Bundle config, CalendarResolverHelper resolverHelper) {
        if (!CalendarPermissionHelper.areCalendarPermissionsGranted(context)) {
            Log.e(TAG, "Calendar permissions not granted at time of action. Cannot add event.");
            // This check is important, but permissions should ideally be confirmed by Tasker
            // or the user via the EditActivity before the action is triggered.
            return;
        }

        String title = config.getString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_TITLE);
        if (title == null || title.trim().isEmpty()) {
            Log.e(TAG, "Event title is missing in configuration. Cannot add event.");
            return;
        }

        String description = config.getString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_DESCRIPTION, "");
        String location = config.getString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_LOCATION, "");
        String startTimeOffsetStr = config.getString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_START_TIME_OFFSET, "60"); // Default 60 mins
        String durationStr = config.getString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_DURATION, "30"); // Default 30 mins

        long calendarId = resolverHelper.getDefaultWritableCalendarId(context);
        if (calendarId == -1) {
            Log.e(TAG, "No writable calendar found. Cannot add event: " + title);
            return;
        }

        try {
            long startTimeOffsetMillis = Long.parseLong(startTimeOffsetStr) * 60 * 1000;
            long durationMillis = Long.parseLong(durationStr) * 60 * 1000;

            long currentTime = System.currentTimeMillis();
            long startTimeMillis = currentTime + startTimeOffsetMillis;
            long endTimeMillis = startTimeMillis + durationMillis;

            String timezone = TimeZone.getDefault().getID();

            Log.d(TAG, "Attempting to add event: Title='" + title + "', StartOffsetMins=" + startTimeOffsetStr +
                    ", DurationMins=" + durationStr + ", CalendarID=" + calendarId);

            Uri newEventUri = resolverHelper.addEvent(context, calendarId, title, description,
                    startTimeMillis, endTimeMillis, location, timezone);

            if (newEventUri != null) {
                Log.i(TAG, "Event added successfully by FireReceiver: " + title + ", URI: " + newEventUri.toString());
                // Optional: Show a Toast (might not work well from BroadcastReceiver on all Android versions)
                // Handler mainHandler = new Handler(context.getMainLooper());
                // mainHandler.post(() -> Toast.makeText(context.getApplicationContext(), "Event '" + title + "' added.", Toast.LENGTH_SHORT).show());
            } else {
                Log.e(TAG, "Failed to add event via FireReceiver: " + title + ". addEvent returned null.");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number format for start time offset or duration for event: " + title, e);
        } catch (Exception e) {
            Log.e(TAG, "Generic exception while adding event '" + title + "' in FireReceiver", e);
        }
    }
     private Bundle getDefaultEventBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(PluginEditActivity.ACTION_TYPE_KEY, PluginEditActivity.ACTION_ADD_EVENT);
        bundle.putString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_TITLE, "Tasker Default Event");
        bundle.putString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_DESCRIPTION, "This is a default event added by Tasker.");
        bundle.putString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_LOCATION, "N/A");
        bundle.putString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_START_TIME_OFFSET, "10"); // 10 minutes from now
        bundle.putString(PluginEditActivity.BUNDLE_KEY_ADD_EVENT_DURATION, "15"); // 15 minutes duration
        return bundle;
    }
}
