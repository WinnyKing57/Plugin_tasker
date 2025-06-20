package com.joaomgcd.taskerpluginlibrary.output.runner

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.output.TaskerPluginOutputForConfig


interface TaskerPluginOutputForRunner<TOutput : Any> : TaskerPluginOutputForConfig<TOutput> {
    fun <TInput : Any> getBlurb(context: Context, input: TInput): String
}
