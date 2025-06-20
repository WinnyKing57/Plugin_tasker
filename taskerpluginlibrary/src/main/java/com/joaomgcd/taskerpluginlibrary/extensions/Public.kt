package com.joaomgcd.taskerpluginlibrary.extensions

import android.app.Activity
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot


fun <T : Any> Bundle.getTaskerInput(inputClass: Class<T>): TaskerInput<T>? {
    val json = getString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON) ?: return null
    return TaskerInput.fromJson(json, inputClass)
}

fun <TActivity : Activity, TInput : Any> TActivity.getTaskerInput(inputClass: Class<TInput>): TaskerInput<TInput>? {
    return intent.getBundleExtra(TaskerPluginConstants.EXTRA_BUNDLE)?.getTaskerInput(inputClass)
}

inline fun <TActivity : Activity, reified TInput : Any> TActivity.getTaskerInput(): TaskerInput<TInput>? {
    return getTaskerInput(TInput::class.java)
}

fun <T : Any> TaskerInputRoot.getTaskerInput(inputClass: Class<T>): T? {
    return TaskerInput.fromJson(this.toString(), inputClass)?.input
}

inline fun <reified T : Any> TaskerInputRoot.getTaskerInput(): T? {
    return getTaskerInput(T::class.java)
}
