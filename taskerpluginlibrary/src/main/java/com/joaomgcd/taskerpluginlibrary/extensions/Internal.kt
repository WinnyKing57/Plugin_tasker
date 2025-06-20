package com.joaomgcd.taskerpluginlibrary.extensions

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutput


internal fun TaskerPluginOutput<*>.getOutputVariables(context: Context): List<TaskerOutputVariable> {
    val outputClass = this::class.java
    val fields = outputClass.declaredFields
    val variables = mutableListOf<TaskerOutputVariable>()
    for (field in fields) {
        if (!TaskerOutputVariable.isOutputVariable(field)) continue

        field.isAccessible = true
        val value = field.get(this) as? TaskerOutputVariable ?: continue
        variables.add(value)
    }
    return variables
}
