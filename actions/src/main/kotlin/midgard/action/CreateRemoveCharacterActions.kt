package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.Character
import midgard.CharacterId
import midgard.World
import midgard.event.CharacterRemovedEvent
import midgard.event.NewCharacterCreatedEvent
import kotlin.reflect.KClass

class CreateCharacterAction(val characterName: String) : Action()

class CreateCharacterActionHandler : ActionHandler<CreateCharacterAction> {
    override val actionType: KClass<CreateCharacterAction>
        get() = CreateCharacterAction::class

    override fun handleAction(action: CreateCharacterAction, world: World) {
        val charId = world.characterIdGenerator.nextId()
        val roomId = world.rooms.keys.asSequence().first()
        val ch = Character(charId, action.characterName, roomId)
        world.offlineCharacters[ch.id] = ch
        world.events.add(NewCharacterCreatedEvent(ch.id, ch.name))
    }
}

class RemoveCharacterAction(val charId: CharacterId) : Action()

class RemoveCharacterActionHandler : ActionHandler<RemoveCharacterAction> {

    override val actionType: KClass<RemoveCharacterAction>
        get() = RemoveCharacterAction::class

    override fun handleAction(action: RemoveCharacterAction, world: World) {
        // only offline characters can be removed
        val character = world.offlineCharacters.remove(action.charId) ?: return //todo: special event?
        world.removedCharacters[character.id] = character
        world.events.add(CharacterRemovedEvent(character.id))
    }
}
