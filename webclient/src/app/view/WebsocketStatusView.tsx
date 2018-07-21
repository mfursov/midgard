import * as React from "react"
import {AppStore} from "../reducer"
import * as ReactRedux from "react-redux"

type StateProps = {
    tick: number,
}

type OwnProps = {}

class WebsocketStatusView extends React.Component<StateProps & OwnProps, {}> {
    render(): React.ReactNode {
        return <div>
            <div>Hello React & Websockets!</div>
            <div>Counter: {this.props.tick}</div>
        </div>
    }
}

function mapStateToProps(state: AppStore): StateProps {
    return {
        tick: state.time.tick
    }
}

export default ReactRedux.connect(mapStateToProps, null)(WebsocketStatusView) as React.ComponentClass<OwnProps>