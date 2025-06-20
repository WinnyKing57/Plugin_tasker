package com.joaomgcd.taskerpluginlibrary.runner

import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputFieldAnno
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariableAnnotation
import kotlin.reflect.KClass

interface TaskerOutputRenames {
    val renames: Map<String, String>
    fun getRename(originalName: String) = renames[originalName]
}

fun KClass<*>.getOutputRenamesFromAnnotations(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    java.declaredFields.forEach { field ->
        field.getAnnotation(TaskerOutputVariableAnnotation::class.java)?.let { annotation ->
            field.getAnnotation(com.joaomgcd.taskerpluginlibrary.output.taskerPluginVariableRenamesAnnotation::class.java)?.let { rename ->
                map[annotation.name] = rename.renames
            }
        }
    }
    return map
}

fun KClass<*>.getInputRenamesFromAnnotations(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    java.declaredFields.forEach { field ->
        field.getAnnotation(TaskerInputFieldAnno::class.java)?.let { annotation ->
            field.getAnnotation(com.joaomgcd.taskerpluginlibrary.output.taskerPluginVariableRenamesAnnotation::class.java)?.let { rename ->
                map[annotation.key] = rename.renames
            }
        }
    }
    return map
}

fun TaskerOutputRenames.getTaskerRenamesBundle(prefix: String = TaskerPluginConstants.RENAME_PREFIX): Map<String, String> {
    return renames.mapKeys { "$prefix${it.key}${TaskerPluginConstants.RENAME_SUFFIX}" }
}
