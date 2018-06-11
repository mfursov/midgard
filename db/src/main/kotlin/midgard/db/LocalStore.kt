package midgard.db

import midgard.Character
import midgard.Id
import midgard.Obj
import midgard.Place
import java.io.File
import java.io.FileReader
import java.io.FileWriter

const val DATA_DIR = "./db/data"

class LocalStore(val format: Format) : Store {

    override fun readPlaces() = File("$DATA_DIR/places").listFiles().map { format.readPlace(FileReader(it)) }

    override fun savePlaces(places: List<Place>) {
        places.forEach {
            format.writePlace(it, FileWriter(File("$DATA_DIR/places/${it.id}")))
        }
    }

    override fun readCharacters(): List<Character> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveCharacter(character: Character) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readObjects(): List<Obj> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveObject(obj: Obj) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Id> readIdsList(idsListId: IdsListId): List<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Id> saveIdsList(ids: List<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> readProperty(name: String): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> saveProperty(name: String, value: T) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}