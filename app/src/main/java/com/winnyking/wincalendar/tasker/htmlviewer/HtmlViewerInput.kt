package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class HtmlViewerInput @JvmOverloads constructor(
    @field:TaskerInputField("code") var code: String? = null,
    @field:TaskerInputField("tasker_variables") var taskerVariables: String? = null
)
