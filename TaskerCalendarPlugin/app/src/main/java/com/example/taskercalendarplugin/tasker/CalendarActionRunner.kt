package com.example.taskercalendarplugin.tasker

import android.content.Context
import android.util.Log
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSuccess
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.example.taskercalendarplugin.tasker.model.CalendarPluginInput
import com.example.taskercalendarplugin.tasker.model.CalendarPluginOutput
import com.example.taskercalendarplugin.util.CalendarResolverHelper // We might use this later when re-enabling logic
import com.example.taskercalendarplugin.util.CalendarPermissionHelper // For permission checks later
import com.example.taskercalendarplugin.ui.PluginEditActivity // For constants

class CalendarActionRunner : TaskerPluginRunnerAction<CalendarPluginInput, CalendarPluginOutput>() {
    private val TAG = "CalendarActionRunner"

    override fun run(context: Context, input: TaskerInput<CalendarPluginInput>): TaskerPluginResult<CalendarPluginOutput> {
        Log.d(TAG, "CalendarActionRunner started.")
        Log.d(TAG, "Input Action Type: ${input.regular.actionType}")
        Log.d(TAG, "Input Event Title: ${input.regular.addEventTitle}")
        // Add more logging of other input fields as needed for debugging

        // Calendar logic is currently commented out in CalendarResolverHelper.
        // When re-enabled, permission checks and calls to CalendarResolverHelper would go here.

        // Example:
        // if (!CalendarPermissionHelper.areCalendarPermissionsGranted(context)) {
        //     Log.e(TAG, "Calendar permissions not granted.")
        //     // Note: A Runner typically cannot request permissions directly.
        //     // Permissions should be checked/requested in the Config Activity.
        //     // If not granted, the plugin action might just fail or do nothing.
        //     return TaskerPluginResultError(1, "Calendar permissions not granted.")
        // }

        // val resolverHelper = CalendarResolverHelper()

        if (input.regular.actionType == PluginEditActivity.ACTION_ADD_EVENT) {
            Log.d(TAG, "Simulating Add Event action for title: ${input.regular.addEventTitle}")
            // Actual call to resolverHelper.addEvent(...) would be here.
            // For now, just returning success.
            if (input.regular.addEventTitle.isNullOrEmpty()) {
                 return TaskerPluginResultError(2, "Event title is empty for Add Event action.")
            }
            // return TaskerPluginResultSuccess(CalendarPluginOutput(errorMessage = null))
        } else if (input.regular.actionType == PluginEditActivity.ACTION_GET_EVENTS) {
            Log.d(TAG, "Simulating Get Events action.")
            // Actual call to resolverHelper.getEvents(...) would be here.
            // For now, returning success with placeholder output.
            // return TaskerPluginResultSuccess(CalendarPluginOutput(nextEventTitle = "Sample Event (Simulated)", errorMessage = null))
        } else {
            Log.w(TAG, "Unknown action type: ${input.regular.actionType}")
            // return TaskerPluginResultError(3, "Unknown action type: ${input.regular.actionType}")
        }

        // Since all calendar logic is stubbed, we'll just return success for now
        // to test visibility and basic plugin flow.
        // Once the original CalendarResolverHelper issues are sorted, this runner
        // will need to be properly implemented.
        Log.d(TAG, "Plugin action (simulated) completed. Returning success.")
        return TaskerPluginResultSuccess(CalendarPluginOutput(errorMessage = "Action simulated, calendar functions disabled."))
    }
}
