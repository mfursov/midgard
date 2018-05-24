package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.ActionType
import midgard.World
import midgard.area.model.CharacterId
import midgard.event.CharacterLinkedEvent
import midgard.event.CharacterUnlinkedEvent

private val LinkCharacterActionType = ActionType("link-character")

class LinkCharacterAction(val charId: CharacterId) : Action(LinkCharacterActionType)

class LinkCharacterActionHandler : ActionHandler<LinkCharacterAction> {
    override val type: ActionType
        get() = LinkCharacterActionType

    override fun handleAction(action: LinkCharacterAction, world: World) {
        val ch = world.offlineCharacters[action.charId] ?: return
        world.characters[ch.id] = ch
        world.offlineCharacters.remove(ch.id)
        world.events.add(CharacterLinkedEvent(ch.id))
    }
}

private val UnlinkCharacterActionType = ActionType("unlink-character")

class UnlinkCharacterAction(val charId: CharacterId) : Action(UnlinkCharacterActionType)

class UnlinkCharacterActionHandler : ActionHandler<UnlinkCharacterAction> {
    override val type: ActionType
        get() = UnlinkCharacterActionType

    override fun handleAction(action: UnlinkCharacterAction, world: World) {
        val ch = world.characters[action.charId] ?: return
        world.offlineCharacters[ch.id] = ch
        world.characters.remove(ch.id)
        world.events.add(CharacterUnlinkedEvent(ch.id))
    }
}
