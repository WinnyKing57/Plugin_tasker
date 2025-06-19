package com.example.taskercalendarplugin.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter; // Added
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView; // Added for permission message
import android.provider.Settings; // Added for opening app settings
import androidx.annotation.NonNull; // Added for onRequestPermissionsResult
import androidx.appcompat.app.AlertDialog; // For rationale dialog
import android.content.DialogInterface; // For dialog listeners
import android.net.Uri;
import android.content.Context; // Added for TaskerPluginConfig

import com.example.taskercalendarplugin.R;
import com.example.taskercalendarplugin.util.CalendarPermissionHelper; // Added
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig; // Added for TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput; // Added for TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos; // Added for TaskerInput
import com.example.taskercalendarplugin.tasker.model.CalendarPluginInput; // Added for TaskerPluginConfig
import com.example.taskercalendarplugin.tasker.CalendarPluginHelper; // Added for TaskerPluginConfig
// The LocaleIntent class previously here is now removed as its functionality
// (EXTRA_BUNDLE, EXTRA_STRING_BLURB, etc.) is handled by the TaskerPluginLibrary.
// Action constants like ACTION_EDIT_SETTING and ACTION_EDIT_CONDITION are typically
// used in AndroidManifest.xml and not directly needed in the Activity code after refactoring
// to the library helper.


public class PluginEditActivity extends Activity implements TaskerPluginConfig<CalendarPluginInput> {

    private static final String TAG = "PluginEditActivity";
    private CalendarPluginHelper taskerHelper; // Added for TaskerPluginConfig

    // --- TaskerPluginConfig Methods ---
    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    // Override methods from Activity that TaskerPluginConfig might call
    @Override
    public Intent getIntent() {
        return super.getIntent(); // Already present in Activity, but explicitly defined by interface
    }

    // @Override // Removing this override as the base Activity method is sufficient
    // public void setResult(int resultCode, Intent data) {
    //     super.setResult(resultCode, data);
    // }

    // Note: finish() is also in the interface, but we'll handle its override when dealing with save/back
    @Override
    public void finish() {
        super.finish(); // Already present in Activity
    }

    @Override
    public void assignFromInput(TaskerInput<CalendarPluginInput> input) {
        if (input == null || input.getRegular() == null) {
            Log.w(TAG, "assignFromInput: null input or regular input received. Setting default UI state.");
            spinnerActionType.setSelection(0); // Default to first item
            updateUiVisibility(spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "");
            // Clear fields or set defaults
            etGetEventsCount.setText("");
            etGetEventsDaysAhead.setText("7"); // Default days
            etAddEventTitle.setText("");
            etAddEventDescription.setText("");
            etAddEventLocation.setText("");
            etAddEventStartTimeOffset.setText("60"); // Default offset
            etAddEventDuration.setText("30"); // Default duration
            if (editTextConfig != null) editTextConfig.setText("");
            return;
        }

        CalendarPluginInput pluginInput = input.getRegular();
        String actionType = pluginInput.getActionType();

        if (actionType == null && pluginInput.getLegacyConfigData() != null) {
            // Handle legacy config
            Log.w(TAG, "assignFromInput: Old configuration data found: " + pluginInput.getLegacyConfigData() + ". Attempting to show in legacy field.");
            if (editTextConfig != null) {
                editTextConfig.setText(pluginInput.getLegacyConfigData());
                editTextConfig.setVisibility(View.VISIBLE);
                layoutGetEventsConfig.setVisibility(View.GONE);
                layoutAddEventConfig.setVisibility(View.GONE);
                spinnerActionType.setEnabled(false);
                buttonSave.setText(R.string.button_save_legacy_config);
            }
            return;
        }

        // Restore normal UI if it was in legacy mode
        if (editTextConfig != null) editTextConfig.setVisibility(View.GONE);
        spinnerActionType.setEnabled(true);
        buttonSave.setText(R.string.button_save_config);


        int spinnerPosition = 0;
        if (ACTION_GET_EVENTS.equals(actionType)) {
            spinnerPosition = getSpinnerPosition(R.array.action_types_array, getString(R.string.action_get_events_label));
            etGetEventsCount.setText(pluginInput.getGetEventsCount() != null ? pluginInput.getGetEventsCount() : "");
            etGetEventsDaysAhead.setText(pluginInput.getGetEventsDaysAhead() != null ? pluginInput.getGetEventsDaysAhead() : "7");
        } else if (ACTION_ADD_EVENT.equals(actionType)) {
            spinnerPosition = getSpinnerPosition(R.array.action_types_array, getString(R.string.action_add_event_label));
            etAddEventTitle.setText(pluginInput.getAddEventTitle() != null ? pluginInput.getAddEventTitle() : "");
            etAddEventDescription.setText(pluginInput.getAddEventDescription() != null ? pluginInput.getAddEventDescription() : "");
            etAddEventLocation.setText(pluginInput.getAddEventLocation() != null ? pluginInput.getAddEventLocation() : "");
            etAddEventStartTimeOffset.setText(pluginInput.getAddEventStartTimeOffset() != null ? pluginInput.getAddEventStartTimeOffset() : "60");
            etAddEventDuration.setText(pluginInput.getAddEventDuration() != null ? pluginInput.getAddEventDuration() : "30");
        } else {
             Log.d(TAG, "assignFromInput: Unknown or null actionType. Setting default UI.");
             spinnerActionType.setSelection(0); // Default to first item
             // Clear fields or set defaults for new config
             etGetEventsCount.setText("");
             etGetEventsDaysAhead.setText("7");
             etAddEventTitle.setText("");
             etAddEventDescription.setText("");
             etAddEventLocation.setText("");
             etAddEventStartTimeOffset.setText("60");
             etAddEventDuration.setText("30");
        }
        spinnerActionType.setSelection(spinnerPosition);
        updateUiVisibility(spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "");
        Log.d(TAG, "Configuration loaded via assignFromInput for action: " + actionType);
    }

    @Override
    public TaskerInput<CalendarPluginInput> getInputForTasker() {
        CalendarPluginInput currentInput = new CalendarPluginInput();
        String selectedActionLabel = "";
        if (spinnerActionType.getSelectedItem() != null) {
            selectedActionLabel = spinnerActionType.getSelectedItem().toString();
        }

        if (selectedActionLabel.equals(getString(R.string.action_get_events_label))) {
            currentInput.setActionType(ACTION_GET_EVENTS);
            currentInput.setGetEventsCount(etGetEventsCount.getText().toString().trim());
            currentInput.setGetEventsDaysAhead(etGetEventsDaysAhead.getText().toString().trim());
        } else if (selectedActionLabel.equals(getString(R.string.action_add_event_label))) {
            currentInput.setActionType(ACTION_ADD_EVENT);
            currentInput.setAddEventTitle(etAddEventTitle.getText().toString().trim());
            currentInput.setAddEventDescription(etAddEventDescription.getText().toString().trim());
            currentInput.setAddEventLocation(etAddEventLocation.getText().toString().trim());
            currentInput.setAddEventStartTimeOffset(etAddEventStartTimeOffset.getText().toString().trim());
            currentInput.setAddEventDuration(etAddEventDuration.getText().toString().trim());
        } else if (editTextConfig != null && editTextConfig.getVisibility() == View.VISIBLE) {
            currentInput.setLegacyConfigData(editTextConfig.getText().toString().trim());
            // actionType might remain null for legacy, or set a specific legacy action type if desired
        } else {
            // Potentially unconfigured or unknown state
             Log.w(TAG, "getInputForTasker: Unselected or unknown action type during save.");
             // Set a default or leave actionType null if that's how unconfigured should be handled
        }
        return new TaskerInput<>(currentInput, new com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos());
    }
    // --- End TaskerPluginConfig Methods ---

    // Configuration Action Types
    public static final String ACTION_TYPE_KEY = "action_type";
    public static final String ACTION_GET_EVENTS = "action_get_events";
    public static final String ACTION_ADD_EVENT = "action_add_event";

    // Bundle Keys for Get Events action
    public static final String BUNDLE_KEY_GET_EVENTS_COUNT = "get_events_count";
    public static final String BUNDLE_KEY_GET_EVENTS_DAYS_AHEAD = "get_events_days_ahead"; // Alternative to count or specific range

    // Bundle Keys for Add Event action
    public static final String BUNDLE_KEY_ADD_EVENT_TITLE = "add_event_title";
    public static final String BUNDLE_KEY_ADD_EVENT_DESCRIPTION = "add_event_description";
    public static final String BUNDLE_KEY_ADD_EVENT_LOCATION = "add_event_location";
    public static final String BUNDLE_KEY_ADD_EVENT_START_TIME_OFFSET = "add_event_start_time_offset"; // e.g., minutes from now
    public static final String BUNDLE_KEY_ADD_EVENT_DURATION = "add_event_duration"; // e.g., minutes

    // Fallback for old simple config (can be removed once fully migrated)
    // private static final String BUNDLE_KEY_CONFIG_DATA = "config_data_key"; // Now handled by CalendarPluginInput's legacyConfigData


    private EditText editTextConfig; // Will be replaced by specific fields. Kept for legacy handling in assignFromInput.
    private Button buttonSave;
    // private boolean isCancelled = true; // Assume cancelled until save is clicked -> Handled by Tasker library
    // private Bundle existingBundle = null; // Handled by Tasker library and assignFromInput

    // UI Elements - to be defined in XML and initialized here
    private android.widget.Spinner spinnerActionType;
    private android.widget.EditText etGetEventsCount, etGetEventsDaysAhead;
    private android.widget.EditText etAddEventTitle, etAddEventDescription, etAddEventLocation;
    private android.widget.EditText etAddEventStartTimeOffset, etAddEventDuration;
    private android.widget.LinearLayout layoutGetEventsConfig, layoutAddEventConfig;
    private TextView tvPermissionStatus; // For displaying permission status


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_edit);

        taskerHelper = new CalendarPluginHelper(this); // Initialize Tasker helper

        // Initialize UI elements (IDs will be defined in the XML)
        spinnerActionType = findViewById(R.id.spinnerActionType);
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus); // Initialize TextView for status
        etGetEventsCount = findViewById(R.id.etGetEventsCount);
        etGetEventsDaysAhead = findViewById(R.id.etGetEventsDaysAhead);
        etAddEventTitle = findViewById(R.id.etAddEventTitle);
        etAddEventDescription = findViewById(R.id.etAddEventDescription);
        etAddEventLocation = findViewById(R.id.etAddEventLocation);
        etAddEventStartTimeOffset = findViewById(R.id.etAddEventStartTimeOffset);
        etAddEventDuration = findViewById(R.id.etAddEventDuration);
        layoutGetEventsConfig = findViewById(R.id.layoutGetEventsConfig);
        layoutAddEventConfig = findViewById(R.id.layoutAddEventConfig);

        // editTextConfig was the old simple text field, hide it for now or remove from layout
        editTextConfig = findViewById(R.id.editTextConfig); // Assuming it's still in layout for a moment
        if (editTextConfig != null) editTextConfig.setVisibility(View.GONE);


        buttonSave = findViewById(R.id.buttonSave);

        // Populate Spinner
        // Options should come from strings.xml for localization
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.action_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActionType.setAdapter(adapter);
        spinnerActionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUiVisibility(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Hide all specific configs
                layoutGetEventsConfig.setVisibility(View.GONE);
                layoutAddEventConfig.setVisibility(View.GONE);
            }
        });

        taskerHelper.onCreate(); // This will call assignFromInput with current Tasker data

        // // Retrieve existing configuration if any -> Handled by taskerHelper.onCreate() & assignFromInput
        // existingBundle = getIntent().getBundleExtra(LocaleIntent.EXTRA_BUNDLE);
        // if (existingBundle == null) {
        //     existingBundle = new Bundle(); // Initialize if null
        //     Log.d(TAG, "No existing configuration found, created new Bundle.");
        // } else {
        //     Log.d(TAG, "Existing configuration bundle found.");
        // }
        // // loadConfiguration() is called in onResume after permission check -> Logic moved to assignFromInput

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validation is kept here for now, but could be moved to CalendarPluginHelper.isInputValid
                if (CalendarPermissionHelper.areCalendarPermissionsGranted(PluginEditActivity.this)) {
                    // Basic validation before attempting to save with helper
                    String selectedActionLabel = spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "";
                    if (selectedActionLabel.equals(getString(R.string.action_get_events_label))) {
                        String countStr = etGetEventsCount.getText().toString().trim();
                        String daysAheadStr = etGetEventsDaysAhead.getText().toString().trim();
                        if (TextUtils.isEmpty(countStr) && TextUtils.isEmpty(daysAheadStr)) {
                            etGetEventsCount.setError(getString(R.string.error_field_required_one_of_two));
                            etGetEventsDaysAhead.setError(getString(R.string.error_field_required_one_of_two));
                            Toast.makeText(PluginEditActivity.this, R.string.error_get_events_config_required, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            if (!TextUtils.isEmpty(countStr)) Integer.parseInt(countStr);
                            if (!TextUtils.isEmpty(daysAheadStr)) Integer.parseInt(daysAheadStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(PluginEditActivity.this, R.string.error_invalid_number_format, Toast.LENGTH_SHORT).show();
                            if (!TextUtils.isEmpty(countStr) && !countStr.matches("\\d+")) etGetEventsCount.setError(getString(R.string.error_invalid_number_format));
                            if (!TextUtils.isEmpty(daysAheadStr) && !daysAheadStr.matches("\\d+")) etGetEventsDaysAhead.setError(getString(R.string.error_invalid_number_format));
                            return;
                        }
                    } else if (selectedActionLabel.equals(getString(R.string.action_add_event_label))) {
                        String title = etAddEventTitle.getText().toString().trim();
                        if (TextUtils.isEmpty(title)) {
                            etAddEventTitle.setError(getString(R.string.error_field_required));
                            Toast.makeText(PluginEditActivity.this, R.string.error_add_event_title_required, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String startTimeOffsetStr = etAddEventStartTimeOffset.getText().toString().trim();
                        String durationStr = etAddEventDuration.getText().toString().trim();
                         try {
                            if (!TextUtils.isEmpty(startTimeOffsetStr)) Integer.parseInt(startTimeOffsetStr); else etAddEventStartTimeOffset.setText("60");
                            if (!TextUtils.isEmpty(durationStr)) Integer.parseInt(durationStr); else etAddEventDuration.setText("30");
                        } catch (NumberFormatException e) {
                            Toast.makeText(PluginEditActivity.this, R.string.error_invalid_number_format, Toast.LENGTH_SHORT).show();
                            if (!TextUtils.isEmpty(startTimeOffsetStr) && !startTimeOffsetStr.matches("\\d*")) etAddEventStartTimeOffset.setError(getString(R.string.error_invalid_number_format));
                            if (!TextUtils.isEmpty(durationStr) && !durationStr.matches("\\d*")) etAddEventDuration.setError(getString(R.string.error_invalid_number_format));
                            return;
                        }
                    } else if (editTextConfig == null || editTextConfig.getVisibility() != View.VISIBLE || TextUtils.isEmpty(editTextConfig.getText().toString().trim())) {
                         // If not legacy mode and no valid new action selected (should be handled by spinner default)
                        if (spinnerActionType.isEnabled()) { // Only error if new UI was supposed to be used
                            Toast.makeText(PluginEditActivity.this, R.string.error_invalid_action, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    // If all validation passes
                    taskerHelper.finishForTasker();
                } else {
                    Toast.makeText(PluginEditActivity.this, R.string.permissions_not_granted_save_disabled, Toast.LENGTH_LONG).show();
                    checkPermissionsAndSetupUI();
                }
            }
        });

        // // Ensure that the result is RESULT_CANCELED if the Activity is finished without saving -> Handled by Tasker library
        // setResult(RESULT_CANCELED);
        // Permission check will be done in onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionsAndSetupUI();
    }

    private void checkPermissionsAndSetupUI() {
        if (CalendarPermissionHelper.areCalendarPermissionsGranted(this)) {
            tvPermissionStatus.setVisibility(View.GONE);
            enableUiComponents(true);
            // loadConfiguration(); // Removed: assignFromInput, called via taskerHelper.onCreate(), handles loading.
                                 // UI state is updated by assignFromInput and enableUiComponents.
        } else {
            tvPermissionStatus.setText(R.string.permissions_required_to_configure);
            tvPermissionStatus.setVisibility(View.VISIBLE);
            enableUiComponents(false); // Disable UI if no permissions
            // Request permissions
            if (CalendarPermissionHelper.shouldShowRationale(this)) {
                showPermissionRationaleDialog();
            } else {
                CalendarPermissionHelper.checkAndRequestCalendarPermissions(this);
            }
        }
    }

    private void enableUiComponents(boolean enabled) {
        spinnerActionType.setEnabled(enabled);
        etGetEventsCount.setEnabled(enabled);
        etGetEventsDaysAhead.setEnabled(enabled);
        etAddEventTitle.setEnabled(enabled);
        etAddEventDescription.setEnabled(enabled);
        etAddEventLocation.setEnabled(enabled);
        etAddEventStartTimeOffset.setEnabled(enabled);
        etAddEventDuration.setEnabled(enabled);
        buttonSave.setEnabled(enabled);
        // Also consider the visibility of config layouts based on current spinner selection
        if (enabled) {
            updateUiVisibility(spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "");
        } else {
            layoutGetEventsConfig.setVisibility(View.GONE);
            layoutAddEventConfig.setVisibility(View.GONE);
        }
    }


    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setPositiveButton(R.string.permission_rationale_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed, open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.permission_rationale_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // User cancelled, UI remains disabled, permission status message visible
                        tvPermissionStatus.setText(R.string.permissions_denied_functionality_limited);
                        enableUiComponents(false);
                    }
                })
                .show();
    }


    private void updateUiVisibility(String selectedAction) {
        if (!spinnerActionType.isEnabled()) { // Don't change visibility if UI is disabled
            layoutGetEventsConfig.setVisibility(View.GONE);
            layoutAddEventConfig.setVisibility(View.GONE);
            return;
        }
        // Assuming action strings from R.array.action_types_array map to these constants
        // or you compare directly with getString(R.string.action_get_events_label)
        String getEventsLabel = getString(R.string.action_get_events_label);
        String addEventLabel = getString(R.string.action_add_event_label);

        if (selectedAction.equals(getEventsLabel)) {
            layoutGetEventsConfig.setVisibility(View.VISIBLE);
            layoutAddEventConfig.setVisibility(View.GONE);
        } else if (selectedAction.equals(addEventLabel)) {
            layoutGetEventsConfig.setVisibility(View.GONE);
            layoutAddEventConfig.setVisibility(View.VISIBLE);
        } else {
            layoutGetEventsConfig.setVisibility(View.GONE);
            layoutAddEventConfig.setVisibility(View.GONE);
        }
    }

    // private void loadConfiguration() { ... } // Method removed, logic is now in assignFromInput, called by taskerHelper.onCreate()

    private int getSpinnerPosition(int arrayResourceId, String valueToFind) {
        String[] array = getResources().getStringArray(arrayResourceId);
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(valueToFind)) {
                return i;
            }
        }
        Log.w(TAG, "Value '" + valueToFind + "' not found in spinner array. Defaulting to 0.");
        return 0;
    }

    // private void saveConfiguration() { ... } // Method removed, logic incorporated into buttonSave OnClickListener and helper methods.

    @Override
    public void onBackPressed() {
        // Log.d(TAG, "Back pressed. isCancelled: " + isCancelled); // isCancelled removed
        Log.d(TAG, "Back pressed.");
        // taskerHelper.onBackPressed(); // Or equivalent if the library provides one for cancellation handling
        super.onBackPressed(); // This will call finish if not overridden further.
                               // The Tasker library ensures RESULT_CANCELED if finishForTasker() isn't called.
    }

    // finish() override is part of TaskerPluginConfig implementation, already added.
    // @Override
    // public void finish() {
    //     // if (isCancelled) { // isCancelled removed
    //     //     setResult(RESULT_CANCELED);
    //     //     Log.d(TAG, "Finishing with RESULT_CANCELED");
    //     // }
    //     super.finish();
    // }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestCode " + requestCode);

        if (CalendarPermissionHelper.handlePermissionResult(requestCode, permissions, grantResults)) {
            Log.d(TAG, "Calendar permissions GRANTED.");
            tvPermissionStatus.setVisibility(View.GONE);
            enableUiComponents(true);
            // loadConfiguration(); // Removed: assignFromInput, called via taskerHelper.onCreate(), handles loading.
                                 // UI state is updated by assignFromInput and enableUiComponents.
            // If Activity was started without permissions, assignFromInput might have used defaults.
            // Re-calling taskerHelper.onCreate() or assignFromInput directly might be needed if
            // the initial input from Tasker should be re-processed after permissions are granted.
            // For now, assume initial load by onCreate's taskerHelper.onCreate() is sufficient.
            // If UI was disabled, enableUiComponents(true) will make it visible with current data.
        } else {
            Log.w(TAG, "Calendar permissions DENIED.");
            tvPermissionStatus.setText(R.string.permissions_denied_functionality_limited);
            tvPermissionStatus.setVisibility(View.VISIBLE);
            enableUiComponents(false);
            // Optionally, if rationale should be shown again or permanent denial, guide to settings
            if (!CalendarPermissionHelper.shouldShowRationale(this) && !CalendarPermissionHelper.areCalendarPermissionsGranted(this)) {
                // Permissions were denied and "don't ask again" was selected
                // Offer to open settings
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied_title)
                .setMessage(R.string.permission_denied_settings_message)
                .setPositiveButton(R.string.permission_rationale_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
