import {Action} from "redux"

export enum ActionType {
    ServerStateUpdate
}

export interface MAction<T> extends Action<ActionType> {
    payload: T,
}

