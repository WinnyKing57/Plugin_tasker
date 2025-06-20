package com.joaomgcd.taskerpluginlibrary.output

import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.Serializable
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import kotlin.reflect.full.memberProperties


interface TaskerPluginOutput<TOutput : Any> : Serializable {
    fun getOutputVariables(context: Context): List<TaskerOutputVariable> {
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


    fun getVariablesBundle(): Bundle {
        val bundle = Bundle()
        for (property in this::class.memberProperties) {
            val outputVarAnnotation = property.annotations.firstOrNull { it is TaskerOutputVariableAnnotation } as? TaskerOutputVariableAnnotation
            val value = property.getter.call(this)

            if (outputVarAnnotation != null) {
                if (value != null && value != Unit) {
                    bundle.putString(outputVarAnnotation.name, value.toString())
                }
            } else {
                val taskerOutputVar = value as? TaskerOutputVariable ?: continue
                if (taskerOutputVar.description != null) { //HACK: only add variable if it has a description. This means it was manually added by user
                    bundle.putString(taskerOutputVar.name, taskerOutputVar.description) //HACK: assumes description has the variable's value
                }
            }
        }
        return bundle
    }


    companion object {
        fun bundleFromVariables(vararg variables: TaskerOutputVariable): Bundle {
            val bundle = Bundle()
            for (variable in variables) {
                bundle.putString(variable.name, variable.description) //HACK: assumes description has the variable's value
            }
            return bundle
        }

        fun getRelevantVariablesBundle(context: Context, output: TaskerPluginOutput<*>?): Bundle {
            val bundle = Bundle()
            val taskerVars = context.getOutputVariables(output, true)
            bundle.putStringArrayList(TaskerPluginConstants.EXTRA_RELEVANT_VARIABLES, ArrayList(taskerVars.map { it.name }))
            return bundle
        }


    }
}
