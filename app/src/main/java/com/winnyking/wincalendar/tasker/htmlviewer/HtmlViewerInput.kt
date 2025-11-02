package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.winnyking.wincalendar.R

@TaskerInputRoot
class HtmlViewerInput @JvmOverloads constructor(
    @field:TaskerInputField("html_content", R.string.html_content_label) var htmlContent: String? = null,
    @field:TaskerInputField("css_content", R.string.css_content_label) var cssContent: String? = null,
    @field:TaskerInputField("js_content", R.string.js_content_label) var jsContent: String? = null,
    @field:TaskerInputField("tasker_variables", R.string.tasker_variables_label) var taskerVariables: String? = null
)
