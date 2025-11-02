package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import org.json.JSONObject

object TaskerVariables {
    fun <T : Any> getVariableValue(input: TaskerInput<T>, variableName: String): String? {
        val viewerInput = input.regular as? HtmlViewerInput ?: return null
        val taskerVariables = viewerInput.taskerVariables ?: return null

        return try {
            val json = JSONObject(taskerVariables)
            json.optString(variableName, null)
        } catch (e: Exception) {
            null
        }
    }
}
