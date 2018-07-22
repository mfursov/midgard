import {AnyAction, combineReducers, Store} from "redux"
import {ServerState, serverStateReducer} from "./reducer/ServerStateReducer"

interface State2 {
    s: string
}

export interface AppStore extends Store<any, AnyAction> {
    serverState: ServerState;
    state2: State2;
}

const initialState2: State2 = {s: "2"}

function reducer2(state: State2 = initialState2, action: any): State2 {
    return state
}

export default combineReducers({
    serverState: serverStateReducer,
    state2: reducer2
})
