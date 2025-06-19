package com.example.taskercalendarplugin.tasker

import android.content.Context
import android.text.TextUtils
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.example.taskercalendarplugin.R // Assuming string resources are in the app module
import com.example.taskercalendarplugin.tasker.model.CalendarPluginInput
import com.example.taskercalendarplugin.tasker.model.CalendarPluginOutput
import com.example.taskercalendarplugin.ui.PluginEditActivity // For action type constants

class CalendarPluginHelper(config: TaskerPluginConfig<CalendarPluginInput>) :
    TaskerPluginConfigHelper<CalendarPluginInput, CalendarPluginOutput, CalendarActionRunner>(config) {

    override val runnerClass = CalendarActionRunner::class.java
    override val inputClass = CalendarPluginInput::class.java
    override val outputClass = CalendarPluginOutput::class.java // Not strictly used if no variables are output by default

    // This is called by the library when Tasker wants to generate a summary (blurb) for the action.
    override fun addToStringBlurb(input: TaskerInput<CalendarPluginInput>, blurbBuilder: StringBuilder) {
        val actionType = input.regular.actionType
        val context = config.context // Get context from the config interface

        if (actionType == PluginEditActivity.ACTION_GET_EVENTS) {
            blurbBuilder.append("Get Calendar Events: ")
            val count = input.regular.getEventsCount
            val daysAhead = input.regular.getEventsDaysAhead
            if (!TextUtils.isEmpty(count)) {
                blurbBuilder.append("Max $count events")
            }
            if (!TextUtils.isEmpty(daysAhead)) {
                if (!TextUtils.isEmpty(count)) blurbBuilder.append(", ")
                blurbBuilder.append("$daysAhead days ahead")
            }
            if (TextUtils.isEmpty(count) && TextUtils.isEmpty(daysAhead)) {
                blurbBuilder.append("(Default: upcoming week)") // Or some other default description
            }
        } else if (actionType == PluginEditActivity.ACTION_ADD_EVENT) {
            blurbBuilder.append("Add Calendar Event: ")
            val title = input.regular.addEventTitle
            if (!TextUtils.isEmpty(title)) {
                blurbBuilder.append(title)
            } else {
                blurbBuilder.append("(No title set)")
            }
        } else if (!input.regular.legacyConfigData.isNullOrEmpty()) {
            blurbBuilder.append("Legacy Config: ${input.regular.legacyConfigData}")
        }
        else {
            blurbBuilder.append("Calendar Plugin (Unconfigured action)")
        }
    }

    // (Optional) Override addOutputs if you want to define which variables are available to Tasker
    // For now, CalendarPluginOutput defines some, and they will be available by default.
    // override fun addOutputs(input: TaskerInput<CalendarPluginInput>, output: TaskerOutputsForConfig) {
    //     super.addOutputs(input, output) // This adds all from CalendarPluginOutput by default
    // }

    // (Optional) Override isInputValid if specific validation is needed before saving.
    // override fun isInputValid(input: TaskerInput<CalendarPluginInput>): SimpleResult {
    //    if (input.regular.actionType == PluginEditActivity.ACTION_ADD_EVENT && input.regular.addEventTitle.isNullOrBlank()) {
    //        return SimpleResultError("Event title cannot be empty for Add Event action.")
    //    }
    //    return super.isInputValid(input)
    // }
}
