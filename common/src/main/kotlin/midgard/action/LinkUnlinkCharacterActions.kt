package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.ActionId
import midgard.ActionType
import midgard.Event
import midgard.Midgard
import midgard.area.model.CharacterId
import midgard.event.LinkCharacterEvent
import midgard.event.UnlinkCharacterEvent
import midgard.nextEid
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

val LinkCharacterActionType = ActionType("link-character")

class LinkCharacterAction(id: ActionId, val charId: CharacterId) : Action(LinkCharacterActionType, id)

class LinkCharacterActionHandler : ActionHandler<LinkCharacterAction>, KoinComponent {
    val midgard by inject<Midgard>()

    override fun handleAction(action: LinkCharacterAction): List<Event> {
        val ch = midgard.offlineCharacters[action.charId] ?: return emptyList()
        midgard.characters[ch.id] = ch
        midgard.offlineCharacters.remove(ch.id)
        return listOf(LinkCharacterEvent(nextEid(), ch.id))
    }
}

val UnlinkCharacterActionType = ActionType("unlink-character")

class UnlinkCharacterAction(id: ActionId, val charId: CharacterId) : Action(UnlinkCharacterActionType, id)

class UnlinkCharacterActionHandler : ActionHandler<UnlinkCharacterAction>, KoinComponent {
    val midgard by inject<Midgard>()

    override fun handleAction(action: UnlinkCharacterAction): List<Event> {
        val ch = midgard.characters[action.charId] ?: return emptyList()
        midgard.offlineCharacters[ch.id] = ch
        midgard.characters.remove(ch.id)
        return listOf(UnlinkCharacterEvent(nextEid(), ch.id))
    }
}



