package com.joaomgcd.taskerpluginlibrary.action

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultAction
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner

interface TaskerPluginRunnerAction<TInput : Any, TOutput : Any> : TaskerPluginRunner<TInput, TOutput> {
    fun run(context: Context, input: TaskerInput<TInput>?): TaskerPluginResultAction
}
