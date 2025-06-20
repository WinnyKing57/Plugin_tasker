package com.joaomgcd.taskerpluginlibrary

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerCondition
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.extensions.requestQueryOrThrow
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutput
import com.joaomgcd.taskerpluginlibrary.output.runner.TaskerPluginOutputForRunner
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultAction
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionState
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable


const val TASKER_PLUGIN_LOG_FUNCTIONS = false
inline fun <T> Any.log(function: () -> T): T {
    val result = function()
    if (TASKER_PLUGIN_LOG_FUNCTIONS) {
        val thisName = if (this is TaskerPluginConfig<*, *, *>) this.javaClass.simpleName else this
        println("TaskerPlugin-$thisName: result: $result")
    }
    return result
}


internal fun Bundle?.getTaskerInput(context: Context): TaskerInput<Any>? = this?.let { bundle ->
    bundle.classLoader = context.classLoader
    bundle.getBundle(TaskerPluginConstants.EXTRA_BUNDLE)?.apply { classLoader = context.classLoader }?.getSerializable(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON)?.let { it as TaskerInput<Any> }
}


internal fun <TInput : Any> Bundle?.getTaskerInputTyped(context: Context): TaskerInput<TInput>? = getTaskerInput(context)?.let { TaskerInput(it.connection, it.regular, it.input as TInput) }


internal fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunnerAction<TInput, TOutput>> Intent?.getTaskerInput(context: Context, helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>) = this?.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT)?.getTaskerInputTyped<TInput>(context)
internal fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunnerCondition<TInput, TOutput>> Intent?.getTaskerInput(context: Context, helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>) = this?.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT)?.getTaskerInputTyped<TInput>(context)


@TaskerPluginAPINotLowLevel
fun <TInput : Any> TaskerInputRoot.getTaskerInput(inputClass: Class<TInput>): TInput? {
    return TaskerInput.fromJson(this.toString(), inputClass)?.input
}


fun <TInput : Any> TaskerInputRoot?.getTaskerInputOrThrow(inputClass: Class<TInput>, errorMessage: String? = null): TInput {
    return this?.getTaskerInput(inputClass) ?: throw Exception(errorMessage ?: "Couldn't get input")
}


fun Context.getOutputVariables(output: TaskerPluginOutput<*>?, addDefaultErrorVariables: Boolean = false): List<TaskerOutputVariable> {
    val variables = mutableListOf<TaskerOutputVariable>()
    output?.getOutputVariables(this)?.let { variables.addAll(it) }
    if (addDefaultErrorVariables) {
        variables.add(TaskerOutputVariable(TaskerPluginConstants.VARIABLE_TASKER_ERROR_CODE, getString(R.string.error_code), getString(R.string.error_code_description)))
        variables.add(TaskerOutputVariable(TaskerPluginConstants.VARIABLE_TASKER_ERROR_MESSAGE, getString(R.string.error_message), getString(R.string.error_message_description)))
    }
    return variables
}

fun Bundle.getPassThroughData(context: Context): Bundle? {
    classLoader = context.classLoader
    return getBundle(TaskerPluginConstants.BUNDLE_KEY_PASS_THROUGH_DATA)
}


object TaskerPluginUtil {
    const val REQUEST_CODE_PERMISSION = 14
    const val EXTRA_DENIED_PERMISSIONS = "denied_permissions"
    const val EXTRA_GRANTED_PERMISSIONS = "granted_permissions"

    @TaskerPluginAPINotLowLevel
    fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> CoroutineScope.runPlugin(context: Context, input: TaskerInput<TInput>?, helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>, requestType: RequestType, resultForTasker: (TaskerPluginResult) -> Unit) = launch {
        val runner = helper.runner ?: return@launch
        val runnerFromInput = input?.connection?.runnerClass?.newInstance() as? TaskerPluginRunner<TInput, TOutput>
        val runnerToUse = runnerFromInput ?: runner
        val result = runnerToUse.run(context, input)
        if (result is TaskerPluginResultAction && result.isSuccessful && helper.passThroughData != null && result.variablesBundle == null) {
            result.variablesBundle = Bundle().apply {
                putBundle(TaskerPluginConstants.BUNDLE_KEY_PASS_THROUGH_DATA, helper.passThroughData)
            }
        }
        resultForTasker(result)
    }


    fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunnerAction<TInput, TOutput>> CoroutineScope.runPluginAction(context: Context, input: TaskerInput<TInput>?, helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>, actionId: String, resultForTasker: (TaskerPluginResultAction) -> Unit) {
        runPlugin(context, input, helper, RequestType.Action) {
            val actionResult = it as TaskerPluginResultAction
            actionResult.actionId = actionId
            resultForTasker(actionResult)
        }
    }

    fun <TInput : Any, TOutput : Any, TConditionRunner : TaskerPluginRunnerCondition<TInput, TOutput>> CoroutineScope.runPluginCondition(context: Context, input: TaskerInput<TInput>?, helper: TaskerPluginConfig<TInput, TOutput, TConditionRunner>, resultForTasker: (TaskerPluginResultCondition) -> Unit) {
        runPlugin(context, input, helper, RequestType.Condition) { resultForTasker(it as TaskerPluginResultCondition) }
    }

    internal fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> Activity.finishForTasker(input: TInput, output: TaskerPluginOutputForRunner<TOutput>?, helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>) {
        val resultBundle = Bundle()
        val inputJson = TaskerInput.toJson(input, helper.runnerClass)
        resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON, inputJson)
        resultBundle.putInt(TaskerPluginConstants.BUNDLE_KEY_VERSION_CODE, helper.versionCode)
        resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_RUNNER_CLASS_NAME, helper.runnerClass?.name)
        helper.passThroughData?.let { resultBundle.putBundle(TaskerPluginConstants.BUNDLE_KEY_PASS_THROUGH_DATA, it) }
        helper.minimumTaskerVersion?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_MINIMUM_TASKER_VERSION, it) }
        helper.targetTaskerVersion?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_TARGET_TASKER_VERSION, it) }
        helper.activityFullName?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_ACTIVITY_FULL_CLASS_NAME, it) }
        helper.helpUrl?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_HELP_URL, it) }
        helper.requestedPermissions?.let { resultBundle.putStringArray(TaskerPluginConstants.BUNDLE_KEY_REQUESTED_PERMISSIONS, it.toTypedArray()) }
        helper.relevantVariables?.let { resultBundle.putStringArrayList(TaskerPluginConstants.EXTRA_RELEVANT_VARIABLES, ArrayList(it)) }
        helper.variableNamerHelper?.let { resultBundle.putParcelable(TaskerOutputVariable.BUNDLE_KEY_NAMER_HELPER, it) }
        helper.variablesPrefix?.let { resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_VARIABLES_PREFIX, it) }
        val resultIntent = Intent()
        val blurb = output?.getBlurb(this, input) ?: helper.getBlurb(this, input)
        resultIntent.putExtra(TaskerPluginConstants.EXTRA_STRING_BLURB, blurb)
        resultIntent.putExtra(TaskerPluginConstants.EXTRA_BUNDLE, resultBundle)
        output?.addOutputToBundle(this, resultBundle)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    fun Activity.requestPermissionsIfNeeded(neededPermissions: List<String>): Boolean {
        val missingPermissions = neededPermissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missingPermissions.isEmpty()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(missingPermissions.toTypedArray(), REQUEST_CODE_PERMISSION)
        }
        return true
    }


    internal fun <TInput : Any> Bundle.getTaskerInputFromBundle(context: Context, inputClass: Class<TInput>): TInput? {
        classLoader = context.classLoader
        return TaskerInput.fromJson(getString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON) ?: return null, inputClass)?.input
    }

    fun <TInput : Any> Intent.getTaskerInputFromIntent(context: Context, inputClass: Class<TInput>): TInput? {
        return getBundleExtra(TaskerPluginConstants.EXTRA_BUNDLE)?.getTaskerInputFromBundle(context, inputClass)
    }

    fun <TInput : Any> Bundle.getTaskerInput(context: Context, inputClass: Class<TInput>): TInput? {
        return getBundle(TaskerPluginConstants.EXTRA_BUNDLE)?.getTaskerInputFromBundle(context, inputClass)
    }

    fun getPassingThroughMessage(context: Context, intent: Intent): Bundle? {
        intent.getBundleExtra(TaskerPluginConstants.EXTRA_BUNDLE)?.let {
            it.classLoader = context.classLoader
            return it.getBundle(TaskerPluginConstants.BUNDLE_KEY_PASS_THROUGH_DATA)
        }
        return null
    }


}

fun TaskerPluginResult.getSerialized(): Bundle {
    val bundle = Bundle()
    bundle.putInt("type", if (this is TaskerPluginResultAction) 0 else 1)
    bundle.putParcelable("value", this as Parcelable)
    return bundle
}

fun Bundle.deserializeTaskerPluginResult(): TaskerPluginResult? {
    classLoader = TaskerPluginResult::class.java.classLoader
    val type = getInt("type", -1)
    if (type == -1) return null
    return getParcelable("value")
}

suspend fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> Context.runPluginNoInput(helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>, requestType: RequestType): TaskerPluginResult? = withContext(Dispatchers.IO) {
    val input = helper.defaultInput?.let { TaskerInput(TaskerInput.Connection("","",""),null, it) }
    val runner = helper.runner ?: return@withContext null
    runner.run(this@runPluginNoInput, input)
}

suspend fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunnerAction<TInput, TOutput>> Context.runPluginActionNoInput(helper: TaskerPluginConfig<TInput, TOutput, TActionRunner>): TaskerPluginResultAction? = runPluginNoInput(helper, RequestType.Action) as TaskerPluginResultAction?
suspend fun <TInput : Any, TOutput : Any, TConditionRunner : TaskerPluginRunnerCondition<TInput, TOutput>> Context.runPluginConditionNoInput(helper: TaskerPluginConfig<TInput, TOutput, TConditionRunner>): TaskerPluginResultCondition? = runPluginNoInput(helper, RequestType.Condition) as TaskerPluginResultCondition?

enum class RequestType {
    Action,
    Condition,
    Event
}

fun Context.showNotification(title: String, text: String, id: Int = 14, channelId: String = "tasker_plugin_service", intent: Intent? = null) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, getString(R.string.tasker_plugin_service), NotificationManager.IMPORTANCE_LOW)
        channel.description = getString(R.string.tasker_plugin_service_description)
        notificationManager.createNotificationChannel(channel)
    }
    val builder = NotificationCompat.Builder(this, channelId)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOngoing(true)
    if (intent != null) {
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, pendingIntentFlags))
    }

    notificationManager.notify(id, builder.build())

}

fun Context.cancelNotification(id: Int = 14) = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(id)

val <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> TaskerPluginConfig<TInput, TOutput, TActionRunner>.activityIntent get() = Intent().setClassName(this.context.packageName, this.activityFullName!!)
fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> TaskerPluginConfig<TInput, TOutput, TActionRunner>.requestQueryOrThrow(input: TInput? = null) = context.requestQueryOrThrow(this, input)
fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> TaskerPluginConfig<TInput, TOutput, TActionRunner>.requestQuery(input: TInput? = null) = context.requestQuery(this, input)

fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> Context.requestQuery(config: TaskerPluginConfig<TInput, TOutput, TActionRunner>, input: TInput? = null): TaskerPluginResult? {
    return try {
        requestQueryOrThrow(config, input)
    } catch (e: Throwable) {
        e.printStackTrace()
        TaskerPluginResult.Failure(e)
    }
}

fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> Context.requestQueryOrThrow(config: TaskerPluginConfig<TInput, TOutput, TActionRunner>, input: TInput? = null): TaskerPluginResult {
    val bundle = Bundle()
    val inputToUse = input ?: config.defaultInput ?: throw TaskerPluginExceptionNoInput(config.name)
    val inputJson = TaskerInput.toJson(inputToUse, config.runnerClass)
    bundle.putString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON, inputJson)

    val resultBundle = contentResolver.call(config.authority, TaskerPluginRunner.METHOD_QUERY, null, bundle)
        ?: throw Exception("No response from ${config.name}")
    val result = resultBundle.deserializeTaskerPluginResult() ?: throw Exception("Couldn't deserialize response from ${config.name}")
    if (result is TaskerPluginResult.Failure) throw result.throwable ?: Exception("Unknown error from ${config.name}")
    return result
}

fun <TInput : Any, TOutput : Any> Context.requestAction(config: TaskerPluginConfig<TInput, TOutput, out TaskerPluginRunnerAction<TInput, TOutput>>, input: TInput? = null): TaskerPluginResultAction {
    val result = requestQueryOrThrow(config, input)
    return result as? TaskerPluginResultAction ?: throw Exception("Wrong result type from ${config.name}: ${result.javaClass.simpleName}")
}

fun <TInput : Any, TOutput : Any> Context.requestCondition(config: TaskerPluginConfig<TInput, TOutput, out TaskerPluginRunnerCondition<TInput, TOutput>>, input: TInput? = null): TaskerPluginResultConditionState {
    val result = requestQueryOrThrow(config, input)
    return result as? TaskerPluginResultCondition ?: throw Exception("Wrong result type from ${config.name}: ${result.javaClass.simpleName}")
}

fun <TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> TaskerPluginConfig<TInput, TOutput, TActionRunner>.getRunnerFromIntent(intent: Intent): TaskerPluginRunner<TInput, TOutput>? {
    val inputBundle = intent.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT) ?: return null
    inputBundle.classLoader = context.classLoader
    val taskerInput = inputBundle.getTaskerInputTyped<TInput>(context) ?: return null
    return taskerInput.connection.runnerClass?.newInstance() as? TaskerPluginRunner<TInput, TOutput>
}


@JvmInline
value class Seconds(val value: Long) {
    valtoMilliseconds get() = value * 1000L
}

@JvmInline
value class Milliseconds(val value: Long)

val Int.seconds get() = Seconds(this.toLong())
val Long.seconds get() = Seconds(this)
val Int.milliseconds get() = Milliseconds(this.toLong())
val Long.milliseconds get() = Milliseconds(this)


interface TaskerPluginClientMethods<TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> {
    val config: TaskerPluginConfig<TInput, TOutput, TActionRunner>
    fun requestQuery(input: TInput? = null) = config.requestQuery(input)
    fun requestQueryOrThrow(input: TInput? = null) = config.requestQueryOrThrow(input)
}

interface TaskerPluginClientMethodsAction<TInput : Any, TOutput : Any> : TaskerPluginClientMethods<TInput, TOutput, TaskerPluginRunnerAction<TInput, TOutput>> {
    fun requestAction(input: TInput? = null) = config.context.requestAction(config, input)
}

interface TaskerPluginClientMethodsCondition<TInput : Any, TOutput : Any> : TaskerPluginClientMethods<TInput, TOutput, TaskerPluginRunnerCondition<TInput, TOutput>> {
    fun requestCondition(input: TInput? = null) = config.context.requestCondition(config, input)
}
typealias Serializable = java.io.Serializable
typealias Parcelable = android.os.Parcelable
