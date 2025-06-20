package com.joaomgcd.taskerpluginlibrary

sealed class TaskerPluginException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class TaskerPluginExceptionNoInput(val pluginName: String) : TaskerPluginException("No input for $pluginName")
class TaskerPluginExceptionNoAction(val pluginName: String) : TaskerPluginException("No action for $pluginName")
class TaskerPluginExceptionNoService(val pluginName: String) : TaskerPluginException("No service for $pluginName")
class TaskerPluginExceptionServiceNotPersistent(val pluginName: String) : TaskerPluginException("$pluginName service is not persistent")
class TaskerPluginExceptionServicePermission(val pluginName: String) : TaskerPluginException("$pluginName service does not have BIND_JOB_SERVICE permission")
class TaskerPluginExceptionServiceNotExported(val pluginName: String) : TaskerPluginException("$pluginName service is not exported")
class TaskerPluginExceptionServiceNotEnabled(val pluginName: String) : TaskerPluginException("$pluginName service is not enabled")
class TaskerPluginExceptionServiceNoIntentFilter(val pluginName: String) : TaskerPluginException("$pluginName service does not have an intent filter for action com.joaomgcd.taskerpluginlibrary.ACTION_EXECUTE_PLUGIN")
class TaskerPluginExceptionValuesDontMatch(val field:String,val valueExpected: Any?, val valueReceived: Any?) : TaskerPluginException("Field $field: value expected: $valueExpected, value received: $valueReceived")
class TaskerPluginExceptionMissingParameter(val parameter:String) : TaskerPluginException("Missing parameter $parameter")
class TaskerPluginExceptionOther(val error:String) : TaskerPluginException(error)
