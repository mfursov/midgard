import {AnyAction, combineReducers, Store} from "redux"
import {Time, timeReducer} from "./reducer/timeReducer"

interface State2 {
    s: string
}

export interface AppStore extends Store<any, AnyAction> {
    time: Time;
    state2: State2;
}

const initialState2: State2 = {s: "2"}

function reducer2(state: State2 = initialState2, action: any): State2 {
    return state
}

export default combineReducers({
    time: timeReducer,
    state2: reducer2
})
