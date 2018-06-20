package midgard.json

open class JSONTokener(protected val inputJson: String) {

    protected val stopChar = Character.MAX_VALUE // TODO: check if it's safe.

    /**
     * The index of the next character to be read.
     * When the input is exhausted, this equals the input's size.
     */
    protected var pos: Int = if (inputJson.startsWith("\ufeff")) 1 else 0 // Consume an optional byte order mark (BOM) if it exists

    /** Returns the next value from the input. */
    open fun nextValue(): Any? {
        val c = nextCleanInternal()
        return when (c) {
            stopChar -> throw IllegalArgumentException("End of input ${this}")
            '{' -> readObject()
            '[' -> readArray()
            '\'', '"' -> nextString(c)
            else -> {
                pos--
                readLiteral()
            }
        }
    }

    open fun nextCleanInternal(): Char {
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
                '#' -> skipToEndOfLine() // Skip a # hash end-of-line comment.

                else -> return c
            }
        }
        return stopChar
    }

    /**
     * Advances the position until after the next newline character. If the line
     * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
     * caller.
     */
    open fun skipToEndOfLine() {
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
    open fun nextString(quote: Char): String? {
        // For strings that are free of escape sequences, we can just extract
        // the result as a substring of the input. But if we encounter an escape
        // sequence, we need to use a StringBuilder to compose the result.
        var builder: StringBuilder? = null

        // the index of the first character not yet appended to the builder.
        var start = pos

        while (pos < inputJson.length) {
            val c = inputJson[pos++]
            if (c == quote) {
                return if (builder == null) {
                    String(inputJson.substring(start, pos - 1).toCharArray()) // a new string avoids memory leaks
                } else {
                    String(builder.append(inputJson, start, pos - 1))
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
    open fun readEscapeCharacter(): Char {
        val escaped = inputJson[pos++]
        when (escaped) {
            'u' -> {
                if (pos + 4 > inputJson.length) {
                    throw IllegalArgumentException("Unterminated escape sequence $this")
                }
                val hex = inputJson.substring(pos, pos + 4)
                pos += 4
                try {
                    return hex.toInt(16).toChar()
                } catch (nfe: NumberFormatException) {
                    throw IllegalArgumentException("Invalid escape sequence: $hex $this")
                }
            }
            't' -> return '\t'
            'b' -> return '\b'
            'n' -> return '\n'
            'r' -> return '\r'
        //todo: 'f' -> return '\f'
            else -> return escaped
        }
    }

    /**
     * Reads a null, boolean, numeric or unquoted string literal value. Numeric
     * values will be returned as an Long, or Double, inputJson that order of
     * preference.
     */
    open fun readLiteral(): Any? {
        val literal = nextToInternal(" :,;{}[]/\\#\t") // todo \f

        when {
            literal.isEmpty() -> throw IllegalArgumentException("Expected literal value $this")
            "null".equals(literal, ignoreCase = true) -> return null
            "true".equals(literal, ignoreCase = true) -> return true
            "false".equals(literal, ignoreCase = true) -> return false

        // try to parse as an integral type...
            literal.indexOf('.') == -1 -> {
                var base = 10
                var number = literal
                if (number.length > 1 && number[0] == '0') {
                    val c1 = number[1]
                    if (c1 == 'x' || c1 == 'X') {
                        number = number.substring(2)
                        base = 16
                    } else {
                        number = number.substring(1)
                        base = 8
                    }
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
     * Returns the string up to but not including any of the given characters or a newline character.
     * This does not consume the excluded character.
     */
    open fun nextToInternal(excluded: String): String {
        val start = pos
        while (pos < inputJson.length) {
            val c = inputJson[pos]
            if (c == '\r' || c == '\n' || excluded.contains(c)) {
                return inputJson.substring(start, pos)
            }
            pos++
        }
        return inputJson.substring(start)
    }

    /** Reads a sequence of key/value pairs and the trailing closing brace '}' of
     * an object. The opening brace '{' should have already been read.*/
    open fun readObject(): JSONObject {
        val result = JSONObject()

        // Peek to see if this is the empty object
        val first = nextCleanInternal()
        if (first == '}') {
            return result
        }
        if (first != stopChar) {
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

            // Expect the name/value separator to be either a colon ':'
            val separator = nextCleanInternal()
            if (separator != ':') {
                throw IllegalArgumentException("Expected ':' after $name ${this}")
            }

            result.setUnsafe(name, nextValue())

            val nextChar = nextCleanInternal()
            when (nextChar) {
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
    open fun readArray(): JSONArray {
        val result = JSONArray()

        /* to cover input that ends with ",]". */
        var hasTrailingSeparator = false

        while (true) {
            var cont = false
            val c1 = nextCleanInternal()
            when (c1) {
                stopChar -> throw IllegalArgumentException("Unterminated array ${this}")
                ']' -> {
                    if (hasTrailingSeparator) {
                        result.add(null)
                    }
                    return result
                }
                ',', ';' -> { // A separator without a value first means "null"
                    result.add(null)
                    hasTrailingSeparator = true
                    cont = true
                }
                else -> pos--
            }

            if (cont) {
                continue
            }

            result.addUnsafe(nextValue())

            val c2 = nextCleanInternal()
            when (c2) {
                ']' -> return result
                ',', ';' -> hasTrailingSeparator = true
                else -> throw IllegalArgumentException("Unterminated array ${this}")
            }
        }
    }

    /** Returns the current position and the entire input string. */
    override fun toString() = "at character $pos of $inputJson"
}
