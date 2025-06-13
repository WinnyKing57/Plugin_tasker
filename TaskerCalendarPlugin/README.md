# Tasker Calendar Plugin
[![Android CI Build APK Status](https://img.shields.io/github/actions/workflow/status/YOUR_USERNAME/YOUR_REPOSITORY/build-apk.yml?branch=main&label=Build)](https://github.com/YOUR_USERNAME/YOUR_REPOSITORY/actions/workflows/build-apk.yml)

A plugin for Tasker to read calendar events and create new ones.

## Features

*   **Get Upcoming Calendar Events**: Configure as a Tasker Event or State context to check for upcoming events.
*   **Add New Events**: Configure as a Tasker Action to create new events in your calendar.
*   **Flexible Event Creation**: Specify title, description, location, start time (as offset from now), and duration for new events.
*   **Tasker Variable Support**: Use Tasker variables in text fields for dynamic event creation.
*   **Event Data Export**: Exposes details of fetched events (title, location, start time, etc.) as Tasker variables.
*   **Error Reporting**: Provides an error variable (`%cal_error`) if calendar queries fail.

## Continuous Integration

This project uses GitHub Actions to build the Android APK. Builds are automatically triggered upon every push or pull request to the `main` branch, and can also be initiated manually from the repository's "Actions" tab (useful for generating builds on demand). You can check the build status using the badge at the top of this README.

Successfully built APKs for the `main` branch (and manual runs) are available as artifacts in the GitHub Actions run summary. You will need to be logged into GitHub to download these artifacts.


## Setup and Installation

1.  **Installation**: Install the Tasker Calendar Plugin APK on your Android device like any other application.
2.  **Permissions**:
    *   The plugin requires **Read Calendar** and **Write Calendar** permissions to function.
    *   When you first configure a plugin action or condition in Tasker, the plugin's configuration screen will appear. If permissions have not been granted, it will attempt to request them.
    *   If you initially deny permissions or select "Don't ask again," you may need to grant them manually via your device's system settings:
        *   Go to `Settings > Apps > Tasker Calendar Plugin > Permissions`.
        *   Ensure "Calendar" permission is allowed.
    *   The plugin's configuration screen will indicate if permissions are missing and guide you.

## Plugin Configuration in Tasker

The Tasker Calendar Plugin can be used as both an "Action" (e.g., to add an event) or as an "Event" or "State" context (e.g., to check if an event is upcoming).

### Adding an Action (e.g., Add Event)

1.  In your Tasker Task, add an action: `Plugin -> Tasker Calendar Plugin`.
2.  Tap the "Configuration" (pencil) icon.
3.  The plugin's configuration screen will appear.
4.  Select **"Add New Event"** from the "Action Type" spinner.
5.  Configure the event details (see "Configuration Screen Details" below).
6.  Tap "Save Configuration".

### Adding an Event/State Context (e.g., Get Upcoming Event)

1.  In your Tasker Profile, add a context: `Event -> Plugin -> Tasker Calendar Plugin` or `State -> Plugin -> Tasker Calendar Plugin`.
2.  Tap the "Configuration" (pencil) icon.
3.  The plugin's configuration screen will appear.
4.  Select **"Get Upcoming Events"** from the "Action Type" spinner.
5.  Configure the event fetching parameters (see "Configuration Screen Details" below).
6.  Tap "Save Configuration".

## Configuration Screen Details (`PluginEditActivity`)

The configuration screen allows you to specify what the plugin should do and with what parameters.

### Action Type Spinner

This is the primary choice that determines the plugin's behavior:

*   **`Get Upcoming Events`**:
    *   Use this for Tasker **Event** or **State** contexts.
    *   It checks if events meeting your criteria exist.
    *   If an event is found, it makes its details available as Tasker variables.
*   **`Add New Event`**:
    *   Use this for Tasker **Actions**.
    *   It creates a new event in your calendar.

### Parameters for "Get Upcoming Events"

When "Get Upcoming Events" is selected, the following fields are available:

*   **`Number of Events`**: (Optional) Specify how many upcoming events to check for. For example, if set to `1`, the condition will be true if at least one event is found. If left blank, `Days Ahead` must be specified.
*   **`Days Ahead`**: (Optional) Specify how many days into the future to look for events (e.g., `7` for one week). If left blank, `Number of Events` must be specified. If both are provided, `Days Ahead` defines the window, and `Number of Events` can refine the condition. (Note: Current implementation primarily uses `Days Ahead` for the window, and `Number of Events` to check if at least that many exist within the window).

### Parameters for "Add New Event"

When "Add New Event" is selected, the following fields are available. Tasker variables (e.g., `%MyTitle`) can be used in these fields.

*   **`Title`***: (Required) The title of the event.
*   **`Description`**: A description for the event.
*   **`Location`**: The location of the event.
*   **`Start Time Offset (minutes)`**: How many minutes *from now* the event should start (e.g., `60` for 1 hour, `1440` for 24 hours). Defaults to `60` if left blank.
*   **`Duration (minutes)`**: The duration of the event in minutes. Defaults to `30` if left blank.

## Tasker Variables Provided (by "Get Upcoming Events")

When the "Get Upcoming Events" condition is satisfied, it provides details of the *first* matching event found via the following Tasker variables. These variables are populated locally within the task that uses the plugin condition/state.

*   **`%next_event_title`**: Title of the event.
*   **`%next_event_start_time`**: Start time of the event in milliseconds since epoch.
*   **`%next_event_location`**: Location of the event.
    *   *(Note: The current implementation in `PluginQueryReceiver` provides these. Older/alternative Tasker syntaxes like `%ev_title(1)` might be relevant if the plugin were to return arrays directly, but the current setup focuses on the single next event for simplicity in conditions).*

*   **`%cal_error`**: If there was an error querying the calendar (e.g., permissions denied at the time of query, configuration error), this variable will contain an error message. Check this if the condition behaves unexpectedly.

*(Note: The plugin currently focuses on the single next event for condition checking. For advanced scenarios returning multiple events as arrays, the plugin and variable naming would need enhancements.)*

## Example Tasker Profiles/Tasks

### Example 1: Morning Briefing for First Event

*   **Profile**: Time context (e.g., Every day at 7:00 AM).
*   **Task**:
    1.  **Plugin Action**: `Plugin -> Tasker Calendar Plugin`
        *   Configuration:
            *   Action Type: `Get Upcoming Events`
            *   Days Ahead: `1` (for events within the next 24 hours)
            *   Number of Events: `1` (to check if at least one exists)
        *   (This setup acts as a condition check by attempting to fetch event data)
    2.  **If `%next_event_title` Is Set**:
        *   `Alert -> Say`: "Good morning! Your first event is %next_event_title. It's at %next_event_location." (Adjust time formatting as needed from `%next_event_start_time`)
    3.  **Else**:
        *   `Alert -> Say`: "Good morning! You have no upcoming events scheduled for today."
    4.  **End If**

### Example 2: Quickly Add a Reminder Event (e.g., via a Tasker Scene or Shortcut)

*   **Task**:
    1.  `Input -> Variable Query`:
        *   Title: "Event Title?"
        *   Variable: `%MyEventTitle`
    2.  `Input -> Variable Query`:
        *   Title: "Start in how many minutes?"
        *   Variable: `%MyEventOffset`
        *   Input Type: Numeric - Integer
    3.  **Plugin Action**: `Plugin -> Tasker Calendar Plugin`
        *   Configuration:
            *   Action Type: `Add New Event`
            *   Title: `%MyEventTitle`
            *   Description: "Quick reminder added via Tasker."
            *   Location: (Leave blank or specify a default)
            *   Start Time Offset (minutes): `%MyEventOffset`
            *   Duration (minutes): `15`
    4.  `Alert -> Flash`: "Event '%MyEventTitle' added to calendar."

## Troubleshooting

*   **Plugin Not Working / No Events Found/Added**:
    *   **Check Permissions**: The most common issue. Go to Android `Settings -> Apps -> Tasker Calendar Plugin -> Permissions` and ensure "Calendar" permission is **Allowed**.
    *   **Calendar Sync**: Ensure that calendar synchronization is active for your Google Account (or other calendar accounts) on your device. `Settings -> Accounts -> Your Account -> Account sync`.
    *   **Error Variable**: If using "Get Upcoming Events", check the value of `%cal_error` in Tasker after the action runs. It might contain specific error messages.
*   **Configuration Not Saving**:
    *   Make sure you tap the "Save Configuration" button within the plugin's configuration screen before exiting back to Tasker.
*   **Tasker Logs**:
    *   Enable Tasker's own logging (Menu -> More -> Run Log) to see if Tasker reports any errors when interacting with the plugin.

## Future Enhancements (Ideas)

*   Support for selecting specific calendars to read from or write to.
*   More advanced event filtering options (e.g., by title keywords, availability).
*   Handling of recurring events.
*   Direct date/time pickers in the configuration screen.
*   Option to modify or delete existing events.

---
*This README provides a general guide. Specific behavior might vary based on your Android version and Tasker version.*
