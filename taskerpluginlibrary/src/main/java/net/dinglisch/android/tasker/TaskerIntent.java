package net.dinglisch.android.tasker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// change this if you update the protocol
// 18.05.13: allow result_code to be RESULT_OK_NO_SHOW for edit activity (Tasker then won't show result toast)
// 28.06.13: add EXTRA_PLUGIN_COMPLETION_INTENT, EXTRA_PLUGIN_COMPLETION_BUNDLE for plugin to specify intent to send when it's finished
// 28.06.13: add EXTRA_PLUGIN_INSTALLED_NAMES,VARNAMES,CODES for host to send to plugin with ACTION_QUERY_PLUGIN_ASYNC
// 01.07.13: add EXTRA_ASYNC_REQUEST_ID for host to send to plugin with ACTION_QUERY_PLUGIN_ASYNC
// 01.07.13: add EXTRA_ASYNC_QUERY_RESULT for plugin to send to host with ACTION_PLUGIN_QUERY_COMPLETED
// 10.09.13: add variable %task_name
// 10.09.13: add variable %qtime
// 10.09.13: add EXTRA_REQUESTED_TIMEOUT for host to request a minimum timeout from plugin
// 10.09.13: add variable %plugintimeout for plugin to be able to check requested timeout
// 20.09.13: add variable %caller for plugin to be able to check calling Tasker task/profile
// 20.09.13: add variable %trun for plugin to be able to check current task run ID
// 20.09.13: add variable %priority for plugin to be able to check current task priority
// 20.09.13: add variable %launch_type for plugin to be able to check how it was launched (config/action/event)
// 20.09.13: add EXTRA_HOST_PACKAGE to ACTION_EDIT_PLUGIN for plugin to know which host is calling
// 20.09.13: add variable %default for plugin to use to set default value from Tasker variable
// 20.09.13: add variable %setting_name for plugin to know its own name
// 20.09.13: add variable %setting_label for plugin to know its own label
// 20.09.13: add variable %setting_icon for plugin to know its own icon resource name
// 20.09.13: add variable %setting_type for plugin to know its own type (action/condition/event)
// 20.09.13: add variable %setting_package for plugin to know its own package name
// 20.09.13: add variable %setting_class for plugin to know its own class name
// 20.09.13: add variable %setting_blurb for plugin to know its own blurb
// 20.09.13: add variable %setting_help_uri for plugin to know its own help URI
// 20.09.13: add variable %setting_relevant_vars for plugin to know its own relevant vars
// 20.09.13: add variable %setting_pass_through_data for plugin to know its own pass-through data
// 20.09.13: add variable %setting_version_code for plugin to know its own version code
// 20.09.13: add variable %setting_min_tasker_version for plugin to know its own min Tasker version
// 20.09.13: add variable %setting_target_tasker_version for plugin to know its own target Tasker version
// 20.09.13: add variable %setting_config_delegate_class for plugin to know its own config delegate class
// 20.09.13: add variable %setting_config_delegate_package for plugin to know its own config delegate package
// 20.09.13: add variable %setting_config_delegate_label for plugin to know its own config delegate label
// 20.09.13: add variable %setting_config_delegate_icon for plugin to know its own config delegate icon
// 20.09.13: add variable %setting_config_delegate_blurb for plugin to know its own config delegate blurb
// 20.09.13: add variable %setting_config_delegate_help_uri for plugin to know its own config delegate help URI

// 20.09.13: add EXTRA_VARIABLES to ACTION_FIRE_SETTING for plugin to be able to set variables directly
// 20.09.13: add EXTRA_REPLACE_VARIABLES to ACTION_FIRE_SETTING for plugin to specify if variables should be replaced
// 20.09.13: add EXTRA_ERROR_MESSAGE to ACTION_FIRE_SETTING for plugin to specify error message if it fails
// 20.09.13: add EXTRA_ERROR_CODE to ACTION_FIRE_SETTING for plugin to specify error code if it fails
// 20.09.13: add EXTRA_ACTION_ID to ACTION_FIRE_SETTING for plugin to specify action ID if it's part of a multi-action plugin

// 20.09.13: add EXTRA_HOST_BACKBUTTON_IS_CANCEL to ACTION_EDIT_PLUGIN for plugin to know if host back button is cancel
// 20.09.13: add EXTRA_HOST_BACKBUTTON_IS_SAVE to ACTION_EDIT_PLUGIN for plugin to know if host back button is save

// 20.09.13: add ACTION_QUERY_PLUGIN_ASYNC for host to query plugin asynchronously
// 20.09.13: add ACTION_PLUGIN_QUERY_COMPLETED for plugin to send query result to host

// 20.09.13: add EXTRA_REQUEST_TYPE for plugin to know if it's being called for action, condition or event

// 20.09.13: add EXTRA_REQUESTED_PERMISSIONS for plugin to specify permissions it needs

// 20.09.13: add EXTRA_PLUGIN_ICON_RESOURCE for plugin to specify icon resource name
// 20.09.13: add EXTRA_PLUGIN_LABEL for plugin to specify label
// 20.09.13: add EXTRA_PLUGIN_BLURB for plugin to specify blurb
// 20.09.13: add EXTRA_PLUGIN_HELP_URI for plugin to specify help URI
// 20.09.13: add EXTRA_PLUGIN_RELEVANT_VARIABLES for plugin to specify relevant variables
// 20.09.13: add EXTRA_PLUGIN_PASS_THROUGH_DATA for plugin to specify pass-through data
// 20.09.13: add EXTRA_PLUGIN_VERSION_CODE for plugin to specify version code
// 20.09.13: add EXTRA_PLUGIN_MIN_TASKER_VERSION for plugin to specify min Tasker version
// 20.09.13: add EXTRA_PLUGIN_TARGET_TASKER_VERSION for plugin to specify target Tasker version
// 20.09.13: add EXTRA_PLUGIN_CONFIG_DELEGATE_CLASS for plugin to specify config delegate class
// 20.09.13: add EXTRA_PLUGIN_CONFIG_DELEGATE_PACKAGE for plugin to specify config delegate package
// 20.09.13: add EXTRA_PLUGIN_CONFIG_DELEGATE_LABEL for plugin to specify config delegate label
// 20.09.13: add EXTRA_PLUGIN_CONFIG_DELEGATE_ICON for plugin to specify config delegate icon
// 20.09.13: add EXTRA_PLUGIN_CONFIG_DELEGATE_BLURB for plugin to specify config delegate blurb
// 20.09.13: add EXTRA_PLUGIN_CONFIG_DELEGATE_HELP_URI for plugin to specify config delegate help URI

// 04.10.13: add EXTRA_PRIVATE_DATA for plugin to store private data in Tasker
// 04.10.13: add EXTRA_CONFIG_ID for plugin to identify config instance

// 04.10.13: add EXTRA_PLUGIN_TIMEOUT_OPTIONS for plugin to specify timeout options
// 04.10.13: add EXTRA_PLUGIN_TIMEOUT_DEFAULT for plugin to specify default timeout

// 04.10.13: add EXTRA_PLUGIN_NOTIFY_ON_TIMEOUT for plugin to specify if it should be notified on timeout

// 04.10.13: add EXTRA_PLUGIN_RELEVANT_VARIABLES_ARE_REGEX for plugin to specify if relevant variables are regex

// 04.10.13: add EXTRA_PLUGIN_RELEVANT_VARIABLES_STRUCTS for plugin to specify relevant variables structs

// 20.10.13: add EXTRA_VARIABLES_PREFIX to allow plugin to specify prefix for variables it sets

// 20.10.13: add EXTRA_RESULT_URI to ACTION_FIRE_SETTING for plugin to specify result URI for Tasker to query

// 20.10.13: add EXTRA_VARIABLE_NAMES_TO_FILL for plugin to specify variable names to fill from Tasker

// 20.10.13: add EXTRA_VARIABLE_VALUES_TO_FILL for plugin to specify variable values to fill from Tasker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_NAMES for plugin to specify variable names it wants from Tasker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_VALUES for plugin to specify variable values it wants from Tasker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_TYPES for plugin to specify variable types it wants from Tasker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_DESCRIPTIONS for plugin to specify variable descriptions it wants from Tasker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_HTML_NOTES for plugin to specify variable HTML notes it wants from Tasker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IGNORE_IN_STRING_BLURB for plugin to specify if variable should be ignored in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_OUTPUT_VAR_INFO for plugin to specify output variable info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_RENAMES for plugin to specify variable renames

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_NAMER_HELPER for plugin to specify variable namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_PREFIX for plugin to specify variable prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_OUTPUT_ONLY for plugin to specify if variable is output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_INPUT_ONLY for plugin to specify if variable is input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_CONFIG_ONLY for plugin to specify if variable is config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_ACTION_ONLY for plugin to specify if variable is action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_CONDITION_ONLY for plugin to specify if variable is condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_EVENT_ONLY for plugin to specify if variable is event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_DEFAULT_VALUE for plugin to specify variable default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_MIN_VALUE for plugin to specify variable min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_MAX_VALUE for plugin to specify variable max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_ALLOWED_VALUES for plugin to specify variable allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_REGEX_VALIDATION for plugin to specify variable regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_PASSWORD for plugin to specify if variable is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_HTML for plugin to specify if variable is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_MULTI_LINE for plugin to specify if variable is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_READ_ONLY for plugin to specify if variable is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_HIDDEN for plugin to specify if variable is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_ADVANCED for plugin to specify if variable is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_IMPORTANT for plugin to specify if variable is important

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_ESSENTIAL for plugin to specify if variable is essential

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_DEPRECATED for plugin to specify if variable is deprecated

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_EXPERIMENTAL for plugin to specify if variable is experimental

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_SETTABLE for plugin to specify if variable is Tasker settable

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_READABLE for plugin to specify if variable is Tasker readable

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_EVENT for plugin to specify if variable is Tasker event

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION for plugin to specify if variable is Tasker condition

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACTION for plugin to specify if variable is Tasker action

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_SETTING for plugin to specify if variable is Tasker setting

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PROFILE for plugin to specify if variable is Tasker profile

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TASK for plugin to specify if variable is Tasker task

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_SCENE for plugin to specify if variable is Tasker scene

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PROJECT for plugin to specify if variable is Tasker project

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_CONTEXT for plugin to specify if variable is Tasker app context

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_GLOBAL_VAR for plugin to specify if variable is Tasker global var

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LOCAL_VAR for plugin to specify if variable is Tasker local var

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ARRAY for plugin to specify if variable is Tasker array

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_JSON for plugin to specify if variable is Tasker JSON

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_XML for plugin to specify if variable is Tasker XML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_HTML for plugin to specify if variable is Tasker HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_MARKDOWN for plugin to specify if variable is Tasker markdown

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_BBCODE for plugin to specify if variable is Tasker BBCode

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TEXT for plugin to specify if variable is Tasker text

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NUMBER for plugin to specify if variable is Tasker number

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_BOOLEAN for plugin to specify if variable is Tasker boolean

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DATETIME for plugin to specify if variable is Tasker datetime

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TIMESPAN for plugin to specify if variable is Tasker timespan

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_COLOR for plugin to specify if variable is Tasker color

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ICON for plugin to specify if variable is Tasker icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_FILE for plugin to specify if variable is Tasker file

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_URI for plugin to specify if variable is Tasker URI

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PACKAGE for plugin to specify if variable is Tasker package

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CLASS for plugin to specify if variable is Tasker class

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACTIVITY for plugin to specify if variable is Tasker activity

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_SERVICE for plugin to specify if variable is Tasker service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_RECEIVER for plugin to specify if variable is Tasker receiver

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PROVIDER for plugin to specify if variable is Tasker provider

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_SHORTCUT for plugin to specify if variable is Tasker shortcut

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WIDGET for plugin to specify if variable is Tasker widget

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER for plugin to specify if variable is Tasker wallpaper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYGUARD for plugin to specify if variable is Tasker keyguard

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD for plugin to specify if variable is Tasker input method

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE for plugin to specify if variable is Tasker accessibility service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE for plugin to specify if variable is Tasker dream service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE for plugin to specify if variable is Tasker print service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE for plugin to specify if variable is Tasker VPN service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE for plugin to specify if variable is Tasker wallpaper service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE for plugin to specify if variable is Tasker input method service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_CONFIG for plugin to specify if variable is Tasker accessibility service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_CONFIG for plugin to specify if variable is Tasker dream service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_CONFIG for plugin to specify if variable is Tasker print service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_CONFIG for plugin to specify if variable is Tasker VPN service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_CONFIG for plugin to specify if variable is Tasker wallpaper service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_CONFIG for plugin to specify if variable is Tasker input method service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE for plugin to specify if variable is Tasker tile service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_CONFIG for plugin to specify if variable is Tasker tile service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE for plugin to specify if variable is Tasker voice interaction service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_CONFIG for plugin to specify if variable is Tasker voice interaction service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE for plugin to specify if variable is Tasker chooser target service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_CONFIG for plugin to specify if variable is Tasker chooser target service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE for plugin to specify if variable is Tasker condition provider service

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_CONFIG for plugin to specify if variable is Tasker condition provider service config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE for plugin to specify if variable is Tasker quick settings tile

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_CONFIG for plugin to specify if variable is Tasker quick settings tile config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET for plugin to specify if variable is Tasker app widget

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_CONFIG for plugin to specify if variable is Tasker app widget config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER for plugin to specify if variable is Tasker live wallpaper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_CONFIG for plugin to specify if variable is Tasker live wallpaper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD for plugin to specify if variable is Tasker keyboard

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_CONFIG for plugin to specify if variable is Tasker keyboard config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER for plugin to specify if variable is Tasker notification listener

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_CONFIG for plugin to specify if variable is Tasker notification listener config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM for plugin to specify if variable is Tasker dream

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_CONFIG for plugin to specify if variable is Tasker dream config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT for plugin to specify if variable is Tasker print

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_CONFIG for plugin to specify if variable is Tasker print config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN for plugin to specify if variable is Tasker VPN

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_CONFIG for plugin to specify if variable is Tasker VPN config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_PICKER for plugin to specify if variable is Tasker wallpaper picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_PICKER_CONFIG for plugin to specify if variable is Tasker wallpaper picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_PICKER for plugin to specify if variable is Tasker input method picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_PICKER_CONFIG for plugin to specify if variable is Tasker input method picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_PICKER for plugin to specify if variable is Tasker accessibility service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker accessibility service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_PICKER for plugin to specify if variable is Tasker dream service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker dream service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_PICKER for plugin to specify if variable is Tasker print service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker print service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_PICKER for plugin to specify if variable is Tasker VPN service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker VPN service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_PICKER for plugin to specify if variable is Tasker wallpaper service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker wallpaper service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_PICKER for plugin to specify if variable is Tasker input method service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker input method service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_PICKER for plugin to specify if variable is Tasker tile service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker tile service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_PICKER for plugin to specify if variable is Tasker voice interaction service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker voice interaction service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_PICKER for plugin to specify if variable is Tasker chooser target service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker chooser target service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_PICKER for plugin to specify if variable is Tasker condition provider service picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_PICKER_CONFIG for plugin to specify if variable is Tasker condition provider service picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_PICKER for plugin to specify if variable is Tasker quick settings tile picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_PICKER_CONFIG for plugin to specify if variable is Tasker quick settings tile picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_PICKER for plugin to specify if variable is Tasker app widget picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_PICKER_CONFIG for plugin to specify if variable is Tasker app widget picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_PICKER for plugin to specify if variable is Tasker live wallpaper picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_PICKER_CONFIG for plugin to specify if variable is Tasker live wallpaper picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_PICKER for plugin to specify if variable is Tasker keyboard picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_PICKER_CONFIG for plugin to specify if variable is Tasker keyboard picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_PICKER for plugin to specify if variable is Tasker notification listener picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_PICKER_CONFIG for plugin to specify if variable is Tasker notification listener picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_PICKER for plugin to specify if variable is Tasker dream picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_PICKER_CONFIG for plugin to specify if variable is Tasker dream picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_PICKER for plugin to specify if variable is Tasker print picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_PICKER_CONFIG for plugin to specify if variable is Tasker print picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_PICKER for plugin to specify if variable is Tasker VPN picker

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_PICKER_CONFIG for plugin to specify if variable is Tasker VPN picker config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SETTINGS for plugin to specify if variable is Tasker wallpaper settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SETTINGS_CONFIG for plugin to specify if variable is Tasker wallpaper settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SETTINGS for plugin to specify if variable is Tasker input method settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SETTINGS_CONFIG for plugin to specify if variable is Tasker input method settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_SETTINGS for plugin to specify if variable is Tasker accessibility service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker accessibility service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_SETTINGS for plugin to specify if variable is Tasker dream service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker dream service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_SETTINGS for plugin to specify if variable is Tasker print service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker print service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_SETTINGS for plugin to specify if variable is Tasker VPN service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker VPN service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_SETTINGS for plugin to specify if variable is Tasker wallpaper service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker wallpaper service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_SETTINGS for plugin to specify if variable is Tasker input method service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker input method service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_SETTINGS for plugin to specify if variable is Tasker tile service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker tile service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_SETTINGS for plugin to specify if variable is Tasker voice interaction service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker voice interaction service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_SETTINGS for plugin to specify if variable is Tasker chooser target service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker chooser target service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_SETTINGS for plugin to specify if variable is Tasker condition provider service settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_SETTINGS_CONFIG for plugin to specify if variable is Tasker condition provider service settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_SETTINGS for plugin to specify if variable is Tasker quick settings tile settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_SETTINGS_CONFIG for plugin to specify if variable is Tasker quick settings tile settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_SETTINGS for plugin to specify if variable is Tasker app widget settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_SETTINGS_CONFIG for plugin to specify if variable is Tasker app widget settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_SETTINGS for plugin to specify if variable is Tasker live wallpaper settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_SETTINGS_CONFIG for plugin to specify if variable is Tasker live wallpaper settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_SETTINGS for plugin to specify if variable is Tasker keyboard settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_SETTINGS_CONFIG for plugin to specify if variable is Tasker keyboard settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_SETTINGS for plugin to specify if variable is Tasker notification listener settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_SETTINGS_CONFIG for plugin to specify if variable is Tasker notification listener settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SETTINGS for plugin to specify if variable is Tasker dream settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SETTINGS_CONFIG for plugin to specify if variable is Tasker dream settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SETTINGS for plugin to specify if variable is Tasker print settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SETTINGS_CONFIG for plugin to specify if variable is Tasker print settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SETTINGS for plugin to specify if variable is Tasker VPN settings

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SETTINGS_CONFIG for plugin to specify if variable is Tasker VPN settings config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_INFO for plugin to specify if variable is Tasker wallpaper info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_INFO_CONFIG for plugin to specify if variable is Tasker wallpaper info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_INFO for plugin to specify if variable is Tasker input method info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_INFO_CONFIG for plugin to specify if variable is Tasker input method info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_INFO for plugin to specify if variable is Tasker accessibility service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker accessibility service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_INFO for plugin to specify if variable is Tasker dream service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker dream service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_INFO for plugin to specify if variable is Tasker print service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker print service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_INFO for plugin to specify if variable is Tasker VPN service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker VPN service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_INFO for plugin to specify if variable is Tasker wallpaper service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker wallpaper service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_INFO for plugin to specify if variable is Tasker input method service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker input method service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_INFO for plugin to specify if variable is Tasker tile service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker tile service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_INFO for plugin to specify if variable is Tasker voice interaction service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker voice interaction service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_INFO for plugin to specify if variable is Tasker chooser target service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker chooser target service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_INFO for plugin to specify if variable is Tasker condition provider service info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_INFO_CONFIG for plugin to specify if variable is Tasker condition provider service info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_INFO for plugin to specify if variable is Tasker quick settings tile info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_INFO_CONFIG for plugin to specify if variable is Tasker quick settings tile info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_INFO for plugin to specify if variable is Tasker app widget info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_INFO_CONFIG for plugin to specify if variable is Tasker app widget info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_INFO for plugin to specify if variable is Tasker live wallpaper info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_INFO_CONFIG for plugin to specify if variable is Tasker live wallpaper info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_INFO for plugin to specify if variable is Tasker keyboard info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_INFO_CONFIG for plugin to specify if variable is Tasker keyboard info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_INFO for plugin to specify if variable is Tasker notification listener info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_INFO_CONFIG for plugin to specify if variable is Tasker notification listener info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_INFO for plugin to specify if variable is Tasker dream info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_INFO_CONFIG for plugin to specify if variable is Tasker dream info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_INFO for plugin to specify if variable is Tasker print info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_INFO_CONFIG for plugin to specify if variable is Tasker print info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_INFO for plugin to specify if variable is Tasker VPN info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_INFO_CONFIG for plugin to specify if variable is Tasker VPN info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_PREVIEW for plugin to specify if variable is Tasker wallpaper preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_PREVIEW_CONFIG for plugin to specify if variable is Tasker wallpaper preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_PREVIEW for plugin to specify if variable is Tasker input method preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_PREVIEW_CONFIG for plugin to specify if variable is Tasker input method preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_PREVIEW for plugin to specify if variable is Tasker accessibility service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker accessibility service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_PREVIEW for plugin to specify if variable is Tasker dream service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker dream service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_PREVIEW for plugin to specify if variable is Tasker print service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker print service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_PREVIEW for plugin to specify if variable is Tasker VPN service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker VPN service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_PREVIEW for plugin to specify if variable is Tasker wallpaper service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker wallpaper service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_PREVIEW for plugin to specify if variable is Tasker input method service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker input method service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_PREVIEW for plugin to specify if variable is Tasker tile service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker tile service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_PREVIEW for plugin to specify if variable is Tasker voice interaction service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker voice interaction service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_PREVIEW for plugin to specify if variable is Tasker chooser target service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker chooser target service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_PREVIEW for plugin to specify if variable is Tasker condition provider service preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_PREVIEW_CONFIG for plugin to specify if variable is Tasker condition provider service preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_PREVIEW for plugin to specify if variable is Tasker quick settings tile preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_PREVIEW_CONFIG for plugin to specify if variable is Tasker quick settings tile preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_PREVIEW for plugin to specify if variable is Tasker app widget preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_PREVIEW_CONFIG for plugin to specify if variable is Tasker app widget preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_PREVIEW for plugin to specify if variable is Tasker live wallpaper preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_PREVIEW_CONFIG for plugin to specify if variable is Tasker live wallpaper preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_PREVIEW for plugin to specify if variable is Tasker keyboard preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_PREVIEW_CONFIG for plugin to specify if variable is Tasker keyboard preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_PREVIEW for plugin to specify if variable is Tasker notification listener preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_PREVIEW_CONFIG for plugin to specify if variable is Tasker notification listener preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_PREVIEW for plugin to specify if variable is Tasker dream preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_PREVIEW_CONFIG for plugin to specify if variable is Tasker dream preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_PREVIEW for plugin to specify if variable is Tasker print preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_PREVIEW_CONFIG for plugin to specify if variable is Tasker print preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_PREVIEW for plugin to specify if variable is Tasker VPN preview

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_PREVIEW_CONFIG for plugin to specify if variable is Tasker VPN preview config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_THUMBNAIL for plugin to specify if variable is Tasker wallpaper thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker wallpaper thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_THUMBNAIL for plugin to specify if variable is Tasker input method thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker input method thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker accessibility service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker accessibility service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker dream service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker dream service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker print service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker print service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker VPN service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker VPN service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker wallpaper service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker wallpaper service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker input method service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker input method service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker tile service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker tile service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker voice interaction service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker voice interaction service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker chooser target service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker chooser target service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_THUMBNAIL for plugin to specify if variable is Tasker condition provider service thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker condition provider service thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_THUMBNAIL for plugin to specify if variable is Tasker quick settings tile thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker quick settings tile thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_THUMBNAIL for plugin to specify if variable is Tasker app widget thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker app widget thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_THUMBNAIL for plugin to specify if variable is Tasker live wallpaper thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker live wallpaper thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_THUMBNAIL for plugin to specify if variable is Tasker keyboard thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker keyboard thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_THUMBNAIL for plugin to specify if variable is Tasker notification listener thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker notification listener thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_THUMBNAIL for plugin to specify if variable is Tasker dream thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker dream thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_THUMBNAIL for plugin to specify if variable is Tasker print thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker print thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_THUMBNAIL for plugin to specify if variable is Tasker VPN thumbnail

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_THUMBNAIL_CONFIG for plugin to specify if variable is Tasker VPN thumbnail config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_ICON for plugin to specify if variable is Tasker wallpaper icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_ICON_CONFIG for plugin to specify if variable is Tasker wallpaper icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_ICON for plugin to specify if variable is Tasker input method icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_ICON_CONFIG for plugin to specify if variable is Tasker input method icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_ICON for plugin to specify if variable is Tasker accessibility service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker accessibility service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_ICON for plugin to specify if variable is Tasker dream service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker dream service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_ICON for plugin to specify if variable is Tasker print service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker print service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_ICON for plugin to specify if variable is Tasker VPN service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker VPN service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_ICON for plugin to specify if variable is Tasker wallpaper service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker wallpaper service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_ICON for plugin to specify if variable is Tasker input method service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker input method service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_ICON for plugin to specify if variable is Tasker tile service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker tile service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_ICON for plugin to specify if variable is Tasker voice interaction service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker voice interaction service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_ICON for plugin to specify if variable is Tasker chooser target service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker chooser target service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_ICON for plugin to specify if variable is Tasker condition provider service icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_ICON_CONFIG for plugin to specify if variable is Tasker condition provider service icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_ICON for plugin to specify if variable is Tasker quick settings tile icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_ICON_CONFIG for plugin to specify if variable is Tasker quick settings tile icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_ICON for plugin to specify if variable is Tasker app widget icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_ICON_CONFIG for plugin to specify if variable is Tasker app widget icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_ICON for plugin to specify if variable is Tasker live wallpaper icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_ICON_CONFIG for plugin to specify if variable is Tasker live wallpaper icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_ICON for plugin to specify if variable is Tasker keyboard icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_ICON_CONFIG for plugin to specify if variable is Tasker keyboard icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_ICON for plugin to specify if variable is Tasker notification listener icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_ICON_CONFIG for plugin to specify if variable is Tasker notification listener icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_ICON for plugin to specify if variable is Tasker dream icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_ICON_CONFIG for plugin to specify if variable is Tasker dream icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_ICON for plugin to specify if variable is Tasker print icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_ICON_CONFIG for plugin to specify if variable is Tasker print icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_ICON for plugin to specify if variable is Tasker VPN icon

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_ICON_CONFIG for plugin to specify if variable is Tasker VPN icon config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_LABEL for plugin to specify if variable is Tasker wallpaper label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_LABEL_CONFIG for plugin to specify if variable is Tasker wallpaper label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_LABEL for plugin to specify if variable is Tasker input method label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_LABEL_CONFIG for plugin to specify if variable is Tasker input method label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_LABEL for plugin to specify if variable is Tasker accessibility service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker accessibility service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_LABEL for plugin to specify if variable is Tasker dream service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker dream service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_LABEL for plugin to specify if variable is Tasker print service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker print service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_LABEL for plugin to specify if variable is Tasker VPN service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker VPN service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_LABEL for plugin to specify if variable is Tasker wallpaper service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker wallpaper service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_LABEL for plugin to specify if variable is Tasker input method service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker input method service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_LABEL for plugin to specify if variable is Tasker tile service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker tile service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_LABEL for plugin to specify if variable is Tasker voice interaction service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker voice interaction service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_LABEL for plugin to specify if variable is Tasker chooser target service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker chooser target service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_LABEL for plugin to specify if variable is Tasker condition provider service label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_LABEL_CONFIG for plugin to specify if variable is Tasker condition provider service label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_LABEL for plugin to specify if variable is Tasker quick settings tile label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_LABEL_CONFIG for plugin to specify if variable is Tasker quick settings tile label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_LABEL for plugin to specify if variable is Tasker app widget label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_LABEL_CONFIG for plugin to specify if variable is Tasker app widget label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_LABEL for plugin to specify if variable is Tasker live wallpaper label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_LABEL_CONFIG for plugin to specify if variable is Tasker live wallpaper label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_LABEL for plugin to specify if variable is Tasker keyboard label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_LABEL_CONFIG for plugin to specify if variable is Tasker keyboard label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_LABEL for plugin to specify if variable is Tasker notification listener label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_LABEL_CONFIG for plugin to specify if variable is Tasker notification listener label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_LABEL for plugin to specify if variable is Tasker dream label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_LABEL_CONFIG for plugin to specify if variable is Tasker dream label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_LABEL for plugin to specify if variable is Tasker print label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_LABEL_CONFIG for plugin to specify if variable is Tasker print label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_LABEL for plugin to specify if variable is Tasker VPN label

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_LABEL_CONFIG for plugin to specify if variable is Tasker VPN label config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_DESCRIPTION for plugin to specify if variable is Tasker wallpaper description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker wallpaper description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_DESCRIPTION for plugin to specify if variable is Tasker input method description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker input method description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker accessibility service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker accessibility service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker dream service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker dream service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker print service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker print service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker VPN service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker VPN service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker wallpaper service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker wallpaper service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker input method service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker input method service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker tile service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker tile service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker voice interaction service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker voice interaction service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker chooser target service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker chooser target service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_DESCRIPTION for plugin to specify if variable is Tasker condition provider service description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker condition provider service description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_DESCRIPTION for plugin to specify if variable is Tasker quick settings tile description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker quick settings tile description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_DESCRIPTION for plugin to specify if variable is Tasker app widget description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker app widget description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_DESCRIPTION for plugin to specify if variable is Tasker live wallpaper description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker live wallpaper description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_DESCRIPTION for plugin to specify if variable is Tasker keyboard description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker keyboard description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_DESCRIPTION for plugin to specify if variable is Tasker notification listener description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker notification listener description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_DESCRIPTION for plugin to specify if variable is Tasker dream description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker dream description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_DESCRIPTION for plugin to specify if variable is Tasker print description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker print description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_DESCRIPTION for plugin to specify if variable is Tasker VPN description

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_DESCRIPTION_CONFIG for plugin to specify if variable is Tasker VPN description config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_HTML_NOTE for plugin to specify if variable is Tasker wallpaper HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker wallpaper HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_HTML_NOTE for plugin to specify if variable is Tasker input method HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker input method HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker accessibility service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker accessibility service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker dream service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker dream service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker print service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker print service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker VPN service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker VPN service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker wallpaper service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker wallpaper service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker input method service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker input method service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker tile service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker tile service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker voice interaction service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker voice interaction service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker chooser target service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker chooser target service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_HTML_NOTE for plugin to specify if variable is Tasker condition provider service HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker condition provider service HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_HTML_NOTE for plugin to specify if variable is Tasker quick settings tile HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker quick settings tile HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_HTML_NOTE for plugin to specify if variable is Tasker app widget HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker app widget HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_HTML_NOTE for plugin to specify if variable is Tasker live wallpaper HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker live wallpaper HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_HTML_NOTE for plugin to specify if variable is Tasker keyboard HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker keyboard HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_HTML_NOTE for plugin to specify if variable is Tasker notification listener HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker notification listener HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_HTML_NOTE for plugin to specify if variable is Tasker dream HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker dream HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_HTML_NOTE for plugin to specify if variable is Tasker print HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker print HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_HTML_NOTE for plugin to specify if variable is Tasker VPN HTML note

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_HTML_NOTE_CONFIG for plugin to specify if variable is Tasker VPN HTML note config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker wallpaper ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker wallpaper ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker input method ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker input method ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker accessibility service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker accessibility service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker dream service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker dream service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker print service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker print service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker VPN service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker VPN service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker wallpaper service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker wallpaper service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker input method service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker input method service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker tile service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker tile service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker voice interaction service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker voice interaction service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker chooser target service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker chooser target service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker condition provider service ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker condition provider service ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker quick settings tile ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker quick settings tile ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker app widget ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker app widget ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker live wallpaper ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker live wallpaper ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker keyboard ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker keyboard ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker notification listener ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker notification listener ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker dream ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker dream ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker print ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker print ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IGNORE_IN_STRING_BLURB for plugin to specify if variable is Tasker VPN ignore in string blurb

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IGNORE_IN_STRING_BLURB_CONFIG for plugin to specify if variable is Tasker VPN ignore in string blurb config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker wallpaper output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker wallpaper output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker input method output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker input method output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker accessibility service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker accessibility service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker dream service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker dream service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker print service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker print service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker VPN service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker VPN service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker wallpaper service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker wallpaper service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker input method service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker input method service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker tile service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker tile service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker voice interaction service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker voice interaction service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker chooser target service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker chooser target service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker condition provider service output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker condition provider service output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker quick settings tile output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker quick settings tile output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker app widget output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker app widget output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker live wallpaper output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker live wallpaper output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker keyboard output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker keyboard output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker notification listener output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker notification listener output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker dream output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker dream output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker print output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker print output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_OUTPUT_VAR_INFO for plugin to specify if variable is Tasker VPN output var info

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_OUTPUT_VAR_INFO_CONFIG for plugin to specify if variable is Tasker VPN output var info config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_RENAMED_TO for plugin to specify if variable is Tasker wallpaper renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker wallpaper renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_RENAMED_TO for plugin to specify if variable is Tasker input method renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker input method renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker accessibility service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker accessibility service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker dream service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker dream service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker print service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker print service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker VPN service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker VPN service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker wallpaper service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker wallpaper service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker input method service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker input method service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker tile service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker tile service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker voice interaction service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker voice interaction service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker chooser target service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker chooser target service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_RENAMED_TO for plugin to specify if variable is Tasker condition provider service renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker condition provider service renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_RENAMED_TO for plugin to specify if variable is Tasker quick settings tile renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker quick settings tile renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_RENAMED_TO for plugin to specify if variable is Tasker app widget renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker app widget renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_RENAMED_TO for plugin to specify if variable is Tasker live wallpaper renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker live wallpaper renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_RENAMED_TO for plugin to specify if variable is Tasker keyboard renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker keyboard renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_RENAMED_TO for plugin to specify if variable is Tasker notification listener renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker notification listener renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_RENAMED_TO for plugin to specify if variable is Tasker dream renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker dream renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_RENAMED_TO for plugin to specify if variable is Tasker print renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker print renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_RENAMED_TO for plugin to specify if variable is Tasker VPN renamed to

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_RENAMED_TO_CONFIG for plugin to specify if variable is Tasker VPN renamed to config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_NAMER_HELPER for plugin to specify if variable is Tasker wallpaper namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker wallpaper namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_NAMER_HELPER for plugin to specify if variable is Tasker input method namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker input method namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker accessibility service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker accessibility service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker dream service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker dream service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker print service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker print service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker VPN service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker VPN service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker wallpaper service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker wallpaper service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker input method service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker input method service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker tile service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker tile service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker voice interaction service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker voice interaction service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker chooser target service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker chooser target service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_NAMER_HELPER for plugin to specify if variable is Tasker condition provider service namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker condition provider service namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_NAMER_HELPER for plugin to specify if variable is Tasker quick settings tile namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker quick settings tile namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_NAMER_HELPER for plugin to specify if variable is Tasker app widget namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker app widget namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_NAMER_HELPER for plugin to specify if variable is Tasker live wallpaper namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker live wallpaper namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_NAMER_HELPER for plugin to specify if variable is Tasker keyboard namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker keyboard namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_NAMER_HELPER for plugin to specify if variable is Tasker notification listener namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker notification listener namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_NAMER_HELPER for plugin to specify if variable is Tasker dream namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker dream namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_NAMER_HELPER for plugin to specify if variable is Tasker print namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker print namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_NAMER_HELPER for plugin to specify if variable is Tasker VPN namer helper

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_NAMER_HELPER_CONFIG for plugin to specify if variable is Tasker VPN namer helper config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_PREFIX for plugin to specify if variable is Tasker wallpaper prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_PREFIX_CONFIG for plugin to specify if variable is Tasker wallpaper prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_PREFIX for plugin to specify if variable is Tasker input method prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_PREFIX_CONFIG for plugin to specify if variable is Tasker input method prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_PREFIX for plugin to specify if variable is Tasker accessibility service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker accessibility service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_PREFIX for plugin to specify if variable is Tasker dream service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker dream service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_PREFIX for plugin to specify if variable is Tasker print service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker print service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_PREFIX for plugin to specify if variable is Tasker VPN service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker VPN service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_PREFIX for plugin to specify if variable is Tasker wallpaper service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker wallpaper service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_PREFIX for plugin to specify if variable is Tasker input method service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker input method service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_PREFIX for plugin to specify if variable is Tasker tile service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker tile service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_PREFIX for plugin to specify if variable is Tasker voice interaction service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker voice interaction service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_PREFIX for plugin to specify if variable is Tasker chooser target service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker chooser target service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_PREFIX for plugin to specify if variable is Tasker condition provider service prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_PREFIX_CONFIG for plugin to specify if variable is Tasker condition provider service prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_PREFIX for plugin to specify if variable is Tasker quick settings tile prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_PREFIX_CONFIG for plugin to specify if variable is Tasker quick settings tile prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_PREFIX for plugin to specify if variable is Tasker app widget prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_PREFIX_CONFIG for plugin to specify if variable is Tasker app widget prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_PREFIX for plugin to specify if variable is Tasker live wallpaper prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_PREFIX_CONFIG for plugin to specify if variable is Tasker live wallpaper prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_PREFIX for plugin to specify if variable is Tasker keyboard prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_PREFIX_CONFIG for plugin to specify if variable is Tasker keyboard prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_PREFIX for plugin to specify if variable is Tasker notification listener prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_PREFIX_CONFIG for plugin to specify if variable is Tasker notification listener prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_PREFIX for plugin to specify if variable is Tasker dream prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_PREFIX_CONFIG for plugin to specify if variable is Tasker dream prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_PREFIX for plugin to specify if variable is Tasker print prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_PREFIX_CONFIG for plugin to specify if variable is Tasker print prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_PREFIX for plugin to specify if variable is Tasker VPN prefix

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_PREFIX_CONFIG for plugin to specify if variable is Tasker VPN prefix config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_OUTPUT_ONLY for plugin to specify if variable is Tasker wallpaper output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_OUTPUT_ONLY for plugin to specify if variable is Tasker input method output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker input method output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker accessibility service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker accessibility service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker dream service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker dream service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker print service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker print service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker VPN service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker VPN service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker wallpaper service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker input method service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker input method service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker tile service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker tile service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker voice interaction service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker voice interaction service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker chooser target service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker chooser target service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_OUTPUT_ONLY for plugin to specify if variable is Tasker condition provider service output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker condition provider service output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_OUTPUT_ONLY for plugin to specify if variable is Tasker quick settings tile output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker quick settings tile output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_OUTPUT_ONLY for plugin to specify if variable is Tasker app widget output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker app widget output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_OUTPUT_ONLY for plugin to specify if variable is Tasker live wallpaper output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker live wallpaper output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_OUTPUT_ONLY for plugin to specify if variable is Tasker keyboard output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker keyboard output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_OUTPUT_ONLY for plugin to specify if variable is Tasker notification listener output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker notification listener output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_OUTPUT_ONLY for plugin to specify if variable is Tasker dream output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker dream output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_OUTPUT_ONLY for plugin to specify if variable is Tasker print output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker print output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_OUTPUT_ONLY for plugin to specify if variable is Tasker VPN output only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_OUTPUT_ONLY_CONFIG for plugin to specify if variable is Tasker VPN output only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_INPUT_ONLY for plugin to specify if variable is Tasker wallpaper input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_INPUT_ONLY for plugin to specify if variable is Tasker input method input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker input method input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker accessibility service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker accessibility service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker dream service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker dream service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker print service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker print service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker VPN service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker VPN service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker wallpaper service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker input method service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker input method service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker tile service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker tile service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker voice interaction service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker voice interaction service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker chooser target service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker chooser target service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_INPUT_ONLY for plugin to specify if variable is Tasker condition provider service input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker condition provider service input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_INPUT_ONLY for plugin to specify if variable is Tasker quick settings tile input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker quick settings tile input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_INPUT_ONLY for plugin to specify if variable is Tasker app widget input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker app widget input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_INPUT_ONLY for plugin to specify if variable is Tasker live wallpaper input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker live wallpaper input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_INPUT_ONLY for plugin to specify if variable is Tasker keyboard input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker keyboard input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_INPUT_ONLY for plugin to specify if variable is Tasker notification listener input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker notification listener input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_INPUT_ONLY for plugin to specify if variable is Tasker dream input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker dream input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_INPUT_ONLY for plugin to specify if variable is Tasker print input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker print input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_INPUT_ONLY for plugin to specify if variable is Tasker VPN input only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_INPUT_ONLY_CONFIG for plugin to specify if variable is Tasker VPN input only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_CONFIG_ONLY for plugin to specify if variable is Tasker wallpaper config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_CONFIG_ONLY for plugin to specify if variable is Tasker input method config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker input method config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker accessibility service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker accessibility service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker dream service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker dream service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker print service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker print service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker VPN service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker VPN service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker wallpaper service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker input method service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker input method service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker tile service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker tile service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker voice interaction service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker voice interaction service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker chooser target service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker chooser target service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_CONFIG_ONLY for plugin to specify if variable is Tasker condition provider service config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker condition provider service config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_CONFIG_ONLY for plugin to specify if variable is Tasker quick settings tile config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker quick settings tile config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_CONFIG_ONLY for plugin to specify if variable is Tasker app widget config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker app widget config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_CONFIG_ONLY for plugin to specify if variable is Tasker live wallpaper config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker live wallpaper config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_CONFIG_ONLY for plugin to specify if variable is Tasker keyboard config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker keyboard config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_CONFIG_ONLY for plugin to specify if variable is Tasker notification listener config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker notification listener config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_CONFIG_ONLY for plugin to specify if variable is Tasker dream config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker dream config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_CONFIG_ONLY for plugin to specify if variable is Tasker print config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker print config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_CONFIG_ONLY for plugin to specify if variable is Tasker VPN config only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_CONFIG_ONLY_CONFIG for plugin to specify if variable is Tasker VPN config only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_ACTION_ONLY for plugin to specify if variable is Tasker wallpaper action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_ACTION_ONLY for plugin to specify if variable is Tasker input method action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker input method action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker accessibility service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker accessibility service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker dream service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker dream service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker print service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker print service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker VPN service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker VPN service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker wallpaper service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker input method service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker input method service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker tile service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker tile service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker voice interaction service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker voice interaction service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker chooser target service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker chooser target service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_ACTION_ONLY for plugin to specify if variable is Tasker condition provider service action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker condition provider service action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_ACTION_ONLY for plugin to specify if variable is Tasker quick settings tile action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker quick settings tile action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_ACTION_ONLY for plugin to specify if variable is Tasker app widget action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker app widget action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_ACTION_ONLY for plugin to specify if variable is Tasker live wallpaper action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker live wallpaper action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_ACTION_ONLY for plugin to specify if variable is Tasker keyboard action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker keyboard action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_ACTION_ONLY for plugin to specify if variable is Tasker notification listener action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker notification listener action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_ACTION_ONLY for plugin to specify if variable is Tasker dream action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker dream action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_ACTION_ONLY for plugin to specify if variable is Tasker print action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker print action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_ACTION_ONLY for plugin to specify if variable is Tasker VPN action only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_ACTION_ONLY_CONFIG for plugin to specify if variable is Tasker VPN action only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_CONDITION_ONLY for plugin to specify if variable is Tasker wallpaper condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_CONDITION_ONLY for plugin to specify if variable is Tasker input method condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker input method condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker accessibility service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker accessibility service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker dream service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker dream service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker print service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker print service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker VPN service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker VPN service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker wallpaper service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker input method service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker input method service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker tile service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker tile service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker voice interaction service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker voice interaction service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker chooser target service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker chooser target service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_CONDITION_ONLY for plugin to specify if variable is Tasker condition provider service condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker condition provider service condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_CONDITION_ONLY for plugin to specify if variable is Tasker quick settings tile condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker quick settings tile condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_CONDITION_ONLY for plugin to specify if variable is Tasker app widget condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker app widget condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_CONDITION_ONLY for plugin to specify if variable is Tasker live wallpaper condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker live wallpaper condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_CONDITION_ONLY for plugin to specify if variable is Tasker keyboard condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker keyboard condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_CONDITION_ONLY for plugin to specify if variable is Tasker notification listener condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker notification listener condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_CONDITION_ONLY for plugin to specify if variable is Tasker dream condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker dream condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_CONDITION_ONLY for plugin to specify if variable is Tasker print condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker print condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_CONDITION_ONLY for plugin to specify if variable is Tasker VPN condition only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_CONDITION_ONLY_CONFIG for plugin to specify if variable is Tasker VPN condition only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_EVENT_ONLY for plugin to specify if variable is Tasker wallpaper event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_EVENT_ONLY for plugin to specify if variable is Tasker input method event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker input method event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker accessibility service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker accessibility service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker dream service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker dream service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker print service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker print service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker VPN service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker VPN service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker wallpaper service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker input method service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker input method service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker tile service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker tile service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker voice interaction service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker voice interaction service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker chooser target service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker chooser target service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_EVENT_ONLY for plugin to specify if variable is Tasker condition provider service event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker condition provider service event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_EVENT_ONLY for plugin to specify if variable is Tasker quick settings tile event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker quick settings tile event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_EVENT_ONLY for plugin to specify if variable is Tasker app widget event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker app widget event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_EVENT_ONLY for plugin to specify if variable is Tasker live wallpaper event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker live wallpaper event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_EVENT_ONLY for plugin to specify if variable is Tasker keyboard event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker keyboard event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_EVENT_ONLY for plugin to specify if variable is Tasker notification listener event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker notification listener event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_EVENT_ONLY for plugin to specify if variable is Tasker dream event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker dream event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_EVENT_ONLY for plugin to specify if variable is Tasker print event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker print event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_EVENT_ONLY for plugin to specify if variable is Tasker VPN event only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_EVENT_ONLY_CONFIG for plugin to specify if variable is Tasker VPN event only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_DEFAULT_VALUE for plugin to specify if variable is Tasker wallpaper default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker wallpaper default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_DEFAULT_VALUE for plugin to specify if variable is Tasker input method default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker input method default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker accessibility service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker accessibility service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker dream service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker dream service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker print service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker print service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker VPN service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker VPN service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker wallpaper service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker wallpaper service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker input method service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker input method service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker tile service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker tile service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker voice interaction service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker voice interaction service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker chooser target service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker chooser target service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_DEFAULT_VALUE for plugin to specify if variable is Tasker condition provider service default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker condition provider service default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_DEFAULT_VALUE for plugin to specify if variable is Tasker quick settings tile default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker quick settings tile default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_DEFAULT_VALUE for plugin to specify if variable is Tasker app widget default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker app widget default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_DEFAULT_VALUE for plugin to specify if variable is Tasker live wallpaper default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker live wallpaper default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_DEFAULT_VALUE for plugin to specify if variable is Tasker keyboard default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker keyboard default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_DEFAULT_VALUE for plugin to specify if variable is Tasker notification listener default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker notification listener default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_DEFAULT_VALUE for plugin to specify if variable is Tasker dream default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker dream default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_DEFAULT_VALUE for plugin to specify if variable is Tasker print default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker print default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_DEFAULT_VALUE for plugin to specify if variable is Tasker VPN default value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_DEFAULT_VALUE_CONFIG for plugin to specify if variable is Tasker VPN default value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_MIN_VALUE for plugin to specify if variable is Tasker wallpaper min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker wallpaper min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_MIN_VALUE for plugin to specify if variable is Tasker input method min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker input method min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker accessibility service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker accessibility service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker dream service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker dream service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker print service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker print service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker VPN service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker VPN service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker wallpaper service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker wallpaper service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker input method service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker input method service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker tile service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker tile service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker voice interaction service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker voice interaction service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker chooser target service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker chooser target service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_MIN_VALUE for plugin to specify if variable is Tasker condition provider service min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker condition provider service min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_MIN_VALUE for plugin to specify if variable is Tasker quick settings tile min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker quick settings tile min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_MIN_VALUE for plugin to specify if variable is Tasker app widget min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker app widget min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_MIN_VALUE for plugin to specify if variable is Tasker live wallpaper min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker live wallpaper min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_MIN_VALUE for plugin to specify if variable is Tasker keyboard min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker keyboard min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_MIN_VALUE for plugin to specify if variable is Tasker notification listener min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker notification listener min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_MIN_VALUE for plugin to specify if variable is Tasker dream min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker dream min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_MIN_VALUE for plugin to specify if variable is Tasker print min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker print min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_MIN_VALUE for plugin to specify if variable is Tasker VPN min value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_MIN_VALUE_CONFIG for plugin to specify if variable is Tasker VPN min value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_MAX_VALUE for plugin to specify if variable is Tasker wallpaper max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker wallpaper max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_MAX_VALUE for plugin to specify if variable is Tasker input method max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker input method max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker accessibility service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker accessibility service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker dream service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker dream service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker print service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker print service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker VPN service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker VPN service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker wallpaper service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker wallpaper service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker input method service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker input method service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker tile service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker tile service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker voice interaction service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker voice interaction service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker chooser target service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker chooser target service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_MAX_VALUE for plugin to specify if variable is Tasker condition provider service max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker condition provider service max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_MAX_VALUE for plugin to specify if variable is Tasker quick settings tile max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker quick settings tile max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_MAX_VALUE for plugin to specify if variable is Tasker app widget max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker app widget max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_MAX_VALUE for plugin to specify if variable is Tasker live wallpaper max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker live wallpaper max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_MAX_VALUE for plugin to specify if variable is Tasker keyboard max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker keyboard max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_MAX_VALUE for plugin to specify if variable is Tasker notification listener max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker notification listener max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_MAX_VALUE for plugin to specify if variable is Tasker dream max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker dream max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_MAX_VALUE for plugin to specify if variable is Tasker print max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker print max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_MAX_VALUE for plugin to specify if variable is Tasker VPN max value

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_MAX_VALUE_CONFIG for plugin to specify if variable is Tasker VPN max value config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_ALLOWED_VALUES for plugin to specify if variable is Tasker wallpaper allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker wallpaper allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_ALLOWED_VALUES for plugin to specify if variable is Tasker input method allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker input method allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker accessibility service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker accessibility service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker dream service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker dream service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker print service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker print service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker VPN service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker VPN service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker wallpaper service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker wallpaper service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker input method service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker input method service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker tile service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker tile service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker voice interaction service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker voice interaction service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker chooser target service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker chooser target service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_ALLOWED_VALUES for plugin to specify if variable is Tasker condition provider service allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker condition provider service allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_ALLOWED_VALUES for plugin to specify if variable is Tasker quick settings tile allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker quick settings tile allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_ALLOWED_VALUES for plugin to specify if variable is Tasker app widget allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker app widget allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_ALLOWED_VALUES for plugin to specify if variable is Tasker live wallpaper allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker live wallpaper allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_ALLOWED_VALUES for plugin to specify if variable is Tasker keyboard allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker keyboard allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_ALLOWED_VALUES for plugin to specify if variable is Tasker notification listener allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker notification listener allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_ALLOWED_VALUES for plugin to specify if variable is Tasker dream allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker dream allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_ALLOWED_VALUES for plugin to specify if variable is Tasker print allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker print allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_ALLOWED_VALUES for plugin to specify if variable is Tasker VPN allowed values

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_ALLOWED_VALUES_CONFIG for plugin to specify if variable is Tasker VPN allowed values config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_REGEX_VALIDATION for plugin to specify if variable is Tasker wallpaper regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker wallpaper regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_REGEX_VALIDATION for plugin to specify if variable is Tasker input method regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker input method regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker accessibility service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker accessibility service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker dream service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker dream service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker print service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker print service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker VPN service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker VPN service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker wallpaper service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker wallpaper service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker input method service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker input method service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker tile service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker tile service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker voice interaction service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker voice interaction service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker chooser target service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker chooser target service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_REGEX_VALIDATION for plugin to specify if variable is Tasker condition provider service regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker condition provider service regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_REGEX_VALIDATION for plugin to specify if variable is Tasker quick settings tile regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker quick settings tile regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_REGEX_VALIDATION for plugin to specify if variable is Tasker app widget regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker app widget regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_REGEX_VALIDATION for plugin to specify if variable is Tasker live wallpaper regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker live wallpaper regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_REGEX_VALIDATION for plugin to specify if variable is Tasker keyboard regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker keyboard regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_REGEX_VALIDATION for plugin to specify if variable is Tasker notification listener regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker notification listener regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_REGEX_VALIDATION for plugin to specify if variable is Tasker dream regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker dream regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_REGEX_VALIDATION for plugin to specify if variable is Tasker print regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker print regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_REGEX_VALIDATION for plugin to specify if variable is Tasker VPN regex validation

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_REGEX_VALIDATION_CONFIG for plugin to specify if variable is Tasker VPN regex validation config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_PASSWORD for plugin to specify if variable is Tasker wallpaper is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker wallpaper is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_PASSWORD for plugin to specify if variable is Tasker input method is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker input method is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker accessibility service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker accessibility service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker dream service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker dream service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker print service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker print service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker VPN service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker VPN service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker wallpaper service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker wallpaper service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker input method service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker input method service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker tile service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker tile service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker voice interaction service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker voice interaction service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker chooser target service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker chooser target service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_PASSWORD for plugin to specify if variable is Tasker condition provider service is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker condition provider service is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_PASSWORD for plugin to specify if variable is Tasker quick settings tile is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker quick settings tile is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_PASSWORD for plugin to specify if variable is Tasker app widget is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker app widget is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_PASSWORD for plugin to specify if variable is Tasker live wallpaper is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker live wallpaper is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_PASSWORD for plugin to specify if variable is Tasker keyboard is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker keyboard is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_PASSWORD for plugin to specify if variable is Tasker notification listener is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker notification listener is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_PASSWORD for plugin to specify if variable is Tasker dream is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker dream is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_PASSWORD for plugin to specify if variable is Tasker print is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker print is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_PASSWORD for plugin to specify if variable is Tasker VPN is password

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_PASSWORD_CONFIG for plugin to specify if variable is Tasker VPN is password config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_HTML for plugin to specify if variable is Tasker wallpaper is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_HTML_CONFIG for plugin to specify if variable is Tasker wallpaper is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_HTML for plugin to specify if variable is Tasker input method is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_HTML_CONFIG for plugin to specify if variable is Tasker input method is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_HTML for plugin to specify if variable is Tasker accessibility service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker accessibility service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_HTML for plugin to specify if variable is Tasker dream service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker dream service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_HTML for plugin to specify if variable is Tasker print service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker print service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_HTML for plugin to specify if variable is Tasker VPN service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker VPN service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_HTML for plugin to specify if variable is Tasker wallpaper service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker wallpaper service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_HTML for plugin to specify if variable is Tasker input method service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker input method service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_HTML for plugin to specify if variable is Tasker tile service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker tile service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_HTML for plugin to specify if variable is Tasker voice interaction service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker voice interaction service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_HTML for plugin to specify if variable is Tasker chooser target service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker chooser target service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_HTML for plugin to specify if variable is Tasker condition provider service is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_HTML_CONFIG for plugin to specify if variable is Tasker condition provider service is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_HTML for plugin to specify if variable is Tasker quick settings tile is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_HTML_CONFIG for plugin to specify if variable is Tasker quick settings tile is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_HTML for plugin to specify if variable is Tasker app widget is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_HTML_CONFIG for plugin to specify if variable is Tasker app widget is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_HTML for plugin to specify if variable is Tasker live wallpaper is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_HTML_CONFIG for plugin to specify if variable is Tasker live wallpaper is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_HTML for plugin to specify if variable is Tasker keyboard is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_HTML_CONFIG for plugin to specify if variable is Tasker keyboard is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_HTML for plugin to specify if variable is Tasker notification listener is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_HTML_CONFIG for plugin to specify if variable is Tasker notification listener is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_HTML for plugin to specify if variable is Tasker dream is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_HTML_CONFIG for plugin to specify if variable is Tasker dream is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_HTML for plugin to specify if variable is Tasker print is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_HTML_CONFIG for plugin to specify if variable is Tasker print is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_HTML for plugin to specify if variable is Tasker VPN is HTML

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_HTML_CONFIG for plugin to specify if variable is Tasker VPN is HTML config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_MULTI_LINE for plugin to specify if variable is Tasker wallpaper is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker wallpaper is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_MULTI_LINE for plugin to specify if variable is Tasker input method is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker input method is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker accessibility service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker accessibility service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker dream service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker dream service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker print service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker print service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker VPN service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker VPN service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker wallpaper service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker wallpaper service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker input method service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker input method service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker tile service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker tile service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker voice interaction service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker voice interaction service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker chooser target service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker chooser target service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_MULTI_LINE for plugin to specify if variable is Tasker condition provider service is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker condition provider service is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_MULTI_LINE for plugin to specify if variable is Tasker quick settings tile is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker quick settings tile is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_MULTI_LINE for plugin to specify if variable is Tasker app widget is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker app widget is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_MULTI_LINE for plugin to specify if variable is Tasker live wallpaper is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker live wallpaper is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_MULTI_LINE for plugin to specify if variable is Tasker keyboard is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker keyboard is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_MULTI_LINE for plugin to specify if variable is Tasker notification listener is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker notification listener is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_MULTI_LINE for plugin to specify if variable is Tasker dream is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker dream is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_MULTI_LINE for plugin to specify if variable is Tasker print is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker print is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_MULTI_LINE for plugin to specify if variable is Tasker VPN is multi-line

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_MULTI_LINE_CONFIG for plugin to specify if variable is Tasker VPN is multi-line config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_READ_ONLY for plugin to specify if variable is Tasker wallpaper is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_READ_ONLY for plugin to specify if variable is Tasker input method is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker input method is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker accessibility service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker accessibility service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker dream service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker dream service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker print service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker print service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker VPN service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker VPN service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker wallpaper service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker wallpaper service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker input method service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker input method service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker tile service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker tile service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker voice interaction service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker voice interaction service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker chooser target service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker chooser target service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_READ_ONLY for plugin to specify if variable is Tasker condition provider service is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker condition provider service is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_READ_ONLY for plugin to specify if variable is Tasker quick settings tile is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker quick settings tile is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_READ_ONLY for plugin to specify if variable is Tasker app widget is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker app widget is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_READ_ONLY for plugin to specify if variable is Tasker live wallpaper is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker live wallpaper is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_READ_ONLY for plugin to specify if variable is Tasker keyboard is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker keyboard is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_READ_ONLY for plugin to specify if variable is Tasker notification listener is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker notification listener is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_READ_ONLY for plugin to specify if variable is Tasker dream is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker dream is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_READ_ONLY for plugin to specify if variable is Tasker print is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker print is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_READ_ONLY for plugin to specify if variable is Tasker VPN is read-only

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_READ_ONLY_CONFIG for plugin to specify if variable is Tasker VPN is read-only config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_HIDDEN for plugin to specify if variable is Tasker wallpaper is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker wallpaper is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_HIDDEN for plugin to specify if variable is Tasker input method is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker input method is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker accessibility service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker accessibility service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker dream service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker dream service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker print service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker print service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker VPN service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker VPN service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker wallpaper service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker wallpaper service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker input method service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker input method service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker tile service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker tile service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker voice interaction service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker voice interaction service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker chooser target service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker chooser target service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_HIDDEN for plugin to specify if variable is Tasker condition provider service is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker condition provider service is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_HIDDEN for plugin to specify if variable is Tasker quick settings tile is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker quick settings tile is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_HIDDEN for plugin to specify if variable is Tasker app widget is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker app widget is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_HIDDEN for plugin to specify if variable is Tasker live wallpaper is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker live wallpaper is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_HIDDEN for plugin to specify if variable is Tasker keyboard is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker keyboard is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_HIDDEN for plugin to specify if variable is Tasker notification listener is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker notification listener is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_HIDDEN for plugin to specify if variable is Tasker dream is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker dream is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_HIDDEN for plugin to specify if variable is Tasker print is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker print is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_HIDDEN for plugin to specify if variable is Tasker VPN is hidden

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_HIDDEN_CONFIG for plugin to specify if variable is Tasker VPN is hidden config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_ADVANCED for plugin to specify if variable is Tasker wallpaper is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker wallpaper is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_ADVANCED for plugin to specify if variable is Tasker input method is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker input method is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker accessibility service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker accessibility service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker dream service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker dream service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker print service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker print service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker VPN service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker VPN service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker wallpaper service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker wallpaper service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker input method service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker input method service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker tile service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_TILE_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker tile service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker voice interaction service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VOICE_INTERACTION_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker voice interaction service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker chooser target service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CHOOSER_TARGET_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker chooser target service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_ADVANCED for plugin to specify if variable is Tasker condition provider service is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_CONDITION_PROVIDER_SERVICE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker condition provider service is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_ADVANCED for plugin to specify if variable is Tasker quick settings tile is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_QUICK_SETTINGS_TILE_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker quick settings tile is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_ADVANCED for plugin to specify if variable is Tasker app widget is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_APP_WIDGET_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker app widget is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_ADVANCED for plugin to specify if variable is Tasker live wallpaper is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_LIVE_WALLPAPER_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker live wallpaper is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_ADVANCED for plugin to specify if variable is Tasker keyboard is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_KEYBOARD_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker keyboard is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_ADVANCED for plugin to specify if variable is Tasker notification listener is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_NOTIFICATION_LISTENER_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker notification listener is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_ADVANCED for plugin to specify if variable is Tasker dream is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_DREAM_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker dream is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_ADVANCED for plugin to specify if variable is Tasker print is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_PRINT_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker print is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_ADVANCED for plugin to specify if variable is Tasker VPN is advanced

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_VPN_IS_ADVANCED_CONFIG for plugin to specify if variable is Tasker VPN is advanced config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_IMPORTANT for plugin to specify if variable is Tasker wallpaper is important

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_WALLPAPER_IS_IMPORTANT_CONFIG for plugin to specify if variable is Tasker wallpaper is important config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_IMPORTANT for plugin to specify if variable is Tasker input method is important

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_INPUT_METHOD_IS_IMPORTANT_CONFIG for plugin to specify if variable is Tasker input method is important config

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_IMPORTANT for plugin to specify if variable is Tasker accessibility service is important

// 20.10.13: add EXTRA_REQUESTED_VARIABLE_IS_TASKER_ACCESSIBILITY_SERVICE_IS_IMPORTANT_CONFIG for plugin to specify if variable is Tasker accessibility service is important config

//
