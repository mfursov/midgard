package midgard.json

import java.util.Arrays
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.indices
import kotlin.collections.last

open class JSONStringer @JvmOverloads constructor(indentSpaces: Int = 0) {

    /**
     * The output data, containing at most one top-level array or object.
     */
    val out = StringBuilder()

    /**
     * Unlike the original implementation, this stack isn't limited to 20
     * levels of nesting.
     */
    private val stack = ArrayList<Scope>()

    /**
     * A string containing a full set of spaces for a single level of
     * indentation, or null for no pretty printing.
     */
    private val indent: String?

    /**
     * Lexical scoping elements within this stringer, necessary to insert the
     * appropriate separator characters (ie. commas and colons) and to detect
     * nesting errors.
     */
    enum class Scope {

        /**
         * An array with no elements requires no separators or newlines before
         * it is closed.
         */
        EMPTY_ARRAY,

        /**
         * A array with at least one value requires a comma and newline before
         * the next element.
         */
        NONEMPTY_ARRAY,

        /**
         * An object with no keys or values requires no separators or newlines
         * before it is closed.
         */
        EMPTY_OBJECT,

        /**
         * An object whose most recent element is a key. The next element must
         * be a value.
         */
        DANGLING_KEY,

        /**
         * An object with at least one name/value pair requires a comma and
         * newline before the next element.
         */
        NONEMPTY_OBJECT,

        /**
         * A special bracketless array needed by JSONStringer.join() and
         * JSONObject.quote() only. Not used for JSON encoding.
         */
        NULL
    }

    init {
        indent = if (indentSpaces > 0) {
            val indentChars = CharArray(indentSpaces)
            Arrays.fill(indentChars, ' ')
            String(indentChars)
        } else null
    }

    /**
     * Begins encoding a new array. Each call to this method must be paired with
     * a call to [.endArray].
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    open fun array(): JSONStringer {
        return open(Scope.EMPTY_ARRAY, "[")
    }

    /**
     * Ends encoding the current array.
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    open fun endArray(): JSONStringer {
        return close(Scope.EMPTY_ARRAY, Scope.NONEMPTY_ARRAY, "]")
    }

    /**
     * Begins encoding a new object. Each call to this method must be paired
     * with a call to [.endObject].
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    open fun startObject(): JSONStringer {
        return open(Scope.EMPTY_OBJECT, "{")
    }

    /**
     * Ends encoding the current object.
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    open fun endObject(): JSONStringer {
        return close(Scope.EMPTY_OBJECT, Scope.NONEMPTY_OBJECT, "}")
    }

    /**
     * Enters a new scope by appending any necessary whitespace and the given
     * bracket.
     */
    open fun open(empty: Scope, openBracket: String): JSONStringer {
        if (stack.isEmpty() && out.isNotEmpty()) {
            throw IllegalArgumentException("Nesting problem: multiple top-level roots")
        }
        beforeValue()
        stack.add(empty)
        out.append(openBracket)
        return this
    }

    /**
     * Closes the current scope by appending any necessary whitespace and the
     * given bracket.
     */
    open fun close(empty: Scope, nonempty: Scope, closeBracket: String): JSONStringer {
        val context = peek()
        if (context != nonempty && context != empty) {
            throw IllegalArgumentException("Nesting problem")
        }

        stack.removeAt(stack.size - 1)
        if (context == nonempty) {
            newline()
        }
        out.append(closeBracket)
        return this
    }

    /**
     * Returns the value on the top of the stack.
     */
    open fun peek(): Scope {
        if (stack.isEmpty()) {
            throw IllegalArgumentException("Nesting problem")
        }
        return stack.last()
    }

    /**
     * Replace the value on the top of the stack with the given value.
     */
    protected open fun replaceTop(topOfStack: Scope) {
        stack[stack.size - 1] = topOfStack
    }

    /**
     * Encodes `value`.
     *
     * @param value a [JSONObject], [JSONArray], String, Boolean,
     * Long, Double or null. May not be [NaNs][Double.isNaN]
     * or [infinities][Double.isInfinite].
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    open fun value(value: Any?): JSONStringer {
        if (stack.isEmpty()) {
            throw IllegalArgumentException("Nesting problem")
        }

        if (value is JSONArray) {
            value.encode(this)
            return this

        } else if (value is JSONObject) {
            value.encode(this)
            return this
        }

        beforeValue()

        when {
            value === null || value is Boolean -> out.append(value)
            value is Number -> out.append(numberToString(value as Number?))
            else -> string(value.toString())
        }
        return this
    }

    /**
     * Encodes `value` to this stringer.
     *
     * @param value The value to encode.
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    open fun value(value: Boolean): JSONStringer {
        if (stack.isEmpty()) {
            throw IllegalArgumentException("Nesting problem")
        }
        beforeValue()
        out.append(value)
        return this
    }

    /**
     * Encodes `value` to this stringer.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    open fun value(value: Double): JSONStringer {
        if (stack.isEmpty()) {
            throw IllegalArgumentException("Nesting problem")
        }
        beforeValue()
        out.append(numberToString(value))
        return this
    }

    /**
     * Encodes `value` to this stringer.
     *
     * @param value The value to encode.
     * @return this stringer.
     * @throws IllegalArgumentException If we have an internal error. Shouldn't happen.
     */
    open fun value(value: Long): JSONStringer {
        if (stack.isEmpty()) {
            throw IllegalArgumentException("Nesting problem")
        }
        beforeValue()
        out.append(value)
        return this
    }

    /**
     * Encodes `key`/`value` pair to this stringer.
     *
     * @param entry The entry to encode.
     * @return this stringer.
     * @throws IllegalArgumentException If we have an internal error. Shouldn't happen.
     */
    open fun entry(entry: Map.Entry<String, Any?>): JSONStringer {
        this.key(entry.key).value(entry.value)
        return this
    }

    open fun string(value: String) {
        out.append("\"")
        var currentChar: Char = 0.toChar()

        var i = 0
        val length = value.length
        while (i < length) {
            val previousChar = currentChar
            currentChar = value[i]

            /*
             * From RFC 4627, "All Unicode characters may be placed within the
             * quotation marks except for the characters that must be escaped:
             * quotation mark, reverse solidus, and the control characters
             * (U+0000 through U+001F)."
             */
            when (currentChar) {
                '"', '\\' -> out.append('\\').append(currentChar)

                '/' -> {
                    // it makes life easier for HTML embedding of javascript if we escape </ sequences
                    if (previousChar == '<') {
                        out.append('\\')
                    }
                    out.append(currentChar)
                }

                '\t' -> out.append("\\t")

                '\b' -> out.append("\\b")

                '\n' -> out.append("\\n")

                '\r' -> out.append("\\r")

            //todo: '\f' -> out.append("\\f")

                else -> if (currentChar.toInt() <= 0x1F) {
                    out.append(String.format("\\u%04x", currentChar.toInt()))
                } else {
                    out.append(currentChar)
                }
            }
            i++

        }
        out.append("\"")
    }

    open fun newline() {
        if (indent == null) {
            return
        }

        out.append("\n")
        for (i in stack.indices) {
            out.append(indent)
        }
    }

    /**
     * Creates String representation of the key (property name) to this stringer
     * Override this method to provide your own representation of the name.
     *
     * @param name the name of the forthcoming value.
     * @return this stringer.
     */
    open fun createKey(name: String): JSONStringer {
        string(name)
        return this
    }

    /**
     * Encodes the key (property name) to this stringer.
     *
     * @param name the name of the forthcoming value. May not be null.
     * @return this stringer.
     * @throws IllegalArgumentException on internal errors, shouldn't happen.
     */
    fun key(name: String): JSONStringer {
        beforeKey()
        return createKey(name)
    }

    /**
     * Inserts any necessary separators and whitespace before a name. Also
     * adjusts the stack to expect the key's value.
     */
    open fun beforeKey() {
        val context = peek()
        if (context == Scope.NONEMPTY_OBJECT) { // first in object
            out.append(',')
        } else if (context != Scope.EMPTY_OBJECT) { // not in an object!
            throw IllegalArgumentException("Nesting problem")
        }
        newline()
        replaceTop(Scope.DANGLING_KEY)
    }

    /**
     * Inserts any necessary separators and whitespace before a literal value,
     * inline array, or inline object. Also adjusts the stack to expect either a
     * closing bracket or another element.
     */
    open fun beforeValue() {
        if (stack.isEmpty()) {
            return
        }

        val context = peek()
        when {
            context == Scope.EMPTY_ARRAY -> { // first in array
                replaceTop(Scope.NONEMPTY_ARRAY)
                newline()
            }
            context == Scope.NONEMPTY_ARRAY -> { // another in array
                out.append(',')
                newline()
            }
            context == Scope.DANGLING_KEY -> { // value for key
                out.append(if (indent == null) ":" else ": ")
                replaceTop(Scope.NONEMPTY_OBJECT)
            }
            context != Scope.NULL -> throw IllegalArgumentException("Nesting problem")
        }
    }

    /**
     * Returns the encoded JSON string.
     * <br></br>
     *
     * If invoked with unterminated arrays or unclosed objects, this method's
     * return value is undefined.
     * <br></br>
     *
     * **Warning:** although it contradicts the general contract
     * of [Object.toString], this method returns null if the stringer
     * contains no data.
     */
    override fun toString() = if (out.isEmpty()) "" else out.toString()

    companion object {
        private const val NEGATIVE_ZERO = -0.0

        /**
         * Encodes the number as a JSON string.
         *
         * @param number a finite value. May not be [NaNs][Double.isNaN] or
         * [infinities][Double.isInfinite].
         * @return The encoded number in string form.
         * @throws IllegalArgumentException On internal errors. Shouldn't happen.
         */
        fun numberToString(number: Number?): String {
            if (number == null) {
                throw IllegalArgumentException("Number must be non-null")
            }

            val doubleValue = number.toDouble()
            JSON.checkDouble(doubleValue)

            // the original returns "-0" instead of "-0.0" for negative zero
            if (number == NEGATIVE_ZERO) {
                return "-0"
            }

            val longValue = number.toLong()
            return if (doubleValue == longValue.toDouble()) longValue.toString() else number.toString()

        }
    }

}
