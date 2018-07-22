import * as React from "react"
import {AppStore} from "../Reducer"
import * as ReactRedux from "react-redux"

type StateProps = {
    tick: number,
}

type OwnProps = {}

class StatusView extends React.Component<StateProps & OwnProps, {}> {
    render(): React.ReactNode {
        return <div>
            <div>Tick: {this.props.tick}</div>
        </div>
    }
}

function mapStateToProps(state: AppStore): StateProps {
    return {
        tick: state.serverState.tick
    }
}

export default ReactRedux.connect(mapStateToProps, null)(StatusView) as React.ComponentClass<OwnProps>