import {Action} from "redux"
import {ActionType, MAction} from "../Actions"

export interface DebugState {
    messages: string[]
}

export function newDebugMessage(message: string): MAction<string> {
    return {
        type: ActionType.DebugMessage,
        payload: message
    }
}

export function debugStateReducer(state: DebugState = {messages: []}, action: Action): DebugState {
    if (action.type === ActionType.DebugMessage) {
        const message = (action as MAction<string>).payload
        console.debug(message)
        const messages = state.messages
        messages.push(message)
        if (messages.length > 3) {
            messages.shift()
        }
        return {messages, ...state}
    }
    return state
}

