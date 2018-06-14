package midgard.json

import java.util.*

@Suppress("unused")
class JSONArray {

    private val values: MutableList<Any?>

    /**
     * Creates a `JSONArray` with no values.
     */
    constructor() {
        values = ArrayList()
    }

    /**
     * Creates a new `JSONArray` with values from the next array in the
     * tokener.
     *
     * @param tokener a tokener whose nextValue() method will yield a
     * `JSONArray`.
     * @throws IllegalArgumentException if the parse fails or doesn't yield a
     * `JSONArray`.
     */
    constructor(tokener: JSONTokener) {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONArray and then steal the data from that.
         */
        val o = tokener.nextValue()
        if (o is JSONArray) {
            values = o.values
        } else {
            JSON.typeMismatch(o, "JSONArray")
        }
    }

    /**
     * Creates a new `JSONArray` with values from the JSON string.
     *
     * @param json a JSON-encoded string containing an array.
     * @throws IllegalArgumentException if the parse fails or doesn't yield a `JSONArray`.
     */
    constructor(json: String) : this(JSONTokener(json))

    /**
     * @return Returns the number of values in this array.
     */
    fun length(): Int {
        return values.size
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @param value The value to append.
     * @return this array.
     */
    fun add(value: Boolean): JSONArray {
        return addUnsafe(value)
    }

    fun addNull(): JSONArray {
        return addUnsafe(null)
    }

    fun addUnsafe(o: Any?): JSONArray {
        values.add(o)
        return this
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this array.
     * @throws IllegalArgumentException If the value is unacceptable.
     */
    fun add(value: Double): JSONArray {
        return addUnsafe(JSON.checkDouble(value))
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @param value The value to append.
     * @return this array.
     */
    fun add(value: Int): JSONArray {
        return addUnsafe(value)
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @param value The value to append.
     * @return this array.
     */
    fun add(value: Long): JSONArray {
        return addUnsafe(value)
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @return this array.
     */
    fun add(value: JSONObject): JSONArray {
        return addUnsafe(value)
    }

    fun add(value: String): JSONArray {
        return addUnsafe(value)
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @return this array.
     */
    fun add(value: JSONArray): JSONArray {
        return addUnsafe(value)
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value The value to set.
     * @return this array.
     * @throws IllegalArgumentException This should never happen.
     */
    operator fun set(index: Int, value: Boolean): JSONArray {
        ensureSize(index)
        values[index] = value
        return this
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this array.
     * @throws IllegalArgumentException If the value is not a finite value.
     */
    operator fun set(index: Int, value: Double): JSONArray {
        // deviate from the original by checking all Numbers, not just floats & doubles
        JSON.checkDouble(value)
        ensureSize(index)
        values[index] = value
        return this
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value The value to set.
     * @return this array.
     * @throws IllegalArgumentException Should never actually happen.
     */
    operator fun set(index: Int, value: Int): JSONArray {
        ensureSize(index)
        values[index] = value
        return this
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @param index Where to add the value.
     * @param value The value to set.
     * @return this array.
     * @throws IllegalArgumentException Should never actually happen.
     */
    operator fun set(index: Int, value: Long): JSONArray {
        ensureSize(index)
        values[index] = value
        return this
    }

    operator fun set(index: Int, value: String): JSONArray {
        ensureSize(index)
        values[index] = value
        return this
    }

    operator fun set(index: Int, value: JSONObject): JSONArray {
        ensureSize(index)
        values[index] = value
        return this
    }

    operator fun set(index: Int, array: JSONArray): JSONArray {
        ensureSize(index)
        values[index] = array
        return this
    }

    fun setNull(index: Int): JSONArray {
        ensureSize(index)
        values[index] = null
        return this
    }

    private fun ensureSize(index: Int) {
        while (values.size <= index) {
            values.add(null)
        }
    }

    /**
     * Returns true if this array has no value at `index`, or if its value
     * is the `null` reference or [JSONObject.NULL].
     *
     * @param index Which value to check.
     * @return true if the value is null.
     */
    fun isNull(index: Int): Boolean {
        val value = opt(index)
        return value === null || value === JSONObject.NULL
    }

    /**
     * Returns the value at `index`.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if this array has no value at `index`, or if
     * that value is the `null` reference. This method returns
     * normally if the value is `JSONObject#NULL`.
     */
    operator fun get(index: Int): Any {
        try {
            return values[index] ?: throw IllegalArgumentException("Value at $index is null.")
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Index " + index + " out of range [0.." + values.size + ")")
        }

    }

    /**
     * Returns the value at `index`, or null if the array has no value
     * at `index`.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    fun opt(index: Int): Any? {
        return if (index < 0 || index >= values.size) {
            null
        } else values[index]
    }

    /**
     * Removes and returns the value at `index`, or null if the array has no value
     * at `index`.
     *
     * @param index Which value to remove.
     * @return The value previously at the specified location.
     */
    fun remove(index: Int): Any? {
        return if (index < 0 || index >= values.size) {
            null
        } else values.removeAt(index)
    }

    /**
     * Returns the value at `index` if it exists and is a boolean or can
     * be coerced to a boolean.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if the value at `index` doesn't exist or
     * cannot be coerced to a boolean.
     */
    fun getBoolean(index: Int): Boolean {
        val o = get(index)
        return JSON.toBoolean(o) ?: JSON.typeMismatch(index, o, "boolean")
    }

    /**
     * Returns the value at `index` if it exists and is a boolean or can
     * be coerced to a boolean. Returns `fallback` otherwise.
     *
     * @param index    Which value to get.
     * @param fallback the fallback value to return if no value exists.
     * @return the value at the specified location or the fallback value.
     */
    @JvmOverloads
    fun optBoolean(index: Int, fallback: Boolean = false): Boolean {
        val `object` = opt(index)
        val result = JSON.toBoolean(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists and is a double or can
     * be coerced to a double.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if the value at `index` doesn't exist or
     * cannot be coerced to a double.
     */
    fun getDouble(index: Int): Double {
        val o = get(index)
        return JSON.toDouble(o) ?: JSON.typeMismatch(index, o, "double")
    }

    /**
     * Returns the value at `index` if it exists and is a double or can
     * be coerced to a double. Returns `fallback` otherwise.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    @JvmOverloads
    fun optDouble(index: Int, fallback: Double = java.lang.Double.NaN): Double {
        val `object` = opt(index)
        val result = JSON.toDouble(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists and is an int or
     * can be coerced to an int.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if the value at `index` doesn't exist or
     * cannot be coerced to a int.
     */
    fun getInt(index: Int): Int {
        val o = get(index)
        return JSON.toInteger(o) ?: JSON.typeMismatch(index, o, "int")
    }

    /**
     * Returns the value at `index` if it exists and is an int or
     * can be coerced to an int. Returns `fallback` otherwise.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    @JvmOverloads
    fun optInt(index: Int, fallback: Int = 0): Int {
        val `object` = opt(index)
        val result = JSON.toInteger(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists and is a long or
     * can be coerced to a long.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if the value at `index` doesn't exist or
     * cannot be coerced to a long.
     */
    fun getLong(index: Int): Long {
        val o = get(index)
        return JSON.toLong(o) ?: JSON.typeMismatch(index, o, "long")
    }

    /**
     * Returns the value at `index` if it exists and is a long or
     * can be coerced to a long. Returns `fallback` otherwise.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    @JvmOverloads
    fun optLong(index: Int, fallback: Long = 0L): Long {
        val `object` = opt(index)
        val result = JSON.toLong(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists, coercing it if
     * necessary.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if no such value exists.
     */
    fun getString(index: Int): String {
        val o = get(index)
        return JSON.toString(o) ?: JSON.typeMismatch(index, o, "String")
    }

    /**
     * Returns the value at `index` if it exists, coercing it if
     * necessary. Returns `fallback` if no such value exists.
     *
     * @param index    Which value to get.
     * @param fallback The fallback value to use if no value is at the specified location.
     * @return the value at the specified location or the fallback value.
     */
    @JvmOverloads
    fun optString(index: Int, fallback: String = ""): String {
        val `object` = opt(index)
        val result = JSON.toString(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONArray`.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if the value doesn't exist or is not a `JSONArray`.
     */
    fun getJSONArray(index: Int): JSONArray {
        val o = get(index)
        if (o is JSONArray) {
            return o
        }
        JSON.typeMismatch(index, o, "JSONArray")
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONArray`. Returns null otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    fun optJSONArray(index: Int): JSONArray? {
        val `object` = opt(index)
        return `object` as? JSONArray
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONObject`.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws IllegalArgumentException if the value doesn't exist or is not a `JSONObject`.
     */
    fun getJSONObject(index: Int): JSONObject {
        val o = get(index)
        if (o is JSONObject) {
            return o
        } else {
            JSON.typeMismatch(index, o, "JSONObject")
        }
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONObject`. Returns null otherwise.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     */
    fun optJSONObject(index: Int): JSONObject? {
        val `object` = opt(index)
        return `object` as? JSONObject
    }

    /**
     * Encodes this array as a compact JSON string, such as:
     * <pre>[94043,90210]</pre>
     *
     *
     * Note 1: this method will not output any fields with 'null' value.
     * Override [JSONStringer.entry] method to have nulls printed.
     *
     *
     * Note 2: this method will suppress any internal exceptions.
     * Use [JSONArray.toString] method directly to handle exceptions manually.
     *
     * @return The string form of this array.
     */
    override fun toString(): String {
        return toString(JSONStringer())
    }

    /**
     * Encodes this array as a human readable JSON string for debugging, such
     * as:
     * <pre>
     * [
     * 94043,
     * 90210
     * ]</pre>
     *
     * @param indentSpaces the number of spaces to indent for each level of
     * nesting.
     * @return The string form of this array.
     * @throws IllegalArgumentException Only if there is a coding error.
     */
    fun toString(indentSpaces: Int): String {
        return toString(JSONStringer(indentSpaces))
    }

    /**
     * Encodes this array using [JSONStringer] provided
     *
     * @param stringer - [JSONStringer] to be used for serialization
     * @return The string representation of this.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    fun toString(stringer: JSONStringer): String {
        encode(stringer)
        return stringer.toString()
    }

    /**
     * Encodes this array using [JSONStringer] provided
     *
     * @param stringer - [JSONStringer] to be used for serialization
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    fun encode(stringer: JSONStringer) {
        stringer.array()
        for (value in values) {
            stringer.value(value)
        }
        stringer.endArray()
    }

    override fun equals(other: Any?): Boolean {
        return other is JSONArray && other.values == values
    }

    override fun hashCode(): Int {
        // diverge from the original, which doesn't implement hashCode
        return values.hashCode()
    }
}
