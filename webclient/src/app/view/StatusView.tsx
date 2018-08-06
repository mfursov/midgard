import * as React from "react"
import {AppStore} from "../Reducer"
import * as ReactRedux from "react-redux"
import {Rect} from "../reducer/UiStateReducer"

type StateProps = {
    tick: number,
    rect: Rect
}

type OwnProps = {}

class StatusView extends React.Component<StateProps & OwnProps, {}> {
    render(): React.ReactNode {
        return <div className="status-view" style={
            {
                left: this.props.rect.x,
                top: this.props.rect.y,
                width: this.props.rect.width,
                height: this.props.rect.height
            }
        }>
            <div>Tick: {this.props.tick}</div>
        </div>
    }
}

function mapStateToProps(state: AppStore): StateProps {
    return {
        tick: state.serverState.tick,
        rect: state.uiState.statusView.rect
    }
}

export default ReactRedux.connect(mapStateToProps, null)(StatusView) as React.ComponentClass<OwnProps>