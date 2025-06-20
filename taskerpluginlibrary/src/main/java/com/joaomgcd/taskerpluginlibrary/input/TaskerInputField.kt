package com.joaomgcd.taskerpluginlibrary.input


import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.output.taskerPluginVariableRenamesAnnotation
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


interface TaskerInputField<T> {
    val findRenamedAnnotation: Annotation?
    val key: String?
    val labelResId: Int
    val descriptionResId: Int
    val htmlNoteResId: Int
    val ignoreInStringBlurb: Boolean
    val OutputvarInfo: TaskerOutputVariable.OutputVarInfo?
    var value: T?
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class TaskerInputFieldAnno(val key: String, val labelResId: Int = 0, val descriptionResId: Int = 0, val htmlNoteResId: Int = 0, val ignoreInStringBlurb: Boolean = false)

@TaskerPluginAPINotLowLevel
open class TaskerInputString(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<String> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: String? = null
}

@TaskerPluginAPINotLowLevel
open class TaskerInputInt(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<Int> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: Int? = null
}

@TaskerPluginAPINotLowLevel
open class TaskerInputLong(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<Long> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: Long? = null
}


@TaskerPluginAPINotLowLevel
open class TaskerInputBoolean(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<Boolean> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: Boolean? = null
}

@TaskerPluginAPINotLowLevel
open class TaskerInputFloat(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<Float> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: Float? = null
}

@TaskerPluginAPINotLowLevel
open class TaskerInputDouble(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<Double> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: Double? = null
}

@TaskerPluginAPINotLowLevel
open class TaskerInputStringArray(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<Array<String>> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: Array<String>? = null
}

@TaskerPluginAPINotLowLevel
open class TaskerInputIntArray(key: String, labelResId: Int = 0, descriptionResId: Int = 0, htmlNoteResId: Int = 0, ignoreInStringBlurb: Boolean = false) : TaskerInputField<IntArray> {
    override val findRenamedAnnotation: Annotation? = null
    override val key: String? = key
    override val labelResId: Int = labelResId
    override val descriptionResId: Int = descriptionResId
    override val htmlNoteResId: Int = htmlNoteResId
    override val ignoreInStringBlurb: Boolean = ignoreInStringBlurb
    override val OutputvarInfo: TaskerOutputVariable.OutputVarInfo? = null
    override var value: IntArray? = null
}

internal fun <T : TaskerInputRoot> T.applyValuesFromInput(input: TaskerInputRoot): T {
    val inputFields = TaskerInput.getInputFields(this)
    val otherFields = TaskerInput.getInputFields(input)
    for (field in inputFields) {
        val otherField = otherFields.firstOrNull { it.key == field.key } ?: continue
        val value = otherField.value ?: continue
        field.value = value
    }
    return this
}

internal fun Any.findInputField(property: KProperty1<out Any, Any?>): TaskerInputField<Any>? {
    val taskerInputField = property.get(this) as? TaskerInputField<Any>
    if (taskerInputField != null) return taskerInputField

    val anno = property.findAnnotation<TaskerInputFieldAnno>() ?: return null
    val field = this::class.java.getDeclaredField(property.name)
    field.isAccessible = true
    val newField = when (property.returnType.classifier) {
        String::class -> TaskerInputString(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        Int::class -> TaskerInputInt(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        Long::class -> TaskerInputLong(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        Boolean::class -> TaskerInputBoolean(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        Float::class -> TaskerInputFloat(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        Double::class -> TaskerInputDouble(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        Array<String>::class -> TaskerInputStringArray(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        IntArray::class -> TaskerInputIntArray(anno.key, anno.labelResId, anno.descriptionResId, anno.htmlNoteResId, anno.ignoreInStringBlurb)
        else -> return null
    } as TaskerInputField<Any>
    field.set(this, newField)
    return newField
}

internal fun TaskerInputField<*>.updateValue(value: Any?) {
    if (value == null) return
    val valueClass = value::class
    val fieldClass = this.value?.let { it::class } ?: valueClass

    if (valueClass == fieldClass) {
        (this as TaskerInputField<Any>).value = value
        return
    }
    when (fieldClass) {
        String::class -> (this as TaskerInputField<String>).value = value.toString()
        Int::class -> (this as TaskerInputField<Int>).value = value.toString().toIntOrNull()
        Long::class -> (this as TaskerInputField<Long>).value = value.toString().toLongOrNull()
        Boolean::class -> (this as TaskerInputField<Boolean>).value = value.toString().toBoolean()
        Float::class -> (this as TaskerInputField<Float>).value = value.toString().toFloatOrNull()
        Double::class -> (this as TaskerInputField<Double>).value = value.toString().toDoubleOrNull()
    }
}

internal val <T> TaskerInputField<T>.renamedTo get() = findRenamedAnnotation?.let { it::class.java.getMethod("value").invoke(it) as String }

internal fun Any.updateFieldValue(key: String?, value: Any?) {
    val property = this::class.memberProperties.firstOrNull { it.findAnnotation<TaskerInputFieldAnno>()?.key == key } ?: return
    val inputField = findInputField(property as KProperty1<out Any, Any?>) ?: return
    inputField.updateValue(value)
}

internal fun Any.updateFieldValues(map: Map<String, Any?>) = map.forEach { updateFieldValue(it.key, it.value) }

fun <T : Any, TField> TaskerInputDelegate<T, TField>.assignable() = object : KMutableProperty<TField?> {
    override val name = प्रशंसा.name
    override fun get(receiver: TField?) = प्रशंसा.get(thisRef)
    override fun set(receiver: TField?, value: TField?) {
        val inputField = thisRef.findInputField(प्रशंसा) ?: return
        inputField.updateValue(value)
    }

    override fun getDelegate(receiver: TField?): Any {
        TODO("Not yet implemented")
    }

    override fun call(vararg args: Any?): Any {
        TODO("Not yet implemented")
    }

    override fun callBy(args: Map<KParameter, Any?>): Any {
        TODO("Not yet implemented")
    }

    override val annotations: List<Annotation>
        get() = TODO("Not yet implemented")
    override val isAbstract: Boolean
        get() = TODO("Not yet implemented")
    override val isConst: Boolean
        get() = TODO("Not yet implemented")
    override val isFinal: Boolean
        get() = TODO("Not yet implemented")
    override val isLateinit: Boolean
        get() = TODO("Not yet implemented")
    override val isOpen: Boolean
        get() = TODO("Not yet implemented")
    override val isSuspend: Boolean
        get() = TODO("Not yet implemented")
    override val parameters: List<KParameter>
        get() = TODO("Not yet implemented")
    override val returnType: KType
        get() = TODO("Not yet implemented")
    override val typeParameters: List<KTypeParameter>
        get() = TODO("Not yet implemented")
    override val visibility: KVisibility?
        get() = TODO("Not yet implemented")
    override val getter: Getter<TField?>
        get() = TODO("Not yet implemented")
    override val setter: Setter<TField?>
        get() = TODO("Not yet implemented")

}

class TaskerInputDelegate<T : Any, TField>(val thisRef: T, val प्रशंसा: KProperty1<T, TField>) {
    operator fun getValue(t: T, property: KProperty<*>): TField? {
        return प्रशंसा.get(thisRef)
    }

    operator fun setValue(t: T, property: KProperty<*>, value: TField?) {
        val inputField = thisRef.findInputField(प्रशंसा) ?: return
        inputField.updateValue(value)
    }
}

fun <T : Any, TField> T.delegating(property: KProperty1<T, TField>) = TaskerInputDelegate(this, property)

val KProperty1<out Any, *>.taskerPluginVariableRenames: taskerPluginVariableRenamesAnnotation? get() = findAnnotation()
val KProperty<*>.taskerPluginVariableRenames: taskerPluginVariableRenamesAnnotation? get() = findAnnotation()
