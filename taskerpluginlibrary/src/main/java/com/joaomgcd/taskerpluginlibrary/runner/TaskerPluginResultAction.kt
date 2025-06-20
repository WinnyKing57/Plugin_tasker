package com.joaomgcd.taskerpluginlibrary.runner

import android.os.Bundle
import android.os.Parcelable
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutput
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskerPluginResultAction(
    override var variablesBundle: Bundle? = null,
    override val replaceVariables: Boolean = false,
    override val isSuccessful: Boolean = true,
    override val errorMessage: String? = null,
    val errorCode: Int? = null,
    var actionId: String? = null
) : TaskerPluginResult(), Parcelable {

    constructor(variables: TaskerPluginOutput<*>?, replaceVariables: Boolean = false) : this(variables?.getVariablesBundle(), replaceVariables)
    constructor(vararg variables: TaskerOutputVariable, replaceVariables: Boolean = false) : this(TaskerPluginOutput.bundleFromVariables(*variables), replaceVariables)

    companion object {
        fun Success(variables: TaskerPluginOutput<*>? = null, replaceVariables: Boolean = false) = TaskerPluginResultAction(variables, replaceVariables)
        fun Success(vararg variables: TaskerOutputVariable, replaceVariables: Boolean = false) = TaskerPluginResultAction(*variables, replaceVariables = replaceVariables)
        fun Failure(throwable: Throwable?, variables: TaskerPluginOutput<*>? = null, errorCode: Int? = null) = TaskerPluginResultAction(variablesBundle = variables?.getVariablesBundle(), isSuccessful = false, errorMessage = throwable?.message, errorCode = errorCode)
        fun Failure(throwable: Throwable?, vararg variables: TaskerOutputVariable, errorCode: Int? = null) = TaskerPluginResultAction(variablesBundle = TaskerPluginOutput.bundleFromVariables(*variables), isSuccessful = false, errorMessage = throwable?.message, errorCode = errorCode)
    }
}
