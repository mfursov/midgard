import * as React from "react"
import {render} from "react-dom"
import StatusView from "./view/StatusView"
import {Provider} from "react-redux"
import rootReducer, {AppStore} from "./Reducer"
import {createStore} from "redux"
import {newServerStateUpdateAction, ServerState} from "./reducer/ServerStateReducer"
import RoomView from "./view/RoomView"
import server from "./ServerInterface"
import {newUiStateUpdateAction} from "./reducer/UiStateReducer"

class App extends React.Component<{}, {}> {

    render(): React.ReactNode {
        return (
            <div>
                <RoomView server={server}/>
                <StatusView/>
            </div>
        )
    }
}

function getStoreEnhancer() {
    return window["__REDUX_DEVTOOLS_EXTENSION__"] && window["__REDUX_DEVTOOLS_EXTENSION__"]()
}

const store: AppStore = createStore(rootReducer, getStoreEnhancer())

export function startApp(elementId: string) {
    render(
        <Provider store={store}>
            <App/>
        </Provider>,
        document.getElementById(elementId)
    )

    initUiState()
    startWebSocketInterface()
}

function initUiState() {
    window.addEventListener("resize", function () {
        store.dispatch(newUiStateUpdateAction())
    })
}

function startWebSocketInterface() {
    console.log("Starting web-socket interface")
    const socket = new WebSocket("ws://" + window.location.host + "/websocket")

    socket.onerror = (e: Event) => console.log("Socket Error: " + JSON.stringify(e))

    socket.onopen = () => {
        console.log("Connected")
        server.setWebSocket(socket)
    }

    socket.onclose = function (event: CloseEvent) {
        console.log("Disconnected with close code " + event.code + " and " + event.reason)
        setTimeout(startWebSocketInterface, 3000)
    }

    socket.onmessage = function (event: MessageEvent) {
        console.log("onmessage: " + event.data)
        const state: ServerState = JSON.parse(event.data) as ServerState
        const action = newServerStateUpdateAction(state)
        store.dispatch(action)
    }
}
