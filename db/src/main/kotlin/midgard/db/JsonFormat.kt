package midgard.db

import com.github.openjson.JSONArray
import com.github.openjson.JSONObject
import midgard.*
import java.io.Reader
import java.io.Writer
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


class JsonFormat : Format {

    override fun writeRoom(room: Room, w: Writer) {
        val roomJson = JSONObject()
        roomJson.put("id", room.id.id)
        roomJson.put("name", room.name)
        if (room.objects.isNotEmpty()) roomJson.put("objects", ids2Json(room.objects))
        if (room.characters.isNotEmpty()) roomJson.put("characters", ids2Json(room.characters))
        if (room.exits.isNotEmpty()) roomJson.put("exists", writeExits(room.exits))
        w.write(roomJson.toString(2))
    }

    override fun readRoom(reader: Reader): Room {
        val json = JSONObject(reader.readText())
        return Room(id = RoomId(json.getString("id")),
                name = json.getString("name"),
                objects = json2Ids(json.optJSONArray("objects"), ObjId::class),
                characters = json2Ids(json.optJSONArray("characters"), CharacterId::class),
                exits = readExits(json.optJSONObject("exits")))
    }

}


private fun ids2Json(objects: Set<Id>): JSONArray {
    val jsonArray = JSONArray()
    objects.forEach { jsonArray.put(it.id) }
    return jsonArray
}

private fun <T : Id> json2Ids(jsonArray: JSONArray?, kls: KClass<T>): MutableSet<T> {
    val result = mutableSetOf<T>()
    if (jsonArray != null && jsonArray.length() != 0) {
        val primaryConstructor = kls.primaryConstructor!!
        for (i in 0..jsonArray.length()) {
            val id = jsonArray.getString(i)
            result.add(primaryConstructor.call(id))
        }
    }
    return result
}

private fun writeExits(exits: Map<Direction, ExitInfo>): JSONObject {
    val exitsJson = JSONObject()
    //todo: optimize direction serialization
    exits.forEach({ dir, exit -> exitsJson.put(dir.name.substring(0, 1).toLowerCase(), JSONObject().put("to", exit.to.id)) })
    return exitsJson
}

private fun readExits(json: JSONObject?): MutableMap<Direction, ExitInfo> {
    val result = mutableMapOf<Direction, ExitInfo>()
    json?.keySet()?.forEach {
        val exitJson = json.getJSONObject(it)
        val exitInfo = ExitInfo(to = RoomId(exitJson.getString("to")))
        val dir = when (it[0]) {
            'n' -> Direction.North
            'e' -> Direction.East
            's' -> Direction.South
            'w' -> Direction.West
            'u' -> Direction.Up
            'd' -> Direction.Down
            else -> throw IllegalArgumentException("Failed to parse exit direction: $exitJson")
        }
        result[dir] = exitInfo
    }
    return result
}

