package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class HtmlViewerInput @JvmOverloads constructor(
    @field:TaskerInputField("html_content") var htmlContent: String? = null,
    @field:TaskerInputField("code") var code: String? = null
)
