package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.ActionId
import midgard.ActionType
import midgard.Event
import midgard.area.model.CharacterId

val LinkCharacterActionType = ActionType("link-character")
val UnlinkCharacterActionType = ActionType("unlink-character")

class LinkCharacterAction(id: ActionId, charId: CharacterId) : Action(LinkCharacterActionType, id)
class UnlinkCharacterAction(id: ActionId, charId: CharacterId) : Action(UnlinkCharacterActionType, id)

class LinkCharacterActionHandler : ActionHandler<LinkCharacterAction> {
    override fun handleAction(action: LinkCharacterAction): List<Event> {
        return emptyList()
    }
}

class UnlinkCharacterActionHandler : ActionHandler<UnlinkCharacterAction> {
    override fun handleAction(action: UnlinkCharacterAction): List<Event> {
        return emptyList()
    }
}



