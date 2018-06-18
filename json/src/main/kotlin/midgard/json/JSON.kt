package midgard.json

import kotlin.reflect.KClass

internal object JSON {

    fun checkDouble(d: Double) = when {
        d.isInfinite() || d.isNaN() -> throw IllegalArgumentException("Illegal numeric value: $d")
        else -> d
    }

    fun toBoolean(value: Any) = when (value) {
        is Boolean -> value
        is String -> when (value) {
            "true" -> true
            "false" -> false
            else -> throwTypeError(value, Boolean::class)
        }
        else -> throwTypeError(value, Boolean::class)
    }

    fun toDouble(value: Any) = when (value) {
        is Double -> value
        is String -> value.toDoubleOrNull() ?: throwTypeError(value, Double::class)
        else -> throwTypeError(value, Double::class)
    }

    fun toLong(value: Any) = when (value) {
        is Long -> value
        is String -> value.toLongOrNull() ?: throwTypeError(value, Long::class)
        else -> throwTypeError(value, Long::class)
    }

    fun toString(value: Any) = value as? String ?: throwTypeError(value, String::class)

    fun toObject(value: Any) = value as? JSONObject ?: throwTypeError(value, JSONObject::class)

    fun toArray(value: Any) = value as? JSONArray ?: throwTypeError(value, JSONArray::class)

    private fun throwTypeError(value: Any, expectedType: KClass<out Any>): Nothing =
            throw IllegalArgumentException("Illegal field value: '$value'. Expected: '${expectedType.simpleName}', got: '${value::class.simpleName}'")
}
