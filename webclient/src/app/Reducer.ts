import {AnyAction, combineReducers, Store} from "redux"
import {ServerState, serverStateReducer} from "./reducer/ServerStateReducer"
import {UiState, uiStateReducer} from "./reducer/UiStateReducer"
import {DebugState, debugStateReducer} from "./reducer/DebugStateReducer"


export interface AppStore extends Store<any, AnyAction> {
    serverState: ServerState;
    uiState: UiState;
    debugState: DebugState;
}

export default combineReducers({
    serverState: serverStateReducer,
    uiState: uiStateReducer,
    debugState: debugStateReducer
})
