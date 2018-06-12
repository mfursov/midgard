package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.CharacterId
import midgard.World
import midgard.event.SayEvent
import kotlin.reflect.KClass

//todo: message format?
class SayAction(val charId: CharacterId, val message: String) : Action()

class SayActionHandler : ActionHandler<SayAction> {
    override val actionType: KClass<SayAction>
        get() = SayAction::class

    override fun handleAction(action: SayAction, world: World) {
        val ch = world.characters[action.charId] ?: return;
        world.events.add(SayEvent(ch.id, ch.roomId, action.message))
    }
}
