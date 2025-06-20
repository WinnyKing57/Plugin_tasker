package net.dinglisch.android.tasker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TaskerPlugin {

    // 3rd party dev constants
    // -----------------------------------------------------------------------------------

    // Paid version of Tasker, also Locale
    public final static String TASKER_PACKAGE = "net.dinglisch.android.taskerm";
    public final static String LOCALE_PACKAGE = "com.twofortyfouram.locale";

    // Other Tasker versions
    public final static String TASKER_PACKAGE_TRIAL = "net.dinglisch.android.taskert";
    public final static String TASKER_PACKAGE_JUNIOR = "net.dinglisch.android.taskerjr";
    public final static String TASKER_PACKAGE_MARKETONLY = "net.dinglisch.android.taskermarket";

    // Intent actions for plugin interaction
    public final static String ACTION_EDIT_PLUGIN = "com.twofortyfouram.locale.intent.action.EDIT_SETTING";
    public final static String ACTION_FIRE_SETTING = "com.twofortyfouram.locale.intent.action.FIRE_SETTING";
    public final static String ACTION_QUERY_PLUGIN = "com.twofortyfouram.locale.intent.action.QUERY_CONDITION";

    // For ACTION_QUERY_PLUGIN_ASYNC, results go to this broadcast receiver
    public final static String ACTION_PLUGIN_QUERY_COMPLETED = "net.dinglisch.android.tasker.ACTION_PLUGIN_QUERY_COMPLETED";

    // For ACTION_QUERY_PLUGIN, this is the result code if condition is NOT satisfied
    public final static int RESULT_CONDITION_UNSATISFIED = Activity.RESULT_FIRST_USER;

    // For ACTION_QUERY_PLUGIN, this is the result code if condition IS satisfied
    public final static int RESULT_CONDITION_SATISFIED = Activity.RESULT_OK;

    // For ACTION_EDIT_PLUGIN, this is the result code if the user cancels the edit
    public final static int RESULT_USER_CANCEL = Activity.RESULT_CANCELED;

    // For ACTION_EDIT_PLUGIN, this is the result code if the user accepts the edit
    public final static int RESULT_OK = Activity.RESULT_OK;

    // For ACTION_EDIT_PLUGIN, this is result code if user accepts and Tasker should not show a toast
    public final static int RESULT_OK_NO_SHOW = Activity.RESULT_FIRST_USER + 1;

    // For ACTION_EDIT_PLUGIN, this is result code if user accepts and Tasker should immediately fire the plugin
    public final static int RESULT_OK_FIRE_IMMEDIATELY = Activity.RESULT_FIRST_USER + 2;

    // For ACTION_EDIT_PLUGIN, this is result code if user accepts and Tasker should immediately fire the plugin and not show a toast
    public final static int RESULT_OK_FIRE_IMMEDIATELY_NO_SHOW = Activity.RESULT_FIRST_USER + 3;

    // For ACTION_EDIT_PLUGIN, this is result code if user accepts and Tasker should immediately fire the plugin and not show a toast and not save the config
    public final static int RESULT_OK_FIRE_IMMEDIATELY_NO_SHOW_NO_SAVE = Activity.RESULT_FIRST_USER + 4;

    // For ACTION_EDIT_PLUGIN, this is result code if user accepts and Tasker should not save the config
    public final static int RESULT_OK_NO_SAVE = Activity.RESULT_FIRST_USER + 5;

    // For ACTION_EDIT_PLUGIN, this is result code if user accepts and Tasker should not show a toast and not save the config
    public final static int RESULT_OK_NO_SHOW_NO_SAVE = Activity.RESULT_FIRST_USER + 6;


    // Intent extras
    public final static String EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";
    public final static String EXTRA_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";

    // For ACTION_FIRE_SETTING, results go to this broadcast receiver
    public final static String ACTION_PLUGIN_SETTING_RESULT = "net.dinglisch.android.tasker.ACTION_PLUGIN_SETTING_RESULT";


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT satisfied
    public final static int RESULT_ASYNC_QUERY_UNSATISFIED = Activity.RESULT_FIRST_USER + 10;

    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition IS satisfied
    public final static int RESULT_ASYNC_QUERY_SATISFIED = Activity.RESULT_FIRST_USER + 11;

    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is UNKNOWN
    public final static int RESULT_ASYNC_QUERY_UNKNOWN = Activity.RESULT_FIRST_USER + 12;

    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is ERROR
    public final static int RESULT_ASYNC_QUERY_ERROR = Activity.RESULT_FIRST_USER + 13;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is PENDING
    public final static int RESULT_ASYNC_QUERY_PENDING = Activity.RESULT_FIRST_USER + 14;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_APPLICABLE
    public final static int RESULT_ASYNC_QUERY_NOT_APPLICABLE = Activity.RESULT_FIRST_USER + 15;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_SUPPORTED
    public final static int RESULT_ASYNC_QUERY_NOT_SUPPORTED = Activity.RESULT_FIRST_USER + 16;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is TIMEOUT
    public final static int RESULT_ASYNC_QUERY_TIMEOUT = Activity.RESULT_FIRST_USER + 17;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is ACCESS_DENIED
    public final static int RESULT_ASYNC_QUERY_ACCESS_DENIED = Activity.RESULT_FIRST_USER + 18;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_INSTALLED
    public final static int RESULT_ASYNC_QUERY_NOT_INSTALLED = Activity.RESULT_FIRST_USER + 19;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_ENABLED
    public final static int RESULT_ASYNC_QUERY_NOT_ENABLED = Activity.RESULT_FIRST_USER + 20;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_CONFIGURED
    public final static int RESULT_ASYNC_QUERY_NOT_CONFIGURED = Activity.RESULT_FIRST_USER + 21;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_COMPATIBLE
    public final static int RESULT_ASYNC_QUERY_NOT_COMPATIBLE = Activity.RESULT_FIRST_USER + 22;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_VALID
    public final static int RESULT_ASYNC_QUERY_NOT_VALID = Activity.RESULT_FIRST_USER + 23;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_REACHABLE
    public final static int RESULT_ASYNC_QUERY_NOT_REACHABLE = Activity.RESULT_FIRST_USER + 24;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_AVAILABLE
    public final static int RESULT_ASYNC_QUERY_NOT_AVAILABLE = Activity.RESULT_FIRST_USER + 25;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_INITIALIZED
    public final static int RESULT_ASYNC_QUERY_NOT_INITIALIZED = Activity.RESULT_FIRST_USER + 26;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_READY
    public final static int RESULT_ASYNC_QUERY_NOT_READY = Activity.RESULT_FIRST_USER + 27;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_CONNECTED
    public final static int RESULT_ASYNC_QUERY_NOT_CONNECTED = Activity.RESULT_FIRST_USER + 28;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_LOGGED_IN
    public final static int RESULT_ASYNC_QUERY_NOT_LOGGED_IN = Activity.RESULT_FIRST_USER + 29;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_AUTHORIZED
    public final static int RESULT_ASYNC_QUERY_NOT_AUTHORIZED = Activity.RESULT_FIRST_USER + 30;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_PERMITTED
    public final static int RESULT_ASYNC_QUERY_NOT_PERMITTED = Activity.RESULT_FIRST_USER + 31;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_FOUND
    public final static int RESULT_ASYNC_QUERY_NOT_FOUND = Activity.RESULT_FIRST_USER + 32;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_EXIST
    public final static int RESULT_ASYNC_QUERY_NOT_EXIST = Activity.RESULT_FIRST_USER + 33;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_EMPTY
    public final static int RESULT_ASYNC_QUERY_NOT_EMPTY = Activity.RESULT_FIRST_USER + 34;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_FULL
    public final static int RESULT_ASYNC_QUERY_NOT_FULL = Activity.RESULT_FIRST_USER + 35;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_MATCH
    public final static int RESULT_ASYNC_QUERY_NOT_MATCH = Activity.RESULT_FIRST_USER + 36;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_EQUAL
    public final static int RESULT_ASYNC_QUERY_NOT_EQUAL = Activity.RESULT_FIRST_USER + 37;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_GREATER
    public final static int RESULT_ASYNC_QUERY_NOT_GREATER = Activity.RESULT_FIRST_USER + 38;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_LESS
    public final static int RESULT_ASYNC_QUERY_NOT_LESS = Activity.RESULT_FIRST_USER + 39;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_RANGE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_RANGE = Activity.RESULT_FIRST_USER + 40;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIST
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIST = Activity.RESULT_FIRST_USER + 41;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_GROUP
    public final static int RESULT_ASYNC_QUERY_NOT_IN_GROUP = Activity.RESULT_FIRST_USER + 42;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ZONE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ZONE = Activity.RESULT_FIRST_USER + 43;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_AREA
    public final static int RESULT_ASYNC_QUERY_NOT_IN_AREA = Activity.RESULT_FIRST_USER + 44;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NETWORK
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NETWORK = Activity.RESULT_FIRST_USER + 45;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_MODE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_MODE = Activity.RESULT_FIRST_USER + 46;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_STATE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_STATE = Activity.RESULT_FIRST_USER + 47;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_STATUS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_STATUS = Activity.RESULT_FIRST_USER + 48;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PROFILE = Activity.RESULT_FIRST_USER + 49;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TASK
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TASK = Activity.RESULT_FIRST_USER + 50;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_SCENE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_SCENE = Activity.RESULT_FIRST_USER + 51;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PROJECT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PROJECT = Activity.RESULT_FIRST_USER + 52;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_CONTEXT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_CONTEXT = Activity.RESULT_FIRST_USER + 53;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_GLOBAL_VAR
    public final static int RESULT_ASYNC_QUERY_NOT_IN_GLOBAL_VAR = Activity.RESULT_FIRST_USER + 54;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LOCAL_VAR
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LOCAL_VAR = Activity.RESULT_FIRST_USER + 55;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ARRAY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ARRAY = Activity.RESULT_FIRST_USER + 56;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_JSON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_JSON = Activity.RESULT_FIRST_USER + 57;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_XML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_XML = Activity.RESULT_FIRST_USER + 58;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_HTML = Activity.RESULT_FIRST_USER + 59;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_MARKDOWN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_MARKDOWN = Activity.RESULT_FIRST_USER + 60;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_BBCODE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_BBCODE = Activity.RESULT_FIRST_USER + 61;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TEXT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TEXT = Activity.RESULT_FIRST_USER + 62;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NUMBER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NUMBER = Activity.RESULT_FIRST_USER + 63;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_BOOLEAN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_BOOLEAN = Activity.RESULT_FIRST_USER + 64;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DATETIME
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DATETIME = Activity.RESULT_FIRST_USER + 65;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TIMESPAN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TIMESPAN = Activity.RESULT_FIRST_USER + 66;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_COLOR
    public final static int RESULT_ASYNC_QUERY_NOT_IN_COLOR = Activity.RESULT_FIRST_USER + 67;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ICON = Activity.RESULT_FIRST_USER + 68;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_FILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_FILE = Activity.RESULT_FIRST_USER + 69;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_URI
    public final static int RESULT_ASYNC_QUERY_NOT_IN_URI = Activity.RESULT_FIRST_USER + 70;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PACKAGE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PACKAGE = Activity.RESULT_FIRST_USER + 71;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CLASS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CLASS = Activity.RESULT_FIRST_USER + 72;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACTIVITY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACTIVITY = Activity.RESULT_FIRST_USER + 73;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_SERVICE = Activity.RESULT_FIRST_USER + 74;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_RECEIVER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_RECEIVER = Activity.RESULT_FIRST_USER + 75;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PROVIDER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PROVIDER = Activity.RESULT_FIRST_USER + 76;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_SHORTCUT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_SHORTCUT = Activity.RESULT_FIRST_USER + 77;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WIDGET
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WIDGET = Activity.RESULT_FIRST_USER + 78;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER = Activity.RESULT_FIRST_USER + 79;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYGUARD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYGUARD = Activity.RESULT_FIRST_USER + 80;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD = Activity.RESULT_FIRST_USER + 81;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE = Activity.RESULT_FIRST_USER + 82;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE = Activity.RESULT_FIRST_USER + 83;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE = Activity.RESULT_FIRST_USER + 84;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE = Activity.RESULT_FIRST_USER + 85;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE = Activity.RESULT_FIRST_USER + 86;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE = Activity.RESULT_FIRST_USER + 87;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE = Activity.RESULT_FIRST_USER + 88;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE = Activity.RESULT_FIRST_USER + 89;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE = Activity.RESULT_FIRST_USER + 90;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE = Activity.RESULT_FIRST_USER + 91;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE = Activity.RESULT_FIRST_USER + 92;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET = Activity.RESULT_FIRST_USER + 93;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER = Activity.RESULT_FIRST_USER + 94;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD = Activity.RESULT_FIRST_USER + 95;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER = Activity.RESULT_FIRST_USER + 96;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM = Activity.RESULT_FIRST_USER + 97;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT = Activity.RESULT_FIRST_USER + 98;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN = Activity.RESULT_FIRST_USER + 99;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SETTINGS = Activity.RESULT_FIRST_USER + 100;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SETTINGS = Activity.RESULT_FIRST_USER + 101;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 102;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 103;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 104;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 105;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 106;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 107;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 108;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 109;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 110;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_SETTINGS = Activity.RESULT_FIRST_USER + 111;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_SETTINGS = Activity.RESULT_FIRST_USER + 112;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_SETTINGS = Activity.RESULT_FIRST_USER + 113;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_SETTINGS = Activity.RESULT_FIRST_USER + 114;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_SETTINGS = Activity.RESULT_FIRST_USER + 115;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_SETTINGS = Activity.RESULT_FIRST_USER + 116;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SETTINGS = Activity.RESULT_FIRST_USER + 117;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SETTINGS = Activity.RESULT_FIRST_USER + 118;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SETTINGS
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SETTINGS = Activity.RESULT_FIRST_USER + 119;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_INFO = Activity.RESULT_FIRST_USER + 120;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_INFO = Activity.RESULT_FIRST_USER + 121;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_INFO = Activity.RESULT_FIRST_USER + 122;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_INFO = Activity.RESULT_FIRST_USER + 123;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_INFO = Activity.RESULT_FIRST_USER + 124;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_INFO = Activity.RESULT_FIRST_USER + 125;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_INFO = Activity.RESULT_FIRST_USER + 126;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_INFO = Activity.RESULT_FIRST_USER + 127;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_INFO = Activity.RESULT_FIRST_USER + 128;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_INFO = Activity.RESULT_FIRST_USER + 129;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_INFO = Activity.RESULT_FIRST_USER + 130;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_INFO = Activity.RESULT_FIRST_USER + 131;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_INFO = Activity.RESULT_FIRST_USER + 132;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_INFO = Activity.RESULT_FIRST_USER + 133;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_INFO = Activity.RESULT_FIRST_USER + 134;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_INFO = Activity.RESULT_FIRST_USER + 135;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_INFO = Activity.RESULT_FIRST_USER + 136;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_INFO = Activity.RESULT_FIRST_USER + 137;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_INFO = Activity.RESULT_FIRST_USER + 138;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_INFO = Activity.RESULT_FIRST_USER + 139;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_PREVIEW = Activity.RESULT_FIRST_USER + 140;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_PREVIEW = Activity.RESULT_FIRST_USER + 141;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 142;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 143;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 144;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 145;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 146;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 147;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 148;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 149;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 150;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_PREVIEW = Activity.RESULT_FIRST_USER + 151;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_PREVIEW = Activity.RESULT_FIRST_USER + 152;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_PREVIEW = Activity.RESULT_FIRST_USER + 153;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_PREVIEW = Activity.RESULT_FIRST_USER + 154;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_PREVIEW = Activity.RESULT_FIRST_USER + 155;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_PREVIEW = Activity.RESULT_FIRST_USER + 156;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_PREVIEW = Activity.RESULT_FIRST_USER + 157;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_PREVIEW = Activity.RESULT_FIRST_USER + 158;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_PREVIEW
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_PREVIEW = Activity.RESULT_FIRST_USER + 159;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_THUMBNAIL = Activity.RESULT_FIRST_USER + 160;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_THUMBNAIL = Activity.RESULT_FIRST_USER + 161;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 162;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 163;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 164;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 165;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 166;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 167;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 168;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 169;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 170;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_THUMBNAIL = Activity.RESULT_FIRST_USER + 171;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_THUMBNAIL = Activity.RESULT_FIRST_USER + 172;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_THUMBNAIL = Activity.RESULT_FIRST_USER + 173;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_THUMBNAIL = Activity.RESULT_FIRST_USER + 174;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_THUMBNAIL = Activity.RESULT_FIRST_USER + 175;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_THUMBNAIL = Activity.RESULT_FIRST_USER + 176;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_THUMBNAIL = Activity.RESULT_FIRST_USER + 177;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_THUMBNAIL = Activity.RESULT_FIRST_USER + 178;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_THUMBNAIL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_THUMBNAIL = Activity.RESULT_FIRST_USER + 179;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_ICON = Activity.RESULT_FIRST_USER + 180;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_ICON = Activity.RESULT_FIRST_USER + 181;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_ICON = Activity.RESULT_FIRST_USER + 182;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_ICON = Activity.RESULT_FIRST_USER + 183;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_ICON = Activity.RESULT_FIRST_USER + 184;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_ICON = Activity.RESULT_FIRST_USER + 185;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_ICON = Activity.RESULT_FIRST_USER + 186;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_ICON = Activity.RESULT_FIRST_USER + 187;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_ICON = Activity.RESULT_FIRST_USER + 188;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_ICON = Activity.RESULT_FIRST_USER + 189;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_ICON = Activity.RESULT_FIRST_USER + 190;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_ICON = Activity.RESULT_FIRST_USER + 191;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_ICON = Activity.RESULT_FIRST_USER + 192;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_ICON = Activity.RESULT_FIRST_USER + 193;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_ICON = Activity.RESULT_FIRST_USER + 194;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_ICON = Activity.RESULT_FIRST_USER + 195;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_ICON = Activity.RESULT_FIRST_USER + 196;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_ICON = Activity.RESULT_FIRST_USER + 197;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_ICON = Activity.RESULT_FIRST_USER + 198;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_ICON
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_ICON = Activity.RESULT_FIRST_USER + 199;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_LABEL = Activity.RESULT_FIRST_USER + 200;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_LABEL = Activity.RESULT_FIRST_USER + 201;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 202;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 203;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 204;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 205;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 206;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 207;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 208;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 209;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 210;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_LABEL = Activity.RESULT_FIRST_USER + 211;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_LABEL = Activity.RESULT_FIRST_USER + 212;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_LABEL = Activity.RESULT_FIRST_USER + 213;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_LABEL = Activity.RESULT_FIRST_USER + 214;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_LABEL = Activity.RESULT_FIRST_USER + 215;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_LABEL = Activity.RESULT_FIRST_USER + 216;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_LABEL = Activity.RESULT_FIRST_USER + 217;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_LABEL = Activity.RESULT_FIRST_USER + 218;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_LABEL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_LABEL = Activity.RESULT_FIRST_USER + 219;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_DESCRIPTION = Activity.RESULT_FIRST_USER + 220;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_DESCRIPTION = Activity.RESULT_FIRST_USER + 221;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 222;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 223;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 224;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 225;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 226;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 227;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 228;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 229;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 230;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_DESCRIPTION = Activity.RESULT_FIRST_USER + 231;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_DESCRIPTION = Activity.RESULT_FIRST_USER + 232;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_DESCRIPTION = Activity.RESULT_FIRST_USER + 233;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_DESCRIPTION = Activity.RESULT_FIRST_USER + 234;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_DESCRIPTION = Activity.RESULT_FIRST_USER + 235;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_DESCRIPTION = Activity.RESULT_FIRST_USER + 236;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_DESCRIPTION = Activity.RESULT_FIRST_USER + 237;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_DESCRIPTION = Activity.RESULT_FIRST_USER + 238;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_DESCRIPTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_DESCRIPTION = Activity.RESULT_FIRST_USER + 239;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_HTML_NOTE = Activity.RESULT_FIRST_USER + 240;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_HTML_NOTE = Activity.RESULT_FIRST_USER + 241;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 242;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 243;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 244;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 245;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 246;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 247;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 248;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 249;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 250;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_HTML_NOTE = Activity.RESULT_FIRST_USER + 251;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_HTML_NOTE = Activity.RESULT_FIRST_USER + 252;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_HTML_NOTE = Activity.RESULT_FIRST_USER + 253;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_HTML_NOTE = Activity.RESULT_FIRST_USER + 254;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_HTML_NOTE = Activity.RESULT_FIRST_USER + 255;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_HTML_NOTE = Activity.RESULT_FIRST_USER + 256;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_HTML_NOTE = Activity.RESULT_FIRST_USER + 257;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_HTML_NOTE = Activity.RESULT_FIRST_USER + 258;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_HTML_NOTE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_HTML_NOTE = Activity.RESULT_FIRST_USER + 259;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 260;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 261;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 262;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 263;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 264;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 265;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 266;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 267;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 268;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 269;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 270;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 271;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 272;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 273;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 274;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 275;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 276;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 277;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 278;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IGNORE_IN_STRING_BLURB
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IGNORE_IN_STRING_BLURB = Activity.RESULT_FIRST_USER + 279;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 280;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 281;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 282;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 283;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 284;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 285;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 286;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 287;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 288;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 289;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 290;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 291;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 292;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 293;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 294;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 295;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 296;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 297;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 298;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_OUTPUT_VAR_INFO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_OUTPUT_VAR_INFO = Activity.RESULT_FIRST_USER + 299;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_RENAMED_TO = Activity.RESULT_FIRST_USER + 300;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_RENAMED_TO = Activity.RESULT_FIRST_USER + 301;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 302;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 303;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 304;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 305;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 306;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 307;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 308;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 309;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 310;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_RENAMED_TO = Activity.RESULT_FIRST_USER + 311;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_RENAMED_TO = Activity.RESULT_FIRST_USER + 312;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_RENAMED_TO = Activity.RESULT_FIRST_USER + 313;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_RENAMED_TO = Activity.RESULT_FIRST_USER + 314;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_RENAMED_TO = Activity.RESULT_FIRST_USER + 315;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_RENAMED_TO = Activity.RESULT_FIRST_USER + 316;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_RENAMED_TO = Activity.RESULT_FIRST_USER + 317;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_RENAMED_TO = Activity.RESULT_FIRST_USER + 318;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_RENAMED_TO
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_RENAMED_TO = Activity.RESULT_FIRST_USER + 319;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_NAMER_HELPER = Activity.RESULT_FIRST_USER + 320;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_NAMER_HELPER = Activity.RESULT_FIRST_USER + 321;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 322;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 323;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 324;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 325;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 326;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 327;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 328;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 329;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 330;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 331;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_NAMER_HELPER = Activity.RESULT_FIRST_USER + 332;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_NAMER_HELPER = Activity.RESULT_FIRST_USER + 333;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_NAMER_HELPER = Activity.RESULT_FIRST_USER + 334;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_NAMER_HELPER = Activity.RESULT_FIRST_USER + 335;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_NAMER_HELPER = Activity.RESULT_FIRST_USER + 336;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_NAMER_HELPER = Activity.RESULT_FIRST_USER + 337;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_NAMER_HELPER = Activity.RESULT_FIRST_USER + 338;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_NAMER_HELPER
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_NAMER_HELPER = Activity.RESULT_FIRST_USER + 339;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_PREFIX = Activity.RESULT_FIRST_USER + 340;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_PREFIX = Activity.RESULT_FIRST_USER + 341;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 342;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 343;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 344;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 345;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 346;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 347;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 348;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 349;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 350;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_PREFIX = Activity.RESULT_FIRST_USER + 351;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_PREFIX = Activity.RESULT_FIRST_USER + 352;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_PREFIX = Activity.RESULT_FIRST_USER + 353;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_PREFIX = Activity.RESULT_FIRST_USER + 354;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_PREFIX = Activity.RESULT_FIRST_USER + 355;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_PREFIX = Activity.RESULT_FIRST_USER + 356;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_PREFIX = Activity.RESULT_FIRST_USER + 357;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_PREFIX = Activity.RESULT_FIRST_USER + 358;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_PREFIX
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_PREFIX = Activity.RESULT_FIRST_USER + 359;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 360;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 361;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 362;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 363;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 364;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 365;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 366;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 367;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 368;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 369;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 370;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 371;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 372;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 373;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 374;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 375;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 376;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 377;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 378;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_OUTPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_OUTPUT_ONLY = Activity.RESULT_FIRST_USER + 379;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_INPUT_ONLY = Activity.RESULT_FIRST_USER + 380;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_INPUT_ONLY = Activity.RESULT_FIRST_USER + 381;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 382;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 383;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 384;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 385;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 386;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 387;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 388;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 389;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 390;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 391;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_INPUT_ONLY = Activity.RESULT_FIRST_USER + 392;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_INPUT_ONLY = Activity.RESULT_FIRST_USER + 393;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_INPUT_ONLY = Activity.RESULT_FIRST_USER + 394;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_INPUT_ONLY = Activity.RESULT_FIRST_USER + 395;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_INPUT_ONLY = Activity.RESULT_FIRST_USER + 396;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_INPUT_ONLY = Activity.RESULT_FIRST_USER + 397;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_INPUT_ONLY = Activity.RESULT_FIRST_USER + 398;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_INPUT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_INPUT_ONLY = Activity.RESULT_FIRST_USER + 399;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 400;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 401;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 402;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 403;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 404;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 405;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 406;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 407;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 408;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 409;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 410;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 411;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 412;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 413;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 414;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 415;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 416;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 417;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 418;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_CONFIG_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_CONFIG_ONLY = Activity.RESULT_FIRST_USER + 419;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_ACTION_ONLY = Activity.RESULT_FIRST_USER + 420;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_ACTION_ONLY = Activity.RESULT_FIRST_USER + 421;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 422;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 423;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 424;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 425;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 426;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 427;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 428;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 429;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 430;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 431;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_ACTION_ONLY = Activity.RESULT_FIRST_USER + 432;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_ACTION_ONLY = Activity.RESULT_FIRST_USER + 433;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_ACTION_ONLY = Activity.RESULT_FIRST_USER + 434;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_ACTION_ONLY = Activity.RESULT_FIRST_USER + 435;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_ACTION_ONLY = Activity.RESULT_FIRST_USER + 436;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_ACTION_ONLY = Activity.RESULT_FIRST_USER + 437;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_ACTION_ONLY = Activity.RESULT_FIRST_USER + 438;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_ACTION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_ACTION_ONLY = Activity.RESULT_FIRST_USER + 439;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 440;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 441;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 442;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 443;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 444;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 445;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 446;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 447;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 448;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 449;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 450;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 451;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 452;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 453;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 454;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 455;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 456;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 457;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 458;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_CONDITION_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_CONDITION_ONLY = Activity.RESULT_FIRST_USER + 459;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_EVENT_ONLY = Activity.RESULT_FIRST_USER + 460;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_EVENT_ONLY = Activity.RESULT_FIRST_USER + 461;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 462;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 463;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 464;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 465;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 466;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 467;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 468;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 469;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 470;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 471;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_EVENT_ONLY = Activity.RESULT_FIRST_USER + 472;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_EVENT_ONLY = Activity.RESULT_FIRST_USER + 473;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_EVENT_ONLY = Activity.RESULT_FIRST_USER + 474;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_EVENT_ONLY = Activity.RESULT_FIRST_USER + 475;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_EVENT_ONLY = Activity.RESULT_FIRST_USER + 476;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_EVENT_ONLY = Activity.RESULT_FIRST_USER + 477;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_EVENT_ONLY = Activity.RESULT_FIRST_USER + 478;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_EVENT_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_EVENT_ONLY = Activity.RESULT_FIRST_USER + 479;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 480;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 481;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 482;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 483;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 484;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 485;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 486;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 487;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 488;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 489;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 490;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 491;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 492;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 493;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 494;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 495;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 496;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 497;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 498;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_DEFAULT_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_DEFAULT_VALUE = Activity.RESULT_FIRST_USER + 499;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_MIN_VALUE = Activity.RESULT_FIRST_USER + 500;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_MIN_VALUE = Activity.RESULT_FIRST_USER + 501;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 502;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 503;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 504;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 505;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 506;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 507;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 508;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 509;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 510;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_MIN_VALUE = Activity.RESULT_FIRST_USER + 511;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_MIN_VALUE = Activity.RESULT_FIRST_USER + 512;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_MIN_VALUE = Activity.RESULT_FIRST_USER + 513;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_MIN_VALUE = Activity.RESULT_FIRST_USER + 514;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_MIN_VALUE = Activity.RESULT_FIRST_USER + 515;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_MIN_VALUE = Activity.RESULT_FIRST_USER + 516;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_MIN_VALUE = Activity.RESULT_FIRST_USER + 517;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_MIN_VALUE = Activity.RESULT_FIRST_USER + 518;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_MIN_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_MIN_VALUE = Activity.RESULT_FIRST_USER + 519;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_MAX_VALUE = Activity.RESULT_FIRST_USER + 520;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_MAX_VALUE = Activity.RESULT_FIRST_USER + 521;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 522;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 523;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 524;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 525;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 526;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 527;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 528;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 529;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 530;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_MAX_VALUE = Activity.RESULT_FIRST_USER + 531;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_MAX_VALUE = Activity.RESULT_FIRST_USER + 532;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_MAX_VALUE = Activity.RESULT_FIRST_USER + 533;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_MAX_VALUE = Activity.RESULT_FIRST_USER + 534;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_MAX_VALUE = Activity.RESULT_FIRST_USER + 535;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_MAX_VALUE = Activity.RESULT_FIRST_USER + 536;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_MAX_VALUE = Activity.RESULT_FIRST_USER + 537;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_MAX_VALUE = Activity.RESULT_FIRST_USER + 538;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_MAX_VALUE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_MAX_VALUE = Activity.RESULT_FIRST_USER + 539;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 540;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 541;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 542;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 543;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 544;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 545;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 546;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 547;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 548;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 549;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 550;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 551;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 552;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 553;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 554;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 555;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 556;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 557;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 558;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_ALLOWED_VALUES
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_ALLOWED_VALUES = Activity.RESULT_FIRST_USER + 559;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 560;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 561;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 562;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 563;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 564;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 565;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 566;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 567;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 568;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 569;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 570;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 571;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 572;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 573;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 574;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 575;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 576;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 577;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 578;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_REGEX_VALIDATION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_REGEX_VALIDATION = Activity.RESULT_FIRST_USER + 579;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_PASSWORD = Activity.RESULT_FIRST_USER + 580;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_PASSWORD = Activity.RESULT_FIRST_USER + 581;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 582;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 583;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 584;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 585;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 586;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 587;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 588;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 589;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 590;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 591;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_PASSWORD = Activity.RESULT_FIRST_USER + 592;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_PASSWORD = Activity.RESULT_FIRST_USER + 593;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_PASSWORD = Activity.RESULT_FIRST_USER + 594;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_PASSWORD = Activity.RESULT_FIRST_USER + 595;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_PASSWORD = Activity.RESULT_FIRST_USER + 596;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_PASSWORD = Activity.RESULT_FIRST_USER + 597;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_PASSWORD = Activity.RESULT_FIRST_USER + 598;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_PASSWORD
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_PASSWORD = Activity.RESULT_FIRST_USER + 599;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_HTML = Activity.RESULT_FIRST_USER + 600;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_HTML = Activity.RESULT_FIRST_USER + 601;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 602;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 603;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 604;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 605;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 606;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 607;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 608;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 609;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 610;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_HTML = Activity.RESULT_FIRST_USER + 611;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_HTML = Activity.RESULT_FIRST_USER + 612;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_HTML = Activity.RESULT_FIRST_USER + 613;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_HTML = Activity.RESULT_FIRST_USER + 614;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_HTML = Activity.RESULT_FIRST_USER + 615;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_HTML = Activity.RESULT_FIRST_USER + 616;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_HTML = Activity.RESULT_FIRST_USER + 617;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_HTML = Activity.RESULT_FIRST_USER + 618;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_HTML
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_HTML = Activity.RESULT_FIRST_USER + 619;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 620;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 621;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 622;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 623;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 624;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 625;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 626;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 627;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 628;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 629;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 630;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 631;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 632;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 633;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 634;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 635;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 636;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 637;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 638;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_MULTI_LINE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_MULTI_LINE = Activity.RESULT_FIRST_USER + 639;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 640;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 641;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 642;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 643;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 644;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 645;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 646;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 647;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 648;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 649;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 650;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 651;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 652;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 653;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 654;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 655;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 656;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 657;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 658;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_READ_ONLY
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_READ_ONLY = Activity.RESULT_FIRST_USER + 659;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_HIDDEN = Activity.RESULT_FIRST_USER + 660;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_HIDDEN = Activity.RESULT_FIRST_USER + 661;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 662;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 663;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 664;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 665;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 666;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 667;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 668;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 669;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 670;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 671;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_HIDDEN = Activity.RESULT_FIRST_USER + 672;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_HIDDEN = Activity.RESULT_FIRST_USER + 673;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_HIDDEN = Activity.RESULT_FIRST_USER + 674;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_HIDDEN = Activity.RESULT_FIRST_USER + 675;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_HIDDEN = Activity.RESULT_FIRST_USER + 676;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_HIDDEN = Activity.RESULT_FIRST_USER + 677;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_HIDDEN = Activity.RESULT_FIRST_USER + 678;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_HIDDEN
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_HIDDEN = Activity.RESULT_FIRST_USER + 679;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_ADVANCED = Activity.RESULT_FIRST_USER + 680;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_ADVANCED = Activity.RESULT_FIRST_USER + 681;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 682;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 683;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 684;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 685;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 686;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 687;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 688;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 689;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 690;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 691;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_ADVANCED = Activity.RESULT_FIRST_USER + 692;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_ADVANCED = Activity.RESULT_FIRST_USER + 693;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_ADVANCED = Activity.RESULT_FIRST_USER + 694;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_ADVANCED = Activity.RESULT_FIRST_USER + 695;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_ADVANCED = Activity.RESULT_FIRST_USER + 696;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_ADVANCED = Activity.RESULT_FIRST_USER + 697;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_ADVANCED = Activity.RESULT_FIRST_USER + 698;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_ADVANCED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_ADVANCED = Activity.RESULT_FIRST_USER + 699;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 700;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 701;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 702;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 703;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 704;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 705;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 706;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 707;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 708;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 709;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 710;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 711;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 712;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 713;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 714;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 715;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 716;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 717;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 718;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_IMPORTANT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_IMPORTANT = Activity.RESULT_FIRST_USER + 719;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 720;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 721;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 722;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 723;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 724;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 725;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 726;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 727;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 728;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 729;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 730;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 731;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 732;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 733;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 734;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 735;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 736;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 737;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 738;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_ESSENTIAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_ESSENTIAL = Activity.RESULT_FIRST_USER + 739;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 740;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 741;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 742;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 743;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 744;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 745;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 746;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 747;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 748;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 749;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 750;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 751;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 752;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 753;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 754;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 755;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 756;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 757;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 758;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_DEPRECATED
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_DEPRECATED = Activity.RESULT_FIRST_USER + 759;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 760;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 761;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 762;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 763;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 764;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 765;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 766;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 767;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 768;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 769;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 770;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 771;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 772;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 773;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 774;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 775;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 776;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 777;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 778;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_EXPERIMENTAL
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_EXPERIMENTAL = Activity.RESULT_FIRST_USER + 779;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 780;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 781;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 782;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 783;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 784;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 785;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 786;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 787;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 788;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 789;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 790;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 791;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 792;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 793;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 794;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 795;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 796;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 797;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 798;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_TASKER_SETTABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_TASKER_SETTABLE = Activity.RESULT_FIRST_USER + 799;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 800;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 801;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 802;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 803;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 804;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 805;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 806;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 807;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 808;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 809;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 810;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 811;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 812;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 813;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 814;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 815;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 816;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 817;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 818;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_TASKER_READABLE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_TASKER_READABLE = Activity.RESULT_FIRST_USER + 819;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 820;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 821;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 822;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 823;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 824;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 825;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 826;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 827;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 828;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 829;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 830;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 831;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 832;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 833;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 834;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 835;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 836;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 837;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 838;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_TASKER_EVENT
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_TASKER_EVENT = Activity.RESULT_FIRST_USER + 839;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 840;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 841;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 842;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 843;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 844;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 845;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 846;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 847;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 848;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 849;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 850;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 851;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 852;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 853;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 854;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 855;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 856;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 857;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 858;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_TASKER_CONDITION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_TASKER_CONDITION = Activity.RESULT_FIRST_USER + 859;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 860;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 861;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 862;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 863;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 864;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 865;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 866;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 867;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 868;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 869;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 870;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 871;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 872;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 873;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 874;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 875;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 876;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 877;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 878;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_TASKER_ACTION
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_TASKER_ACTION = Activity.RESULT_FIRST_USER + 879;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 880;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 881;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 882;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 883;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 884;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 885;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 886;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 887;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_TILE_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 888;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VOICE_INTERACTION_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 889;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CHOOSER_TARGET_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 890;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_CONDITION_PROVIDER_SERVICE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 891;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_QUICK_SETTINGS_TILE_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 892;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_APP_WIDGET_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_APP_WIDGET_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 893;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_LIVE_WALLPAPER_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_LIVE_WALLPAPER_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 894;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_KEYBOARD_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_KEYBOARD_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 895;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_NOTIFICATION_LISTENER_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 896;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 897;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 898;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_IS_TASKER_SETTING
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_IS_TASKER_SETTING = Activity.RESULT_FIRST_USER + 899;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 900;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 901;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_ACCESSIBILITY_SERVICE_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 902;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_DREAM_SERVICE_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_DREAM_SERVICE_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 903;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_PRINT_SERVICE_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_PRINT_SERVICE_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 904;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_VPN_SERVICE_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_VPN_SERVICE_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 905;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_WALLPAPER_SERVICE_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_WALLPAPER_SERVICE_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 906;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_PROFILE
    public final static int RESULT_ASYNC_QUERY_NOT_IN_INPUT_METHOD_SERVICE_IS_TASKER_PROFILE = Activity.RESULT_FIRST_USER + 907;


    // For ACTION_QUERY_PLUGIN_ASYNC, this is the result code if condition is NOT_IN_TILE_SERVICE_IS_TASKER_PROFILE
    public final static int RESULT
