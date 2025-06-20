package com.joaomgcd.taskerpluginlibrary.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.R
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.TaskerPluginUtil
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner
import com.joaomgcd.taskerpluginlibrary.log
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutput
import com.joaomgcd.taskerpluginlibrary.output.runner.TaskerPluginOutputForRunner


abstract class TaskerPluginConfigHelper<TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>>(val activity: Activity, private val config: TaskerPluginConfig<TInput, TOutput, TActionRunner>) {
    val context: Context get() = activity.applicationContext
    val hostInfo by lazy { HostInfo(context, activity.intent.getStringExtra("net.dinglisch.android.tasker.extras.HOST_PACKAGE")) }
    val hostCaps by lazy {
        object : HostCapabilities {
            override val hostInfo = this@TaskerPluginConfigHelper.hostInfo
        }
    }

    val inputForConfig: TInput? by lazy { TaskerPluginUtil.getTaskerInputFromIntent(activity.intent, config.inputClass) }
    val passThroughDataFromHost: Bundle? by lazy { TaskerPluginUtil.getPassingThroughMessage(context, activity.intent) }

    fun assignFromInput(input: TInput?) = log { if (input == null) Unit else assignValues(input) }
    protected abstract fun assignValues(input: TInput)
    protected abstract fun getInput(): TaskerInputRoot
    protected open fun getOutput(): TaskerPluginOutputForRunner<TOutput>? = null
    protected open fun getRelevantVariables(): List<String>? = config.relevantVariables
    protected open fun getPassThroughData(): Bundle? = config.passThroughData
    protected open fun getPermissionsGranted(permissionsGranted: List<String>) {}
    protected open fun getPermissionsDenied(permissionsDenied: List<String>) {}

    init {
        assignFromInput(inputForConfig)
    }

    fun finishForTasker() = log {
        val currentInput = getInput()
        val inputForTasker = TaskerInput.fromJson(currentInput.toString(), config.inputClass)!!.input //TODO: this is not very efficient
        TaskerPluginUtil.finishForTasker(inputForTasker, getOutput(), activity, config, getPassThroughData(), getRelevantVariables())
    }


    private fun TaskerPluginUtil.finishForTasker(input: TInput, output: TaskerPluginOutputForRunner<TOutput>?, activity: Activity, config: TaskerPluginConfig<TInput, TOutput, TActionRunner>, passThroughData: Bundle?, relevantVariables: List<String>?) {
        val resultBundle = Bundle()
        val inputJson = TaskerInput.toJson(input, config.runnerClass)
        resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON, inputJson)
        resultBundle.putInt(TaskerPluginConstants.BUNDLE_KEY_VERSION_CODE, config.versionCode)
        resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_RUNNER_CLASS_NAME, config.runnerClass?.name)
        passThroughData?.let { resultBundle.putBundle(TaskerPluginConstants.BUNDLE_KEY_PASS_THROUGH_DATA, it) }
        config.minimumTaskerVersion?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_MINIMUM_TASKER_VERSION, it) }
        config.targetTaskerVersion?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_TARGET_TASKER_VERSION, it) }
        config.activityFullName?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_ACTIVITY_FULL_CLASS_NAME, it) }
        config.helpUrl?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_HELP_URL, it) }
        config.requestedPermissions?.let { resultBundle.putStringArray(TaskerPluginConstants.BUNDLE_KEY_REQUESTED_PERMISSIONS, it.toTypedArray()) }
        relevantVariables?.let { resultBundle.putStringArrayList(TaskerPluginConstants.EXTRA_RELEVANT_VARIABLES, ArrayList(it)) }
        config.variableNamerHelper?.let { resultBundle.putParcelable(TaskerOutputVariable.BUNDLE_KEY_NAMER_HELPER, it) }
        config.variablesPrefix?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_VARIABLES_PREFIX, it) }


        val resultIntent = Intent()
        val blurb = output?.getBlurb(activity, input) ?: config.getBlurb(activity, input)
        resultIntent.putExtra(TaskerPluginConstants.EXTRA_STRING_BLURB, blurb)
        resultIntent.putExtra(TaskerPluginConstants.EXTRA_BUNDLE, resultBundle)

        output?.addOutputToBundle(context, resultBundle)

        activity.setResult(Activity.RESULT_OK, resultIntent)
        activity.finish()
    }

    fun requestPermissionsIfNeeded() = log {
        val neededPermissions = config.requestedPermissions ?: return@log false
        TaskerPluginUtil.requestPermissionsIfNeeded(activity, neededPermissions)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == TaskerPluginUtil.REQUEST_CODE_PERMISSION) {
            val (granted, denied) = permissions.zip(grantResults.toTypedArray()).partition { it.second == android.content.pm.PackageManager.PERMISSION_GRANTED }
            getPermissionsGranted(granted.map { it.first })
            getPermissionsDenied(denied.map { it.first })
        }
    }
}

private fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> TaskerPluginConfig<TInput, TOutput, TActionRunner>.getOutputVariables(context: Context): List<TaskerOutputVariable> {
    val variables = mutableListOf<TaskerOutputVariable>()
    output?.getOutputVariables(context)?.let { variables.addAll(it) }
    variables.add(TaskerOutputVariable(TaskerPluginConstants.VARIABLE_TASKER_ERROR_CODE, context.getString(R.string.error_code), context.getString(R.string.error_code_description)))
    variables.add(TaskerOutputVariable(TaskerPluginConstants.VARIABLE_TASKER_ERROR_MESSAGE, context.getString(R.string.error_message), context.getString(R.string.error_message_description)))
    return variables
}
