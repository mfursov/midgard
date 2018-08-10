import * as React from "react"
import {AppStore} from "../Reducer"
import * as ReactRedux from "react-redux"
import {Rect} from "../reducer/UiStateReducer"

type StateProps = {
    messages: string,
    rect: Rect
}

type OwnProps = {}

class DebugView extends React.Component<StateProps & OwnProps, {}> {

    render(): React.ReactNode {
        return (
            <div className="debug-view" style={
                {
                    left: this.props.rect.x,
                    top: this.props.rect.y,
                    width: this.props.rect.width,
                    height: this.props.rect.height
                }
            }>
                <pre>{this.props.messages}</pre>
            </div>
        )
    }
}

function mapStateToProps(state: AppStore): StateProps {
    return {
        messages: state.debugState.messages.join("\n"),
        rect: state.uiState.debugView.rect
    }
}

export default ReactRedux.connect(mapStateToProps, null)(DebugView) as React.ComponentClass<OwnProps>