package com.example.taskercalendarplugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.taskercalendarplugin.util.CalendarResolverHelper;
import com.example.taskercalendarplugin.model.EventDTO;
import com.example.taskercalendarplugin.ui.PluginEditActivity; // ✅ Import ajouté ici

import java.util.List;
import java.util.Calendar;

// Standard Tasker plugin API constants (similar to PluginEditActivity)
final class LocaleIntent {
    public static final String EXTRA_BUNDLE = "com.twofortyfouram.locale.api.intent.extra.BUNDLE";
    public static final String ACTION_QUERY_CONDITION = "com.twofortyfouram.locale.api.intent.action.QUERY_CONDITION";
    public static final int RESULT_CONDITION_SATISFIED = 16;
    public static final int RESULT_CONDITION_UNSATISFIED = 17;
    public static final int RESULT_CONDITION_UNKNOWN = 18;

    public static class TaskerPlugin {
        public static final String VARIABLE_BUNDLE_KEY = "net.dinglisch.android.tasker.extras.VARIABLE_BUNDLE";
        public static void addVariableBundle(Bundle extrasBundle, Bundle variables) {
            if (extrasBundle == null || variables == null) return;
            extrasBundle.putBundle(VARIABLE_BUNDLE_KEY, variables);
        }
        public static void addVariable(Bundle bundle, String name, String value) {
            if (bundle == null || name == null || value == null) return;
            if (!name.startsWith("%")) {
                Log.w("TaskerPlugin", "Tasker variable names should start with %");
            }
            bundle.putString(name, value);
        }
    }
}

public class PluginQueryReceiver extends BroadcastReceiver {

    private static final String TAG = "PluginQueryReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!LocaleIntent.ACTION_QUERY_CONDITION.equals(intent.getAction())) {
            Log.e(TAG, "Received incorrect Intent action: " + intent.getAction());
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
            return;
        }

        Log.d(TAG, "Query Receiver invoked for action: " + intent.getAction());
        Bundle configuration = intent.getBundleExtra(LocaleIntent.EXTRA_BUNDLE);
        Bundle resultBundle = getResultExtras(true);

        if (configuration == null) {
            Log.e(TAG, "No configuration bundle received.");
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
            LocaleIntent.TaskerPlugin.addVariable(
                new Bundle(), "%cal_error", "No configuration provided to plugin."
            );
            return;
        }

        String actionType = configuration.getString(PluginEditActivity.ACTION_TYPE_KEY);
        Log.d(TAG, "Received configuration. Action type: " + actionType);

        CalendarResolverHelper resolverHelper = new CalendarResolverHelper();
        boolean conditionSatisfied = false;

        try {
            if (PluginEditActivity.ACTION_GET_EVENTS.equals(actionType)) {
                String countStr = configuration.getString(PluginEditActivity.BUNDLE_KEY_GET_EVENTS_COUNT);
                String daysAheadStr = configuration.getString(PluginEditActivity.BUNDLE_KEY_GET_EVENTS_DAYS_AHEAD);
                List<EventDTO> events;

                if (countStr != null && !countStr.isEmpty()) {
                    int count = Integer.parseInt(countStr);
                    events = resolverHelper.getUpcomingEvents(context);
                    conditionSatisfied = events.size() >= count;
                    Log.d(TAG, "Queried for " + count + " events, found " + events.size());
                } else if (daysAheadStr != null && !daysAheadStr.isEmpty()) {
                    int days = Integer.parseInt(daysAheadStr);
                    Calendar cal = Calendar.getInstance();
                    long startTime = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_YEAR, days);
                    long endTime = cal.getTimeInMillis();
                    events = resolverHelper.getEvents(context, startTime, endTime);
                    conditionSatisfied = !events.isEmpty();
                    Log.d(TAG, "Queried for events " + days + " days ahead, found " + events.size());
                } else {
                    String oldConfigData = configuration.getString("config_data_key");
                    if ("upcoming_event_exists".equalsIgnoreCase(oldConfigData)) {
                        events = resolverHelper.getUpcomingEvents(context);
                        conditionSatisfied = !events.isEmpty();
                    } else {
                        Log.w(TAG, "No valid parameters for ACTION_GET_EVENTS");
                        setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
                        LocaleIntent.TaskerPlugin.addVariable(
                            new Bundle(), "%cal_error", "Missing parameters for Get Events action."
                        );
                        return;
                    }
                }

                if (conditionSatisfied && events != null && !events.isEmpty()) {
                    Bundle taskerVariables = new Bundle();
                    EventDTO nextEvent = events.get(0);
                    taskerVariables.putString("%next_event_title", nextEvent.getTitle());
                    taskerVariables.putString("%next_event_start_time", String.valueOf(nextEvent.getDtstart()));
                    taskerVariables.putString("%next_event_location", nextEvent.getEventLocation() != null ? nextEvent.getEventLocation() : "N/A");
                    LocaleIntent.TaskerPlugin.addVariableBundle(resultBundle, taskerVariables);
                }

            } else {
                Log.w(TAG, "Unknown action type in QueryReceiver: " + actionType);
                setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
                LocaleIntent.TaskerPlugin.addVariable(
                    new Bundle(), "%cal_error", "Unsupported action type: " + actionType
                );
                return;
            }

            setResultCode(conditionSatisfied ? LocaleIntent.RESULT_CONDITION_SATISFIED : LocaleIntent.RESULT_CONDITION_UNSATISFIED);

        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException during calendar operation", se);
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
            LocaleIntent.TaskerPlugin.addVariable(new Bundle(), "%cal_error", "Permission denied: " + se.getMessage());
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Invalid number format", nfe);
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
            LocaleIntent.TaskerPlugin.addVariable(new Bundle(), "%cal_error", "Invalid number in config: " + nfe.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in PluginQueryReceiver", e);
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
            LocaleIntent.TaskerPlugin.addVariable(new Bundle(), "%cal_error", "Error: " + e.getMessage());
        }
    }
}