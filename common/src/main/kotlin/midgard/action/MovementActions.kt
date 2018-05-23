package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.ActionId
import midgard.ActionType
import midgard.World
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.event.WalkEvent
import midgard.nextEid

private val WalkActionType = ActionType("walk")

class WalkAction(id: ActionId, val charId: CharacterId, val direction: Direction) : Action(WalkActionType, id)

class WalkActionHandler : ActionHandler<WalkAction> {

    override val type: ActionType
        get() = WalkActionType

    override fun handleAction(action: WalkAction, world: World) {
        val character = world.characters[action.charId] ?: throw IllegalStateException("Character not found: ${action.charId}")
        val place = world.places[character.placeId] ?: throw IllegalStateException("Character place is not found: ${action.charId}, place: ${character.placeId}")
        val exit = place.exits[action.direction] ?: return
        character.placeId = exit.toPlaceId
        world.events.add(WalkEvent(nextEid(), action.charId, action.direction))
    }
}
