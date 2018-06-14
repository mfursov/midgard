package midgard.json;

import java.io.IOException;
import java.io.Reader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSONTokener {

    /**
     * The input JSON.
     */
    @NotNull
    private final String in;

    /**
     * The index of the next character to be returned by {@link #next}. When
     * the input is exhausted, this equals the input's length.
     */
    private int pos;

    /**
     * @param in JSON encoded string. Null is not permitted and will yield a
     *           tokener that throws {@code NullPointerExceptions} when methods are
     *           called.
     */
    public JSONTokener(@NotNull String in) {
        // consume an optional byte order mark (BOM) if it exists
        if (in.startsWith("\ufeff")) {
            in = in.substring(1);
        }
        this.in = in;
    }

    public JSONTokener(@NotNull Reader input) throws IOException {
        StringBuilder s = new StringBuilder();
        char[] readBuf = new char[102400];
        int n = input.read(readBuf);
        while (n >= 0) {
            s.append(readBuf, 0, n);
            n = input.read(readBuf);
        }
        in = s.toString();
        pos = 0;
    }

    /**
     * Returns the next value from the input.
     *
     * @return a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     * Integer, Long, Double or {@link JSONObject#NULL}.
     * @throws IllegalArgumentException if the input is malformed.
     */
    @Nullable
    public Object nextValue() {
        char c = nextCleanInternal();
        switch (c) {
            case Character.MAX_VALUE:
                throw new IllegalArgumentException("End of input" + this);

            case '{':
                return readObject();

            case '[':
                return readArray();

            case '\'':
            case '"':
                return nextString(c);

            default:
                pos--;
                return readLiteral();
        }
    }

    private char nextCleanInternal() {
        while (pos < in.length()) {
            char c = in.charAt(pos++);
            switch (c) {
                case '\t':
                case ' ':
                case '\n':
                case '\r':
                    continue;

                case '/':
                    if (pos == in.length()) {
                        return c;
                    }

                    char peek = in.charAt(pos);
                    switch (peek) {
                        case '*':
                            // skip a /* c-style comment */
                            pos++;
                            int commentEnd = in.indexOf("*/", pos);
                            if (commentEnd == -1) {
                                throw new IllegalArgumentException("Unterminated comment" + this);
                            }
                            pos = commentEnd + 2;
                            continue;

                        case '/':
                            // skip a // end-of-line comment
                            pos++;
                            skipToEndOfLine();
                            continue;

                        default:
                            return c;
                    }

                case '#':
                    /*
                     * Skip a # hash end-of-line comment. The JSON RFC doesn't
                     * specify this behavior, but it's required to parse
                     * existing documents. See http://b/2571423.
                     */
                    skipToEndOfLine();
                    continue;

                default:
                    return c;
            }
        }

        return Character.MAX_VALUE;
    }

    /**
     * Advances the position until after the next newline character. If the line
     * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
     * caller.
     */
    private void skipToEndOfLine() {
        for (; pos < in.length(); pos++) {
            char c = in.charAt(pos);
            if (c == '\r' || c == '\n') {
                pos++;
                break;
            }
        }
    }

    /**
     * Returns the string up to but not including {@code quote}, unescaping any
     * character escape sequences encountered along the way. The opening quote
     * should have already been read. This consumes the closing quote, but does
     * not include it in the returned string.
     *
     * @param quote either ' or ".
     * @return The unescaped string.
     * @throws IllegalArgumentException if the string isn't terminated by a closing quote correctly.
     */
    @Nullable
    public String nextString(char quote) {
        /*
         * For strings that are free of escape sequences, we can just extract
         * the result as a substring of the input. But if we encounter an escape
         * sequence, we need to use a StringBuilder to compose the result.
         */
        StringBuilder builder = null;

        /* the index of the first character not yet appended to the builder. */
        int start = pos;

        while (pos < in.length()) {
            int c = in.charAt(pos++);
            if (c == quote) {
                if (builder == null) {
                    // a new string avoids leaking memory
                    //noinspection RedundantStringConstructorCall
                    return new String(in.substring(start, pos - 1));
                } else {
                    builder.append(in, start, pos - 1);
                    return builder.toString();
                }
            }

            if (c == '\\') {
                if (pos == in.length()) {
                    throw new IllegalArgumentException("Unterminated escape sequence" + this);
                }
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(in, start, pos - 1);
                builder.append(readEscapeCharacter());
                start = pos;
            }
        }

        throw new IllegalArgumentException("Unterminated string" + this);
    }

    /**
     * Unescapes the character identified by the character or characters that
     * immediately follow a backslash. The backslash '\' should have already
     * been read. This supports both unicode escapes "u000A" and two-character
     * escapes "\n".
     */
    private char readEscapeCharacter() {
        char escaped = in.charAt(pos++);
        switch (escaped) {
            case 'u':
                if (pos + 4 > in.length()) {
                    throw new IllegalArgumentException("Unterminated escape sequence" + this);
                }
                String hex = in.substring(pos, pos + 4);
                pos += 4;
                try {
                    return (char) Integer.parseInt(hex, 16);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(("Invalid escape sequence: " + hex) + this);
                }

            case 't':
                return '\t';

            case 'b':
                return '\b';

            case 'n':
                return '\n';

            case 'r':
                return '\r';

            case 'f':
                return '\f';

            case '\'':
            case '"':
            case '\\':
            default:
                return escaped;
        }
    }

    /**
     * Reads a null, boolean, numeric or unquoted string literal value. Numeric
     * values will be returned as an Integer, Long, or Double, in that order of
     * preference.
     */
    private Object readLiteral() {
        String literal = nextToInternal("{}[]/\\:,=;# \t\f");

        if (literal.length() == 0) {
            throw new IllegalArgumentException("Expected literal value" + this);
        } else if ("null".equalsIgnoreCase(literal)) {
            return JSONObject.NULL;
        } else if ("true".equalsIgnoreCase(literal)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(literal)) {
            return Boolean.FALSE;
        }

        /* try to parse as an integral type... */
        if (literal.indexOf('.') == -1) {
            int base = 10;
            String number = literal;
            if (number.startsWith("0x") || number.startsWith("0X")) {
                number = number.substring(2);
                base = 16;
            } else if (number.startsWith("0") && number.length() > 1) {
                number = number.substring(1);
                base = 8;
            }
            try {
                long longValue = Long.parseLong(number, base);
                if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
                    return (int) longValue;
                } else {
                    return longValue;
                }
            } catch (NumberFormatException e) {
                /*
                 * This only happens for integral numbers greater than
                 * Long.MAX_VALUE, numbers in exponential form (5e-10) and
                 * unquoted strings. Fall through to try floating point.
                 */
            }
        }

        /* ...next try to parse as a floating point... */
        try {
            return Double.valueOf(literal);
        } catch (NumberFormatException ignored) {
        }

        /* ... finally give up. We have an unquoted string */
        //noinspection RedundantStringConstructorCall
        return new String(literal); // a new string avoids leaking memory
    }

    /**
     * Returns the string up to but not including any of the given characters or
     * a newline character. This does not consume the excluded character.
     */
    @NotNull
    private String nextToInternal(String excluded) {
        int start = pos;
        for (; pos < in.length(); pos++) {
            char c = in.charAt(pos);
            if (c == '\r' || c == '\n' || excluded.indexOf(c) != -1) {
                return in.substring(start, pos);
            }
        }
        return in.substring(start);
    }

    /**
     * Reads a sequence of key/value pairs and the trailing closing brace '}' of
     * an object. The opening brace '{' should have already been read.
     */
    @NotNull
    private JSONObject readObject() {
        JSONObject result = new JSONObject();

        /* Peek to see if this is the empty object. */
        char first = nextCleanInternal();
        if (first == '}') {
            return result;
        } else if (first != Character.MAX_VALUE) {
            pos--;
        }

        while (true) {
            Object name = nextValue();
            if (!(name instanceof String)) {
                if (name == null) {
                    throw new IllegalArgumentException("Names cannot be null" + this);
                } else {
                    throw new IllegalArgumentException(("Names must be strings, but " + name
                            + " is of type " + name.getClass().getName()) + this);
                }
            }

            /*
             * Expect the name/value separator to be either a colon ':', an
             * equals sign '=', or an arrow "=>". The last two are bogus but we
             * include them because that's what the original implementation did.
             */
            int separator = nextCleanInternal();
            if (separator != ':' && separator != '=') {
                throw new IllegalArgumentException(("Expected ':' after " + name) + this);
            }
            if (pos < in.length() && in.charAt(pos) == '>') {
                pos++;
            }

            result.putUnsafe((String) name, nextValue());

            switch (nextCleanInternal()) {
                case '}':
                    return result;
                case ';':
                case ',':
                    continue;
                default:
                    throw new IllegalArgumentException("Unterminated object" + this);
            }
        }
    }

    /**
     * Reads a sequence of values and the trailing closing brace ']' of an
     * array. The opening brace '[' should have already been read. Note that
     * "[]" yields an empty array, but "[,]" returns a two-element array
     * equivalent to "[null,null]".
     */
    @NotNull
    private JSONArray readArray() {
        JSONArray result = new JSONArray();

        /* to cover input that ends with ",]". */
        boolean hasTrailingSeparator = false;

        while (true) {
            switch (nextCleanInternal()) {
                case Character.MAX_VALUE:
                    throw new IllegalArgumentException("Unterminated array" + this);
                case ']':
                    if (hasTrailingSeparator) {
                        result.addNull();
                    }
                    return result;
                case ',':
                case ';':
                    /* A separator without a value first means "null". */
                    result.addNull();
                    hasTrailingSeparator = true;
                    continue;
                default:
                    pos--;
            }

            result.addUnsafe(nextValue());

            switch (nextCleanInternal()) {
                case ']':
                    return result;
                case ',':
                case ';':
                    hasTrailingSeparator = true;
                    continue;
                default:
                    throw new IllegalArgumentException("Unterminated array" + this);
            }
        }
    }

    /**
     * Returns the current position and the entire input string.
     */
    @NotNull
    @Override
    public String toString() {
        // consistent with the original implementation
        return " at character " + pos + " of " + in;
    }

    /*
     * Legacy APIs.
     *
     * None of the methods below are on the critical path of parsing JSON
     * documents. They exist only because they were exposed by the original
     * implementation and may be used by some clients.
     */

    /**
     * Returns true until the input has been exhausted.
     *
     * @return true if more input exists.
     */
    public boolean more() {
        return pos < in.length();
    }

    /**
     * Returns the next available character, or the null character '\0' if all
     * input has been exhausted. The return value of this method is ambiguous
     * for JSON strings that contain the character '\0'.
     *
     * @return the next character.
     */
    public char next() {
        return pos < in.length() ? in.charAt(pos++) : '\0';
    }

    /**
     * Returns the next available character if it equals {@code c}. Otherwise an
     * exception is thrown.
     *
     * @param c The character we are looking for.
     * @return the next character.
     * @throws IllegalArgumentException If the next character isn't {@code c}
     */
    public char next(char c) {
        char result = next();
        if (result != c) {
            throw new IllegalArgumentException(("Expected " + c + " but was " + result) + this);
        }
        return result;
    }

    /**
     * Returns the next character that is not whitespace and does not belong to
     * a comment. If the input is exhausted before such a character can be
     * found, the null character '\0' is returned. The return value of this
     * method is ambiguous for JSON strings that contain the character '\0'.
     *
     * @return The next non-whitespace character.
     * @throws IllegalArgumentException Should not be possible.
     */
    public char nextClean() {
        char next = nextCleanInternal();
        return next == Character.MAX_VALUE ? '\0' : next;
    }

    /**
     * Returns the next {@code length} characters of the input.
     *
     * <p>The returned string shares its backing character array with this
     * tokener's input string. If a reference to the returned string may be held
     * indefinitely, you should use {@code new String(result)} to copy it first
     * to avoid memory leaks.
     *
     * @param length The desired number of characters to return.
     * @return The next few characters.
     * @throws IllegalArgumentException if the remaining input is not long enough to
     *                                  satisfy this request.
     */
    @NotNull
    public String next(int length) {
        if (pos + length > in.length()) {
            throw new IllegalArgumentException((length + " is out of bounds") + this);
        }
        String result = in.substring(pos, pos + length);
        pos += length;
        return result;
    }

    /**
     * Returns the {@link String#trim trimmed} string holding the characters up
     * to but not including the first of:
     * <ul>
     * <li>any character in {@code excluded}
     * <li>a newline character '\n'
     * <li>a carriage return '\r'
     * </ul>
     *
     * <p>The returned string shares its backing character array with this
     * tokener's input string. If a reference to the returned string may be held
     * indefinitely, you should use {@code new String(result)} to copy it first
     * to avoid memory leaks.
     *
     * @param excluded The limiting string where the search should stop.
     * @return a possibly-empty string
     */
    @NotNull
    public String nextTo(@Nullable String excluded) {
        if (excluded == null) {
            throw new NullPointerException("excluded == null");
        }
        return nextToInternal(excluded).trim();
    }

    /**
     * Equivalent to {@code nextTo(String.valueOf(excluded))}.
     *
     * @param excluded The limiting character.
     * @return a possibly-empty string
     */
    @NotNull
    public String nextTo(char excluded) {
        return nextToInternal(String.valueOf(excluded)).trim();
    }

    /**
     * Advances past all input up to and including the next occurrence of
     * {@code thru}. If the remaining input doesn't contain {@code thru}, the
     * input is exhausted.
     *
     * @param thru The string to skip over.
     */
    public void skipPast(String thru) {
        int thruStart = in.indexOf(thru, pos);
        pos = thruStart == -1 ? in.length() : (thruStart + thru.length());
    }

    /**
     * Advances past all input up to but not including the next occurrence of
     * {@code to}. If the remaining input doesn't contain {@code to}, the input
     * is unchanged.
     *
     * @param to The character we want to skip to.
     * @return The value of {@code to} or null.
     */
    public char skipTo(char to) {
        int index = in.indexOf(to, pos);
        if (index != -1) {
            pos = index;
            return to;
        } else {
            return '\0';
        }
    }

    /**
     * Unreads the most recent character of input. If no input characters have
     * been read, the input is unchanged.
     */
    public void back() {
        if (--pos == -1) {
            pos = 0;
        }
    }

    /**
     * Returns the integer [0..15] value for the given hex character, or -1
     * for non-hex input.
     *
     * @param hex a character in the ranges [0-9], [A-F] or [a-f]. Any other
     *            character will yield a -1 result.
     * @return The decoded integer.
     */
    public static int dehexchar(char hex) {
        if (hex >= '0' && hex <= '9') {
            return hex - '0';
        } else if (hex >= 'A' && hex <= 'F') {
            return hex - 'A' + 10;
        } else if (hex >= 'a' && hex <= 'f') {
            return hex - 'a' + 10;
        } else {
            return -1;
        }
    }
}
