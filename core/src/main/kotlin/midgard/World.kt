package midgard


class World(

        /** All active places in the world. */
        val places: MutableMap<PlaceId, Place>,

        /** Online characters map. */
        val characters: MutableMap<CharacterId, Character>,

        /** Offline characters map. */
        val offlineCharacters: MutableMap<CharacterId, Character>,

        /** Removed characters. They live here for some period of time. */
        val removedCharacters: MutableMap<CharacterId, Character>,

        /** All objects by ID */
        val objects: MutableMap<ObjectId, Object>,

        /** List of all pending events. */
        val events: MutableList<Event>,

        val random: Random,

        val eventIdGenerator: IdGenerator<EventId>,

        val actionIdGenerator: IdGenerator<ActionId>,

        val characterIdGenerator: IdGenerator<CharacterId>,

        val objectIdGenerator: IdGenerator<ObjectId>
)

