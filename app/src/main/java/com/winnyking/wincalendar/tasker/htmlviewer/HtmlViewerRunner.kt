package com.winnyking.wincalendar.tasker.htmlviewer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class HtmlViewerRunner : TaskerPluginRunnerAction<HtmlViewerInput, String>() {
    override fun run(context: Context, input: TaskerInput<HtmlViewerInput>): TaskerPluginResult<String> {
        val viewerInput = input.regular ?: return TaskerPluginResultError(IllegalArgumentException("Input cannot be null"))
        val html = viewerInput.htmlContent ?: viewerInput.code ?: ""

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

        return TaskerPluginResultSucess(result.toString())
    }
}
