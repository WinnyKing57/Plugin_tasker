package com.joaomgcd.taskerpluginlibrary.runner

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import com.joaomgcd.taskerpluginlibrary.R
import com.joaomgcd.taskerpluginlibrary.RequestType
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.cancelNotification
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.getTaskerInput
import com.joaomgcd.taskerpluginlibrary.showNotification
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**Only use this if you know what you're doing. This will make the action run in a foreground service. Useful for actions that take a long time to complete.*/
open class IntentServiceParallel : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val activeTasks = ConcurrentHashMap<String, Job>()
    private val notificationIdBase = AtomicInteger(1414) // Base for unique notification IDs

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY // Should not happen with START_REDELIVER_INTENT

        val notificationId = notificationIdBase.getAndIncrement()
        showNotification(getString(R.string.running_tasker_plugin), getString(R.string.app_name), notificationId)

        val actionId = intent.getStringExtra(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ACTION_ID_KEY)
        val config = intent.getParcelableExtra<TaskerPluginConfig<Any, Any, TaskerPluginRunner<Any, Any>>>(TaskerPluginConstants.EXTRA_CONFIG)
        val requestType = intent.getSerializableExtra(TaskerPluginConstants.EXTRA_REQUEST_TYPE) as RequestType

        if (config == null || requestType == null) {
            stopSelf(startId) // Invalid intent
            return START_NOT_STICKY
        }

        val taskKey = actionId ?: "condition_${System.currentTimeMillis()}"

        activeTasks[taskKey]?.cancel() // Cancel previous task with same key if any

        val job = serviceScope.launch(Dispatchers.IO) { // Offload to IO dispatcher
            try {
                val taskerInput = intent.getBundleExtra(TaskerPluginConstants.EXTRA_INPUT)?.getTaskerInput(this@IntentServiceParallel)
                val runner = config.getRunnerFromIntent(intent) ?: config.runner ?: throw Exception("No runner found")

                val result = runner.run(this@IntentServiceParallel, taskerInput)

                // Send result back to Tasker
                val resultIntent = Intent().apply {
                    putExtra(TaskerPluginConstants.IsTaskerMessage, true)
                    putExtra(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ACTION_ID_KEY, actionId) // null for conditions
                    putExtra(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_VARIABLES_KEY, result.variablesBundle)
                    if (result is TaskerPluginResult.Failure) {
                        putExtra(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_KEY, Bundle().apply {
                            putInt(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_CODE_KEY, (result as? TaskerPluginResultAction)?.errorCode ?: 0)
                            putString(TaskerPluginConstants.TASKER_MESSAGE_RESPONSE_ERROR_MESSAGE_KEY, result.errorMessage)
                        })
                    }
                }

                when (requestType) {
                    RequestType.Action -> sendBroadcast(resultIntent.setAction("net.dinglisch.android.tasker.ACTION_PLUGIN_SEND_VARIABLES"))
                    RequestType.Condition -> sendBroadcast(resultIntent.setAction("net.dinglisch.android.tasker.ACTION_PLUGIN_QUERY_REQUEST_RESULT"))
                    else -> {} // Event not supported here
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally, send an error broadcast to Tasker
            } finally {
                activeTasks.remove(taskKey)
                if (activeTasks.isEmpty()) {
                    stopSelf(startId) // Stop service if no more tasks
                }
                cancelNotification(notificationId)
            }
        }
        activeTasks[taskKey] = job

        return START_REDELIVER_INTENT // Redeliver intent if service is killed
    }


    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel() // Cancel all coroutines
        activeTasks.values.forEach { it.cancel() } // Ensure all individual jobs are cancelled
        activeTasks.clear()
        cancelNotification() // Clear any persistent notification
    }

    companion object {
        fun enqueueWork(context: Context, intent: Intent) {
            val serviceIntent = Intent(context, IntentServiceParallel::class.java).apply {
                putExtras(intent.extras ?: Bundle()) // Copy extras
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
