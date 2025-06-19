package com.example.taskercalendarplugin.tasker.model

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.example.taskercalendarplugin.R // Ensure R is imported

// --- Input Class ---
@TaskerInputRoot
class CalendarPluginInput @JvmOverloads constructor(
    @field:TaskerInputField(key = "action_type", labelResId = R.string.label_action_type)
    var actionType: String? = null,

    @field:TaskerInputField(key = "get_events_count", labelResId = R.string.label_max_events_to_get, ignoreInStringBlurb = true)
    var getEventsCount: String? = null,

    @field:TaskerInputField(key = "get_events_days_ahead", labelResId = R.string.label_days_ahead_to_check, ignoreInStringBlurb = true)
    var getEventsDaysAhead: String? = null,

    @field:TaskerInputField(key = "add_event_title", labelResId = R.string.label_event_title)
    var addEventTitle: String? = null,

    @field:TaskerInputField(key = "add_event_description", labelResId = R.string.label_event_description, ignoreInStringBlurb = true)
    var addEventDescription: String? = null,

    @field:TaskerInputField(key = "add_event_location", labelResId = R.string.label_event_location, ignoreInStringBlurb = true)
    var addEventLocation: String? = null,

    @field:TaskerInputField(key = "add_event_start_time_offset", labelResId = R.string.label_start_offset_mins, ignoreInStringBlurb = true)
    var addEventStartTimeOffset: String? = null,

    @field:TaskerInputField(key = "add_event_duration", labelResId = R.string.label_duration_mins, ignoreInStringBlurb = true)
    var addEventDuration: String? = null,

    @field:TaskerInputField(key = "config_data_key", labelResId = R.string.label_legacy_config, ignoreInStringBlurb = true)
    var legacyConfigData: String? = null
)

// --- Output Class ---
@TaskerOutputObject
class CalendarPluginOutput @JvmOverloads constructor(
    @get:TaskerOutputVariable("next_event_title", R.string.label_next_event_title, R.string.label_next_event_title)
    val nextEventTitle: String? = null,

    @get:TaskerOutputVariable("next_event_start_time", R.string.label_next_event_start_time_ms, R.string.label_next_event_start_time_ms)
    val nextEventStartTime: Long? = null,

    @get:TaskerOutputVariable("next_event_location", R.string.label_next_event_location, R.string.label_next_event_location)
    val nextEventLocation: String? = null,

    @get:TaskerOutputVariable("error_message", R.string.label_error_message, R.string.label_error_message)
    val errorMessage: String? = null
)
