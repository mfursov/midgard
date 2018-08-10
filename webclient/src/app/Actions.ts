import {Action} from "redux"

export enum ActionType {
    UiStateUpdate,
    ServerStateUpdate,
    DebugMessage
}

export interface MAction<T> extends Action<ActionType> {
    payload: T,
}

