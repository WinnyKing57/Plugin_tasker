package com.joaomgcd.taskerpluginlibrary.action

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ACTION_ID_KEY
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_KEY
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_REPLACE_VARIABLES_KEY
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_VARIABLES_KEY
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.extensions.requestQueryOrThrow
import com.joaomgcd.taskerpluginlibrary.getTaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.IntentServiceParallel
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


private fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunnerAction<TInput, TOutput>> BroadcastReceiver.finishAction(context: Context, intent: Intent, helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>, result: TaskerPluginResultAction?) {
    if (result == null) return
    val actionId = result.actionId ?: return
    val resultCode = if (result.isSuccessful) Activity.RESULT_OK else Activity.RESULT_CANCELED
    val resultBundle = Bundle()
    resultBundle.putBoolean(TASKER_MESSAGE_RESPONSE_REPLACE_VARIABLES_KEY, result.replaceVariables)
    result.variablesBundle?.let { resultBundle.putBundle(TASKER_MESSAGE_RESPONSE_VARIABLES_KEY, it) }

    if (!result.isSuccessful) {
        val errorBundle = Bundle()
        errorBundle.putInt(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_CODE_KEY, result.errorCode ?: 0)
        errorBundle.putString(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_MESSAGE_KEY, result.errorMessage)
        resultBundle.putBundle(TASKER_MESSAGE_RESPONSE_ERROR_KEY, errorBundle)
    }

    resultBundle.putString(TASKER_MESSAGE_RESPONSE_ACTION_ID_KEY, actionId)
    setResult(resultCode, null, resultBundle)
}


open class BroadcastReceiverAction<TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunnerAction<TInput, TOutput>>(private val helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>) : BroadcastReceiver() {
    private val scope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val pendingResult = goAsync()
        val taskerInput = intent.getTaskerInput(context, helper)
        val actionId = intent.getStringExtra(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ACTION_ID_KEY) ?: return
        scope.launch {
            val result = try {
                context.requestQueryOrThrow(helper,taskerInput?.input) as TaskerPluginResultAction
            } catch (e: Throwable) {
                TaskerPluginResultAction.Failure(e)
            }
            result.actionId = actionId
            finishAction(context, intent, helper, result)
            pendingResult.finish()
            if (helper.destroyOnFinish) {
                scope.cancel()
            }
        }
    }
}

abstract class BroadcastReceiverActionTasker<TInput : Any, TOutput : Any> : BroadcastReceiver() {
    protected abstract fun getConfig(context: Context, taskerInput: com.joaomgcd.taskerpluginlibrary.input.TaskerInput<TInput>?): TaskerPluginConfig<TInput, TOutput, TaskerPluginRunnerAction<TInput, TOutput>>?
    private val scope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val pendingResult = goAsync()
        val taskerInput = intent.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT)?.getTaskerInput(context)
        val config = getConfig(context, taskerInput) ?: return
        val actionId = intent.getStringExtra(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ACTION_ID_KEY) ?: return

        scope.launch {
            val result = try {
                context.requestQueryOrThrow(config,taskerInput?.input) as TaskerPluginResultAction
            } catch (e: Throwable) {
                TaskerPluginResultAction.Failure(e)
            }
            result.actionId = actionId
            finishAction(context, intent, config, result)
            pendingResult.finish()
            if (config.destroyOnFinish) {
                scope.cancel()
            }
        }
    }
}

/**Only use this if you know what you're doing. This will make the action run in a foreground service. Useful for actions that take a long time to complete.*/
abstract class BroadcastReceiverActionForeground<TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunnerAction<TInput, TOutput>>(private val helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        intent.putExtra(TaskerPluginConstants.EXTRA_CONFIG, helper)
        intent.putExtra(TaskerPluginConstants.EXTRA_REQUEST_TYPE, TaskerPluginResult.Type.Action)
        IntentServiceParallel.enqueueWork(context, intent)
    }
}

/**Only use this if you know what you're doing. This will make the action run in a foreground service. Useful for actions that take a long time to complete.*/
abstract class BroadcastReceiverActionTaskerForeground<TInput : Any, TOutput : Any> : BroadcastReceiver() {
    protected abstract fun getConfig(context: Context, taskerInput: com.joaomgcd.taskerpluginlibrary.input.TaskerInput<TInput>?): TaskerPluginConfig<TInput, TOutput, TaskerPluginRunnerAction<TInput, TOutput>>?

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val taskerInput = intent.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT)?.getTaskerInput(context)
        val config = getConfig(context, taskerInput) ?: return
        intent.putExtra(TaskerPluginConstants.EXTRA_CONFIG, config)
        intent.putExtra(TaskerPluginConstants.EXTRA_REQUEST_TYPE, TaskerPluginResult.Type.Action)
        IntentServiceParallel.enqueueWork(context, intent)
    }
}
