package com.winnyking.wincalendar.tasker.htmlviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction;
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput;
import com.joaomgcd.taskerpluginlibrary.runner.TaskerExecutionResult;
import com.joaomgcd.taskerpluginlibrary.runner.TaskerExecutionResultError;
import com.joaomgcd.taskerpluginlibrary.runner.TaskerExecutionResultSuccess;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HtmlViewerRunner extends TaskerPluginRunnerAction<HtmlViewerInput, String> {
    @Override
    public TaskerExecutionResult<String> run(Context context, TaskerInput<HtmlViewerInput> input) {
        HtmlViewerInput viewerInput = input.getInput();
        if (viewerInput == null) {
            return new TaskerExecutionResultError(new Throwable("Input cannot be null"));
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder result = new StringBuilder();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WebViewActivity.ACTION_FINISH.equals(intent.getAction())) {
                    result.append(intent.getStringExtra(WebViewActivity.EXTRA_VARIABLE_VALUES));
                    latch.countDown();
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(WebViewActivity.ACTION_FINISH));

        Intent intent = new Intent(context, WebViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("html_content", viewerInput.htmlContent);
        intent.putExtra("css_content", viewerInput.cssContent);
        intent.putExtra("js_content", viewerInput.jsContent);
        intent.putExtra("tasker_variables", viewerInput.taskerVariables);

        context.startActivity(intent);

        try {
            // Wait for a reasonable amount of time for the user to interact with the WebView
            latch.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            return new TaskerExecutionResultError(e);
        } finally {
            context.unregisterReceiver(receiver);
        }

        return new TaskerExecutionResultSuccess(result.toString());
    }
}
