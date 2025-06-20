package com.winnyking.wincalendar.tasker.helloworld;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig;
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoInputOrOutput; // Adjusted
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput;
import com.winnyking.wincalendar.R; // Correct R file

public class HelloWorldActivity extends Activity { // Standard Activity, not implementing TaskerPluginConfig directly

    private EditText editTextMessage;
    private Button buttonSave;
    private TaskerPluginConfigHelperNoInputOrOutput<HelloWorldInput, HelloWorldRunner> helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSave = findViewById(R.id.buttonSave);

        // Define a TaskerPluginConfig instance for the helper
        TaskerPluginConfig<HelloWorldInput, Void, HelloWorldRunner> config = new TaskerPluginConfig<HelloWorldInput, Void, HelloWorldRunner>() {
            @Override
            public Context getContext() {
                return HelloWorldActivity.this.getApplicationContext();
            }

            @Override
            public Class<HelloWorldActivity> getActivityClass() { // Return this activity's class
                return HelloWorldActivity.class;
            }

            @Override
            public Class<HelloWorldRunner> getRunnerClass() {
                return HelloWorldRunner.class;
            }

            @Override
            public Class<HelloWorldInput> getInputClass() {
                return HelloWorldInput.class;
            }

            @Override
            public Class<Void> getOutputClass() { // For NoOutput runner
                return Void.class;
            }

            @Override
            public String getHelpUrl() { return null; }

            @Override
            public String getBlurb(Context context, HelloWorldInput input) { // Added context param
                return input != null && input.message != null && !input.message.isEmpty() ?
                       "Logs: " + input.message :
                       getString(R.string.hello_world_blurb); // Use string resource
            }
        };

        helper = new TaskerPluginConfigHelperNoInputOrOutput<HelloWorldInput, HelloWorldRunner>(this, config) {
            @Override
            public void assignValues(TaskerInput<HelloWorldInput> input) { // Corrected signature
                 if (input != null && input.getInput() != null && input.getInput().message != null) {
                    editTextMessage.setText(input.getInput().message);
                }
            }

            @Override
            public TaskerInput<HelloWorldInput> getInput() { // Corrected signature
                // Construct TaskerInput with all three parts, even if some are null/default
                return new TaskerInput<>(
                        new TaskerInput.Connection(getRunnerClass().getName(), "runner", null), // Provide basic connection info
                        null, // No regular variables for this simple case
                        new HelloWorldInput(editTextMessage.getText().toString())
                );
            }
        };

        helper.onCreate();

        buttonSave.setOnClickListener(v -> helper.finishForTasker());
    }
}
