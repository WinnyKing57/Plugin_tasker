package com.joaomgcd.taskerpluginlibrary.condition

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionState


interface TaskerPluginRunnerConditionNoInput<TOutput : Any> : TaskerPluginRunnerCondition<Unit, TOutput> {
    override fun run(context: Context, input: TaskerInput<Unit>?): TaskerPluginResultCondition = run(context)
    fun run(context: Context): TaskerPluginResultCondition
}

interface TaskerPluginRunnerConditionNoOutput<TInput : Any> : TaskerPluginRunnerCondition<TInput, Unit> {
    override fun run(context: Context, input: TaskerInput<TInput>?): TaskerPluginResultCondition {
        val status = run(context, input?.regular, input?.input)
        return TaskerPluginConditionResult(status)
    }

    fun run(context: Context, regularVariables: Map<String, String>?, input: TInput?): TaskerPluginResultConditionState
}


interface TaskerPluginRunnerConditionNoInputOrOutput : TaskerPluginRunnerCondition<Unit, Unit> {
    override fun run(context: Context, input: TaskerInput<Unit>?): TaskerPluginResultCondition {
        val status = run(context, input?.regular)
        return TaskerPluginConditionResult(status)
    }

    fun run(context: Context, regularVariables: Map<String, String>?): TaskerPluginResultConditionState
}
