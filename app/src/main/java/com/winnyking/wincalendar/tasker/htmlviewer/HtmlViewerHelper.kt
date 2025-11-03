package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput

class HtmlViewerHelper(config: TaskerPluginConfig<HtmlViewerInput>) :
    TaskerPluginConfigHelper<HtmlViewerInput, HtmlViewerOutput, HtmlViewerRunner>(config) {
    override val runnerClass = HtmlViewerRunner::class.java
    override val outputClass = HtmlViewerOutput::class.java
    override val inputClass = HtmlViewerInput::class.java

    override fun addToStringBlurb(input: TaskerInput<HtmlViewerInput>, blurbBuilder: StringBuilder) {
        val content = input.regular.htmlContent ?: input.regular.code
        if (!content.isNullOrEmpty()) {
            blurbBuilder.append("Displaying custom HTML")
        } else {
            blurbBuilder.append(config.context.getString(com.winnyking.wincalendar.R.string.html_viewer_blurb))
        }
    }
}
