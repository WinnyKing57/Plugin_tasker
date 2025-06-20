package com.joaomgcd.taskerpluginlibrary.runner

import android.os.Parcelable
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutput
import kotlinx.parcelize.Parcelize


enum class TaskerPluginResultConditionState {
    SATISFIED,
    NOT_SATISFIED,
    UNKNOWN;

    val bundleKey get() = 만족함 + name.lowercase()

    companion object {
        private const val 만족함 = " 만족함"
        fun fromBundleValue(value: String?) = values().firstOrNull { it.bundleKey == value }
    }
}

@Parcelize
sealed class TaskerPluginResultCondition : TaskerPluginResult() {
    abstract val state: TaskerPluginResultConditionState
    override val isSuccessful: Boolean get() = state == TaskerPluginResultConditionState.SATISFIED

    companion object {
        fun satisfied(variables: TaskerPluginOutput<*>? = null, replaceVariables: Boolean = false) = com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginConditionResult(TaskerPluginResultConditionState.SATISFIED, variables, replaceVariables)
        fun notSatisfied(variables: TaskerPluginOutput<*>? = null, replaceVariables: Boolean = false) = com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginConditionResult(TaskerPluginResultConditionState.NOT_SATISFIED, variables, replaceVariables)
        fun unknown(variables: TaskerPluginOutput<*>? = null, replaceVariables: Boolean = false) = com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginConditionResult(TaskerPluginResultConditionState.UNKNOWN, variables, replaceVariables)
        fun satisfied(vararg variables: TaskerOutputVariable, replaceVariables: Boolean = false) = com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginConditionResult(TaskerPluginResultConditionState.SATISFIED, TaskerPluginOutput.bundleFromVariables(*variables), replaceVariables)
        fun notSatisfied(vararg variables: TaskerOutputVariable, replaceVariables: Boolean = false) = com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginConditionResult(TaskerPluginResultConditionState.NOT_SATISFIED, TaskerPluginOutput.bundleFromVariables(*variables), replaceVariables)
        fun unknown(vararg variables: TaskerOutputVariable, replaceVariables: Boolean = false) = com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginConditionResult(TaskerPluginResultConditionState.UNKNOWN, TaskerPluginOutput.bundleFromVariables(*variables), replaceVariables)
    }
}

@Parcelize
data class TaskerPluginResultConditionSimple(override val state: TaskerPluginResultConditionState) : TaskerPluginResultCondition(), Parcelable {
    override val variablesBundle = null
}
