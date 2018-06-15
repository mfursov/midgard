package midgard.json

class JSONArray {

    private val values: ArrayList<Any?>

    constructor() {
        values = ArrayList()
    }

    constructor(initialSize: Int) {
        values = ArrayList(initialSize)
        resize(initialSize)
    }

    constructor(json: String) : this(JSONTokener(json))

    constructor(tokener: JSONTokener) {
        // Getting the parser to populate this could get tricky.
        // Instead, just parse to temporary JSONArray and then steal the data from that.
        val o = tokener.nextValue()
        if (o is JSONArray) values = o.values else throw IllegalArgumentException("Not a valid array")
    }

    fun size() = values.size

    fun add(value: Boolean) = addUnsafe(value)

    fun add(value: Double) = addUnsafe(JSON.checkDouble(value))

    fun add(value: Int) = addUnsafe(value.toLong())

    fun add(value: Long) = addUnsafe(value)

    fun add(value: String) = addUnsafe(value)

    fun add(value: JSONObject?) = addUnsafe(value)

    fun add(value: JSONArray) = addUnsafe(value)

    internal fun addUnsafe(o: Any?): JSONArray {
        values.add(o)
        return this
    }

    operator fun set(index: Int, value: Boolean) = setUnsafe(index, value)

    operator fun set(index: Int, value: Double) = setUnsafe(index, JSON.checkDouble(value))

    operator fun set(index: Int, value: Int) = setUnsafe(index, value.toLong())

    operator fun set(index: Int, value: Long) = setUnsafe(index, value)

    operator fun set(index: Int, value: String) = setUnsafe(index, value)

    operator fun set(index: Int, value: JSONObject?) = setUnsafe(index, value)

    operator fun set(index: Int, array: JSONArray) = setUnsafe(index, array)

    private fun setUnsafe(index: Int, value: Any?): JSONArray {
        if (!isValidRange(index)) throw IndexOutOfBoundsException("Index $index is out of the valid range [0..${size() - 1}] ")
        values[index] = value
        return this
    }

    fun resize(newSize: Int) {
        when {
            newSize == values.size -> return
            newSize < 0 -> throw IllegalArgumentException("Size < 0: $newSize")
            newSize == 0 -> values.clear()
            newSize < values.size -> while (values.size != newSize) values.removeAt(values.size - 1)
            else -> {
                values.ensureCapacity(newSize)
                while (values.size < newSize) values.add(null)
            }
        }
    }

    fun clear() {
        values.clear()
    }

    fun isNull(index: Int) = isValidRange(index) && opt(index) === null

    operator fun get(index: Int) = values[index]

    fun opt(index: Int) = if (isValidRange(index)) values[index] else null

    private fun <T> notNull(value: T?, index: Int) = when {
        value != null -> value
        !isValidRange(index) -> throw IndexOutOfBoundsException("Index $index is out of the valid range [0..${size() - 1}] ")
        else -> throw NullPointerException("[$index] value is null")
    }

    fun remove(index: Int) = if (isValidRange(index)) values.removeAt(index) else null

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

    private fun isValidRange(index: Int) = index >= 0 && index < values.size

    fun isEmpty() = size() == 0

    fun isNotEmpty() = size() > 0
}
