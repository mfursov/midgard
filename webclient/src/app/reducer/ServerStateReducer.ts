import {Action} from "redux"
import {ActionType, MAction} from "../Actions"

export enum ExitDirection {North = 0, East = 1, South = 2, West = 3, Up = 4, Down = 5}

export interface ExitInfo {
    direction: ExitDirection,
    name: string
}

export interface RoomInfo {
    name: string,
    exits: ExitInfo[]
}

export interface ServerState {
    tick: number
    room: RoomInfo
}

const initialRoomInfo: RoomInfo = {
    name: "Void",
    exits: []
}

const initialServerState: ServerState = {
    tick: 0,
    room: initialRoomInfo
}

export function newServerStateUpdateAction(newState: ServerState): MAction<ServerState> {
    return {
        type: ActionType.ServerStateUpdate,
        payload: newState
    }
}

export function serverStateReducer(state: ServerState = initialServerState, action: Action): ServerState {
    if (action.type == ActionType.ServerStateUpdate) {
        const newServerState = (action as MAction<ServerState>).payload
        return {...newServerState}
    }
    return state
}
