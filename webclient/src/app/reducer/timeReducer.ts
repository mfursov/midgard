import {Action} from "redux"

const initialTimeState: Time = {tick: 0}

export interface Time {
    tick: number
}

export enum TimeActionType {
    Tick
}

export interface TimeAction extends Action<TimeActionType> {
}

export class TickAction implements TimeAction {
    type: TimeActionType = TimeActionType.Tick
    tick: number
}

export function timeReducer(state: Time = initialTimeState, action: TimeAction): Time {
    if (action.type == TimeActionType.Tick) {
        return {...state, tick: action["tick"]}
    }
    return state
}
