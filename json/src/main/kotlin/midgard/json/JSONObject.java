package midgard.json;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSONObject {
    private static final Double NEGATIVE_ZERO = -0d;

    /**
     * A sentinel value used to explicitly define a name with no value. Unlike
     * {@code null}, names with this value:
     * <ul>
     * <li>show up in the {@link #keys} iterator
     * <li>return {@code true} for {@link #has(String)}
     * <li>do not throw on {@link #get(String)}
     * <li>are included in the encoded JSON string.
     * </ul>
     *
     * <p>This value violates the general contract of {@link Object#equals} by
     * returning true when compared to {@code null}. Its {@link #toString}
     * method returns "null".
     */
    public static final Object NULL = new Object() {
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            return o == this || o == null; // API specifies this broken equals implementation
        }

        // at least make the broken equals(null) consistent with Objects.hashCode(null).
        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "null";
        }
    };

    @NotNull
    private final LinkedHashMap<String, Object> nameValuePairs = new LinkedHashMap<>();

    /**
     * Creates a {@code JSONObject} with no name/value mappings.
     */
    public JSONObject() {
    }

    /**
     * Creates a new {@code JSONObject} with name/value mappings from the next
     * object in the tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     *                 {@code JSONObject}.
     * @throws IllegalArgumentException if the parse fails or doesn't yield a
     *                                  {@code JSONObject}.
     */
    public JSONObject(@NotNull JSONTokener readFrom) {
        init(readFrom);
    }

    /**
     * Creates a new {@code JSONObject} with name/value mappings from the JSON
     * string.
     *
     * @param json a JSON-encoded string containing an object.
     * @throws IllegalArgumentException if the parse fails or doesn't yield a {@code
     *                                  JSONObject}.
     */
    public JSONObject(@NotNull String json) {
        this(new JSONTokener(json));
    }

    /**
     * Creates a new {@code JSONObject} by copying mappings for the listed names
     * from the given object. Names that aren't present in {@code copyFrom} will
     * be skipped.
     *
     * @param copyFrom The source object.
     * @param names    The names of the fields to copy.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    public JSONObject(@NotNull JSONObject copyFrom, @NotNull String[] names) {
        for (String name : names) {
            Object value = copyFrom.opt(name);
            if (value != null) {
                nameValuePairs.put(name, value);
            }
        }
    }


    private void init(@NotNull JSONTokener readFrom) {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONObject and then steal the data from that.
         */
        Object object = readFrom.nextValue();
        if (object instanceof JSONObject) {
            this.nameValuePairs.putAll(((JSONObject) object).nameValuePairs);
        } else {
            throw JSON.typeMismatch(object, "JSONObject");
        }
    }

    @NotNull
    public static String[] getNames(@NotNull JSONObject x) {
        Set<String> names = x.keySet();
        String[] r = new String[names.size()];
        int i = 0;
        for (String name : names) {
            r[i++] = name;
        }
        return r;
    }

    /**
     * Returns the number of name/value mappings in this object.
     *
     * @return the length of this.
     */
    public int length() {
        return nameValuePairs.size();
    }

    /**
     * Maps {@code name} to {@code value}, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name of the value to insert.
     * @param value The value to insert.
     * @return this object.
     * @throws IllegalArgumentException Should not be possible.
     */
    @NotNull
    public JSONObject set(@NotNull String name, boolean value) {
        return putUnsafe(checkName(name), value);
    }

    /**
     * Maps {@code name} to {@code value}, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name for the new value.
     * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
     *              {@link Double#isInfinite() infinities}.
     * @return this object.
     * @throws IllegalArgumentException if value is NaN or infinite.
     */
    @NotNull
    public JSONObject set(@NotNull String name, double value) {
        return putUnsafe(checkName(name), JSON.checkDouble(value));
    }

    /**
     * Maps {@code name} to {@code value}, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name for the new value.
     * @param value The new value.
     * @return this object.
     * @throws IllegalArgumentException Should not be possible.
     */
    @NotNull
    public JSONObject set(@NotNull String name, int value) {
        return putUnsafe(checkName(name), value);
    }

    /**
     * Maps {@code name} to {@code value}, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name of the new value.
     * @param value The new value to insert.
     * @return this object.
     * @throws IllegalArgumentException Should not be possible.
     */
    @NotNull
    public JSONObject set(@NotNull String name, long value) {
        return putUnsafe(checkName(name), value);
    }

    @NotNull
    public JSONObject set(@NotNull String name, @Nullable JSONObject value) {
        return putUnsafe(checkName(name), value);
    }

    @NotNull
    public JSONObject set(@NotNull String name, @Nullable JSONArray value) {
        return putUnsafe(name, value);
    }

    @NotNull
    public JSONObject set(@NotNull String name, @Nullable String value) {
        return putUnsafe(name, value);
    }


    @NotNull
    protected JSONObject putUnsafe(@NotNull String name, @Nullable Object value) {
        if (value == null) {
            nameValuePairs.remove(name);
            return this;
        }
        nameValuePairs.put(checkName(name), value);
        return this;
    }


    @NotNull
    protected String checkName(@Nullable String name) {
        if (name == null) {
            throw new IllegalArgumentException("Names must be non-null");
        }
        return name;
    }

    /**
     * Removes the named mapping if it exists; does nothing otherwise.
     *
     * @param name The name of the mapping to remove.
     * @return the value previously mapped by {@code name}, or null if there was
     * no such mapping.
     */
    @Nullable
    public Object remove(@NotNull String name) {
        return nameValuePairs.remove(name);
    }

    /**
     * Returns true if this object has no mapping for {@code name} or if it has
     * a mapping whose value is {@link #NULL}.
     *
     * @param name The name of the value to check on.
     * @return true if the field doesn't exist or is null.
     */
    public boolean isNull(@NotNull String name) {
        Object value = nameValuePairs.get(name);
        return value == null || value == NULL;
    }

    /**
     * Returns true if this object has a mapping for {@code name}. The mapping
     * may be {@link #NULL}.
     *
     * @param name The name of the value to check on.
     * @return true if this object has a field named {@code name}
     */
    public boolean has(@NotNull String name) {
        return nameValuePairs.containsKey(name);
    }

    /**
     * Returns the value mapped by {@code name}, or throws if no such mapping exists.
     *
     * @param name The name of the value to get.
     * @return The value.
     * @throws IllegalArgumentException if no such mapping exists.
     */
    @NotNull
    public Object get(@NotNull String name) {
        Object result = nameValuePairs.get(name);
        if (result == null) {
            throw new IllegalArgumentException("No value for " + name);
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name}, or null if no such mapping
     * exists.
     *
     * @param name The name of the value to get.
     * @return The value.
     */
    @Nullable
    public Object opt(@NotNull String name) {
        return nameValuePairs.get(name);
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a boolean or
     * can be coerced to a boolean, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     *                                  to a boolean.
     */
    public boolean getBoolean(@NotNull String name) {
        Object object = get(name);
        Boolean result = JSON.toBoolean(object);
        if (result == null) {
            throw JSON.typeMismatch(name, object, "boolean");
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a boolean or
     * can be coerced to a boolean, or false otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     */
    public boolean optBoolean(@NotNull String name) {
        return optBoolean(name, false);
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a boolean or
     * can be coerced to a boolean, or {@code fallback} otherwise.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    public boolean optBoolean(@NotNull String name, boolean fallback) {
        Object object = opt(name);
        Boolean result = JSON.toBoolean(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a double or
     * can be coerced to a double, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     *                                  to a double.
     */
    public double getDouble(@NotNull String name) {
        Object object = get(name);
        Double result = JSON.toDouble(object);
        if (result == null) {
            throw JSON.typeMismatch(name, object, "double");
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a double or
     * can be coerced to a double, or {@code NaN} otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     */
    public double optDouble(@NotNull String name) {
        return optDouble(name, Double.NaN);
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a double or
     * can be coerced to a double, or {@code fallback} otherwise.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    public double optDouble(@NotNull String name, double fallback) {
        Object object = opt(name);
        Double result = JSON.toDouble(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is an int or
     * can be coerced to an int, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     *                                  to an int.
     */
    public int getInt(@NotNull String name) {
        Object object = get(name);
        Integer result = JSON.toInteger(object);
        if (result == null) {
            throw JSON.typeMismatch(name, object, "int");
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is an int or
     * can be coerced to an int, or 0 otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     */
    public int optInt(@NotNull String name) {
        return optInt(name, 0);
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is an int or
     * can be coerced to an int, or {@code fallback} otherwise.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    public int optInt(@NotNull String name, int fallback) {
        Object object = opt(name);
        Integer result = JSON.toInteger(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a long or
     * can be coerced to a long, or throws otherwise.
     * Note that JSON represents numbers as doubles,
     * <p>
     * so this is <a href="#lossy">lossy</a>; use strings to transfer numbers
     * via JSON without loss.
     *
     * @param name The name of the field that we want.
     * @return The value of the field.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     *                                  to a long.
     */
    public long getLong(@NotNull String name) {
        Object object = get(name);
        Long result = JSON.toLong(object);
        if (result == null) {
            throw JSON.typeMismatch(name, object, "long");
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a long or
     * can be coerced to a long, or 0 otherwise. Note that JSON represents numbers as doubles,
     * so this is <a href="#lossy">lossy</a>; use strings to transfer numbers via JSON.
     *
     * @param name The name of the field we want.
     * @return The selected value.
     */
    public long optLong(@NotNull String name) {
        return optLong(name, 0L);
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a long or
     * can be coerced to a long, or {@code fallback} otherwise. Note that JSON represents
     * numbers as doubles, so this is <a href="#lossy">lossy</a>; use strings to transfer
     * numbers via JSON.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    public long optLong(@NotNull String name, long fallback) {
        Object object = opt(name);
        Long result = JSON.toLong(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value mapped by {@code name} if it exists, coercing it if
     * necessary, or throws if no such mapping exists.
     *
     * @param name The name of the field we want.
     * @return The value of the field.
     * @throws IllegalArgumentException if no such mapping exists.
     */
    @NotNull
    public String getString(@NotNull String name) {
        Object object = get(name);
        String result = JSON.toString(object);
        if (result == null) {
            throw JSON.typeMismatch(name, object, "String");
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists, coercing it if
     * necessary, or the empty string if no such mapping exists.
     *
     * @param name The name of the field we want.
     * @return The value of the field.
     */
    @Nullable
    public String optString(@NotNull String name) {
        return optString(name, "");
    }

    /**
     * Returns the value mapped by {@code name} if it exists, coercing it if
     * necessary, or {@code fallback} if no such mapping exists.
     *
     * @param name     The name of the field that we want.
     * @param fallback The value to return if the field doesn't exist.
     * @return The value of the field or fallback.
     */
    @NotNull
    public String optString(@NotNull String name, @NotNull String fallback) {
        Object object = opt(name);
        String result = JSON.toString(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a {@code
     * JSONArray}, or throws otherwise.
     *
     * @param name The field we want to get.
     * @return The value of the field (if it is a JSONArray.
     * @throws IllegalArgumentException if the mapping doesn't exist or is not a {@code
     *                                  JSONArray}.
     */
    @NotNull
    public JSONArray getJSONArray(@NotNull String name) {
        Object object = get(name);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            throw JSON.typeMismatch(name, object, "JSONArray");
        }
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a {@code
     * JSONArray}, or null otherwise.
     *
     * @param name The name of the field we want.
     * @return The value of the specified field (assuming it is a JSNOArray
     */
    @Nullable
    public JSONArray optJSONArray(@NotNull String name) {
        Object object = opt(name);
        return object instanceof JSONArray ? (JSONArray) object : null;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a {@code
     * JSONObject}, or throws otherwise.
     *
     * @param name The name of the field that we want.
     * @return a specified field value (if it is a JSONObject)
     * @throws IllegalArgumentException if the mapping doesn't exist or is not a {@code
     *                                  JSONObject}.
     */
    @NotNull
    public JSONObject getJSONObject(@NotNull String name) {
        Object object = get(name);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        } else {
            throw JSON.typeMismatch(name, object, "JSONObject");
        }
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a {@code
     * JSONObject}, or null otherwise.
     *
     * @param name The name of the value we want.
     * @return The specified value.
     */
    @Nullable
    public JSONObject optJSONObject(@NotNull String name) {
        Object object = opt(name);
        return object instanceof JSONObject ? (JSONObject) object : null;
    }

    /**
     * Returns an iterator of the {@code String} names in this object. The
     * returned iterator supports {@link Iterator#remove() remove}, which will
     * remove the corresponding mapping from this object. If this object is
     * modified after the iterator is returned, the iterator's behavior is
     * undefined. The order of the keys is undefined.
     *
     * @return an iterator over the keys.
     */
    @NotNull
    public Iterator<String> keys() {
        return nameValuePairs.keySet().iterator();
    }

    /**
     * Returns the set of {@code String} names in this object. The returned set
     * is a view of the keys in this object. {@link Set#remove(Object)} will remove
     * the corresponding mapping from this object and set iterator behaviour
     * is undefined if this object is modified after it is returned.
     * <p>
     * See {@link #keys()}.
     *
     * @return The names in this object.
     */
    @NotNull
    public Set<String> keySet() {
        return nameValuePairs.keySet();
    }

    /**
     * Encodes this object as a compact JSON string, such as:
     * <pre>{"query":"Pizza","locations":[94043,90210]}</pre>
     * <p>
     * Note 1: this method will not output any fields with 'null' value.
     * Override {@link JSONStringer#entry} method to have nulls printed.
     * <p>
     * Note 2: this method will suppress any internal exceptions.
     * Use {@link JSONObject#toString(JSONStringer)} method directly to handle exceptions manually.
     */
    @NotNull
    @Override
    public String toString() {
        return toString(new JSONStringer());
    }

    /**
     * Encodes this object as a human readable JSON string for debugging, such
     * as:
     * <pre>
     * {
     *     "query": "Pizza",
     *     "locations": [
     *         94043,
     *         90210
     *     ]
     * }</pre>
     *
     * @param indentSpaces the number of spaces to indent for each level of
     *                     nesting.
     * @return The string containing the pretty form of this.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    public String toString(int indentSpaces) {
        return toString(new JSONStringer(indentSpaces));
    }

    /**
     * Encodes this object using {@link JSONStringer} provided
     *
     * @param stringer - {@link JSONStringer} to be used for serialization
     * @return The string representation of this.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    public String toString(JSONStringer stringer) {
        encode(stringer);
        return stringer.toString();
    }

    /**
     * Encodes this object using {@link JSONStringer} provided
     *
     * @param stringer - {@link JSONStringer} to be used for serialization
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    protected void encode(JSONStringer stringer) {
        stringer.object();
        for (Map.Entry<String, Object> entry : nameValuePairs.entrySet()) {
            stringer.entry(entry);
        }
        stringer.endObject();
    }

    /**
     * Encodes the number as a JSON string.
     *
     * @param number a finite value. May not be {@link Double#isNaN() NaNs} or
     *               {@link Double#isInfinite() infinities}.
     * @return The encoded number in string form.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    public static String numberToString(Number number) {
        if (number == null) {
            throw new IllegalArgumentException("Number must be non-null");
        }

        double doubleValue = number.doubleValue();
        JSON.checkDouble(doubleValue);

        // the original returns "-0" instead of "-0.0" for negative zero
        if (number.equals(NEGATIVE_ZERO)) {
            return "-0";
        }

        long longValue = number.longValue();
        if (doubleValue == longValue) {
            return Long.toString(longValue);
        }

        return number.toString();
    }

    /**
     * Encodes {@code data} as a JSON string. This applies quotes and any
     * necessary character escaping.
     *
     * @param data the string to encode. Null will be interpreted as an empty
     *             string.
     * @return the quoted string.
     */
    public static String quote(String data) {
        if (data == null) {
            return "\"\"";
        }
        try {
            JSONStringer stringer = new JSONStringer();
            stringer.open(JSONStringer.Scope.NULL, "");
            stringer.value(data);
            stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "");
            return stringer.toString();
        } catch (IllegalArgumentException e) {
            throw new AssertionError();
        }
    }
}
