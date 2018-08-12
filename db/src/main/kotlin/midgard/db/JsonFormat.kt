package midgard.db

import midgard.*
import midgard.json.JSONArray
import midgard.json.JSONObject
import java.io.Reader
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

private val DIR_2_JSON = EnumMap<Direction, String>(mutableMapOf(
        Pair(Direction.North, "north"),
        Pair(Direction.East, "east"),
        Pair(Direction.South, "south"),
        Pair(Direction.West, "west"),
        Pair(Direction.Up, "up"),
        Pair(Direction.Down, "down")
))

class JsonFormat : Format {

//    override fun writeRoom(room: Room, w: Writer) {
//        val json = JSONObject()
//        room.apply {
//            json["id"] = id.id
//            json["name"] = name
//            if (objects.isNotEmpty()) json["objects"] = ids2Json(objects)
//            if (characters.isNotEmpty()) json["characters"] = ids2Json(characters)
//            if (exits.isNotEmpty()) json["exits"] = writeExits(exits)
//        }
//        w.write(json.toString(2))
//    }

    override fun readRooms(reader: Reader): List<Room> {
        val areaJson = JSONObject(reader.readText())
        val roomsArray = areaJson.getArray("rooms")
        val roomsList = mutableListOf<Room>()
        for (i in 0 until roomsArray.size()) {
            val roomJson = roomsArray.getObject(i)
            roomsList.add(Room(id = RoomId(roomJson.getString("id")),
                    name = roomJson.getString("name"),
                    objects = mutableSetOf(),
                    characters = mutableSetOf(),
                    exits = readExits(roomJson)))
        }
        return roomsList
    }

}


@Suppress("unused")
private fun ids2Json(objects: Set<Id>): JSONArray {
    val jsonArray = JSONArray()
    objects.forEach { jsonArray.add(it.id) }
    return jsonArray
}

@Suppress("unused")
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

@Suppress("unused")
private fun writeExits(exits: Map<Direction, ExitInfo>): JSONObject {
    val exitsJson = JSONObject()
    exits.forEach { dir, exit -> exitsJson[DIR_2_JSON[dir]!!] = JSONObject().set("to", exit.to.id) }
    return exitsJson
}

private fun readExits(roomJson: JSONObject): MutableMap<Direction, ExitInfo> {
    val result = mutableMapOf<Direction, ExitInfo>()
    DIR_2_JSON.forEach {
        result[it.key] = (readExit(roomJson, it.value) ?: return@forEach)
    }
    return result
}

fun readExit(roomJson: JSONObject, exitName: String): ExitInfo? {
    val exitJson = roomJson.opt(exitName) ?: return null
    if (exitJson is String) {
        return ExitInfo(to = RoomId(exitJson))
    }
    throw RuntimeException("Unsupported exit format: $exitJson")
}
