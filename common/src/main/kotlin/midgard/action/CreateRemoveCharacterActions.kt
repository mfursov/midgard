package midgard.action

import midgard.Action
import midgard.ActionHandler
import midgard.World
import midgard.area.model.Character
import midgard.area.model.CharacterId
import midgard.event.CharacterRemovedEvent
import midgard.event.NewCharacterCreatedEvent

class CreateCharacterAction(val characterName: String) : Action()

class CreateCharacterActionHandler : ActionHandler<CreateCharacterAction> {
    override fun handleAction(action: CreateCharacterAction, world: World) {
        val charId = CharacterId("${++world.characterIdCounter}")
        val character = Character(charId, action.characterName, world.places.keys.asSequence().first())
        world.offlineCharacters[character.id] = character
        world.events.add(NewCharacterCreatedEvent(character.id))
    }
}

class RemoveCharacterAction(val charId: CharacterId) : Action()

class RemoveCharacterActionHandler : ActionHandler<RemoveCharacterAction> {
    override fun handleAction(action: RemoveCharacterAction, world: World) {
        // only offline characters can be removed
        val character = world.offlineCharacters.remove(action.charId) ?: return //todo: special event?
        world.removedCharacters[character.id] = character
        world.events.add(CharacterRemovedEvent(character.id))
    }
}
