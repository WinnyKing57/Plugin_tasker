package com.joaomgcd.taskerpluginlibrary.condition

import android.os.Bundle
import android.os.Parcelable
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionState
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskerPluginConditionResult(override val state: TaskerPluginResultConditionState, override val variablesBundle: Bundle? = null, override val replaceVariables: Boolean = false) : TaskerPluginResultCondition() {
    constructor(state: TaskerPluginResultConditionState, variables: TaskerPluginOutput<*>?, replaceVariables: Boolean = false) : this(state, variables?.getVariablesBundle(), replaceVariables)
    constructor(state: TaskerPluginResultConditionState, vararg variables: TaskerOutputVariable, replaceVariables: Boolean = false) : this(state, TaskerPluginOutput.bundleFromVariables(*variables), replaceVariables)
}
