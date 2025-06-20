package com.winnyking.wincalendar.tasker.helloworld;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle; // Added for Bundle
import android.widget.Button;
import android.widget.EditText;

// Assuming TaskerPluginConfigHelperNoOutputOrInputOrUpdate is a valid helper or will be created.
// If not, this might need to be TaskerPluginConfigHelperNoInputOrOutput or similar from the library.
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoInputOrOutput;
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput; // Keep this
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants; // For EXTRA_BUNDLE etc.
import android.content.Intent; // For Intent


import com.winnyking.wincalendar.R;
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig;


public class HelloWorldActivity extends Activity {

    private EditText editTextMessage;
    private Button buttonSave;
    // Using TaskerPluginConfigHelperNoInputOrOutput as a placeholder for the described NoOutputOrInputOrUpdate
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
            public Class<HelloWorldActivity> getActivityClass() {
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
            public Class<Void> getOutputClass() {
                return Void.class; // For NoOutput
            }

            @Override
            public String getHelpUrl() { return null; } // Optional

            @Override
            public String getBlurb(HelloWorldInput input) {
                return input != null && input.message != null ? "Message: " + input.message : "Hello World!";
            }
        };

        helper = new TaskerPluginConfigHelperNoInputOrOutput<HelloWorldInput, HelloWorldRunner>(this, config) {
            @Override
            public void assignValues(TaskerInput<HelloWorldInput> input) {
                 if (input != null && input.getInput() != null && input.getInput().message != null) {
                    editTextMessage.setText(input.getInput().message);
                }
            }

            @Override
            public TaskerInput<HelloWorldInput> getInput() {
                return new TaskerInput<>(null, null, new HelloWorldInput(editTextMessage.getText().toString()));
            }
        };

        helper.onCreate(); // Call onCreate for the helper

        buttonSave.setOnClickListener(v -> helper.finishForTasker());
    }
}
