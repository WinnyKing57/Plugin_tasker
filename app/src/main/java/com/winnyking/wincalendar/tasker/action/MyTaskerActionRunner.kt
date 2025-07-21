package com.winnyking.wincalendar.tasker.action

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerExecutionContext
import com.joaomgcd.taskerpluginlibrary.runner.TaskerExecutionResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerExecutionResultSuccess

class MyTaskerActionRunner : TaskerPluginRunnerAction<String, String>() {
    override fun run(context: Context, input: TaskerInput<String>): TaskerExecutionResult<String> {
        // This is where you would put your plugin's logic.
        // You can access any input fields defined in your configuration activity here.
        // For this example, we'll just return a success message with the current time.
        val currentTime = System.currentTimeMillis().toString()
        return TaskerExecutionResultSuccess("Hello from the plugin at $currentTime!")
    }
}
