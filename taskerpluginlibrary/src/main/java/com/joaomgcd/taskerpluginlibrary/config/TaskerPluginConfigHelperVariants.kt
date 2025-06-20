package com.joaomgcd.taskerpluginlibrary.config

import android.app.Activity
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner


abstract class TaskerPluginConfigHelperNoInput<TOutput : Any, TActionRunner : TaskerPluginRunner<Unit, TOutput>>(activity: Activity, config: TaskerPluginConfig<Unit, TOutput, TActionRunner>) : TaskerPluginConfigHelper<Unit, TOutput, TActionRunner>(activity, config) {
    override fun assignValues(input: Unit) {}
    override fun getInput(): TaskerInputRoot = TaskerInputRoot()
}

abstract class TaskerPluginConfigHelperNoOutput<TInput : Any, TActionRunner : TaskerPluginRunner<TInput, Unit>>(activity: Activity, config: TaskerPluginConfig<TInput, Unit, TActionRunner>) : TaskerPluginConfigHelper<TInput, Unit, TActionRunner>(activity, config) {
    override fun getOutput() = null
}

abstract class TaskerPluginConfigHelperNoInputOrOutput<TActionRunner : TaskerPluginRunner<Unit, Unit>>(activity: Activity, config: TaskerPluginConfig<Unit, Unit, TActionRunner>) : TaskerPluginConfigHelper<Unit, Unit, TActionRunner>(activity, config) {
    override fun assignValues(input: Unit) {}
    override fun getInput(): TaskerInputRoot = TaskerInputRoot()
    override fun getOutput() = null
}
