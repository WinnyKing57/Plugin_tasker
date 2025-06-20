package com.joaomgcd.taskerpluginlibrary.output.runner

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import kotlin.reflect.KProperty

class TaskerOutputVariableValueGetter<T>(private val variable: TaskerOutputVariable, private val valueProvider: () -> T?) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = valueProvider()
}
