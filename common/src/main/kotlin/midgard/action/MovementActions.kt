package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.World
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.event.CharacterEntersEvent
import midgard.event.CharacterLeavesEvents
import kotlin.reflect.KClass

class WalkAction(val charId: CharacterId, val direction: Direction) : Action()

class WalkActionHandler : ActionHandler<WalkAction> {
    override val actionType: KClass<WalkAction>
        get() = WalkAction::class

    override fun handleAction(action: WalkAction, world: World) {
        val character = world.characters[action.charId] ?: throw IllegalStateException("Character not found: ${action.charId}")
        val place = world.places[character.placeId] ?: throw IllegalStateException("Character place is not found: ${action.charId}, place: ${character.placeId}")
        val exit = place.exits[action.direction] ?: return
        character.placeId = exit.toPlaceId
        world.events.add(CharacterLeavesEvents(action.charId, action.direction))
        world.events.add(CharacterEntersEvent(action.charId, action.direction))
    }
}
