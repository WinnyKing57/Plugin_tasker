package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariables

class HtmlViewerHelper(config: TaskerPluginConfig<HtmlViewerInput>) :
    TaskerPluginConfigHelper<HtmlViewerInput, TaskerOutputVariables, HtmlViewerRunner>(config) {
    override val runnerClass = HtmlViewerRunner::class.java
    override val outputClass = TaskerOutputVariables::class.java
    override val inputClass = HtmlViewerInput::class.java

    override fun addToStringBlurb(input: TaskerInput<HtmlViewerInput>, blurbBuilder: StringBuilder) {
        val content = input.regular.htmlContent
        if (!content.isNullOrEmpty()) {
            blurbBuilder.append("Displaying custom HTML")
        } else {
            blurbBuilder.append(config.context.getString(com.winnyking.wincalendar.R.string.html_viewer_blurb))
        }
    }

    override fun getOutputVariables(input: HtmlViewerInput): TaskerOutputVariables {
        val variables = TaskerOutputVariables()
        val outputVariables = input.outputVariables ?: return variables
        for (variable in outputVariables.split(",")) {
            val trimmed = variable.trim()
            if (trimmed.isNotEmpty()) {
                variables.add(TaskerOutputVariable(trimmed, trimmed, trimmed))
            }
        }
        return variables
    }
}
