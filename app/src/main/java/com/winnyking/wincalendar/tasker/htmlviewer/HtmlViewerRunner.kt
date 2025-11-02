package com.winnyking.wincalendar.tasker.htmlviewer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunner
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariables
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class HtmlViewerRunner : TaskerPluginRunner<HtmlViewerInput, TaskerOutputVariables>() {
    override fun run(context: Context, input: TaskerInput<HtmlViewerInput>): TaskerPluginResult<TaskerOutputVariables> {
        val viewerInput = input.regular ?: return TaskerPluginResultError(IllegalArgumentException("Input cannot be null"))
        var html = viewerInput.htmlContent ?: viewerInput.code ?: ""

        val matcher = Pattern.compile("%([a-zA-Z0-9_]+)").matcher(html)
        while (matcher.find()) {
            val variableName = matcher.group(1)
            val variableValue = input.getVariableValue(variableName)
            if (variableValue != null) {
                html = html.replace("%$variableName", variableValue)
            }
        }

        val latch = CountDownLatch(1)
        val result = StringBuilder()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (WebViewActivity.ACTION_FINISH == intent.action) {
                    result.append(intent.getStringExtra(WebViewActivity.EXTRA_VARIABLE_VALUES))
                    latch.countDown()
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(WebViewActivity.ACTION_FINISH))

        val intent = Intent(context, WebViewActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("html", html)
        }

        context.startActivity(intent)

        try {
            if (!latch.await(5, TimeUnit.MINUTES)) {
                // Handle timeout
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return TaskerPluginResultError(e)
        } finally {
            context.unregisterReceiver(receiver)
        }

        val variables = TaskerOutputVariables()
        try {
            val json = JSONObject(result.toString())
            for (key in json.keys()) {
                variables.add(TaskerOutputVariable(key, json.getString(key), json.getString(key)))
            }
        } catch (e: Exception) {
            // result is not a valid JSON, so we can't extract variables
        }

        return TaskerPluginResultSucess(variables)
    }
}
