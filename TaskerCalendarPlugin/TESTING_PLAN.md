# Tasker Calendar Plugin - Manual Test Plan

This document outlines the manual test cases for the Tasker Calendar Plugin.

## 1. Prerequisites

*   [ ] Android Device or Emulator (Android 5.0+).
*   [ ] Tasker application installed and enabled.
*   [ ] A Google Account (or other calendar provider) configured on the device with:
    *   [ ] Several existing calendar events in the next few days/week.
    *   [ ] At least one writable calendar.
*   [ ] The Tasker Calendar Plugin APK installed.

## 2. Installation Test

*   **Step**: Install the Tasker Calendar Plugin APK.
*   **Expected Result**: Plugin installs successfully. App icon might not be visible in launcher (as it's primarily a plugin), or a basic info screen might appear if a launcher activity were added. Plugin appears in Android Settings -> Apps.
*   **Pass/Fail**: [ ] Pass [ ] Fail

## 3. Permission Handling Tests

*   **Test Case 3.1: Initial Permission Request**
    *   **Step**: In Tasker, attempt to add a "Tasker Calendar Plugin" action or event/state for the first time. Open the configuration screen.
    *   **Expected Result**: The plugin configuration screen should appear. It should either immediately request Calendar permissions, or show a message that permissions are needed and provide a button/guidance to grant them.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 3.2: Granting Permissions**
    *   **Step**: When prompted, grant Calendar permissions.
    *   **Expected Result**: The permission status message on the configuration screen should disappear (or update to "granted"). UI elements (spinner, input fields) should become enabled.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 3.3: Denying Permissions**
    *   **Step**: When prompted, deny Calendar permissions.
    *   **Expected Result**: The permission status message should indicate permissions are denied. UI elements should be disabled. Attempting to save configuration should be blocked or result in a non-functional plugin.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 3.4: Denying Permissions with "Don't Ask Again" & Rationale**
    *   **Step**: Deny permissions. If prompted again, deny and select "Don't ask again". Re-enter plugin configuration.
    *   **Expected Result**: The configuration screen should show a message explaining why permissions are needed and offer a button to open App Settings (Rationale Dialog). UI elements remain disabled.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 3.5: Granting Permissions via Settings**
    *   **Step**: After denying with "Don't Ask Again", open App Settings (either via the rationale dialog or manually) and grant Calendar permissions for the plugin. Return to the plugin configuration in Tasker.
    *   **Expected Result**: The configuration screen should now reflect that permissions are granted (status message hidden/updated, UI enabled).
    *   **Pass/Fail**: [ ] Pass [ ] Fail

## 4. PluginEditActivity (Configuration UI) Tests - "Get Upcoming Events"

*   **Test Case 4.1: Select Action Type**
    *   **Step**: On the configuration screen, select "Get Upcoming Events" from the spinner.
    *   **Expected Result**: The "Get Events Configuration" section becomes visible. The "Add Event Configuration" section remains hidden.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 4.2: Configure "Number of Events"**
    *   **Step**: Enter `3` in "Number of events". Leave "Days ahead" blank. Click "Save Configuration".
    *   **Expected Result**: Configuration saves. Blurb in Tasker should reflect "Get Events: 3 events" (or similar).
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 4.3: Configure "Days Ahead"**
    *   **Step**: Clear "Number of events". Enter `5` in "Days ahead". Click "Save Configuration".
    *   **Expected Result**: Configuration saves. Blurb in Tasker should reflect "Get Events: 5 days ahead" (or similar).
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 4.4: Configure Both "Number of Events" and "Days Ahead"**
    *   **Step**: Enter `2` in "Number of events" and `3` in "Days ahead". Click "Save Configuration".
    *   **Expected Result**: Configuration saves. Blurb reflects both values (e.g., "Get Events: 2 events, 3 days ahead").
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 4.5: Missing Configuration for "Get Upcoming Events"**
    *   **Step**: Leave both "Number of events" and "Days ahead" blank. Click "Save Configuration".
    *   **Expected Result**: An error message (`setError` on EditTexts, or a Toast) should appear. Configuration should not save.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 4.6: Load Existing "Get Upcoming Events" Configuration**
    *   **Step**: Re-edit a previously saved "Get Upcoming Events" configuration.
    *   **Expected Result**: The spinner should be set to "Get Upcoming Events", and the previously saved values for count/days ahead should be populated in the correct fields.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

## 5. PluginEditActivity (Configuration UI) Tests - "Add New Event"

*   **Test Case 5.1: Select Action Type**
    *   **Step**: On the configuration screen, select "Add New Event" from the spinner.
    *   **Expected Result**: The "Add Event Configuration" section becomes visible. The "Get Events Configuration" section remains hidden.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 5.2: Configure All Fields for "Add New Event"**
    *   **Step**: Fill in Title ("Test Event"), Description ("Test Desc"), Location ("Test Loc"), Start Time Offset ("10"), Duration ("5"). Click "Save Configuration".
    *   **Expected Result**: Configuration saves. Blurb in Tasker should reflect "Add Event: Test Event" (or similar).
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 5.3: Missing Title for "Add New Event"**
    *   **Step**: Leave "Title" blank. Fill other fields. Click "Save Configuration".
    *   **Expected Result**: An error message on the Title field. Configuration should not save.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 5.4: Non-numeric Input for Offset/Duration**
    *   **Step**: Enter "abc" in "Start Time Offset". Click "Save Configuration".
    *   **Expected Result**: An error message on the field. Configuration should not save. Repeat for "Duration".
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 5.5: Load Existing "Add New Event" Configuration**
    *   **Step**: Re-edit a previously saved "Add New Event" configuration.
    *   **Expected Result**: Spinner set to "Add New Event", all previously saved text fields populated.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

## 6. Tasker Integration - "Get Upcoming Events" (Query Receiver) Tests

*   **Test Case 6.1: Condition Satisfied (Event Exists)**
    *   **Step**: Create a Tasker profile with a "Tasker Calendar Plugin" state context. Configure it for "Get Upcoming Events" to match an existing event (e.g., Days Ahead: 1, Number of Events: 1). Link to a task that flashes `%next_event_title`.
    *   **Expected Result**: The profile becomes active. The task runs and flashes the correct event title. `%cal_error` should not be set or be empty.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 6.2: Condition Unsatisfied (No Event Exists)**
    *   **Step**: Configure "Get Upcoming Events" for a condition that won't be met (e.g., Days Ahead: 1, for a day with no events, or Number of Events: 5 when only 1 exists).
    *   **Expected Result**: The profile does not become active / the linked task does not run if it's an event context. If a state, it becomes inactive. `%cal_error` should not be set.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 6.3: Legacy "upcoming_event_exists" (if still supported by receiver)**
    *   **Step**: If the receiver still handles the old plain text config, test with that.
    *   **Expected Result**: Behaves like 6.1 or 6.2 based on event presence.
    *   **Pass/Fail**: [ ] Pass [ ] Fail (N/A if legacy not supported)

## 7. Tasker Integration - "Add New Event" (Fire Receiver) Tests

*   **Test Case 7.1: Add Event Successfully**
    *   **Step**: Create a Tasker task. Add a "Tasker Calendar Plugin" action. Configure it for "Add New Event" with valid Title, Offset (e.g., 5 mins), Duration. Run the task.
    *   **Expected Result**: The event is added to the device's default writable calendar at the correct time. Check the calendar app to confirm.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 7.2: Add Event with Tasker Variables**
    *   **Step**: In a Tasker task, set a variable `%MyTitle` to "Event From Tasker Var". Configure "Add New Event" action with Title: `%MyTitle`. Run task.
    *   **Expected Result**: Event is added with the title "Event From Tasker Var".
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 7.3: Add Event - Invalid Configuration (e.g., missing title - though UI should prevent this save)**
    *   **Step**: If possible to save an invalid config (e.g., if validation was bypassed or during legacy testing), run a task with such a configuration.
    *   **Expected Result**: Event is not added. Check logs for errors. Tasker might show an error for the action.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

## 8. Error Handling Verification

*   **Test Case 8.1: Query with Permissions Revoked**
    *   **Step**: Configure "Get Upcoming Events". Then, go to Android Settings and revoke Calendar permission for the plugin. Trigger the Tasker profile/task.
    *   **Expected Result**: Profile condition should be unsatisfied/unknown. `%cal_error` variable in Tasker should be populated with a permission error message.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 8.2: Fire Action with Permissions Revoked**
    *   **Step**: Configure "Add New Event". Revoke Calendar permission. Trigger the Tasker task.
    *   **Expected Result**: Event is not added. Check plugin logs for permission error. Tasker may or may not show an error for the action itself, but the action should fail silently or with a log.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

*   **Test Case 8.3: Invalid Numeric Input in Configuration (Query Receiver)**
    *   **Step**: Manually create a bundle for `PluginQueryReceiver` (if possible through Tasker testing tools, or by editing saved Tasker data) with non-numeric `get_events_count`. Trigger.
    *   **Expected Result**: `%cal_error` should indicate an invalid number format or configuration error. Condition unsatisfied/unknown.
    *   **Pass/Fail**: [ ] Pass [ ] Fail

## 9. Logging Verification

*   **Step**: While performing other tests, use `adb logcat` to monitor plugin logs filtering by the relevant TAGs (e.g., `PluginEditActivity`, `PluginQueryReceiver`, `PluginFireReceiver`, `CalendarResolverHelper`).
*   **Expected Result**:
    *   [ ] Informative debug messages (`Log.d`) for normal operations.
    *   [ ] Warning messages (`Log.w`) for recoverable issues or unexpected but non-fatal states.
    *   [ ] Error messages (`Log.e`) with stack traces for exceptions.
    *   [ ] Permission grant/denial events are logged in `PluginEditActivity`.
    *   [ ] Receivers log received configurations and outcomes.
*   **Pass/Fail**: [ ] Pass [ ] Fail

## 10. README Review

*   **Step**: Read through `README.md`.
*   **Expected Result**:
    *   [ ] Information is clear, accurate, and comprehensive.
    *   [ ] Setup instructions are easy to follow.
    *   [ ] Configuration details match the actual UI and behavior.
    *   [ ] Example tasks are understandable.
    *   [ ] Troubleshooting tips are relevant.
*   **Pass/Fail**: [ ] Pass [ ] Fail

This testing plan provides a structured approach to manually verify the plugin's functionality.
