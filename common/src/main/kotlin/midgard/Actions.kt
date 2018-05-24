package midgard

import midgard.action.CreateCharacterActionHandler
import midgard.action.LinkCharacterActionHandler
import midgard.action.RemoveCharacterActionHandler
import midgard.action.UnlinkCharacterActionHandler
import midgard.action.WalkActionHandler

internal fun buildActionHandlers(): Map<ActionType, ActionHandler<Action>> {
    val map = mutableMapOf<ActionType, ActionHandler<Action>>()
    add(map, CreateCharacterActionHandler())
    add(map, RemoveCharacterActionHandler())
    add(map, LinkCharacterActionHandler())
    add(map, UnlinkCharacterActionHandler())
    add(map, WalkActionHandler())
    return map
}

internal fun add(map: MutableMap<ActionType, ActionHandler<Action>>, actionHandler: ActionHandler<*>) {
    if (map.containsKey(actionHandler.type)) {
        throw RuntimeException("Duplicate action type: ${actionHandler.type}")
    }
    @Suppress("UNCHECKED_CAST")
    map[actionHandler.type] = actionHandler as ActionHandler<Action>
}
