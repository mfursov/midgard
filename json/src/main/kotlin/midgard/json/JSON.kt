package midgard.json

import kotlin.reflect.KClass

internal object JSON {

    fun checkDouble(d: Double): Double {
        if (d.isInfinite() || d.isNaN()) {
            throw IllegalArgumentException("Forbidden numeric value: $d")
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
        throw IllegalArgumentException(buildTypeError(value, Boolean::class))
    }

    fun toDouble(value: Any) = when (value) {
        is Double -> value
        is String -> value.toDoubleOrNull() ?: throw IllegalArgumentException(buildTypeError(value, Double::class))
        else -> throw IllegalArgumentException(buildTypeError(value, Double::class))
    }

    fun toLong(value: Any) = when (value) {
        is Long -> value
        is String -> value.toLongOrNull() ?: throw IllegalArgumentException(buildTypeError(value, Long::class))
        else -> throw IllegalArgumentException(buildTypeError(value, Long::class))
    }

    fun toString(value: Any) = when (value) {
        is String -> value
        else -> throw IllegalArgumentException(buildTypeError(value, String::class))
    }

    fun toObject(value: Any) = when (value) {
        is JSONObject -> value
        else -> throw IllegalArgumentException(buildTypeError(value, JSONObject::class))
    }

    fun toArray(value: Any) = when (value) {
        is JSONArray -> value
        else -> throw IllegalArgumentException(buildTypeError(value, JSONArray::class))
    }

    private fun buildTypeError(value: Any, expectedType: KClass<out Any>) = "Illegal field value: '$value'. Expected: '${expectedType.simpleName}', got: '${value::class.simpleName}'"
}
