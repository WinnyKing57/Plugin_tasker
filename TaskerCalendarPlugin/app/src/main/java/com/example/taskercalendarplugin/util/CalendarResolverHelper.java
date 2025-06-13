package com.example.taskercalendarplugin.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import androidx.core.content.ContextCompat;

import com.example.taskercalendarplugin.model.CalendarDTO; // Updated import
import com.example.taskercalendarplugin.model.EventDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CalendarResolverHelper {

    private static final String TAG = "CalendarResolverHelper";

    // Projection for querying calendars
    private static final String[] CALENDAR_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 2
    };
    private static final int CALENDAR_PROJECTION_ID_INDEX = 0;
    private static final int CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX = 2;


    // Projection for querying events
    private static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events._ID,                 // 0
            CalendarContract.Events.TITLE,               // 1
            CalendarContract.Events.DESCRIPTION,         // 2
            CalendarContract.Events.DTSTART,             // 3
            CalendarContract.Events.DTEND,               // 4
            CalendarContract.Events.EVENT_LOCATION,      // 5
            CalendarContract.Events.ALL_DAY              // 6
    };
    private static final int EVENT_PROJECTION_ID_INDEX = 0;
    private static final int EVENT_PROJECTION_TITLE_INDEX = 1;
    private static final int EVENT_PROJECTION_DESCRIPTION_INDEX = 2;
    private static final int EVENT_PROJECTION_DTSTART_INDEX = 3;
    private static final int EVENT_PROJECTION_DTEND_INDEX = 4;
    private static final int EVENT_PROJECTION_EVENT_LOCATION_INDEX = 5;
    private static final int EVENT_PROJECTION_ALL_DAY_INDEX = 6;

    // For getDefaultWritableCalendarId
    private static final String[] CALENDAR_WRITE_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,         // 2
            CalendarContract.Calendars.CAN_MODIFY_EVENTS              // 3
    };
    private static final int CALENDAR_WRITE_PROJECTION_ID_INDEX = 0;
    private static final int CALENDAR_WRITE_PROJECTION_ACCESS_LEVEL_INDEX = 2;
    private static final int CALENDAR_WRITE_PROJECTION_CAN_MODIFY_EVENTS_INDEX = 3;


    /**
     * Queries available calendars.
     *
     * @param context The context to use for getting the ContentResolver.
     * @return A list of CalendarDTO objects.
     */
    public List<CalendarDTO> getCalendars(Context context) {
        Log.d(TAG, "getCalendars() called.");
        ArrayList<CalendarDTO> calendars = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "READ_CALENDAR permission not granted. Cannot query calendars.");
            return calendars; // Return empty list if permission is missing
        }

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            Log.e(TAG, "Failed to obtain ContentResolver instance.");
            return calendars;
        }

        Cursor cursor = null;
        try {
            cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                    CALENDAR_PROJECTION, null, null, null);

            if (cursor != null) {
                Log.d(TAG, "Successfully queried calendars. Count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(CALENDAR_PROJECTION_ID_INDEX);
                    String displayName = cursor.getString(CALENDAR_PROJECTION_DISPLAY_NAME_INDEX);
                    String ownerAccount = cursor.getString(CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX);
                    calendars.add(new CalendarDTO(id, displayName, ownerAccount));
                }
            } else {
                Log.d(TAG, "Calendar query returned null cursor.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while querying calendars.", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception while querying calendars.", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "Returning " + calendars.size() + " calendars.");
        return calendars;
    }


    /**
     * Fetches events from the Calendar Provider based on a time range.
     *
     * @param context         The context to use for getting the ContentResolver.
     * @param startTimeMillis The start of the time range in milliseconds.
     * @param endTimeMillis   The end of the time range in milliseconds.
     * @return A list of EventDTO objects.
     */
    public List<EventDTO> getEvents(Context context, long startTimeMillis, long endTimeMillis) {
        Log.d(TAG, "getEvents() called for time range: " + startTimeMillis + " to " + endTimeMillis);
        ArrayList<EventDTO> events = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "READ_CALENDAR permission not granted. Cannot query events.");
            return events; // Return empty list if permission is missing
        }

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            Log.e(TAG, "Failed to obtain ContentResolver instance.");
            return events;
        }

        // Selection criteria:
        // Catches events that:
        // 1. Start during the period: (DTSTART >= startTimeMillis AND DTSTART <= endTimeMillis)
        // 2. End during the period: (DTEND >= startTimeMillis AND DTEND <= endTimeMillis)
        // 3. Span the entire period: (DTSTART < startTimeMillis AND DTEND > endTimeMillis)
        // Note: For all-day events, DTSTART and DTEND might be at midnight in UTC.
        // Adjust timezones or handling if precise all-day event boundaries are critical.
        String selection = "((" + CalendarContract.Events.DTSTART + " >= ? AND " +
                CalendarContract.Events.DTSTART + " <= ?) OR (" +
                CalendarContract.Events.DTEND + " >= ? AND " +
                CalendarContract.Events.DTEND + " <= ?) OR (" +
                CalendarContract.Events.DTSTART + " < ? AND " +
                CalendarContract.Events.DTEND + " > ?))";

        String[] selectionArgs = new String[]{
                String.valueOf(startTimeMillis), String.valueOf(endTimeMillis),
                String.valueOf(startTimeMillis), String.valueOf(endTimeMillis),
                String.valueOf(startTimeMillis), String.valueOf(endTimeMillis)
        };

        Cursor cursor = null;
        try {
            // Using Instances.CONTENT_URI might be better for recurring events,
            // as it expands them. For simplicity, Events.CONTENT_URI is used here.
            // To query instances and expand recurring events:
            // Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            // ContentUris.appendId(builder, startTimeMillis);
            // ContentUris.appendId(builder, endTimeMillis);
            // cursor = resolver.query(builder.build(), EVENT_PROJECTION, selection, selectionArgs, CalendarContract.Events.DTSTART + " ASC");

            cursor = resolver.query(CalendarContract.Events.CONTENT_URI,
                    EVENT_PROJECTION, selection, selectionArgs, CalendarContract.Events.DTSTART + " ASC");

            if (cursor != null) {
                Log.d(TAG, "Successfully queried events. Count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(EVENT_PROJECTION_ID_INDEX);
                    String title = cursor.getString(EVENT_PROJECTION_TITLE_INDEX);
                    String description = cursor.getString(EVENT_PROJECTION_DESCRIPTION_INDEX);
                    long dtstart = cursor.getLong(EVENT_PROJECTION_DTSTART_INDEX);
                    long dtend = cursor.isNull(EVENT_PROJECTION_DTEND_INDEX) ? dtstart : cursor.getLong(EVENT_PROJECTION_DTEND_INDEX); // Handle null DTEND for some events
                    String eventLocation = cursor.getString(EVENT_PROJECTION_EVENT_LOCATION_INDEX);
                    int allDayInt = cursor.getInt(EVENT_PROJECTION_ALL_DAY_INDEX);
                    boolean allDay = allDayInt == 1;

                    events.add(new EventDTO(id, title, description, dtstart, dtend, eventLocation, allDay));
                }
            } else {
                Log.d(TAG, "Event query returned null cursor.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while querying events.", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception while querying events.", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "Returning " + events.size() + " events.");
        return events;
    }

    /**
     * Gets upcoming events from now until a week in the future.
     *
     * @param context The context to use.
     * @return A list of EventDTO objects.
     */
    public List<EventDTO> getUpcomingEvents(Context context) {
        Log.d(TAG, "getUpcomingEvents() called.");
        Calendar calendar = Calendar.getInstance();
        long startTimeMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.WEEK_OF_YEAR, 1); // Events for the next 7 days
        long endTimeMillis = calendar.getTimeInMillis();

        Log.d(TAG, "Upcoming events range: from " + startTimeMillis + " to " + endTimeMillis);
        return getEvents(context, startTimeMillis, endTimeMillis);
    }

    /**
     * Gets upcoming events from now until a month in the future.
     *
     * @param context The context to use.
     * @return A list of EventDTO objects.
     */
    public List<EventDTO> getUpcomingEventsForMonth(Context context) {
        Log.d(TAG, "getUpcomingEventsForMonth() called.");
        Calendar calendar = Calendar.getInstance();
        long startTimeMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1); // Events for the next month
        long endTimeMillis = calendar.getTimeInMillis();

        Log.d(TAG, "Upcoming events for month range: from " + startTimeMillis + " to " + endTimeMillis);
        return getEvents(context, startTimeMillis, endTimeMillis);
    }

    /**
     * Adds a new event to the specified calendar.
     *
     * @param context         The context to use.
     * @param calendarId      The ID of the calendar to add the event to.
     * @param title           The title of the event.
     * @param description     The description of the event.
     * @param startTimeMillis The start time of the event in milliseconds.
     * @param endTimeMillis   The end time of the event in milliseconds.
     * @param location        The location of the event.
     * @param timezone        The timezone for the event. Defaults to system default if null/empty.
     * @return The URI of the newly created event, or null if insertion fails or permissions are missing.
     */
    public Uri addEvent(Context context, long calendarId, String title, String description,
                        long startTimeMillis, long endTimeMillis, String location, String timezone) {
        Log.d(TAG, "addEvent() called for calendar ID: " + calendarId + ", title: " + title);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "WRITE_CALENDAR permission not granted. Cannot add event.");
            return null;
        }

        // Input validation
        if (title == null || title.trim().isEmpty()) {
            Log.e(TAG, "Event title cannot be empty.");
            return null;
        }
        if (startTimeMillis <= 0 || endTimeMillis <= 0 || endTimeMillis < startTimeMillis) {
            Log.e(TAG, "Invalid event start/end times. Start: " + startTimeMillis + ", End: " + endTimeMillis);
            return null;
        }
        if (calendarId <= 0) {
            Log.e(TAG, "Invalid Calendar ID: " + calendarId);
            return null;
        }

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            Log.e(TAG, "Failed to obtain ContentResolver instance.");
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startTimeMillis);
        values.put(CalendarContract.Events.DTEND, endTimeMillis);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description != null ? description : "");
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_LOCATION, location != null ? location : "");

        if (timezone == null || timezone.trim().isEmpty()) {
            values.put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().getID());
            Log.d(TAG, "Using default timezone: " + java.util.TimeZone.getDefault().getID());
        } else {
            values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
        }

        Uri eventUri = null;
        try {
            eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, values);
            if (eventUri != null) {
                Log.d(TAG, "Event added successfully. URI: " + eventUri.toString());
            } else {
                Log.e(TAG, "Failed to add event. Insert returned null URI.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while adding event.", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception while adding event.", e);
        }

        return eventUri;
    }

    /**
     * Gets the ID of a default writable calendar.
     * It prioritizes calendars that are not hidden and are primary for the account if possible.
     *
     * @param context The context to use.
     * @return The ID of a writable calendar, or -1 if none is found or permissions are missing.
     */
    public long getDefaultWritableCalendarId(Context context) {
        Log.d(TAG, "getDefaultWritableCalendarId() called.");

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "READ_CALENDAR permission not granted. Cannot query for writable calendars.");
            return -1;
        }

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            Log.e(TAG, "Failed to obtain ContentResolver instance.");
            return -1;
        }

        String selection = "(" + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= ? AND " +
                           CalendarContract.Calendars.CAN_MODIFY_EVENTS + " = ?)";
        String[] selectionArgs = new String[]{
                String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR),
                "1"
        };
        // Prefer primary calendars if available, then other writable ones.
        String sortOrder = CalendarContract.Calendars.IS_PRIMARY + " DESC, " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " ASC";


        Cursor cursor = null;
        long calendarId = -1;

        try {
            cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                    CALENDAR_WRITE_PROJECTION, selection, selectionArgs, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                calendarId = cursor.getLong(CALENDAR_WRITE_PROJECTION_ID_INDEX);
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
                Log.d(TAG, "Found writable calendar. ID: " + calendarId + ", Name: " + displayName);
            } else {
                Log.d(TAG, "No writable calendar found or cursor was null.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while querying for writable calendars.", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception while querying for writable calendars.", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (calendarId == -1) {
            Log.w(TAG, "Could not find any default writable calendar.");
        }
        return calendarId;
    }
}
