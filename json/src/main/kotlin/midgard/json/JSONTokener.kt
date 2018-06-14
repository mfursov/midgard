package midgard.json

private const val STOP_CHAR = Character.MAX_VALUE // TODO: check if it's safe.

class JSONTokener(jsonString: String) {

    /** The input JSON. */
    private val inputJson: String

    /**  The index of the next character to be returned by [.next]. When the input is exhausted, this equals the input's size.*/
    private var pos: Int = 0

    init {
        var json = jsonString
        if (json.startsWith("\ufeff")) { // consume an optional byte order mark (BOM) if it exists
            json = json.substring(1)
        }
        this.inputJson = json
    }

    /** Returns the next value from the input. */
    fun nextValue(): Any? {
        val c = nextCleanInternal()
        return when (c) {
            STOP_CHAR -> throw IllegalArgumentException("End of input ${this}")
            '{' -> readObject()
            '[' -> readArray()
            '\'', '"' -> nextString(c)
            else -> {
                pos--
                readLiteral()
            }
        }
    }

    private fun nextCleanInternal(): Char {
        while (pos < inputJson.length) {
            val c = inputJson[pos++]
            when (c) {
                '\t', ' ', '\n', '\r' -> {
                }
                '/' -> {
                    if (pos == inputJson.length) {
                        return c
                    }
                    val peek = inputJson[pos]
                    when (peek) {
                        '*' -> { // skip a /* c-style comment */
                            pos++
                            val commentEnd = inputJson.indexOf("*/", pos)
                            if (commentEnd == -1) {
                                throw IllegalArgumentException("Unterminated comment ${this}")
                            }
                            pos = commentEnd + 2
                        }
                        '/' -> { // skip a // end-of-line comment
                            pos++
                            skipToEndOfLine()
                        }

                        else -> return c
                    }
                }
                '#' -> { // Skip a # hash end-of-line comment.
                    skipToEndOfLine()
                }

                else -> return c
            }
        }
        return STOP_CHAR
    }

    /**
     * Advances the position until after the next newline character. If the line
     * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
     * caller.
     */
    private fun skipToEndOfLine() {
        while (pos < inputJson.length) {
            val c = inputJson[pos]
            if (c == '\r' || c == '\n') {
                pos++
                break
            }
            pos++
        }
    }

    /**
     * Returns the string up to but not including `quote`, unescaping any
     * character escape sequences encountered along the way. The opening quote
     * should have already been read. This consumes the closing quote, but does
     * not include it inputJson the returned string.
     *
     * @param quote either ' or ".
     * @return The unescaped string.
     * @throws IllegalArgumentException if the string isn't terminated by a closing quote correctly.
     */
    fun nextString(quote: Char): String? {
        /*
         * For strings that are free of escape sequences, we can just extract
         * the result as a substring of the input. But if we encounter an escape
         * sequence, we need to use a StringBuilder to compose the result.
         */
        var builder: StringBuilder? = null

        /* the index of the first character not yet appended to the builder. */
        var start = pos

        while (pos < inputJson.length) {
            val c = inputJson[pos++]
            if (c == quote) {
                return if (builder == null) { // a new string avoids leaking memory
                    inputJson.substring(start, pos - 1)
                } else {
                    builder.append(inputJson, start, pos - 1)
                    builder.toString()
                }
            }

            if (c == '\\') {
                if (pos == inputJson.length) {
                    throw IllegalArgumentException("Unterminated escape sequence ${this}")
                }
                if (builder == null) {
                    builder = StringBuilder()
                }
                builder.append(inputJson, start, pos - 1)
                builder.append(readEscapeCharacter())
                start = pos
            }
        }
        throw IllegalArgumentException("Unterminated string ${this}")
    }

    /**
     * Unescapes the character identified by the character or characters that
     * immediately follow a backslash. The backslash '\' should have already
     * been read. This supports both unicode escapes "u000A" and two-character
     * escapes "\n".
     */
    private fun readEscapeCharacter(): Char {
        val escaped = inputJson[pos++]
        when (escaped) {
            'u' -> {
                if (pos + 4 > inputJson.length) {
                    throw IllegalArgumentException("Unterminated escape sequence $this")
                }
                val hex = inputJson.substring(pos, pos + 4)
                pos += 4
                try {
                    return Integer.parseInt(hex, 16).toChar()
                } catch (nfe: NumberFormatException) {
                    throw IllegalArgumentException("Invalid escape sequence: $hex $this")
                }
            }
            't' -> return '\t'
            'b' -> return '\b'
            'n' -> return '\n'
            'r' -> return '\r'
        //todo: 'f' -> return '\f'
            '\'', '"', '\\' -> return escaped
            else -> return escaped
        }
    }

    /**
     * Reads a null, boolean, numeric or unquoted string literal value. Numeric
     * values will be returned as an Long, or Double, inputJson that order of
     * preference.
     */
    private fun readLiteral(): Any? {
        val literal = nextToInternal("{}[]/\\:,=;# \t") // todo \f

        when {
            literal.isEmpty() -> throw IllegalArgumentException("Expected literal value $this")
            "null".equals(literal, ignoreCase = true) -> return null
            "true".equals(literal, ignoreCase = true) -> return true
            "false".equals(literal, ignoreCase = true) -> return false

        // try to parse as an integral type...
            literal.indexOf('.') == -1 -> {
                var base = 10
                var number = literal
                if (number.startsWith("0x") || number.startsWith("0X")) {
                    number = number.substring(2)
                    base = 16
                } else if (number.startsWith("0") && number.length > 1) {
                    number = number.substring(1)
                    base = 8
                }
                try {
                    return number.toLong(base)
                } catch (e: NumberFormatException) {
                    // This only happens for integral numbers greater than Long.MAX_VALUE,
                    // numbers inputJson exponential form (5e-10) and unquoted strings.
                    // Fall through to try floating point.
                }
            }
        }

        // next try to parse as a floating point or give up...
        return literal.toDoubleOrNull() ?: literal
    }

    /**
     * Returns the string up to but not including any of the given characters or
     * a newline character. This does not consume the excluded character.
     */
    private fun nextToInternal(excluded: String): String {
        val start = pos
        while (pos < inputJson.length) {
            val c = inputJson[pos]
            if (c == '\r' || c == '\n' || excluded.indexOf(c) != -1) {
                return inputJson.substring(start, pos)
            }
            pos++
        }
        return inputJson.substring(start)
    }

    /**
     * Reads a sequence of key/value pairs and the trailing closing brace '}' of
     * an object. The opening brace '{' should have already been read.
     */
    private fun readObject(): JSONObject {
        val result = JSONObject()

        /* Peek to see if this is the empty object. */
        val first = nextCleanInternal()
        if (first == '}') {
            return result
        }
        if (first != STOP_CHAR) {
            pos--
        }
        while (true) {
            val name = nextValue()
            if (name !is String) {
                when (name) {
                    null -> throw IllegalArgumentException("Names cannot be null ${this}")
                    else -> throw IllegalArgumentException(("Names must be strings, but $name is of type ${name::class}") + this)
                }
            }

            /*
             * Expect the name/value separator to be either a colon ':', an
             * equals sign '=', or an arrow "=>". The last two are bogus but we
             * include them because that's what the original implementation did.
             */
            val separator = nextCleanInternal()
            if (separator != ':' && separator != '=') {
                throw IllegalArgumentException("Expected ':' after $name ${this}")
            }
            if (pos < inputJson.length && inputJson[pos] == '>') {
                pos++
            }

            result.setUnsafe((name as String?)!!, nextValue())

            when (nextCleanInternal()) {
                '}' -> return result
                ';', ',' -> {
                }
                else -> throw IllegalArgumentException("Unterminated object ${this}")
            }
        }
    }

    /**
     * Reads a sequence of values and the trailing closing brace ']' of an
     * array. The opening brace '[' should have already been read. Note that
     * "[]" yields an empty array, but "[,]" returns a two-element array
     * equivalent to "[null,null]".
     */
    private fun readArray(): JSONArray {
        val result = JSONArray()

        /* to cover input that ends with ",]". */
        var hasTrailingSeparator = false

        while (true) {
            var cont = false
            when (nextCleanInternal()) {
                STOP_CHAR -> throw IllegalArgumentException("Unterminated array ${this}")
                ']' -> {
                    if (hasTrailingSeparator) {
                        result.addNull()
                    }
                    return result
                }
                ',', ';' -> {
                    /* A separator without a value first means "null". */
                    result.addNull()
                    hasTrailingSeparator = true
                    cont = true
                }
                else -> pos--
            }

            if (cont) {
                continue
            }

            result.addUnsafe(nextValue())

            when (nextCleanInternal()) {
                ']' -> return result
                ',', ';' -> hasTrailingSeparator = true
                else -> throw IllegalArgumentException("Unterminated array ${this}")
            }
        }
    }

    /**
     * Returns the current position and the entire input string.
     */
    override fun toString() = "at character $pos of $inputJson"

    /*
     * Legacy APIs.
     *
     * None of the methods below are on the critical path of parsing JSON
     * documents. They exist only because they were exposed by the original
     * implementation and may be used by some clients.
     */

    /**
     * Returns the next available character, or the null character '\0' if all
     * input has been exhausted. The return value of this method is ambiguous
     * for JSON strings that contain the character '\0'.
     *
     * @return the next character.
     */
    operator fun next() = if (pos < inputJson.length) inputJson[pos++] else '\u0000'

    /**
     * Returns the next available character if it equals `c`. Otherwise an
     * exception is thrown.
     *
     * @param c The character we are looking for.
     * @return the next character.
     * @throws IllegalArgumentException If the next character isn't `c`
     */
    fun next(c: Char): Char {
        val result = next()
        if (result != c) {
            throw IllegalArgumentException("Expected $c but was $result $this")
        }
        return result
    }

    /**
     * Returns the next `size` characters of the input.
     *
     *
     * The returned string shares its backing character array with this
     * tokener's input string. If a reference to the returned string may be held
     * indefinitely, you should use `new String(result)` to copy it first
     * to avoid memory leaks.
     *
     * @param length The desired number of characters to return.
     * @return The next few characters.
     * @throws IllegalArgumentException if the remaining input is not long enough to
     * satisfy this request.
     */
    fun next(length: Int): String {
        if (pos + length > inputJson.length) {
            throw IllegalArgumentException("$length is out of bounds $this")
        }
        val result = inputJson.substring(pos, pos + length)
        pos += length
        return result
    }
}
