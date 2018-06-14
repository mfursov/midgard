package midgard.json

class JSONObject {

    private val nameValuePairs = LinkedHashMap<String, Any?>()

    constructor()

    constructor(readFrom: JSONTokener) {
        val o = readFrom.nextValue()
        if (o is JSONObject) nameValuePairs.putAll(o.nameValuePairs) else throw IllegalArgumentException("Not a valid JSON object")
    }

    constructor(json: String) : this(JSONTokener(json))

    fun size() = nameValuePairs.size

    operator fun set(name: String, value: Boolean) = setUnsafe(name, value)

    operator fun set(name: String, value: Double) = setUnsafe(name, JSON.checkDouble(value))

    operator fun set(name: String, value: Int) = setUnsafe(name, value)

    operator fun set(name: String, value: Long) = setUnsafe(name, value)

    operator fun set(name: String, value: JSONObject) = setUnsafe(name, value)

    operator fun set(name: String, value: JSONArray) = setUnsafe(name, value)

    operator fun set(name: String, value: String) = setUnsafe(name, value)

    fun setNull(name: String) = setUnsafe(name, null)

    internal fun setUnsafe(name: String, value: Any?): JSONObject {
        nameValuePairs[checkName(name)] = value
        return this
    }

    private fun checkName(name: String?) = name ?: throw IllegalArgumentException("Name can't be null")

    fun remove(name: String) = nameValuePairs.remove(name)

    fun isNull(name: String) = nameValuePairs.containsKey(name) && nameValuePairs[name] == null

    fun has(name: String) = nameValuePairs.containsKey(name)

    private fun getNN(name: String, type: String): Any {
        return get(name) ?: if (has(name)) {
            throw IllegalArgumentException("$name: null can't be converted to $type")
        } else {
            throw IllegalArgumentException("Property not found: $name")
        }
    }

    operator fun get(name: String) = if (nameValuePairs.containsKey(name)) opt(name) else throw IllegalArgumentException("Property not found :$name")

    fun opt(name: String) = nameValuePairs[name]

    fun getBoolean(name: String) = JSON.toBoolean(getNN(name, "boolean"))

    fun optBoolean(name: String): Boolean? {
        val o = opt(name) ?: return null
        return JSON.toBoolean(o)
    }

    fun getDouble(name: String) = JSON.toDouble(getNN(name, "double"))

    fun optDouble(name: String): Double? {
        val o = opt(name) ?: return null
        return JSON.toDouble(o)
    }

    fun getInt(name: String) = JSON.toInt(getNN(name, "int"))

    fun optInt(name: String): Int? {
        val o = opt(name) ?: return null
        return JSON.toInt(o)
    }

    fun getLong(name: String) = JSON.toLong(getNN(name, "long"))

    fun optLong(name: String): Long? {
        val o = opt(name) ?: return null
        return JSON.toLong(o)
    }

    fun getString(name: String) = JSON.toString(getNN(name, "string"))

    fun optString(name: String): String? {
        val o = opt(name) ?: return null
        return JSON.toString(o)
    }

    fun getArray(name: String): JSONArray {
        val o = getNN(name, "array")
        if (o is JSONArray) return o else throw IllegalArgumentException("Not an array: $o")
    }

    fun optArray(name: String): JSONArray? {
        val o = opt(name) ?: return null
        if (o is JSONArray) return o else throw IllegalArgumentException("Not an array: $o")
    }

    fun getObject(name: String): JSONObject {
        val o = getNN(name, "object")
        if (o is JSONObject) return o else throw IllegalArgumentException("Not an object: $o")
    }

    fun optObject(name: String): JSONObject? {
        val o = opt(name) ?: return null
        if (o is JSONObject) return o else throw IllegalArgumentException("Not an object: $o")
    }

    fun keys() = nameValuePairs.keys.iterator()

    fun keySet() = nameValuePairs.keys

    override fun toString() = toString(JSONStringer())

    fun toString(indentSpaces: Int): String {
        return toString(JSONStringer(indentSpaces))
    }

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
}
