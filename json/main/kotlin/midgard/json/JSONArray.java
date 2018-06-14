package midgard.json;

import java.util.ArrayList;
import java.util.List;

public class JSONArray {
    private final List<Object> values;

    /**
     * Creates a {@code JSONArray} with no values.
     */
    public JSONArray() {
        values = new ArrayList<>();
    }

    /**
     * Creates a new {@code JSONArray} with values from the next array in the
     * tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     *                 {@code JSONArray}.
     * @throws JSONException if the parse fails or doesn't yield a
     *                       {@code JSONArray}.
     */
    public JSONArray(JSONTokener readFrom) throws JSONException {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONArray and then steal the data from that.
         */
        Object object = readFrom.nextValue();
        if (object instanceof JSONArray) {
            values = ((JSONArray) object).values;
        } else {
            //noinspection ConstantConditions
            throw JSON.typeMismatch(object, "JSONArray");
        }
    }

    /**
     * Creates a new {@code JSONArray} with values from the JSON string.
     *
     * @param json a JSON-encoded string containing an array.
     * @throws JSONException if the parse fails or doesn't yield a {@code
     *                       JSONArray}.
     */
    public JSONArray(String json) throws JSONException {
        this(new JSONTokener(json));
    }

    /**
     * @return Returns the number of values in this array.
     */
    public int length() {
        return values.size();
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @param value The value to append.
     * @return this array.
     */
    public JSONArray add(boolean value) {
        return addUnsafe(value);
    }

    public JSONArray addNull() {
        return addUnsafe(null);
    }

    protected JSONArray addUnsafe(Object o) {
        values.add(o);
        return this;
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
     *              {@link Double#isInfinite() infinities}.
     * @return this array.
     * @throws JSONException If the value is unacceptable.
     */
    public JSONArray add(double value) throws JSONException {
        return addUnsafe(JSON.checkDouble(value));
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @param value The value to append.
     * @return this array.
     */
    public JSONArray add(int value) {
        return addUnsafe(value);
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @param value The value to append.
     * @return this array.
     */
    public JSONArray add(long value) {
        return addUnsafe(value);
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @return this array.
     */
    public JSONArray add(JSONObject value) {
        return addUnsafe(value);
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @return this array.
     */
    public JSONArray add(JSONArray value) {
        return addUnsafe(value);
    }

    /**
     * Sets the value at {@code index} to {@code value}, null padding this array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value The value to set.
     * @return this array.
     * @throws JSONException This should never happen.
     */
    public JSONArray set(int index, boolean value) throws JSONException {
        ensureSize(index);
        values.set(index, value);
        return this;
    }

    /**
     * Sets the value at {@code index} to {@code value}, null padding this array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
     *              {@link Double#isInfinite() infinities}.
     * @return this array.
     * @throws JSONException If the value is not a finite value.
     */
    public JSONArray set(int index, double value) throws JSONException {
        // deviate from the original by checking all Numbers, not just floats & doubles
        JSON.checkDouble(value);
        ensureSize(index);
        values.set(index, value);
        return this;
    }

    /**
     * Sets the value at {@code index} to {@code value}, null padding this array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value The value to set.
     * @return this array.
     * @throws JSONException Should never actually happen.
     */
    public JSONArray set(int index, int value) throws JSONException {
        ensureSize(index);
        values.set(index, value);
        return this;
    }

    /**
     * Sets the value at {@code index} to {@code value}, null padding this array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value The value to set.
     * @return this array.
     * @throws JSONException Should never actually happen.
     */
    public JSONArray set(int index, long value) throws JSONException {
        ensureSize(index);
        values.set(index, value);
        return this;
    }

    public JSONArray set(int index, JSONObject value) throws JSONException {
        ensureSize(index);
        values.set(index, value);
        return this;
    }

    public JSONArray set(int index, JSONArray array) throws JSONException {
        ensureSize(index);
        values.set(index, array);
        return this;
    }

    public JSONArray setNull(int index) throws JSONException {
        ensureSize(index);
        values.set(index, null);
        return this;
    }

    protected void ensureSize(int index) {
        while (values.size() <= index) {
            values.add(null);
        }
    }

    /**
     * Returns true if this array has no value at {@code index}, or if its value
     * is the {@code null} reference or {@link JSONObject#NULL}.
     *
     * @param index Which value to check.
     * @return true if the value is null.
     */
    public boolean isNull(int index) {
        Object value = opt(index);
        return value == null || value == JSONObject.NULL;
    }

    /**
     * Returns the value at {@code index}.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if this array has no value at {@code index}, or if
     *                       that value is the {@code null} reference. This method returns
     *                       normally if the value is {@code JSONObject#NULL}.
     */
    public Object get(int index) throws JSONException {
        try {
            Object value = values.get(index);
            if (value == null) {
                throw new JSONException("Value at " + index + " is null.");
            }
            return value;
        } catch (IndexOutOfBoundsException e) {
            throw new JSONException("Index " + index + " out of range [0.." + values.size() + ")");
        }
    }

    /**
     * Returns the value at {@code index}, or null if the array has no value
     * at {@code index}.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public Object opt(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    /**
     * Removes and returns the value at {@code index}, or null if the array has no value
     * at {@code index}.
     *
     * @param index Which value to remove.
     * @return The value previously at the specified location.
     */
    public Object remove(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.remove(index);
    }

    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a boolean.
     */
    public boolean getBoolean(int index) throws JSONException {
        Object object = get(index);
        Boolean result = JSON.toBoolean(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "boolean");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean. Returns false otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public boolean optBoolean(int index) {
        return optBoolean(index, false);
    }

    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean. Returns {@code fallback} otherwise.
     *
     * @param index    Which value to get.
     * @param fallback the fallback value to return if no value exists.
     * @return the value at the specified location or the fallback value.
     */
    public boolean optBoolean(int index, boolean fallback) {
        Object object = opt(index);
        Boolean result = JSON.toBoolean(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a double.
     */
    public double getDouble(int index) throws JSONException {
        Object object = get(index);
        Double result = JSON.toDouble(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "double");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double. Returns {@code NaN} otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public double optDouble(int index) {
        return optDouble(index, Double.NaN);
    }

    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double. Returns {@code fallback} otherwise.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    public double optDouble(int index, double fallback) {
        Object object = opt(index);
        Double result = JSON.toDouble(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a int.
     */
    public int getInt(int index) throws JSONException {
        Object object = get(index);
        Integer result = JSON.toInteger(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "int");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int. Returns 0 otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public int optInt(int index) {
        return optInt(index, 0);
    }

    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int. Returns {@code fallback} otherwise.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    public int optInt(int index, int fallback) {
        Object object = opt(index);
        Integer result = JSON.toInteger(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a long.
     */
    public long getLong(int index) throws JSONException {
        Object object = get(index);
        Long result = JSON.toLong(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "long");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long. Returns 0 otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public long optLong(int index) {
        return optLong(index, 0L);
    }

    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long. Returns {@code fallback} otherwise.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    public long optLong(int index, long fallback) {
        Object object = opt(index);
        Long result = JSON.toLong(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if no such value exists.
     */
    public String getString(int index) throws JSONException {
        Object object = get(index);
        String result = JSON.toString(object);
        if (result == null) {
            throw JSON.typeMismatch(index, object, "String");
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary. Returns the empty string if no such value exists.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public String optString(int index) {
        return optString(index, "");
    }

    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary. Returns {@code fallback} if no such value exists.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    public String optString(int index, String fallback) {
        Object object = opt(index);
        String result = JSON.toString(object);
        return result != null ? result : fallback;
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONArray}.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if the value doesn't exist or is not a {@code
     *                       JSONArray}.
     */
    public JSONArray getJSONArray(int index) throws JSONException {
        Object object = get(index);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            throw JSON.typeMismatch(index, object, "JSONArray");
        }
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONArray}. Returns null otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public JSONArray optJSONArray(int index) {
        Object object = opt(index);
        return object instanceof JSONArray ? (JSONArray) object : null;
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONObject}.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONException if the value doesn't exist or is not a {@code
     *                       JSONObject}.
     */
    public JSONObject getJSONObject(int index) throws JSONException {
        Object object = get(index);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        } else {
            throw JSON.typeMismatch(index, object, "JSONObject");
        }
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONObject}. Returns null otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    public JSONObject optJSONObject(int index) {
        Object object = opt(index);
        return object instanceof JSONObject ? (JSONObject) object : null;
    }

    /**
     * Returns a new string by alternating this array's values with {@code
     * separator}. This array's string values are quoted and have their special
     * characters escaped. For example, the array containing the strings '12"
     * pizza', 'taco' and 'soda' joined on '+' returns this:
     * <pre>"12\" pizza"+"taco"+"soda"</pre>
     *
     * @param separator The string used to separate the returned values.
     * @return the conjoined values.
     * @throws JSONException Only if there is a coding error.
     */
    public String join(String separator) throws JSONException {
        JSONStringer stringer = new JSONStringer();
        stringer.open(JSONStringer.Scope.NULL, "");
        for (int i = 0, size = values.size(); i < size; i++) {
            if (i > 0) {
                stringer.out.append(separator);
            }
            stringer.value(values.get(i));
        }
        stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "");
        return stringer.out.toString();
    }

    /**
     * Encodes this array as a compact JSON string, such as:
     * <pre>[94043,90210]</pre>
     * <p>
     * Note 1: this method will not output any fields with 'null' value.
     * Override {@link JSONStringer#entry} method to have nulls printed.
     * <p>
     * Note 2: this method will suppress any internal exceptions.
     * Use {@link JSONArray#toString(JSONStringer)} method directly to handle exceptions manually.
     *
     * @return The string form of this array.
     */
    @Override
    public String toString() {
        try {
            return toString(new JSONStringer());
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Encodes this array as a human readable JSON string for debugging, such
     * as:
     * <pre>
     * [
     *     94043,
     *     90210
     * ]</pre>
     *
     * @param indentSpaces the number of spaces to indent for each level of
     *                     nesting.
     * @return The string form of this array.
     * @throws JSONException Only if there is a coding error.
     */
    public String toString(int indentSpaces) throws JSONException {
        return toString(new JSONStringer(indentSpaces));
    }

    /**
     * Encodes this array using {@link JSONStringer} provided
     *
     * @param stringer - {@link JSONStringer} to be used for serialization
     * @return The string representation of this.
     * @throws JSONException On internal errors. Shouldn't happen.
     */
    public String toString(JSONStringer stringer) throws JSONException {
        encode(stringer);
        return stringer.toString();
    }

    /**
     * Encodes this array using {@link JSONStringer} provided
     *
     * @param stringer - {@link JSONStringer} to be used for serialization
     * @throws JSONException On internal errors. Shouldn't happen.
     */
    protected void encode(JSONStringer stringer) {
        stringer.array();
        for (Object value : values) {
            stringer.value(value);
        }
        stringer.endArray();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JSONArray && ((JSONArray) o).values.equals(values);
    }

    @Override
    public int hashCode() {
        // diverge from the original, which doesn't implement hashCode
        return values.hashCode();
    }
}
