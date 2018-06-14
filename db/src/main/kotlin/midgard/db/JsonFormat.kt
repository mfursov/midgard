package midgard.db

import midgard.*
import midgard.json.JSONArray
import midgard.json.JSONObject
import java.io.Reader
import java.io.Writer
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

private val DIR_2_JSON = EnumMap<Direction, String>(mutableMapOf(
        Pair(Direction.North, "n"),
        Pair(Direction.East, "e"),
        Pair(Direction.South, "s"),
        Pair(Direction.West, "w"),
        Pair(Direction.Up, "u"),
        Pair(Direction.Down, "d")
))

private fun <K, V> Map<K, V>.reversed() = HashMap<V, K>().also { me -> entries.forEach { me[it.value] = it.key } }

private val JSON_2_DIR = DIR_2_JSON.reversed()

class JsonFormat : Format {

    override fun writeRoom(room: Room, w: Writer) {
        val json = JSONObject()
        room.apply {
            json["id"] = id.id
            json["name"] = name
            if (objects.isNotEmpty()) json["objects"] = ids2Json(objects)
            if (characters.isNotEmpty()) json["characters"] = ids2Json(characters)
            if (exits.isNotEmpty()) json["exits"] = writeExits(exits)
        }
        w.write(json.toString(2))
    }

    override fun readRoom(reader: Reader): Room {
        val json = JSONObject(reader.readText())
        return Room(id = RoomId(json.getString("id")),
                name = json.getString("name"),
                objects = json2Ids(json.optArray("objects"), ObjId::class),
                characters = json2Ids(json.optArray("characters"), CharacterId::class),
                exits = readExits(json.optObject("exits")))
    }

}


private fun ids2Json(objects: Set<Id>): JSONArray {
    val jsonArray = JSONArray()
    objects.forEach { jsonArray.add(it.id) }
    return jsonArray
}

private fun <T : Id> json2Ids(jsonArray: JSONArray?, kls: KClass<T>): MutableSet<T> {
    val result = mutableSetOf<T>()
    if (jsonArray != null && jsonArray.size() != 0) {
        val primaryConstructor = kls.primaryConstructor!!
        for (i in 0 until jsonArray.size()) {
            val id = jsonArray.getString(i)
            result.add(primaryConstructor.call(id))
        }
    }
    return result
}

private fun writeExits(exits: Map<Direction, ExitInfo>): JSONObject {
    val exitsJson = JSONObject()
    exits.forEach({ dir, exit -> exitsJson[DIR_2_JSON[dir]!!] = JSONObject().set("to", exit.to.id) })
    return exitsJson
}

private fun readExits(json: JSONObject?): MutableMap<Direction, ExitInfo> {
    val result = mutableMapOf<Direction, ExitInfo>()
    json?.keySet()?.forEach {
        val exitJson = json.getObject(it)
        val dir = JSON_2_DIR[it] ?: throw IllegalArgumentException("Failed to deserialize direction: $it in $exitJson")
        result[dir] = ExitInfo(to = RoomId(exitJson.getString("to")))
    }
    return result
}

