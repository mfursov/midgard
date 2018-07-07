import * as React from "react"
import {AppStore} from "../reducer"
import * as ReactRedux from 'react-redux'

type StateProps = {
    counter: number,
}

type OwnProps = {}

class WebsocketStatusView extends React.Component<StateProps & OwnProps, {}> {
    render(): React.ReactNode {
        return <div>
            <div>Hello React & Websockets!</div>
            <div>Counter: ${this.props.counter}</div>
        </div>
    }
}

function mapStateToProps(state: AppStore): StateProps {
    return {
        counter: state.state1.counter
    }
}

export default ReactRedux.connect(mapStateToProps, null)(WebsocketStatusView) as React.ComponentClass<OwnProps>