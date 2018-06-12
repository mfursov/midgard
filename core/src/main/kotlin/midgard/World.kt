package midgard


class World(

        /** All active places in the world. */
        val rooms: MutableMap<RoomId, Room>,

        /** Online characters map. */
        val characters: MutableMap<CharacterId, Character>,

        /** Offline characters map. */
        val offlineCharacters: MutableMap<CharacterId, Character>,

        /** Removed characters. They live here for some period of time. */
        val removedCharacters: MutableMap<CharacterId, Character>,

        /** All objects by ID */
        val objects: MutableMap<ObjId, Obj>,

        /** List of all pending events. */
        val events: MutableList<Event>,

        /** List of all pending actions. */
        val actions: MutableList<Action>,

        val random: Random,

        val eventIdGenerator: IdGenerator<EventId>,

        val actionIdGenerator: IdGenerator<ActionId>,

        val characterIdGenerator: IdGenerator<CharacterId>,

        val objectIdGenerator: IdGenerator<ObjId>,

        var tick: Long
)

