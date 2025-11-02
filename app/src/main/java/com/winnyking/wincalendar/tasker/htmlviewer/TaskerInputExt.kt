package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.input.TaskerInput

fun <T : Any> TaskerInput<T>.getVariableValue(variableName: String): String? {
    return TaskerVariables.getVariableValue(this, variableName)
}
