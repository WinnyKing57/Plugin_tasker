package com.winnyking.wincalendar.tasker.htmlviewer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSuccess
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HtmlViewerRunner : TaskerPluginRunnerAction<HtmlViewerInput, String>() {
    override fun run(context: Context, input: TaskerInput<HtmlViewerInput>): TaskerPluginResult<String> {
        val viewerInput = input.regular
        if (viewerInput == null) {
            return TaskerPluginResultError(Throwable("Input cannot be null"))
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
            putExtra("html_content", viewerInput.htmlContent)
            putExtra("css_content", viewerInput.cssContent)
            putExtra("js_content", viewerInput.jsContent)
            putExtra("tasker_variables", viewerInput.taskerVariables)
        }

        context.startActivity(intent)

        try {
            latch.await(5, TimeUnit.MINUTES)
        } catch (e: InterruptedException) {
            return TaskerPluginResultError(e)
        } finally {
            context.unregisterReceiver(receiver)
        }

        return TaskerPluginResultSuccess(result.toString())
    }
}
