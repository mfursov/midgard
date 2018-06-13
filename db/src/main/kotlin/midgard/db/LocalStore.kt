package midgard.db

import midgard.Character
import midgard.Id
import midgard.Obj
import midgard.Room
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class LocalStore(val dataDir: String, val format: Format) : Store {

    override fun loadRooms() = File("$dataDir/rooms").listFiles().map {
        FileReader(it).use { format.readRoom(it) }
    }

    override fun saveRooms(rooms: Collection<Room>) {
        rooms.forEach { room ->
            FileWriter(File("$dataDir/rooms/${room.id.id}.json"))
                    .use { format.writeRoom(room, it) }
        }
    }

    override fun loadCharacters(): List<Character> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveCharacter(character: Character) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadObjects(): List<Obj> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveObject(obj: Obj) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Id> loadIdsList(idsListId: IdsListId): List<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Id> saveIdsList(ids: List<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> loadProperty(name: String): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> saveProperty(name: String, value: T) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}