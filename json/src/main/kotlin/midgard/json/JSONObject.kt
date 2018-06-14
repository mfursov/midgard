package midgard.json

import java.util.*

@Suppress("unused")
class JSONObject {

    private val nameValuePairs = LinkedHashMap<String, Any>()

    /**
     * Creates a `JSONObject` with no name/value mappings.
     */
    constructor()

    /**
     * Creates a new `JSONObject` with name/value mappings from the next
     * object in the tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     * `JSONObject`.
     * @throws IllegalArgumentException if the parse fails or doesn't yield a
     * `JSONObject`.
     */
    constructor(readFrom: JSONTokener) {
        init(readFrom)
    }

    /**
     * Creates a new `JSONObject` with name/value mappings from the JSON
     * string.
     *
     * @param json a JSON-encoded string containing an object.
     * @throws IllegalArgumentException if the parse fails or doesn't yield a `JSONObject`.
     */
    constructor(json: String) : this(JSONTokener(json))


    private fun init(readFrom: JSONTokener) {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONObject and then steal the data from that.
         */
        val o = readFrom.nextValue()
        if (o is JSONObject) {
            this.nameValuePairs.putAll(o.nameValuePairs)
        } else {
            JSON.typeMismatch(o, "JSONObject")
        }
    }

    /**
     * Returns the number of name/value mappings in this object.
     *
     * @return the length of this.
     */
    fun length(): Int {
        return nameValuePairs.size
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name of the value to insert.
     * @param value The value to insert.
     * @return this object.
     * @throws IllegalArgumentException Should not be possible.
     */
    operator fun set(name: String, value: Boolean): JSONObject {
        return putUnsafe(checkName(name), value)
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name for the new value.
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this object.
     * @throws IllegalArgumentException if value is NaN or infinite.
     */
    operator fun set(name: String, value: Double): JSONObject {
        return putUnsafe(checkName(name), JSON.checkDouble(value))
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name for the new value.
     * @param value The new value.
     * @return this object.
     * @throws IllegalArgumentException Should not be possible.
     */
    operator fun set(name: String, value: Int): JSONObject {
        return putUnsafe(checkName(name), value)
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param name  The name of the new value.
     * @param value The new value to insert.
     * @return this object.
     * @throws IllegalArgumentException Should not be possible.
     */
    operator fun set(name: String, value: Long): JSONObject {
        return putUnsafe(checkName(name), value)
    }

    operator fun set(name: String, value: JSONObject?): JSONObject {
        return putUnsafe(checkName(name), value)
    }

    operator fun set(name: String, value: JSONArray?): JSONObject {
        return putUnsafe(name, value)
    }

    operator fun set(name: String, value: String?): JSONObject {
        return putUnsafe(name, value)
    }


    fun putUnsafe(name: String, value: Any?): JSONObject {
        if (value == null) {
            nameValuePairs.remove(name)
            return this
        }
        nameValuePairs[checkName(name)] = value
        return this
    }


    fun checkName(name: String?): String {
        if (name == null) {
            throw IllegalArgumentException("Names must be non-null")
        }
        return name
    }

    /**
     * Removes the named mapping if it exists; does nothing otherwise.
     *
     * @param name The name of the mapping to remove.
     * @return the value previously mapped by `name`, or null if there was
     * no such mapping.
     */
    fun remove(name: String): Any? {
        return nameValuePairs.remove(name)
    }

    /**
     * Returns true if this object has no mapping for `name` or if it has
     * a mapping whose value is [.NULL].
     *
     * @param name The name of the value to check on.
     * @return true if the field doesn't exist or is null.
     */
    fun isNull(name: String): Boolean {
        val value = nameValuePairs[name]
        return value === null || value === NULL
    }

    /**
     * Returns true if this object has a mapping for `name`. The mapping
     * may be [.NULL].
     *
     * @param name The name of the value to check on.
     * @return true if this object has a field named `name`
     */
    fun has(name: String): Boolean {
        return nameValuePairs.containsKey(name)
    }

    /**
     * Returns the value mapped by `name`, or throws if no such mapping exists.
     *
     * @param name The name of the value to get.
     * @return The value.
     * @throws IllegalArgumentException if no such mapping exists.
     */
    operator fun get(name: String): Any {
        return nameValuePairs[name] ?: throw IllegalArgumentException("No value for $name")
    }

    /**
     * Returns the value mapped by `name`, or null if no such mapping
     * exists.
     *
     * @param name The name of the value to get.
     * @return The value.
     */
    fun opt(name: String): Any? {
        return nameValuePairs[name]
    }

    /**
     * Returns the value mapped by `name` if it exists and is a boolean or
     * can be coerced to a boolean, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     * to a boolean.
     */
    fun getBoolean(name: String): Boolean {
        val o = get(name)
        return JSON.toBoolean(o) ?: JSON.typeMismatch(name, o, "boolean")
    }

    /**
     * Returns the value mapped by `name` if it exists and is a boolean or
     * can be coerced to a boolean, or `fallback` otherwise.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    @JvmOverloads
    fun optBoolean(name: String, fallback: Boolean = false): Boolean {
        val `object` = opt(name)
        val result = JSON.toBoolean(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists and is a double or
     * can be coerced to a double, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     * to a double.
     */
    fun getDouble(name: String): Double {
        val o = get(name)
        return JSON.toDouble(o) ?: JSON.typeMismatch(name, o, "double")
    }

    /**
     * Returns the value mapped by `name` if it exists and is a double or
     * can be coerced to a double, or `fallback` otherwise.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    @JvmOverloads
    fun optDouble(name: String, fallback: Double = java.lang.Double.NaN): Double {
        val `object` = opt(name)
        val result = JSON.toDouble(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists and is an int or
     * can be coerced to an int, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     * to an int.
     */
    fun getInt(name: String): Int {
        val o = get(name)
        return JSON.toInteger(o) ?: JSON.typeMismatch(name, o, "int")
    }

    /**
     * Returns the value mapped by `name` if it exists and is an int or
     * can be coerced to an int, or `fallback` otherwise.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    @JvmOverloads
    fun optInt(name: String, fallback: Int = 0): Int {
        val `object` = opt(name)
        val result = JSON.toInteger(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists and is a long or
     * can be coerced to a long, or throws otherwise.
     * Note that JSON represents numbers as doubles,
     *
     *
     * so this is [lossy](#lossy); use strings to transfer numbers
     * via JSON without loss.
     *
     * @param name The name of the field that we want.
     * @return The value of the field.
     * @throws IllegalArgumentException if the mapping doesn't exist or cannot be coerced
     * to a long.
     */
    fun getLong(name: String): Long {
        val o = get(name)
        return JSON.toLong(o) ?: JSON.typeMismatch(name, o, "long")
    }

    /**
     * Returns the value mapped by `name` if it exists and is a long or
     * can be coerced to a long, or `fallback` otherwise. Note that JSON represents
     * numbers as doubles, so this is [lossy](#lossy); use strings to transfer
     * numbers via JSON.
     *
     * @param name     The name of the field we want.
     * @param fallback The value to return if the field isn't there.
     * @return The selected value or the fallback.
     */
    @JvmOverloads
    fun optLong(name: String, fallback: Long = 0L): Long {
        val `object` = opt(name)
        val result = JSON.toLong(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists, coercing it if
     * necessary, or throws if no such mapping exists.
     *
     * @param name The name of the field we want.
     * @return The value of the field.
     * @throws IllegalArgumentException if no such mapping exists.
     */
    fun getString(name: String): String {
        val o = get(name)
        return JSON.toString(o) ?: JSON.typeMismatch(name, o, "String")
    }

    /**
     * Returns the value mapped by `name` if it exists, coercing it if
     * necessary, or the empty string if no such mapping exists.
     *
     * @param name The name of the field we want.
     * @return The value of the field.
     */
    fun optString(name: String): String? {
        return optString(name, "")
    }

    /**
     * Returns the value mapped by `name` if it exists, coercing it if
     * necessary, or `fallback` if no such mapping exists.
     *
     * @param name     The name of the field that we want.
     * @param fallback The value to return if the field doesn't exist.
     * @return The value of the field or fallback.
     */
    fun optString(name: String, fallback: String): String {
        val `object` = opt(name)
        val result = JSON.toString(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONArray`, or throws otherwise.
     *
     * @param name The field we want to get.
     * @return The value of the field (if it is a JSONArray.
     * @throws IllegalArgumentException if the mapping doesn't exist or is not a `JSONArray`.
     */
    fun getJSONArray(name: String): JSONArray {
        val `object` = get(name)
        if (`object` is JSONArray) {
            return `object`
        } else {
            JSON.typeMismatch(name, `object`, "JSONArray")
        }
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONArray`, or null otherwise.
     *
     * @param name The name of the field we want.
     * @return The value of the specified field (assuming it is a JSNOArray
     */
    fun optJSONArray(name: String): JSONArray? {
        val `object` = opt(name)
        return `object` as? JSONArray
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONObject`, or throws otherwise.
     *
     * @param name The name of the field that we want.
     * @return a specified field value (if it is a JSONObject)
     * @throws IllegalArgumentException if the mapping doesn't exist or is not a `JSONObject`.
     */
    fun getJSONObject(name: String): JSONObject {
        val `object` = get(name)
        if (`object` is JSONObject) {
            return `object`
        } else {
            JSON.typeMismatch(name, `object`, "JSONObject")
        }
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONObject`, or null otherwise.
     *
     * @param name The name of the value we want.
     * @return The specified value.
     */
    fun optJSONObject(name: String): JSONObject? {
        val o = opt(name)
        return o as? JSONObject
    }

    /**
     * Returns an iterator of the `String` names in this object. The
     * returned iterator supports [remove] which will
     * remove the corresponding mapping from this object. If this object is
     * modified after the iterator is returned, the iterator's behavior is
     * undefined. The order of the keys is undefined.
     *
     * @return an iterator over the keys.
     */
    fun keys(): Iterator<String> {
        return nameValuePairs.keys.iterator()
    }

    /**
     * Returns the set of `String` names in this object. The returned set
     * is a view of the keys in this object. [remove] will remove
     * the corresponding mapping from this object and set iterator behaviour
     * is undefined if this object is modified after it is returned.
     *
     *
     * See [.keys].
     *
     * @return The names in this object.
     */
    fun keySet(): Set<String> {
        return nameValuePairs.keys
    }

    /**
     * Encodes this object as a compact JSON string, such as:
     * <pre>{"query":"Pizza","locations":[94043,90210]}</pre>
     *
     *
     * Note 1: this method will not output any fields with 'null' value.
     * Override [JSONStringer.entry] method to have nulls printed.
     *
     *
     * Note 2: this method will suppress any internal exceptions.
     * Use [JSONObject.toString] method directly to handle exceptions manually.
     */
    override fun toString(): String {
        return toString(JSONStringer())
    }

    /**
     * Encodes this object as a human readable JSON string for debugging, such
     * as:
     * <pre>
     * {
     * "query": "Pizza",
     * "locations": [
     * 94043,
     * 90210
     * ]
     * }</pre>
     *
     * @param indentSpaces the number of spaces to indent for each level of
     * nesting.
     * @return The string containing the pretty form of this.
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    fun toString(indentSpaces: Int): String {
        return toString(JSONStringer(indentSpaces))
    }

    /**
     * Encodes this object using [JSONStringer] provided
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
     * Encodes this object using [JSONStringer] provided
     *
     * @param stringer - [JSONStringer] to be used for serialization
     * @throws IllegalArgumentException On internal errors. Shouldn't happen.
     */
    fun encode(stringer: JSONStringer) {
        stringer.obj()
        for (entry in nameValuePairs.entries) {
            stringer.entry(entry)
        }
        stringer.endObject()
    }

    companion object {
        private const val NEGATIVE_ZERO = -0.0

        /**
         * A sentinel value used to explicitly define a name with no value. Unlike
         * `null`, names with this value:
         *
         *  * show up in the [.keys] iterator
         *  * return `true` for [.has]
         *  * do not throw on [.get]
         *  * are included in the encoded JSON string.
         *
         *
         *
         * This value violates the general contract of [Object.equals] by
         * returning true when compared to `null`. Its [.toString]
         * method returns "null".
         */
        val NULL: Any = object : Any() {
            override fun equals(other: Any?) = other === this || other === null // API specifies this broken equals implementation

            // at least make the broken equals(null) consistent with Objects.hashCode(null).
            override fun hashCode() = 0

            override fun toString() = "null"
        }

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
            return if (doubleValue == longValue.toDouble()) {
                java.lang.Long.toString(longValue)
            } else number.toString()

        }
    }
}
