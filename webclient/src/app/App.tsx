import * as React from 'react'
import {render} from 'react-dom'
import {WebsocketStatusView} from './view/WebsocketStatusView'

class App extends React.Component<{}, {}> {

    render(): React.ReactNode {
        return <WebsocketStatusView/>
    }
}

export function renderApp(elementId: string) {
    console.log('render:'+elementId);
    render(
        <div>
            <App/>
        </div>,
        document.getElementById(elementId)
    )
}
