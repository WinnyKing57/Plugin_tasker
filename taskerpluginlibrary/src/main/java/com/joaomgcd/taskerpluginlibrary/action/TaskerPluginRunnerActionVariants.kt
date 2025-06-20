package com.joaomgcd.taskerpluginlibrary.action

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultAction


interface TaskerPluginRunnerActionNoInput<TOutput : Any> : TaskerPluginRunnerAction<Unit, TOutput> {
    override fun run(context: Context, input: TaskerInput<Unit>?): TaskerPluginResultAction = run(context)
    fun run(context: Context): TaskerPluginResultAction
}

interface TaskerPluginRunnerActionNoOutput<TInput : Any> : TaskerPluginRunnerAction<TInput, Unit> {
    override fun run(context: Context, input: TaskerInput<TInput>?): TaskerPluginResultAction {
        run(context, input?.regular, input?.input)
        return TaskerPluginResultAction.Success()
    }

    fun run(context: Context, regularVariables: Map<String, String>?, input: TInput?)
}


interface TaskerPluginRunnerActionNoInputOrOutput : TaskerPluginRunnerAction<Unit, Unit> {
    override fun run(context: Context, input: TaskerInput<Unit>?): TaskerPluginResultAction {
        run(context, input?.regular)
        return TaskerPluginResultAction.Success()
    }

    fun run(context: Context, regularVariables: Map<String, String>?)
}
