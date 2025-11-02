package com.winnyking.wincalendar.tasker.htmlviewer;

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputFieldAnno;
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot;
import com.winnyking.wincalendar.R;

@TaskerInputRoot
public class HtmlViewerInput extends TaskerInputRoot {
    @TaskerInputFieldAnno(key = "html_content", labelResId = R.string.html_content_label)
    public String htmlContent;

    @TaskerInputFieldAnno(key = "css_content", labelResId = R.string.css_content_label)
    public String cssContent;

    @TaskerInputFieldAnno(key = "js_content", labelResId = R.string.js_content_label)
    public String jsContent;

    @TaskerInputFieldAnno(key = "tasker_variables", labelResId = R.string.tasker_variables_label)
    public String taskerVariables;

    public HtmlViewerInput() {}

    public HtmlViewerInput(String htmlContent, String cssContent, String jsContent, String taskerVariables) {
        this.htmlContent = htmlContent;
        this.cssContent = cssContent;
        this.jsContent = jsContent;
        this.taskerVariables = taskerVariables;
    }
}
