package com.example.taskercalendarplugin.tasker.model

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
// Removed import com.example.taskercalendarplugin.R as it's no longer directly used in annotations

// --- Input Class ---
@TaskerInputRoot
class CalendarPluginInput @JvmOverloads constructor(
    @field:TaskerInputField(key = "action_type", labelResIdName = "label_action_type")
    var actionType: String? = null,

    @field:TaskerInputField(key = "get_events_count", labelResIdName = "label_max_events_to_get", ignoreInStringBlurb = true)
    var getEventsCount: String? = null,

    @field:TaskerInputField(key = "get_events_days_ahead", labelResIdName = "label_days_ahead_to_check", ignoreInStringBlurb = true)
    var getEventsDaysAhead: String? = null,

    @field:TaskerInputField(key = "add_event_title", labelResIdName = "label_event_title")
    var addEventTitle: String? = null,

    @field:TaskerInputField(key = "add_event_description", labelResIdName = "label_event_description", ignoreInStringBlurb = true)
    var addEventDescription: String? = null,

    @field:TaskerInputField(key = "add_event_location", labelResIdName = "label_event_location", ignoreInStringBlurb = true)
    var addEventLocation: String? = null,

    @field:TaskerInputField(key = "add_event_start_time_offset", labelResIdName = "label_start_offset_mins", ignoreInStringBlurb = true)
    var addEventStartTimeOffset: String? = null,

    @field:TaskerInputField(key = "add_event_duration", labelResIdName = "label_duration_mins", ignoreInStringBlurb = true)
    var addEventDuration: String? = null,

    @field:TaskerInputField(key = "config_data_key", labelResIdName = "label_legacy_config", ignoreInStringBlurb = true)
    var legacyConfigData: String? = null
)

// --- Output Class ---
@TaskerOutputObject
class CalendarPluginOutput @JvmOverloads constructor(
    @get:TaskerOutputVariable(name = "next_event_title", labelResIdName = "label_next_event_title", htmlLabelResIdName = "label_next_event_title")
    val nextEventTitle: String? = null,

    @get:TaskerOutputVariable(name = "next_event_start_time", labelResIdName = "label_next_event_start_time_ms", htmlLabelResIdName = "label_next_event_start_time_ms")
    val nextEventStartTime: Long? = null,

    @get:TaskerOutputVariable(name = "next_event_location", labelResIdName = "label_next_event_location", htmlLabelResIdName = "label_next_event_location")
    val nextEventLocation: String? = null,

    @get:TaskerOutputVariable(name = "error_message", labelResIdName = "label_error_message", htmlLabelResIdName = "label_error_message")
    val errorMessage: String? = null
)
