package com.winnyking.wincalendar.tasker.helloworld;

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputFieldAnno;
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot;
import com.winnyking.wincalendar.R; // Correct R file import


@TaskerInputRoot
public class HelloWorldInput extends TaskerInputRoot {
    @TaskerInputFieldAnno(key = "message", labelResId = R.string.hello_world_input_label)
    public String message;

    // Required empty constructor
    public HelloWorldInput() {}

    public HelloWorldInput(String message) {
        this.message = message;
    }
}
