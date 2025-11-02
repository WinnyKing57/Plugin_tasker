package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class HtmlViewerInput @JvmOverloads constructor(
    @field:TaskerInputField("html_content", labelResId = RIds.html_content_label) var htmlContent: String? = null,
    @field:TaskerInputField("css_content", labelResId = RIds.css_content_label) var cssContent: String? = null,
    @field:TaskerInputField("js_content", labelResId = RIds.js_content_label) var jsContent: String? = null,
    @field:TaskerInputField("tasker_variables", labelResId = RIds.tasker_variables_label) var taskerVariables: String? = null
)
