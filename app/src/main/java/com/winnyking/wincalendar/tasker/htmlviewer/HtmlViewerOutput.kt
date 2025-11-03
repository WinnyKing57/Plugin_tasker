package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputRoot
class HtmlViewerOutput(
    @TaskerOutputVariable("button_clicked", "Button Clicked", "The value of the button that was clicked in the HTML page.")
    var buttonClicked: String? = null,
    @TaskerOutputVariable("error_code", "Error Code", "An error code if something went wrong.")
    var errorCode: Int? = null,
    @TaskerOutputVariable("error_message", "Error Message", "An error message if something went wrong.")
    var errorMessage: String? = null
)
