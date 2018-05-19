package midgard

import midgard.action.LinkCharacterActionHandler
import midgard.action.LinkCharacterActionType
import midgard.action.UnlinkCharacterActionHandler
import midgard.action.UnlinkCharacterActionType

internal fun buildActionHandlers(): Map<ActionType, ActionHandler<Action>> {
    val map = mutableMapOf<ActionType, ActionHandler<Action>>()
    add(map, LinkCharacterActionType, LinkCharacterActionHandler())
    add(map, UnlinkCharacterActionType, UnlinkCharacterActionHandler())
    return map
}

internal fun add(map: MutableMap<ActionType, ActionHandler<Action>>, actionType: ActionType, actionHandler: ActionHandler<*>) {
    if (map.containsKey(actionType)) {
        throw RuntimeException("Duplicate action type: $actionType")
    }
    @Suppress("UNCHECKED_CAST")
    map[actionType] = actionHandler as ActionHandler<Action>
}
