package net.dinglisch.android.tasker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants

abstract class PluginResultReceiver : BroadcastReceiver() {
    abstract fun onReceiveResult(context: Context, intent: Intent?, bundle: Bundle)
    override fun onReceive(context: Context, intent: Intent?) {
        val bundle = intent?.getBundleExtra(TaskerPluginConstants.EXTRA_BUNDLE) ?: return
        onReceiveResult(context, intent, bundle)
    }
}
