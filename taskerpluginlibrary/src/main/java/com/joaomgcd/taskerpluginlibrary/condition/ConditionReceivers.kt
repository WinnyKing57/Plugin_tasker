package com.joaomgcd.taskerpluginlibrary.condition

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.extensions.requestQueryOrThrow
import com.joaomgcd.taskerpluginlibrary.getTaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.IntentServiceParallel
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


private fun <TInput : Any, TOutput : Any, TConditionRunner : TaskerPluginRunnerCondition<TInput, TOutput>> BroadcastReceiver.finishCondition(context: Context, intent: Intent, helper: TaskerPluginConfig<TInput, TOutput, TConditionRunner>, result: TaskerPluginResultCondition?) {
    if (result == null) return

    val satisfy = result.state == TaskerPluginResultConditionState.SATISFIED
    val resultCode = if (satisfy) Activity.RESULT_OK else Activity.RESULT_CANCELED_CONDITIONS_NOT_SATISFIED
    val resultBundle = Bundle()
    resultBundle.putBoolean(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_REPLACE_VARIABLES_KEY, result.replaceVariables)
    result.variablesBundle?.let { resultBundle.putBundle(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_VARIABLES_KEY, it) }

    setResult(resultCode, null, resultBundle)
}

private const val Activity_RESULT_CANCELED_CONDITIONS_NOT_SATISFIED = Activity.RESULT_FIRST_USER

open class BroadcastReceiverCondition<TInput : Any, TOutput : Any, TConditionRunner : TaskerPluginRunnerCondition<TInput, TOutput>>(private val helper: TaskerPluginConfig<TInput, TOutput, TConditionRunner>) : BroadcastReceiver() {
    private val scope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val pendingResult = goAsync()
        val taskerInput = intent.getTaskerInput(context, helper)

        scope.launch {
            val result = try {
                context.requestQueryOrThrow(helper,taskerInput?.input) as TaskerPluginResultCondition
            } catch (e: Throwable) {
                TaskerPluginResultCondition(TaskerPluginResultConditionState.UNKNOWN)
            }

            finishCondition(context, intent, helper, result)
            pendingResult.finish()
            if (helper.destroyOnFinish) {
                scope.cancel()
            }
        }
    }
}

abstract class BroadcastReceiverConditionTasker<TInput : Any, TOutput : Any> : BroadcastReceiver() {
    protected abstract fun getConfig(context: Context, taskerInput: com.joaomgcd.taskerpluginlibrary.input.TaskerInput<TInput>?): TaskerPluginConfig<TInput, TOutput, TaskerPluginRunnerCondition<TInput, TOutput>>?
    private val scope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val pendingResult = goAsync()
        val taskerInput = intent.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT)?.getTaskerInput(context)
        val config = getConfig(context, taskerInput) ?: return

        scope.launch {
            val result = try {
                context.requestQueryOrThrow(config,taskerInput?.input) as TaskerPluginResultCondition
            } catch (e: Throwable) {
                TaskerPluginResultCondition(TaskerPluginResultConditionState.UNKNOWN)
            }
            finishCondition(context, intent, config, result)
            pendingResult.finish()
            if (config.destroyOnFinish) {
                scope.cancel()
            }
        }
    }
}


/**Only use this if you know what you're doing. This will make the condition run in a foreground service. Useful for conditions that take a long time to complete.*/
abstract class BroadcastReceiverConditionForeground<TInput : Any, TOutput : Any, TConditionRunner : TaskerPluginRunnerCondition<TInput, TOutput>>(private val helper: TaskerPluginConfig<TInput, TOutput, TConditionRunner>) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        intent.putExtra(TaskerPluginConstants.EXTRA_CONFIG, helper)
        intent.putExtra(TaskerPluginConstants.EXTRA_REQUEST_TYPE, TaskerPluginResult.Type.Condition)
        IntentServiceParallel.enqueueWork(context, intent)
    }
}

/**Only use this if you know what you're doing. This will make the condition run in a foreground service. Useful for conditions that take a long time to complete.*/
abstract class BroadcastReceiverConditionTaskerForeground<TInput : Any, TOutput : Any> : BroadcastReceiver() {
    protected abstract fun getConfig(context: Context, taskerInput: com.joaomgcd.taskerpluginlibrary.input.TaskerInput<TInput>?): TaskerPluginConfig<TInput, TOutput, TaskerPluginRunnerCondition<TInput, TOutput>>?

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val taskerInput = intent.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT)?.getTaskerInput(context)
        val config = getConfig(context, taskerInput) ?: return
        intent.putExtra(TaskerPluginConstants.EXTRA_CONFIG, config)
        intent.putExtra(TaskerPluginConstants.EXTRA_REQUEST_TYPE, TaskerPluginResult.Type.Condition)
        IntentServiceParallel.enqueueWork(context, intent)
    }
}
