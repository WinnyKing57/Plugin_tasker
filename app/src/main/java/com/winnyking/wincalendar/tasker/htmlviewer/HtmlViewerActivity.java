package com.winnyking.wincalendar.tasker.htmlviewer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig;
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper;
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput;
import com.winnyking.wincalendar.R;

public class HtmlViewerActivity extends Activity {

    private EditText editTextHtmlContent;
    private EditText editTextCssContent;
    private EditText editTextJsContent;
    private EditText editTextTaskerVariables;
    private Button buttonSave;
    private TaskerPluginConfigHelper<HtmlViewerInput, String, HtmlViewerRunner> helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_viewer);

        editTextHtmlContent = findViewById(R.id.editTextHtmlContent);
        editTextCssContent = findViewById(R.id.editTextCssContent);
        editTextJsContent = findViewById(R.id.editTextJsContent);
        editTextTaskerVariables = findViewById(R.id.editTextTaskerVariables);
        buttonSave = findViewById(R.id.buttonSave);

        TaskerPluginConfig<HtmlViewerInput, String, HtmlViewerRunner> config = new TaskerPluginConfig<HtmlViewerInput, String, HtmlViewerRunner>() {
            @Override
            public Context getContext() {
                return HtmlViewerActivity.this.getApplicationContext();
            }

            @Override
            public Class<HtmlViewerActivity> getActivityClass() {
                return HtmlViewerActivity.class;
            }

            @Override
            public Class<HtmlViewerRunner> getRunnerClass() {
                return HtmlViewerRunner.class;
            }

            @Override
            public Class<HtmlViewerInput> getInputClass() {
                return HtmlViewerInput.class;
            }

            @Override
            public Class<String> getOutputClass() {
                return String.class;
            }

            @Override
            public String getHelpUrl() { return null; }

            @Override
            public String getBlurb(Context context, HtmlViewerInput input) {
                if (input != null && input.htmlContent != null && !input.htmlContent.isEmpty()) {
                    return "Displaying custom HTML";
                }
                return getString(R.string.html_viewer_blurb);
            }
        };

        helper = new TaskerPluginConfigHelper<HtmlViewerInput, String, HtmlViewerRunner>(this, config) {
            @Override
            public void assignValues(TaskerInput<HtmlViewerInput> input) {
                if (input != null && input.getInput() != null) {
                    HtmlViewerInput viewerInput = input.getInput();
                    editTextHtmlContent.setText(viewerInput.htmlContent);
                    editTextCssContent.setText(viewerInput.cssContent);
                    editTextJsContent.setText(viewerInput.jsContent);
                    editTextTaskerVariables.setText(viewerInput.taskerVariables);
                }
            }

            @Override
            public TaskerInput<HtmlViewerInput> getInput() {
                return new TaskerInput<>(
                        new TaskerInput.Connection(getRunnerClass().getName(), "runner", null),
                        null,
                        new HtmlViewerInput(
                                editTextHtmlContent.getText().toString(),
                                editTextCssContent.getText().toString(),
                                editTextJsContent.getText().toString(),
                                editTextTaskerVariables.getText().toString()
                        )
                );
            }
        };

        helper.onCreate();

        buttonSave.setOnClickListener(v -> helper.finishForTasker());
    }
}
