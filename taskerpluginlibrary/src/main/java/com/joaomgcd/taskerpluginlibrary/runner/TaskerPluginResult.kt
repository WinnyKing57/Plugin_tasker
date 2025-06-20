package com.joaomgcd.taskerpluginlibrary.runner

import android.os.Bundle
import android.os.Parcelable
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import kotlinx.parcelize.Parcelize

sealed class TaskerPluginResult : Parcelable {
    abstract val variablesBundle: Bundle?
    abstract val isSuccessful: Boolean
    open val replaceVariables: Boolean get() = false
    open val errorMessage: String? get() = null

    @Parcelize
    class SuccessUnit : TaskerPluginResult() {
        override val variablesBundle = null
        override val isSuccessful = true
    }

    @Parcelize
    data class Success<TOutput : Parcelable>(val output: TOutput? = null, override val variablesBundle: Bundle? = null, override val replaceVariables: Boolean = false) : TaskerPluginResult() {
        override val isSuccessful = true
    }


    @Parcelize
    data class Failure(val throwable: Throwable? = null, override val variablesBundle: Bundle? = null) : TaskerPluginResult() {
        constructor(errorMessage: String, errorCode: Int? = null) : this(Exception(errorMessage)) {
            this.errorCode = errorCode
        }

        override val isSuccessful = false
        override val errorMessage: String? get() = throwable?.message
        var errorCode: Int? = null
            private set
    }

    enum class Type {
        Action,
        Condition,
        Event
    }

    val taskerErrorMessageBundle: Bundle?
        get() {
            if (isSuccessful) return null
            if (this !is Failure) return null
            val errorBundle = Bundle()
            errorBundle.putInt(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_CODE_KEY, errorCode ?: 0)
            errorBundle.putString(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_MESSAGE_KEY, errorMessage)
            return errorBundle
        }
}
