package com.winnyking.wincalendar.tasker.helloworld;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput;
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput;
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult;
// import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess; // Corrected to TaskerPluginResult.Success
import kotlin.Unit; // Import for Unit

public class HelloWorldRunner implements TaskerPluginRunnerActionNoOutput<HelloWorldInput> { // Specify implements
    @Override
    public TaskerPluginResult<Unit> run(Context context, TaskerInput<HelloWorldInput> input) {
        String message = "No message provided (WinCalendar).";
        if (input != null && input.getInput() != null && input.getInput().message != null && !input.getInput().message.isEmpty()) {
            message = input.getInput().message;
        }
        Log.d("HelloWorldRunner", "Message from Tasker: " + message);

        final String finalMessage = message;
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "WinCalendar HelloWorld: " + finalMessage, Toast.LENGTH_LONG).show()
        );

        return new TaskerPluginResult.Success<>(null); // Use Unit for NoOutput, wrapped in Success
    }
}
