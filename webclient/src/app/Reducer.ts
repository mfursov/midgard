import {AnyAction, combineReducers, Store} from "redux"
import {ServerState, serverStateReducer} from "./reducer/ServerStateReducer"
import {UiState, uiStateReducer} from "./reducer/UiStateReducer"


export interface AppStore extends Store<any, AnyAction> {
    serverState: ServerState;
    uiState: UiState;
}

export default combineReducers({
    serverState: serverStateReducer,
    uiState: uiStateReducer
})
