package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.World
import midgard.CharacterId
import midgard.Direction
import midgard.event.CharacterEntersEvent
import midgard.event.CharacterLeavesEvent
import kotlin.reflect.KClass

class WalkAction(val charId: CharacterId, val direction: Direction) : Action()

class WalkActionHandler : ActionHandler<WalkAction> {
    override val actionType: KClass<WalkAction>
        get() = WalkAction::class

    override fun handleAction(action: WalkAction, world: World) {
        val char = world.characters[action.charId] ?: throw IllegalStateException("Character not found: ${action.charId}")
        val place = world.rooms[char.roomId] ?: throw IllegalStateException("Character place is not found: ${action.charId}, place: ${char.roomId}")
        val exit = place.exits[action.direction] ?: return
        char.roomId = exit.to
        world.events.add(CharacterLeavesEvent(action.charId, place.id, action.direction))
        world.events.add(CharacterEntersEvent(action.charId, char.roomId, action.direction))
    }
}
