package midgard.json

import kotlin.reflect.KClass

internal object JSON {

    fun checkDouble(d: Double): Double {
        if (d.isInfinite() || d.isNaN()) {
            throw IllegalArgumentException("Illegal numeric value: $d")
        }
        return d
    }

    fun toBoolean(value: Any): Boolean {
        when (value) {
            is Boolean -> return value
            is String -> when {
                "true".equals(value, ignoreCase = true) -> return true
                "false".equals(value, ignoreCase = true) -> return false
            }
        }
        throwTypeError(value, Boolean::class)
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
