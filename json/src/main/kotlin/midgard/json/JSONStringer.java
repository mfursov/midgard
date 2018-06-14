package midgard.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSONStringer {

    /**
     * The output data, containing at most one top-level array or object.
     */
    protected final StringBuilder out = new StringBuilder();

    /**
     * Lexical scoping elements within this stringer, necessary to insert the
     * appropriate separator characters (ie. commas and colons) and to detect
     * nesting errors.
     */
    enum Scope {

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
        NULL,
    }

    /**
     * Unlike the original implementation, this stack isn't limited to 20
     * levels of nesting.
     */
    private final List<Scope> stack = new ArrayList<>();

    /**
     * A string containing a full set of spaces for a single level of
     * indentation, or null for no pretty printing.
     */
    private final String indent;

    public JSONStringer() {
        this(0);
    }

    public JSONStringer(int indentSpaces) {
        if (indentSpaces > 0) {
            char[] indentChars = new char[indentSpaces];
            Arrays.fill(indentChars, ' ');
            indent = new String(indentChars);
        } else {
            indent = null;
        }
    }

    /**
     * Begins encoding a new array. Each call to this method must be paired with
     * a call to {@link #endArray}.
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    @NotNull
    public JSONStringer array() {
        return open(Scope.EMPTY_ARRAY, "[");
    }

    /**
     * Ends encoding the current array.
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    @NotNull
    public JSONStringer endArray() {
        return close(Scope.EMPTY_ARRAY, Scope.NONEMPTY_ARRAY, "]");
    }

    /**
     * Begins encoding a new object. Each call to this method must be paired
     * with a call to {@link #endObject}.
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    @NotNull
    public JSONStringer object() {
        return open(Scope.EMPTY_OBJECT, "{");
    }

    /**
     * Ends encoding the current object.
     *
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    @NotNull
    public JSONStringer endObject() {
        return close(Scope.EMPTY_OBJECT, Scope.NONEMPTY_OBJECT, "}");
    }

    /**
     * Enters a new scope by appending any necessary whitespace and the given
     * bracket.
     */
    @NotNull
    JSONStringer open(Scope empty, String openBracket) {
        if (stack.isEmpty() && out.length() > 0) {
            throw new IllegalArgumentException("Nesting problem: multiple top-level roots");
        }
        beforeValue();
        stack.add(empty);
        out.append(openBracket);
        return this;
    }

    /**
     * Closes the current scope by appending any necessary whitespace and the
     * given bracket.
     */
    @NotNull
    JSONStringer close(@NotNull Scope empty, @NotNull Scope nonempty, @NotNull String closeBracket) {
        Scope context = peek();
        if (context != nonempty && context != empty) {
            throw new IllegalArgumentException("Nesting problem");
        }

        stack.remove(stack.size() - 1);
        if (context == nonempty) {
            newline();
        }
        out.append(closeBracket);
        return this;
    }

    /**
     * Returns the value on the top of the stack.
     */
    @NotNull
    private Scope peek() {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Nesting problem");
        }
        return stack.get(stack.size() - 1);
    }

    /**
     * Replace the value on the top of the stack with the given value.
     */
    private void replaceTop(Scope topOfStack) {
        stack.set(stack.size() - 1, topOfStack);
    }

    /**
     * Encodes {@code value}.
     *
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double or null. May not be {@link Double#isNaN() NaNs}
     *              or {@link Double#isInfinite() infinities}.
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    @NotNull
    public JSONStringer value(@Nullable Object value) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Nesting problem");
        }

        if (value instanceof JSONArray) {
            ((JSONArray) value).encode(this);
            return this;

        } else if (value instanceof JSONObject) {
            ((JSONObject) value).encode(this);
            return this;
        }

        beforeValue();

        if (value == null
                || value instanceof Boolean
                || value == JSONObject.NULL) {
            out.append(value);

        } else if (value instanceof Number) {
            out.append(JSONObject.numberToString((Number) value));
        } else {
            string(value.toString());
        }
        return this;
    }

    /**
     * Encodes {@code value} to this stringer.
     *
     * @param value The value to encode.
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    public JSONStringer value(boolean value) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Nesting problem");
        }
        beforeValue();
        out.append(value);
        return this;
    }

    /**
     * Encodes {@code value} to this stringer.
     *
     * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
     *              {@link Double#isInfinite() infinities}.
     * @return this stringer.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    public JSONStringer value(double value) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Nesting problem");
        }
        beforeValue();
        out.append(JSONObject.numberToString(value));
        return this;
    }

    /**
     * Encodes {@code value} to this stringer.
     *
     * @param value The value to encode.
     * @return this stringer.
     * @throws IllegalArgumentException If we have an internal error. Shouldn't happen.
     */
    @NotNull
    public JSONStringer value(long value) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Nesting problem");
        }
        beforeValue();
        out.append(value);
        return this;
    }

    /**
     * Encodes {@code key}/{@code value} pair to this stringer.
     *
     * @param entry The entry to encode.
     * @return this stringer.
     * @throws IllegalArgumentException If we have an internal error. Shouldn't happen.
     */
    @NotNull
    public JSONStringer entry(@NotNull Map.Entry<String, Object> entry) {
        if (!JSONObject.NULL.equals(entry.getValue())) {
            this.key(entry.getKey()).value(entry.getValue());
        }
        return this;
    }

    private void string(@NotNull String value) {
        out.append("\"");
        char currentChar = 0;

        for (int i = 0, length = value.length(); i < length; i++) {
            char previousChar = currentChar;
            currentChar = value.charAt(i);

            /*
             * From RFC 4627, "All Unicode characters may be placed within the
             * quotation marks except for the characters that must be escaped:
             * quotation mark, reverse solidus, and the control characters
             * (U+0000 through U+001F)."
             */
            switch (currentChar) {
                case '"':
                case '\\':
                    out.append('\\').append(currentChar);
                    break;

                case '/':
                    // it makes life easier for HTML embedding of javascript if we escape </ sequences
                    if (previousChar == '<') {
                        out.append('\\');
                    }
                    out.append(currentChar);
                    break;

                case '\t':
                    out.append("\\t");
                    break;

                case '\b':
                    out.append("\\b");
                    break;

                case '\n':
                    out.append("\\n");
                    break;

                case '\r':
                    out.append("\\r");
                    break;

                case '\f':
                    out.append("\\f");
                    break;

                default:
                    if (currentChar <= 0x1F) {
                        out.append(String.format("\\u%04x", (int) currentChar));
                    } else {
                        out.append(currentChar);
                    }
                    break;
            }

        }
        out.append("\"");
    }

    private void newline() {
        if (indent == null) {
            return;
        }

        out.append("\n");
        for (int i = 0; i < stack.size(); i++) {
            out.append(indent);
        }
    }

    /**
     * Creates String representation of the key (property name) to this stringer
     * Override this method to provide your own representation of the name.
     *
     * @param name the name of the forthcoming value.
     * @return this stringer.
     */
    @NotNull
    protected JSONStringer createKey(String name) {
        string(name);
        return this;
    }

    /**
     * Encodes the key (property name) to this stringer.
     *
     * @param name the name of the forthcoming value. May not be null.
     * @return this stringer.
     * @throws IllegalArgumentException on internal errors, shouldn't happen.
     */
    @NotNull
    public JSONStringer key(@NotNull String name) {
        beforeKey();
        return createKey(name);
    }

    /**
     * Inserts any necessary separators and whitespace before a name. Also
     * adjusts the stack to expect the key's value.
     */
    private void beforeKey() {
        Scope context = peek();
        if (context == Scope.NONEMPTY_OBJECT) { // first in object
            out.append(',');
        } else if (context != Scope.EMPTY_OBJECT) { // not in an object!
            throw new IllegalArgumentException("Nesting problem");
        }
        newline();
        replaceTop(Scope.DANGLING_KEY);
    }

    /**
     * Inserts any necessary separators and whitespace before a literal value,
     * inline array, or inline object. Also adjusts the stack to expect either a
     * closing bracket or another element.
     */
    private void beforeValue() {
        if (stack.isEmpty()) {
            return;
        }

        Scope context = peek();
        if (context == Scope.EMPTY_ARRAY) { // first in array
            replaceTop(Scope.NONEMPTY_ARRAY);
            newline();
        } else if (context == Scope.NONEMPTY_ARRAY) { // another in array
            out.append(',');
            newline();
        } else if (context == Scope.DANGLING_KEY) { // value for key
            out.append(indent == null ? ":" : ": ");
            replaceTop(Scope.NONEMPTY_OBJECT);
        } else if (context != Scope.NULL) {
            throw new IllegalArgumentException("Nesting problem");
        }
    }

    /**
     * Returns the encoded JSON string.
     * <br>
     * <p>If invoked with unterminated arrays or unclosed objects, this method's
     * return value is undefined.
     * <br>
     * <p><strong>Warning:</strong> although it contradicts the general contract
     * of {@link Object#toString}, this method returns null if the stringer
     * contains no data.
     */
    @NotNull
    @Override
    public String toString() {
        return out.length() == 0 ? "" : out.toString();
    }
}
