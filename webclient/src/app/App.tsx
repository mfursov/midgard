import * as React from "react"
import {render} from "react-dom"
import WebsocketStatusView from "./view/WebsocketStatusView"
import {Provider} from "react-redux"
import rootReducer, {AppStore} from "./reducer"
import {createStore} from "redux"
import {TickAction, TimeActionType} from "./reducer/timeReducer"

class App extends React.Component<{}, {}> {

    render(): React.ReactNode {
        return <WebsocketStatusView/>
    }
}

function getStoreEnhancer() {
    return window["__REDUX_DEVTOOLS_EXTENSION__"] && window["__REDUX_DEVTOOLS_EXTENSION__"]()
}

const store: AppStore = createStore(rootReducer, getStoreEnhancer())

export function renderApp(elementId: string) {
    console.log("render:" + elementId)
    render(
        <Provider store={store}>
            <App/>
        </Provider>,
        document.getElementById(elementId)
    )
}

function loop() {
    // setTimeout(function () {
    //     store.dispatch({type: ""})
    //     loop()
    // }, 2000)

    const socket = new WebSocket("ws://" + window.location.host + "/websocket")

    socket.onerror = function () {
        console.log("socket error")
    }

    socket.onopen = function () {
        socket.send("Connected")
    }

    socket.onclose = function (evt) {
        let explanation = ""
        if (evt.reason && evt.reason.length > 0) {
            explanation = "reason: " + evt.reason
        } else {
            explanation = "without a reason specified"
        }

        console.log("Disconnected with close code " + evt.code + " and " + explanation)
        //setTimeout(socket.connect, 5000)
    }

    socket.onmessage = function (event: MessageEvent) {
        console.log("message: "+ event.data)
        store.dispatch({type: TimeActionType.Tick, tick: event.data as number} as TickAction)
    }
}

loop()