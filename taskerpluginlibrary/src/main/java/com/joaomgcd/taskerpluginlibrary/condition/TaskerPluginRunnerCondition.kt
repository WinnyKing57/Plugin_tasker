package com.joaomgcd.taskerpluginlibrary.condition

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner

interface TaskerPluginRunnerCondition<TInput : Any, TOutput : Any> : TaskerPluginRunner<TInput, TOutput> {
    fun run(context: Context, input: TaskerInput<TInput>?): TaskerPluginResultCondition
}
