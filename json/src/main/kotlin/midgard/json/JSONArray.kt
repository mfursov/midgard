package midgard.json

class JSONArray {

    private val values: MutableList<Any?>

    constructor() {
        values = mutableListOf()
    }

    constructor(tokener: JSONTokener) {
        // Getting the parser to populate this could get tricky. Instead, just parse to temporary JSONArray and then steal the data from that.
        val o = tokener.nextValue()
        if (o is JSONArray) values = o.values else throw IllegalArgumentException("Not a valid array")
    }

    constructor(json: String) : this(JSONTokener(json))

    fun size() = values.size

    fun add(value: Boolean) = addUnsafe(value)

    fun add(value: Double) = addUnsafe(JSON.checkDouble(value))

    //TODO: fun add(value: Number) = if (value is Float || value is Double) addUnsafe(JSON.checkDouble(value.toDouble())) else add(value.toLong())

    fun add(value: Int) = addUnsafe(value.toLong())

    fun add(value: Long) = addUnsafe(value)

    fun add(value: JSONObject) = addUnsafe(value)

    fun add(value: String) = addUnsafe(value)

    fun add(value: JSONArray) = addUnsafe(value)

    fun addNull() = addUnsafe(null)

    internal fun addUnsafe(o: Any?): JSONArray {
        values.add(o)
        return this
    }

    operator fun set(index: Int, value: Boolean) = setUnsafe(index, value)

    operator fun set(index: Int, value: Double) = setUnsafe(index, JSON.checkDouble(value))

    operator fun set(index: Int, value: Int) = setUnsafe(index, value.toLong())

    operator fun set(index: Int, value: Long) = setUnsafe(index, value)

    operator fun set(index: Int, value: String) = setUnsafe(index, value)

    operator fun set(index: Int, value: JSONObject) = setUnsafe(index, value)

    operator fun set(index: Int, array: JSONArray) = setUnsafe(index, array)

    fun setNull(index: Int) = setUnsafe(index, null)

    private fun setUnsafe(index: Int, value: Any?): JSONArray {
        while (values.size <= index) { //todo: remove this logic?
            values.add(null)
        }
        values[index] = value
        return this
    }

    fun isNull(index: Int) = index < values.size && opt(index) === null

    operator fun get(index: Int) = values[index]

    fun opt(index: Int) = if (index < 0 || index >= values.size) null else values[index]

    private fun <T> notNull(value: T?, index: Int) = when {
        value != null -> value
        index <= 0 || index > size() - 1 -> throw IndexOutOfBoundsException("Index: $index is not within a valid range: 0..${size()})")
        else -> throw NullPointerException("[$index] value is null")
    }

    fun remove(index: Int) = if (index < 0 || index >= values.size) null else values.removeAt(index)

    fun getBoolean(index: Int) = notNull(optBoolean(index), index)

    fun optBoolean(index: Int): Boolean? {
        val o = opt(index) ?: return null
        return JSON.toBoolean(o)
    }

    fun getDouble(index: Int) = notNull(optDouble(index), index)

    fun optDouble(index: Int): Double? {
        val o = opt(index) ?: return null
        return JSON.toDouble(o)
    }

    fun getLong(index: Int) = notNull(optLong(index), index)

    fun optLong(index: Int): Long? {
        val o = opt(index) ?: return null
        return JSON.toLong(o)
    }

    fun getString(index: Int) = notNull(optString(index), index)

    fun optString(index: Int): String? {
        val o = opt(index) ?: return null
        return JSON.toString(o)
    }

    fun getArray(index: Int) = notNull(optArray(index), index)

    fun optArray(index: Int): JSONArray? {
        val o = opt(index) ?: return null
        return JSON.toArray(o)
    }

    fun getObject(index: Int) = notNull(optObject(index), index)

    fun optObject(index: Int): JSONObject? {
        val o = opt(index) ?: return null
        return JSON.toObject(o)
    }

    override fun toString() = toString(JSONStringer())

    fun toString(indentSpaces: Int) = toString(JSONStringer(indentSpaces))

    fun toString(stringer: JSONStringer): String {
        encode(stringer)
        return stringer.toString()
    }

    fun encode(stringer: JSONStringer) {
        stringer.array()
        values.forEach { stringer.value(it) }
        stringer.endArray()
    }

    override fun equals(other: Any?) = other is JSONArray && other.values == values

    override fun hashCode() = values.hashCode()

    /**
     * Returns a new string by alternating this array's values with `separator`. This array's string values are quoted and have their special
     * characters escaped. For example, the array containing the strings '12"
     * pizza', 'taco' and 'soda' joined on '+' returns this:
     * <pre>"12\" pizza"+"taco"+"soda"</pre>
     *
     * @param separator The string used to separate the returned values.
     * @return the conjoined values.
     */
    fun join(separator: String): String {
        val stringer = JSONStringer()
        stringer.open(JSONStringer.Scope.NULL, "")
        var i = 0
        val size = values.size
        while (i < size) {
            if (i > 0) {
                stringer.out.append(separator)
            }
            stringer.value(values[i])
            i++
        }
        stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "")
        return stringer.out.toString()
    }

    fun isEmpty() = size() == 0

    fun isNotEmpty() = size() > 0
}
