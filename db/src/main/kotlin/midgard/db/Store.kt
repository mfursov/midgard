package midgard.db

import midgard.Character
import midgard.Id
import midgard.Obj
import midgard.Room

enum class IdsListId {
    OnlineCharacters,
    OfflineCharacters,
    RemovedCharacters
}

interface Store {

    fun readRooms(): List<Room>
    fun saveRooms(rooms: List<Room>)

    fun readCharacters(): List<Character>
    fun saveCharacter(character: Character)

    fun readObjects(): List<Obj>
    fun saveObject(obj: Obj)

    fun <T : Id> readIdsList(idsListId: IdsListId): List<T>
    fun <T : Id> saveIdsList(ids: List<T>)

    fun <T> readProperty(name: String): T
    fun <T> saveProperty(name: String, value: T)
}

interface Translator {
    fun tr(messageId: String): String
}
