import {Action} from "redux"
import {ActionType, MAction} from "../Actions"

export interface Rect {
    x: number,
    y: number,
    width: number,
    height: number
}

export interface UiState {
    roomView: {
        rect: Rect
    }
    statusView: {
        rect: Rect
    }
    debugView: {
        rect: Rect
    }
}

function buildCurrentUiState(): UiState {
    const statusViewHeight = 50
    const debugViewHeight = 50
    const roomViewHeight = window.innerHeight - statusViewHeight - debugViewHeight
    return {
        roomView: {
            rect: {
                x: 0,
                y: 0,
                height: roomViewHeight,
                width: window.innerWidth
            }
        },
        statusView: {
            rect: {
                x: 0,
                y: roomViewHeight,
                height: statusViewHeight,
                width: window.innerWidth
            }
        },
        debugView: {
            rect: {
                x: 0,
                y: roomViewHeight + statusViewHeight,
                height: debugViewHeight,
                width: window.innerWidth
            }
        }
    }
}

export function newUiStateUpdateAction(): MAction<void> {
    return {
        type: ActionType.UiStateUpdate,
        payload: undefined
    }
}

export function uiStateReducer(state: UiState = buildCurrentUiState(), action: Action): UiState {
    if (action.type == ActionType.UiStateUpdate) {
        return buildCurrentUiState()
    }
    return state
}

