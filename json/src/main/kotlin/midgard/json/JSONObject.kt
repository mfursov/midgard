package midgard.json

class JSONObject {

    private val nameValuePairs = LinkedHashMap<String, Any?>()

    constructor()

    constructor(readFrom: JSONTokener) {
        val o = readFrom.nextValue()
        when (o) {
            is JSONObject -> nameValuePairs.putAll(o.nameValuePairs)
            else -> throw IllegalArgumentException("Not a valid JSON object")
        }
    }

    constructor(json: String) : this(JSONTokener(json))

    fun size() = nameValuePairs.size

    operator fun set(name: String, value: Boolean) = setUnsafe(name, value)

    operator fun set(name: String, value: Double) = setUnsafe(name, JSON.checkDouble(value))

    operator fun set(name: String, value: Long) = setUnsafe(name, value)

    operator fun set(name: String, value: Int) = setUnsafe(name, value.toLong())

    operator fun set(name: String, value: JSONObject?) = setUnsafe(name, value)

    operator fun set(name: String, value: JSONArray) = setUnsafe(name, value)

    operator fun set(name: String, value: String) = setUnsafe(name, value)

    internal fun setUnsafe(name: String, value: Any?): JSONObject {
        nameValuePairs[checkName(name)] = value
        return this
    }

    private fun checkName(name: String?) = name ?: throw IllegalArgumentException("Name can't be null")

    fun remove(name: String) = nameValuePairs.remove(name)

    fun isNull(name: String) = nameValuePairs.containsKey(name) && nameValuePairs[name] == null

    fun has(name: String) = nameValuePairs.containsKey(name)

    private fun <T> notNull(value: T?, name: String) = when {
        value != null -> value
        has(name) -> throw NullPointerException("$name: is null")
        else -> throw IllegalArgumentException("Property not found: $name")
    }

    operator fun get(name: String) = if (nameValuePairs.containsKey(name)) opt(name) else throw IllegalArgumentException("Property not found :$name")

    fun opt(name: String) = nameValuePairs[name]

    fun getBoolean(name: String) = notNull(optBoolean(name), name)

    fun optBoolean(name: String): Boolean? {
        val o = opt(name) ?: return null
        return JSON.toBoolean(o)
    }

    fun getDouble(name: String) = notNull(optDouble(name), name)

    fun optDouble(name: String): Double? {
        val o = opt(name) ?: return null
        return JSON.toDouble(o)
    }

    fun getLong(name: String) = notNull(optLong(name), name)

    fun optLong(name: String): Long? {
        val o = opt(name) ?: return null
        return JSON.toLong(o)
    }

    fun getString(name: String) = notNull(optString(name), name)

    fun optString(name: String): String? {
        val o = opt(name) ?: return null
        return JSON.toString(o)
    }

    fun getArray(name: String) = notNull(optArray(name), name)

    fun optArray(name: String): JSONArray? {
        val o = opt(name) ?: return null
        return JSON.toArray(o)
    }

    fun getObject(name: String) = notNull(optObject(name), name)

    fun optObject(name: String): JSONObject? {
        val o = opt(name) ?: return null
        return JSON.toObject(o)
    }

    fun keySet() = nameValuePairs.keys

    fun toString(indentSpaces: Int) = toString(JSONStringer(indentSpaces))

    fun toString(stringer: JSONStringer): String {
        encode(stringer)
        return stringer.toString()
    }

    fun encode(stringer: JSONStringer) {
        stringer.startObject()
        nameValuePairs.entries.forEach { stringer.entry(it) }
        stringer.endObject()
    }

    fun isEmpty() = size() == 0

    fun isNotEmpty() = size() > 0

    override fun toString() = toString(JSONStringer())

    override fun equals(other: Any?) = other is JSONObject && nameValuePairs == other.nameValuePairs

    override fun hashCode() = nameValuePairs.hashCode()

}
