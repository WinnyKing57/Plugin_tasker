package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class HtmlViewerInput @JvmOverloads constructor(
    @field:TaskerInputField("html_content") var htmlContent: String? = null,
    @field:TaskerInputField("css_content") var cssContent: String? = null,
    @field:TaskerInputField("js_content") var jsContent: String? = null,
    @field:TaskerInputField("tasker_variables") var taskerVariables: String? = null
)
