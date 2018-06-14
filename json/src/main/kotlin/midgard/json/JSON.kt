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

    fun toBoolean(value: Any): Boolean {
        when (value) {
            is Boolean -> return value
            is String -> {
                when {
                    "true".equals(value, ignoreCase = true) -> return true
                    "false".equals(value, ignoreCase = true) -> return false
                }
            }
        }
        throw IllegalArgumentException("Not a boolean: $value")
    }

    fun toDouble(value: Any): Double {
        when (value) {
            is Double -> return value
            is Number -> return value.toDouble()
            is String -> try {
                return value.toDouble()
            } catch (ignored: NumberFormatException) {
            }
        }
        throw IllegalArgumentException("Not a double: $value")
    }

    fun toInt(value: Any): Int {
        when (value) {
            is Int -> return value
            is Number -> return value.toInt()
            is String -> try {
                return value.toDouble().toInt()
            } catch (ignored: NumberFormatException) {
            }
        }
        throw IllegalArgumentException("Not an integer: $value")
    }

    fun toLong(value: Any): Long {
        when (value) {
            is Long -> return value
            is Number -> return value.toLong()
            is String -> try {
                return value.toDouble().toLong()
            } catch (ignored: NumberFormatException) {
            }
        }
        throw IllegalArgumentException("Not an long: $value")
    }

    //todo: avoid auto-conversion?
    fun toString(value: Any) = when (value) {
        is String -> value
        else -> value.toString()
    }
}
