package com.example.taskercalendarplugin.tasker.model

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

// --- Input Class ---
@TaskerInputRoot
class CalendarPluginInput @JvmOverloads constructor(
    @field:TaskerInputField("action_type", label = "Action Type")
    var actionType: String? = null, // Will store selected action (e.g., "action_get_events", "action_add_event")

    // Fields for "Get Events" action
    @field:TaskerInputField("get_events_count", label = "Max Events to Get", ignoreInStringBlurb = true)
    var getEventsCount: String? = null,

    @field:TaskerInputField("get_events_days_ahead", label = "Days Ahead to Check", ignoreInStringBlurb = true)
    var getEventsDaysAhead: String? = null,

    // Fields for "Add Event" action
    @field:TaskerInputField("add_event_title", label = "Event Title")
    var addEventTitle: String? = null,

    @field:TaskerInputField("add_event_description", label = "Event Description", ignoreInStringBlurb = true)
    var addEventDescription: String? = null,

    @field:TaskerInputField("add_event_location", label = "Event Location", ignoreInStringBlurb = true)
    var addEventLocation: String? = null,

    @field:TaskerInputField("add_event_start_time_offset", label = "Start Offset (mins)", ignoreInStringBlurb = true)
    var addEventStartTimeOffset: String? = null, // e.g., minutes from now

    @field:TaskerInputField("add_event_duration", label = "Duration (mins)", ignoreInStringBlurb = true)
    var addEventDuration: String? = null, // e.g., minutes

    // Fallback for old simple config (if needed, or can be removed)
    @field:TaskerInputField("config_data_key", label = "Legacy Config", ignoreInStringBlurb = true)
    var legacyConfigData: String? = null
)

// --- Output Class ---
@TaskerOutputObject
class CalendarPluginOutput @JvmOverloads constructor(
    @get:TaskerOutputVariable("next_event_title", label = "Next Event Title")
    val nextEventTitle: String? = null,

    @get:TaskerOutputVariable("next_event_start_time", label = "Next Event Start Time (ms)")
    val nextEventStartTime: Long? = null,

    @get:TaskerOutputVariable("next_event_location", label = "Next Event Location")
    val nextEventLocation: String? = null,

    @get:TaskerOutputVariable("error_message", label = "Error Message")
    val errorMessage: String? = null
)
