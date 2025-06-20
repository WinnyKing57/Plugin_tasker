package com.joaomgcd.taskerpluginlibrary.runner

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.RequestType
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.TaskerPluginExceptionNoInput
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerCondition
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.getSerialized
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses


interface TaskerPluginRunner<TInput : Any, TOutput : Any> {
    fun run(context: android.content.Context, input: TaskerInput<TInput>?): TaskerPluginResult

    companion object {
        const val METHOD_QUERY = "query"
        fun <TInput : Any, TOutput : Any> KClass<out TaskerPluginRunner<TInput, TOutput>>.getOutputClass(): KClass<TOutput>? {
            return superclasses.firstOrNull { it == TaskerPluginRunnerAction::class || it == TaskerPluginRunnerCondition::class }
                ?.typeParameters?.getOrNull(1)?.let { it.upperBounds.firstOrNull()?.classifier as? KClass<TOutput> }
        }

    }
}

abstract class TaskerPluginRunnerProvider<TInput : Any, TOutput : Any, TActionRunner : TaskerPluginRunner<TInput, TOutput>> : ContentProvider() {
    abstract val config: TaskerPluginConfig<TInput, TOutput, TActionRunner>
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != TaskerPluginRunner.METHOD_QUERY) return null
        val input = extras?.let { config.getTaskerInputFromIntent(it) } ?: TaskerInput(TaskerInput.Connection("", "", null), null, config.defaultInput ?: throw TaskerPluginExceptionNoInput(config.name))
        val runner = config.runner ?: return null //TODO: error
        val result = runBlocking { runner.run(context!!, input) } //TODO: don't block
        return result.getSerialized()
    }

    override fun onCreate() = true
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
