package com.joaomgcd.taskerpluginlibrary.output

import android.content.Context
import android.os.Bundle


interface TaskerPluginOutputForConfig<TOutput : Any> : TaskerPluginOutput<TOutput> {
    fun addOutputToBundle(context: Context, bundle: Bundle)
}
