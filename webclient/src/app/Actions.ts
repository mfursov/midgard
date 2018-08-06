import {Action} from "redux"

export enum ActionType {
    UiStateUpdate,
    ServerStateUpdate
}

export interface MAction<T> extends Action<ActionType> {
    payload: T,
}

