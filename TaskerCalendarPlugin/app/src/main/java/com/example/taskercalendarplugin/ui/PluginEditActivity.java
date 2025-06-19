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


import com.example.taskercalendarplugin.R;
import com.example.taskercalendarplugin.util.CalendarPermissionHelper; // Added
// These are the standard Tasker plugin API constants
// For a real library, you'd import these (e.g., com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE)
// but for now, we define them if not using the actual Tasker library.
// If you add the TaskerPluginSDK.aar or similar, you would remove these manual definitions.
final class LocaleIntent {
    public static final String EXTRA_BUNDLE = "com.twofortyfouram.locale.api.intent.extra.BUNDLE";
    public static final String EXTRA_STRING_BLURB = "com.twofortyfouram.locale.api.intent.extra.BLURB";
    // Action for a setting/action configuration
    public static final String ACTION_EDIT_SETTING = "com.twofortyfouram.locale.api.intent.action.EDIT_SETTING";
    // Action for a condition configuration
    public static final String ACTION_EDIT_CONDITION = "com.twofortyfouram.locale.api.intent.action.EDIT_CONDITION";
}


public class PluginEditActivity extends Activity {

    private static final String TAG = "PluginEditActivity";

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
    private static final String BUNDLE_KEY_CONFIG_DATA = "config_data_key";


    private EditText editTextConfig; // Will be replaced by specific fields
    private Button buttonSave;
    private boolean isCancelled = true; // Assume cancelled until save is clicked
    private Bundle existingBundle = null;

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


        // Retrieve existing configuration if any
        existingBundle = getIntent().getBundleExtra(LocaleIntent.EXTRA_BUNDLE);
        if (existingBundle == null) {
            existingBundle = new Bundle(); // Initialize if null
            Log.d(TAG, "No existing configuration found, created new Bundle.");
        } else {
            Log.d(TAG, "Existing configuration bundle found.");
        }
        // loadConfiguration() is called in onResume after permission check

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CalendarPermissionHelper.areCalendarPermissionsGranted(PluginEditActivity.this)) {
                    saveConfiguration();
                } else {
                    Toast.makeText(PluginEditActivity.this, R.string.permissions_not_granted_save_disabled, Toast.LENGTH_LONG).show();
                    // Optionally, trigger permission request again or guide user to settings
                    checkPermissionsAndSetupUI();
                }
            }
        });

        // Ensure that the result is RESULT_CANCELED if the Activity is finished without saving
        setResult(RESULT_CANCELED);
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
            loadConfiguration(); // Load config only if permissions are granted
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

    private void loadConfiguration() {
        if (!CalendarPermissionHelper.areCalendarPermissionsGranted(this)) {
            Log.w(TAG, "loadConfiguration: Permissions not granted. Skipping load.");
            // UI should be disabled by checkPermissionsAndSetupUI
            return;
        }

        if (existingBundle == null || existingBundle.isEmpty()) {
            Log.d(TAG, "loadConfiguration: Bundle is null or empty, setting default UI state.");
            spinnerActionType.setSelection(0);
            updateUiVisibility(spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "");
            return;
        }

        String actionType = existingBundle.getString(ACTION_TYPE_KEY);
        if (actionType == null) {
            String oldConfig = existingBundle.getString(BUNDLE_KEY_CONFIG_DATA);
            if (oldConfig != null && editTextConfig != null) {
                Log.w(TAG, "Old configuration data found: " + oldConfig + ". Attempting to show in legacy field.");
                editTextConfig.setText(oldConfig);
                editTextConfig.setVisibility(View.VISIBLE); // Show old field
                layoutGetEventsConfig.setVisibility(View.GONE);
                layoutAddEventConfig.setVisibility(View.GONE);
                spinnerActionType.setEnabled(false); // Disable new UI if old config is present
                buttonSave.setText(R.string.button_save_legacy_config); // Change button text
            } else {
                spinnerActionType.setSelection(0);
                updateUiVisibility(spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "");
            }
            return;
        }

        int spinnerPosition = 0;
        if (ACTION_GET_EVENTS.equals(actionType)) {
            spinnerPosition = getSpinnerPosition(R.array.action_types_array, getString(R.string.action_get_events_label));
            etGetEventsCount.setText(existingBundle.getString(BUNDLE_KEY_GET_EVENTS_COUNT, ""));
            etGetEventsDaysAhead.setText(existingBundle.getString(BUNDLE_KEY_GET_EVENTS_DAYS_AHEAD, "7"));
        } else if (ACTION_ADD_EVENT.equals(actionType)) {
            spinnerPosition = getSpinnerPosition(R.array.action_types_array, getString(R.string.action_add_event_label));
            etAddEventTitle.setText(existingBundle.getString(BUNDLE_KEY_ADD_EVENT_TITLE, ""));
            etAddEventDescription.setText(existingBundle.getString(BUNDLE_KEY_ADD_EVENT_DESCRIPTION, ""));
            etAddEventLocation.setText(existingBundle.getString(BUNDLE_KEY_ADD_EVENT_LOCATION, ""));
            etAddEventStartTimeOffset.setText(existingBundle.getString(BUNDLE_KEY_ADD_EVENT_START_TIME_OFFSET, "60"));
            etAddEventDuration.setText(existingBundle.getString(BUNDLE_KEY_ADD_EVENT_DURATION, "30"));
        }
        spinnerActionType.setSelection(spinnerPosition);
        updateUiVisibility(spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "");
        Log.d(TAG, "Configuration loaded for action: " + actionType);
    }

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


    private void saveConfiguration() {
        // Reset errors
        etGetEventsCount.setError(null);
        etGetEventsDaysAhead.setError(null);
        etAddEventTitle.setError(null);
        etAddEventStartTimeOffset.setError(null);
        etAddEventDuration.setError(null);

        final Intent resultIntent = new Intent();
        final Bundle resultBundle = new Bundle();

        String selectedActionLabel = spinnerActionType.getSelectedItem() != null ? spinnerActionType.getSelectedItem().toString() : "";
        String actionTypeConstant = "";
        String blurb = "";

        if (selectedActionLabel.equals(getString(R.string.action_get_events_label))) {
            actionTypeConstant = ACTION_GET_EVENTS;
            String countStr = etGetEventsCount.getText().toString().trim();
            String daysAheadStr = etGetEventsDaysAhead.getText().toString().trim();

            if (TextUtils.isEmpty(countStr) && TextUtils.isEmpty(daysAheadStr)) {
                etGetEventsCount.setError(getString(R.string.error_field_required_one_of_two));
                etGetEventsDaysAhead.setError(getString(R.string.error_field_required_one_of_two));
                Toast.makeText(this, R.string.error_get_events_config_required, Toast.LENGTH_LONG).show();
                return;
            }
            // Further validation for numeric input if desired
            try {
                if (!TextUtils.isEmpty(countStr)) Integer.parseInt(countStr);
                if (!TextUtils.isEmpty(daysAheadStr)) Integer.parseInt(daysAheadStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_number_format, Toast.LENGTH_SHORT).show();
                if (!TextUtils.isEmpty(countStr) && !countStr.matches("\\d+")) etGetEventsCount.setError(getString(R.string.error_invalid_number_format));
                if (!TextUtils.isEmpty(daysAheadStr) && !daysAheadStr.matches("\\d+")) etGetEventsDaysAhead.setError(getString(R.string.error_invalid_number_format));
                return;
            }

            resultBundle.putString(ACTION_TYPE_KEY, actionTypeConstant);
            resultBundle.putString(BUNDLE_KEY_GET_EVENTS_COUNT, countStr);
            resultBundle.putString(BUNDLE_KEY_GET_EVENTS_DAYS_AHEAD, daysAheadStr);
            blurb = getString(R.string.blurb_get_events_prefix);
            if (!TextUtils.isEmpty(countStr)) blurb += countStr + getString(R.string.blurb_get_events_count_suffix);
            if (!TextUtils.isEmpty(daysAheadStr)) blurb += (!TextUtils.isEmpty(countStr) ? ", " : "") + daysAheadStr + getString(R.string.blurb_get_events_days_suffix);

        } else if (selectedActionLabel.equals(getString(R.string.action_add_event_label))) {
            actionTypeConstant = ACTION_ADD_EVENT;
            String title = etAddEventTitle.getText().toString().trim();
            String startTimeOffsetStr = etAddEventStartTimeOffset.getText().toString().trim();
            String durationStr = etAddEventDuration.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etAddEventTitle.setError(getString(R.string.error_field_required));
                Toast.makeText(this, R.string.error_add_event_title_required, Toast.LENGTH_SHORT).show();
                return;
            }
             try {
                if (!TextUtils.isEmpty(startTimeOffsetStr)) Integer.parseInt(startTimeOffsetStr); else etAddEventStartTimeOffset.setText("60"); // Default if empty
                if (!TextUtils.isEmpty(durationStr)) Integer.parseInt(durationStr); else etAddEventDuration.setText("30"); // Default if empty
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_number_format, Toast.LENGTH_SHORT).show();
                if (!TextUtils.isEmpty(startTimeOffsetStr) && !startTimeOffsetStr.matches("\\d*")) etAddEventStartTimeOffset.setError(getString(R.string.error_invalid_number_format));
                if (!TextUtils.isEmpty(durationStr) && !durationStr.matches("\\d*")) etAddEventDuration.setError(getString(R.string.error_invalid_number_format));
                return;
            }


            resultBundle.putString(ACTION_TYPE_KEY, actionTypeConstant);
            resultBundle.putString(BUNDLE_KEY_ADD_EVENT_TITLE, title);
            resultBundle.putString(BUNDLE_KEY_ADD_EVENT_DESCRIPTION, etAddEventDescription.getText().toString().trim());
            resultBundle.putString(BUNDLE_KEY_ADD_EVENT_LOCATION, etAddEventLocation.getText().toString().trim());
            resultBundle.putString(BUNDLE_KEY_ADD_EVENT_START_TIME_OFFSET, etAddEventStartTimeOffset.getText().toString());
            resultBundle.putString(BUNDLE_KEY_ADD_EVENT_DURATION, etAddEventDuration.getText().toString());
            blurb = getString(R.string.blurb_add_event_prefix) + title;
        } else {
             String oldConfigText = "";
             if (editTextConfig != null && editTextConfig.getVisibility() == View.VISIBLE) {
                oldConfigText = editTextConfig.getText().toString().trim();
             }
             if (!TextUtils.isEmpty(oldConfigText)) {
                 resultBundle.putString(BUNDLE_KEY_CONFIG_DATA, oldConfigText);
                 blurb = getString(R.string.blurb_legacy_prefix) + oldConfigText;
                 Log.w(TAG, "Saving legacy configuration: " + oldConfigText);
             } else if (spinnerActionType.isEnabled()){ // Only error if new UI was supposed to be used
                Toast.makeText(this, R.string.error_invalid_action, Toast.LENGTH_SHORT).show();
                return;
             } else { // No valid new or old config to save
                 Toast.makeText(this, R.string.error_no_config_to_save, Toast.LENGTH_SHORT).show();
                 return;
             }
        }

        resultIntent.putExtra(LocaleIntent.EXTRA_BUNDLE, resultBundle);
        resultIntent.putExtra(LocaleIntent.EXTRA_STRING_BLURB, blurb);

        setResult(RESULT_OK, resultIntent);
        isCancelled = false;
        Log.d(TAG, "Configuration saved. Action: " + actionTypeConstant + ", Blurb: " + blurb);
        finish();
    }
    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed. isCancelled: " + isCancelled);
        super.onBackPressed(); // This will call finish if not overridden further.
    }

    @Override
    public void finish() {
        if (isCancelled) {
            setResult(RESULT_CANCELED);
            Log.d(TAG, "Finishing with RESULT_CANCELED");
        }
        super.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestCode " + requestCode);

        if (CalendarPermissionHelper.handlePermissionResult(requestCode, permissions, grantResults)) {
            Log.d(TAG, "Calendar permissions GRANTED.");
            tvPermissionStatus.setVisibility(View.GONE);
            enableUiComponents(true);
            loadConfiguration(); // Reload/setup UI now that we have permissions
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
