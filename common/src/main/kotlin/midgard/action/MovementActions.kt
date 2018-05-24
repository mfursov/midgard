package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.ActionType
import midgard.World
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.event.CharacterEntersEvent
import midgard.event.CharacterLeavesEvents

class WalkAction(val charId: CharacterId, val direction: Direction) : Action(TYPE) {
    companion object {
        val TYPE = ActionType("walk")
    }
}

class WalkActionHandler : ActionHandler<WalkAction> {

    override val type: ActionType
        get() = WalkAction.TYPE

    override fun handleAction(action: WalkAction, world: World) {
        val character = world.characters[action.charId] ?: throw IllegalStateException("Character not found: ${action.charId}")
        val place = world.places[character.placeId] ?: throw IllegalStateException("Character place is not found: ${action.charId}, place: ${character.placeId}")
        val exit = place.exits[action.direction] ?: return
        character.placeId = exit.toPlaceId
        world.events.add(CharacterLeavesEvents(action.charId, action.direction))
        world.events.add(CharacterEntersEvent(action.charId, action.direction))
    }
}
