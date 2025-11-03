package com.winnyking.wincalendar.tasker.htmlviewer

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputRoot
class HtmlViewerOutput(
    private val buttonValue: String? = null,
    private val errorCodeValue: Int? = null,
    private val errorMessageValue: String? = null
) {
    @TaskerOutputVariable("button_clicked")
    fun getButtonClicked(): String? {
        return buttonValue
    }

    @TaskerOutputVariable("error_code")
    fun getErrorCode(): Int? {
        return errorCodeValue
    }

    @TaskerOutputVariable("error_message")
    fun getErrorMessage(): String? {
        return errorMessageValue
    }
}
