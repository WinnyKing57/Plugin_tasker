package com.joaomgcd.taskerpluginlibrary.config

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.R
import com.joaomgcd.taskerpluginlibrary.Serializable
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner

interface TaskerPluginConfig<TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> : Serializable {
    val context: Context
    val name: String get() = activityClass.simpleName
    val activityClass: Class<out Activity>
    val activityFullName get() = activityClass.name
    val runnerClass: Class<TActionRunner>?
    val relevantVariables: List<String>? get() = context.getOutputVariables(output, true).map { it.name }
    val output: TaskerPluginOutput<TOutput>? get() = null
    val inputClass: Class<TInput>
    val outputClass: Class<TOutput>? get() = null
    val passThroughData: Bundle? get() = null
    val versionCode: Int get() = 1
    val minimumTaskerVersion: String? get() = TaskerPluginConstants.DEFAULT_MINIMUM_TASKER_VERSION
    val targetTaskerVersion: String? get() = null
    val helpUrl: String? get() = null
    val requestedPermissions: List<String>? get() = null
    val defaultInput: TInput? get() = null
    fun getBlurb(context: Context, input: TInput?): String = name
    val destroyOnFinish: Boolean get() = false
    val isEvent get() = false
    val variableNamerHelper: TaskerOutputVariable.NamerHelper? get() = null
    val variablesPrefix: String? get() = null
    val authority get() = "${context.packageName}.provider"
    val runner: TActionRunner? get() = runnerClass?.newInstance()
    val hostCaps get() = object : HostCapabilities { override val hostInfo = HostInfo(context, null) }
    fun getTaskerInputFromIntent(intentBundle: Bundle) = TaskerInput.fromJson(intentBundle.getString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON)!!, inputClass)!!
}
