package midgard.json

internal object JSON {
    /**
     * Returns the input if it is a JSON-permissible value; throws otherwise.
     */
    fun checkDouble(d: Double): Double {
        if (d.isInfinite() || d.isNaN()) {
            throw IllegalArgumentException("Forbidden numeric value: $d")
        }
        return d
    }

    fun toBoolean(value: Any?): Boolean? {
        when (value) {
            is Boolean -> return value
            is String -> {
                return when {
                    "true".equals(value, ignoreCase = true) -> true
                    "false".equals(value, ignoreCase = true) -> false
                    else -> null
                }
            }
        }
        return null
    }

    fun toDouble(value: Any?): Double? {
        when (value) {
            is Double -> return value
            is Number -> return value.toDouble()
            is String -> try {
                return value.toDouble()
            } catch (ignored: NumberFormatException) {
            }
        }
        return null
    }

    fun toInteger(value: Any?): Int? {
        when (value) {
            is Int -> return value
            is Number -> return value.toInt()
            is String -> try {
                return value.toDouble().toInt()
            } catch (ignored: NumberFormatException) {
            }
        }
        return null
    }

    fun toLong(value: Any?) = when (value) {
        is Long -> value
        is Number -> value.toLong()
        is String -> try {
            value.toDouble().toLong()
        } catch (ignored: NumberFormatException) {
            null
        }
        else -> null
    }

    fun toString(value: Any?) = when {
        value is String -> value
        value != null -> value.toString()
        else -> null
    }

    fun typeMismatch(indexOrName: Any, actual: Any?, requiredType: String): Nothing {
        if (actual == null) {
            throw IllegalArgumentException("Value at $indexOrName is null.")
        }
        throw IllegalArgumentException("Value $actual at $indexOrName cannot be converted to $requiredType")
    }

    fun typeMismatch(actual: Any?, requiredType: String): Nothing {
        if (actual == null) {
            throw IllegalArgumentException("Value is null.")
        }
        throw IllegalArgumentException("Value $actual cannot be converted to $requiredType")
    }
}
