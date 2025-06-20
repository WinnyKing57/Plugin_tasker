package com.joaomgcd.taskerpluginlibrary.input

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.joaomgcd.taskerpluginlibrary.Serializable
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


private const val REGULAR_VARIABLES_PREFIX = "regular."
private const val CONNECTION_VARIABLES_PREFIX = "connection."
private const val CONNECTION_VARIABLES_RENAME_SEPARATOR = ":"
private const val RENAME_VARIABLES_PREFIX = "rename."

open class TaskerInputRoot : Serializable {
    @Transient
    internal var jsonObject: JsonObject? = null
    override fun toString(): String = jsonObject?.toString() ?: Gson().toJson(this)

    companion object {
        internal fun getInputFields(input: TaskerInputRoot): List<TaskerInputField<Any>> {
            val inputFields = mutableListOf<TaskerInputField<Any>>()
            for (property in input::class.memberProperties) {
                val taskerInputField = input.findInputField(property) ?: continue
                inputFields.add(taskerInputField)
            }
            return inputFields
        }
    }
}

data class TaskerInput<TInput>(
    val connection: Connection,
    val regular: Map<String, String>?,
    val input: TInput?
) : Serializable {
    data class Connection(
        val name: String,
        val type: String,
        val runnerSignature: String?
    ) : Serializable {
        val runnerClass by lazy { runnerSignature?.let { Class.forName(it) } }
    }


    companion object {
        private val gson = Gson()
        fun <TInput> fromJson(jsonString: String, inputClass: Class<TInput>): TaskerInput<TInput>? {
            return try {
                val json = gson.fromJson(jsonString, JsonObject::class.java)
                val connection = gson.fromJson(json.getAsJsonObject("connection"), Connection::class.java)
                val regular = json.getAsJsonObject("regular")?.let { regularJson ->
                    regularJson.entrySet().associate { it.key to it.value.asString }
                }
                val inputJson = json.getAsJsonObject("input")
                val input = gson.fromJson(inputJson, inputClass) as TInput
                if (input is TaskerInputRoot) {
                    input.jsonObject = inputJson
                }
                TaskerInput(connection, regular, input)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                null
            }
        }

        fun <TInput : Any> toJson(input: TInput, runnerClass: Class<out TaskerPluginRunner<TInput, *>>?): String {
            val inputObject = JsonObject()

            val inputFields = if (input is TaskerInputRoot) getInputFields(input) else null
            fun getInputValue(key: String): JsonElement? {
                if (input !is TaskerInputRoot) return null
                val value = inputFields?.firstOrNull { it.key == key }?.value ?: return null
                return gson.toJsonTree(value)
            }

            val inputJsonObject = JsonObject()


            if (input is TaskerInputRoot) {
                for (property in input::class.memberProperties) {
                    val annotation = property.findAnnotation<TaskerInputFieldAnno>() ?: continue
                    val key = annotation.key
                    val value = getInputValue(key) ?: continue
                    inputJsonObject.add(key, value)
                }
            } else {
                val inputJson = gson.toJsonTree(input)
                if (inputJson is JsonObject) {
                    inputJson.entrySet().forEach { inputJsonObject.add(it.key, it.value) }
                } else {
                    // Handle cases where input is a primitive or a list, though Tasker typically expects an object
                    inputJsonObject.add("value", inputJson),
                }
            }


            inputObject.add("input", inputJsonObject)

            val connectionObject = JsonObject()
            connectionObject.addProperty("name", "")
            connectionObject.addProperty("type", "")
            runnerClass?.name?.let { connectionObject.addProperty("runnerSignature", it) }
            inputObject.add("connection", connectionObject)

            return inputObject.toString()
        }


    }
}
