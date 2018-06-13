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

    fun loadRooms(): List<Room>
    fun saveRooms(rooms: Collection<Room>)

    fun loadCharacters(): List<Character>
    fun saveCharacter(character: Character)

    fun loadObjects(): List<Obj>
    fun saveObject(obj: Obj)

    fun <T : Id> loadIdsList(idsListId: IdsListId): List<T>
    fun <T : Id> saveIdsList(ids: List<T>)

    fun <T> loadProperty(name: String): T
    fun <T> saveProperty(name: String, value: T)
}

interface Translator {
    fun tr(messageId: String): String
}
