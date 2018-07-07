import * as React from 'react'
import {render} from 'react-dom'
import WebsocketStatusView from './view/WebsocketStatusView'
import {Provider} from "react-redux"
import rootReducer, {AppStore} from "./reducer"
import {createStore} from 'redux'

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
    console.log('render:' + elementId)
    render(
        <Provider store={store}>
            <App/>
        </Provider>,
        document.getElementById(elementId)
    )
}

function loop() {
    setTimeout(function () {
        store.dispatch({type: ""})
        loop()
    }, 2000)
}

loop()