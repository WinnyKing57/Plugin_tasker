package com.joaomgcd.taskerpluginlibrary.output

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import com.joaomgcd.taskerpluginlibrary.R
import com.joaomgcd.taskerpluginlibrary.Serializable
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputFieldAnno
import com.joaomgcd.taskerpluginlibrary.input.findInputField
import com.joaomgcd.taskerpluginlibrary.input.taskerPluginVariableRenames
import com.joaomgcd.taskerpluginlibrary.log
import kotlinx.parcelize.Parcelize
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class TaskerOutputVariableAnnotation(val name: String, val labelResId: Int = 0, val descriptionResId: Int = 0, val htmlNoteResId: Int = 0, val ignoreInStringBlurb: Boolean = false)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class taskerPluginVariableRenamesAnnotation(val renames: String)

class TaskerOutputVariable(var name: String, var label: String, var description: String?, var htmlNote: String? = null, var ignoreInStringBlurb: Boolean = false) : Serializable {
    constructor(context: Context, name: String, labelResId: Int, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) :
            this(name, context.getString(labelResId), context.getStringOrNull(descriptionResId), context.getStringOrNull(htmlNoteResId), ignoreInStringBlurb)

    val nameNoDots get() = name.replace(".", "_")

    override fun toString(): String {
        return "Variable Name: $name, Label: $label, Description: $description"
    }

    internal fun <TInput> getSpacedName(input: TInput?, namerHelper: NamerHelper?): String {
        val nameFromHelper = namerHelper?.getName(this, input)
        return nameFromHelper ?: nameNoDots
    }

    @Parcelize
    data class OutputVarInfo(val name: String, val labelResId: Int, val descriptionResId: Int, val htmlNoteResId: Int, val ignoreInStringBlurb: Boolean) : Parcelable, Serializable {
        fun getOutputVariable(context: Context, value: Any?): TaskerOutputVariable {
            val variable = TaskerOutputVariable(context, name, labelResId, descriptionResId, htmlNoteResId, ignoreInStringBlurb)
            if (value != null) {
                variable.name = variable.name.lowercase()
            }
            return variable
        }
    }

    @Parcelize
    open class NamerHelper : Parcelable, Serializable {
        open fun <TInput> getName(variable: TaskerOutputVariable, input: TInput?): String? = null
    }

    companion object {
        internal const val BUNDLE_KEY_NAMER_HELPER = "com.joaomgcd.taskerpluginlibrary.BUNDLE_KEY_NAMER_HELPER"
        internal fun isOutputVariable(field: Field): Boolean {
            val annotation = field.getAnnotation(TaskerOutputVariableAnnotation::class.java)
            return annotation != null || field.type == TaskerOutputVariable::class.java
        }

        internal fun isOutputVariable(property: KProperty<*>): Boolean {
            val annotation = property.findAnnotation<TaskerOutputVariableAnnotation>()
            return annotation != null || property.returnType.classifier == TaskerOutputVariable::class
        }
    }
}


internal fun Context.getStringOrNull(resId: Int?): String? {
    if (resId == null || resId == 0) return null
    return try {
        getString(resId)
    } catch (e: Exception) {
        null
    }
}

internal fun <TOutput : Any> TOutput.getOutputVariablesFromObject(context: Context, addPrefix: String?): List<TaskerOutputVariable> = log {
    this::class.memberProperties.mapNotNull { property ->
        val outputVar = property.findAnnotation<TaskerOutputVariableAnnotation>()?.let { annotation ->
            TaskerOutputVariable(context, annotation.name, annotation.labelResId, annotation.descriptionResId, annotation.htmlNoteResId, annotation.ignoreInStringBlurb)
        } ?: (property.getter.call(this) as? TaskerOutputVariable)

        outputVar?.apply {
            if (addPrefix != null) {
                name = "$addPrefix.$name"
            }
        }
    }
}

internal fun <TInput : Any> TInput.getOutputVariablesFromInputFields(context: Context, addPrefix: String?): List<TaskerOutputVariable> = log {
    this::class.memberProperties.mapNotNull { property ->
        val inputField = findInputField(property) as? TaskerInputField<Any> ?: return@mapNotNull null
        val outputVarInfo = inputField.OutputvarInfo ?: return@mapNotNull null
        outputVarInfo.getOutputVariable(context, inputField.value).apply {
            if (addPrefix != null) {
                name = "$addPrefix.$name"
            }
        }
    }
}


internal fun <TInput : Any> TInput.getRenamesFromInputFields(): Map<String, String> = log {
    this::class.memberProperties.mapNotNull { property ->
        val inputField = findInputField(property) as? TaskerInputField<Any> ?: return@mapNotNull null
        val renames = inputField.renamedTo ?: return@mapNotNull null
        val originalName = inputField.OutputvarInfo?.name ?: return@mapNotNull null
        originalName to renames
    }.toMap()
}

internal fun <TOutput : Any> TOutput.getRenamesFromOutputVariables(): Map<String, String> = log {
    this::class.memberProperties.mapNotNull { property ->
        val renames = property.taskerPluginVariableRenames?.renames ?: return@mapNotNull null
        val originalName = property.findAnnotation<TaskerOutputVariableAnnotation>()?.name ?: return@mapNotNull null
        originalName to renames
    }.toMap()
}

internal fun <TInput : Any> Bundle.getVariableRenames(input: TInput?, namerHelper: TaskerOutputVariable.NamerHelper?): Map<String, String> {
    val map = mutableMapOf<String, String>()
    fun Map<String, String>?.addToMap() = this?.let { map.putAll(it) }
    input?.getRenamesFromInputFields()?.addToMap()
    val outputClass = getString(TaskerPluginConstants.BUNDLE_KEY_RUNNER_CLASS_NAME)?.let { Class.forName(it) }?.let { TaskerPluginRunner.getOutputClass(it) }
    outputClass?.getRenamesFromOutputVariables()?.addToMap()
    return map.map { rename ->
        val originalName = namerHelper?.getName(TaskerOutputVariable(rename.key, "", ""), input) ?: rename.key.replace(".", "_")
        val newName = namerHelper?.getName(TaskerOutputVariable(rename.value, "", ""), input) ?: rename.value.replace(".", "_")
        "$originalName${TaskerPluginConstants.RENAME_SUFFIX}" to newName
    }.toMap()

}

private fun <T : Any> Class<T>.getRenamesFromOutputVariables(): Map<String, String>? = log {
    val renamesField = try {
        this.getDeclaredField("Companion")
    } catch (e: NoSuchFieldException) {
        return@log null
    }
    val companion = renamesField.get(null) ?: return@log null
    val renamesProperty = companion::class.memberProperties.firstOrNull { it.name == "renames" } ?: return@log null
    @Suppress("UNCHECKED_CAST")
    return@log renamesProperty.getter.call(companion) as? Map<String, String>

}
