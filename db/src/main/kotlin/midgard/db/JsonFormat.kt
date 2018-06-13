package midgard.db

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import midgard.Direction
import midgard.Id
import midgard.Room
import java.io.Reader
import java.io.Writer
import kotlin.reflect.full.primaryConstructor


class JsonFormat : Format {
    val gsonFormat = initGSON()

    override fun writeRoom(id: Room, w: Writer) = w.write(gsonFormat.toJson(id))
    override fun readRoom(reader: Reader): Room = gsonFormat.fromJson(reader.readText())
}

private fun initGSON(): Gson {
    return GsonBuilder()
            .registerTypeAdapter<Direction> {
                serialize { it.src.name.substring(0, 1).toLowerCase().toJson() }
                deserialize {
                    when (it.json.string) {
                        "n" -> Direction.North
                        "e" -> Direction.East
                        "s" -> Direction.South
                        "w" -> Direction.West
                        "u" -> Direction.Up
                        "d" -> Direction.Down
                        else -> throw IllegalArgumentException("Illegal direction key: ${it.json.string}")
                    }
                }
            }
            .registerTypeHierarchyAdapter<Id> {
                serialize { it.src.id.toJson() }
                deserialize {
                    val clazz = Class.forName(it.type.typeName).kotlin
                    clazz.primaryConstructor!!.call(it.json.string) as Id?
                }
            }
            .setPrettyPrinting()
            .create()!!
}

