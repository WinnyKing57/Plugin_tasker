package com.winnyking.wincalendar.tasker.helloworld;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput;
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput;
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult;
import kotlin.Unit; // Import for Unit

// Ensure HelloWorldInput is imported if not in the same exact package, though it is.
// import com.winnyking.wincalendar.tasker.helloworld.HelloWorldInput;

public class HelloWorldRunner implements TaskerPluginRunnerActionNoOutput<HelloWorldInput> {
    @Override
    // Ensure TaskerPluginResult<Unit> is the return type for NoOutput
    public TaskerPluginResult<Unit> run(Context context, TaskerInput<HelloWorldInput> input) {
        String message = "No message provided (WinCalendar).";
        // Check input.getInput() for the actual structured input
        if (input != null && input.getInput() != null && input.getInput().message != null && !input.getInput().message.isEmpty()) {
            message = input.getInput().message;
        }
        Log.d("HelloWorldRunner", "Message from Tasker: " + message);

        final String finalMessage = message;
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "WinCalendar HelloWorld: " + finalMessage, Toast.LENGTH_LONG).show()
        );

        // For NoOutput, we return TaskerPluginResult.Success with null or Unit.INSTANCE if needed by generic type.
        // The library's TaskerPluginResultAction.Success() handles this.
        return new TaskerPluginResult.Success<>(null);
    }
}
