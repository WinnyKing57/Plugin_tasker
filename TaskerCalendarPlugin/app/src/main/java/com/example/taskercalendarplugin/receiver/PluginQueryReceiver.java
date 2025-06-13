package com.example.taskercalendarplugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.taskercalendarplugin.util.CalendarResolverHelper;
import com.example.taskercalendarplugin.model.EventDTO;

import java.util.List;

// Standard Tasker plugin API constants (similar to PluginEditActivity)
final class LocaleIntent {
    public static final String EXTRA_BUNDLE = "com.twofortyfouram.locale.api.intent.extra.BUNDLE";
    public static final String ACTION_QUERY_CONDITION = "com.twofortyfouram.locale.api.intent.action.QUERY_CONDITION";
    public static final int RESULT_CONDITION_SATISFIED = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_SATISFIED;
    public static final int RESULT_CONDITION_UNSATISFIED = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_UNSATISFIED;
    public static final int RESULT_CONDITION_UNKNOWN = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_UNKNOWN;

    // Hypothetical TaskerPlugin class/methods for variable passing
    public static class TaskerPlugin { // Keep this nested or move to a proper place if SDK is added
        public static final String VARIABLE_BUNDLE_KEY = "net.dinglisch.android.tasker.extras.VARIABLE_BUNDLE";
        public static void addVariableBundle(Bundle extrasBundle, Bundle variables) {
            if (extrasBundle == null || variables == null) return;
            extrasBundle.putBundle(VARIABLE_BUNDLE_KEY, variables);
        }
        // Example of how one might add a single variable if the helper supports it
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
    // Using constants from PluginEditActivity for bundle keys
    // private static final String BUNDLE_KEY_CONFIG_DATA = "config_data_key"; // Old key

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!LocaleIntent.ACTION_QUERY_CONDITION.equals(intent.getAction())) {
            Log.e(TAG, "Received incorrect Intent action: " + intent.getAction());
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN); // Let Tasker know something is wrong
            return;
        }

        Log.d(TAG, "Query Receiver invoked for action: " + intent.getAction());
        Bundle configuration = intent.getBundleExtra(LocaleIntent.EXTRA_BUNDLE);
        Bundle resultBundle = getResultExtras(true); // Ensure result bundle exists for variables

        if (configuration == null) {
            Log.e(TAG, "No configuration bundle received.");
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
            LocaleIntent.TaskerPlugin.addVariable(resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) != null ?
                            resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) : new Bundle(),
                    "%cal_error", "No configuration provided to plugin.");
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
                Log.d(TAG, "Get Events config: Count='" + countStr + "', DaysAhead='" + daysAheadStr + "'");

                List<EventDTO> events;
                if (countStr != null && !countStr.isEmpty()) {
                    int count = Integer.parseInt(countStr);
                    // This is a simplification; getUpcomingEvents currently gets for a week.
                    // A more robust implementation would fetch all events in a range and then take 'count'.
                    // For now, we'll use getUpcomingEvents and check if size >= count or similar.
                    events = resolverHelper.getUpcomingEvents(context); // Default: 1 week
                    conditionSatisfied = events.size() >= count;
                     Log.d(TAG, "Queried for " + count + " events, found " + events.size() + ". Condition: " + conditionSatisfied);
                } else if (daysAheadStr != null && !daysAheadStr.isEmpty()) {
                    int days = Integer.parseInt(daysAheadStr);
                    Calendar cal = Calendar.getInstance();
                    long startTime = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_YEAR, days);
                    long endTime = cal.getTimeInMillis();
                    events = resolverHelper.getEvents(context, startTime, endTime);
                    conditionSatisfied = !events.isEmpty();
                    Log.d(TAG, "Queried for events " + days + " days ahead, found " + events.size() + ". Condition: " + conditionSatisfied);
                } else {
                     // Fallback to legacy config string if new keys are missing
                    String oldConfigData = configuration.getString("config_data_key"); // old key
                    if ("upcoming_event_exists".equalsIgnoreCase(oldConfigData)) {
                        events = resolverHelper.getUpcomingEvents(context);
                        conditionSatisfied = !events.isEmpty();
                        Log.d(TAG, "Legacy 'upcoming_event_exists': Found " + events.size() + " events. Condition: " + conditionSatisfied);
                    } else {
                        Log.w(TAG, "ACTION_GET_EVENTS: No valid parameters (count or daysAhead) or legacy config found.");
                        setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
                        LocaleIntent.TaskerPlugin.addVariable(resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) != null ?
                                        resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) : new Bundle(),
                                "%cal_error", "Missing parameters for Get Events action.");
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
                    Log.d(TAG, "Variables added for next event: " + nextEvent.getTitle());
                }

            } else {
                Log.w(TAG, "Unknown or unsupported action type in QueryReceiver: " + actionType);
                setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
                 LocaleIntent.TaskerPlugin.addVariable(resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) != null ?
                                resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) : new Bundle(),
                        "%cal_error", "Unsupported action type: " + actionType);
                return;
            }

            setResultCode(conditionSatisfied ? LocaleIntent.RESULT_CONDITION_SATISFIED : LocaleIntent.RESULT_CONDITION_UNSATISFIED);
            Log.d(TAG, "Final result code: " + getResultCode());

        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException during calendar operation in QueryReceiver.", se);
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN); // Or UNSATISFIED
            LocaleIntent.TaskerPlugin.addVariable(resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) != null ?
                            resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) : new Bundle(),
                    "%cal_error", "Permission denied: " + se.getMessage());
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "NumberFormatException for configuration parameters.", nfe);
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN);
            LocaleIntent.TaskerPlugin.addVariable(resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) != null ?
                            resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) : new Bundle(),
                    "%cal_error", "Invalid number in config: " + nfe.getMessage());
        }
        catch (Exception e) {
            Log.e(TAG, "Unexpected error during QueryReceiver processing.", e);
            setResultCode(LocaleIntent.RESULT_CONDITION_UNKNOWN); // Or UNSATISFIED
            LocaleIntent.TaskerPlugin.addVariable(resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) != null ?
                            resultBundle.getBundle(LocaleIntent.TaskerPlugin.VARIABLE_BUNDLE_KEY) : new Bundle(),
                    "%cal_error", "Error: " + e.getMessage());
        }
    }
}
