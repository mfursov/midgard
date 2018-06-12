import * as React from 'react'
import { render } from 'react-dom'
import { WebsocketStatusView } from './page/WebsocketStatusView'

class App extends React.Component<{}, {}> {

  render(): React.ReactNode {
    return <WebsocketStatusView/>
  }
}

export function renderApp(elementId: string) {
  render(
    <div>
      <App/>
    </div>,
    document.getElementById(elementId)
  )
}
