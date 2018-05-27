package midgard

import midgard.action.CreateCharacterActionHandler
import midgard.action.LinkCharacterActionHandler
import midgard.action.RemoveCharacterActionHandler
import midgard.action.UnlinkCharacterActionHandler
import midgard.action.WalkActionHandler
import kotlin.reflect.KClass

internal fun buildActionHandlers(): Map<KClass<Action>, ActionHandler<Action>> {
    val map = mutableMapOf<KClass<Action>, ActionHandler<Action>>()
    add(map, CreateCharacterActionHandler())
    add(map, RemoveCharacterActionHandler())
    add(map, LinkCharacterActionHandler())
    add(map, UnlinkCharacterActionHandler())
    add(map, WalkActionHandler())
    return map
}

@Suppress("UNCHECKED_CAST")
internal fun add(map: MutableMap<KClass<Action>, ActionHandler<Action>>, actionHandler: ActionHandler<*>) {
    if (map.containsKey(actionHandler::class as KClass<Action>)) {
        throw RuntimeException("Duplicate action type: ${actionHandler::class}")
    }
    map[actionHandler.actionType as KClass<Action>] = actionHandler as ActionHandler<Action>
}
