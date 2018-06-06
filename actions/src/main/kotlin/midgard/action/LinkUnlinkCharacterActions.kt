package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.World
import midgard.CharacterId
import midgard.event.CharacterLinkedEvent
import midgard.event.CharacterUnlinkedEvent
import kotlin.reflect.KClass

class LinkCharacterAction(val charId: CharacterId) : Action()

class LinkCharacterActionHandler : ActionHandler<LinkCharacterAction> {
    override val actionType: KClass<LinkCharacterAction>
        get() = LinkCharacterAction::class

    override fun handleAction(action: LinkCharacterAction, world: World) {
        val ch = world.offlineCharacters[action.charId] ?: return
        world.characters[ch.id] = ch
        world.offlineCharacters.remove(ch.id)
        world.events.add(CharacterLinkedEvent(ch.id))
    }
}

class UnlinkCharacterAction(val charId: CharacterId) : Action()

class UnlinkCharacterActionHandler : ActionHandler<UnlinkCharacterAction> {
    override val actionType: KClass<UnlinkCharacterAction>
        get() = UnlinkCharacterAction::class

    override fun handleAction(action: UnlinkCharacterAction, world: World) {
        val ch = world.characters[action.charId] ?: return
        world.offlineCharacters[ch.id] = ch
        world.characters.remove(ch.id)
        world.events.add(CharacterUnlinkedEvent(ch.id))
    }
}
