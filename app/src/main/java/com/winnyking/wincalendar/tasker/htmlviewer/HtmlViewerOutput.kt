package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputRoot
class HtmlViewerOutput(
    @field:TaskerOutputVariable("button_clicked")
    var buttonClicked: String? = null,
    @field:TaskerOutputVariable("error_code")
    var errorCode: Int? = null,
    @field:TaskerOutputVariable("error_message")
    var errorMessage: String? = null
)
