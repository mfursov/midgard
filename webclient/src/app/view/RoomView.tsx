import * as React from "react"
import {MouseEvent} from "react"
import {AppStore} from "../Reducer"
import * as ReactRedux from "react-redux"
import {RoomInfo} from "../reducer/ServerStateReducer"
import {Dispatch} from "redux"
import {ServerInterface} from "../ServerInterface"
import {GestureRecognizerBinder} from "../util/GestureRecognizerBinder"
import {SimpleSwipeRecognizer} from "../util/SimpleSwipeRecognizer"
import {DollarRecognizer, Stroke} from "../util/DollarRecognizer"
import {Rect} from "../reducer/UiStateReducer"

type StateProps = {
    room: RoomInfo,
    rect: Rect
}

type DispatchProps = {
    // move: (direction: ExitDirection) => void
}

type OwnProps = {
    server: ServerInterface
}
type AllProps = StateProps & OwnProps & DispatchProps

class RoomView extends React.Component<AllProps, {}> {
    domElement: HTMLElement
    gesturesBinder: GestureRecognizerBinder

    constructor(props: AllProps, state: any) {
        super(props, state)
        this.gesturesBinder = new GestureRecognizerBinder([
            new SimpleSwipeRecognizer(),
            new DollarRecognizer([Stroke.V, Stroke.Circle])
        ])
    }

    render(): React.ReactNode {
        return (
            <div ref={ref => this.domElement = ref} className="room-view" style={
                {
                    left: this.props.rect.x,
                    top: this.props.rect.y,
                    width: this.props.rect.width,
                    height: this.props.rect.height
                }
            }>
                <div style={{textAlign: "center"}}>{this.props.room.name}</div>
                <div className="room-exits-block">
                    {
                        this.props.room.exits.map(exitInfo => {
                            return (
                                <span key={`exit-${exitInfo.direction}`} style={{marginLeft: 5}}>
                                    <a href="#"
                                       className="room-exit"
                                       onClick={(e: MouseEvent) => {
                                           e.preventDefault()
                                           this.props.server.move(exitInfo.direction)
                                       }}>
                                        {exitInfo.name}
                                    </a>
                                </span>)
                        })
                    }
                </div>
            </div>
        )
    }

    componentDidMount() {
        this.gesturesBinder.attach(this.domElement, eventName => console.log("gesture: " + eventName))
    }

    componentWillUnmount() {
        this.gesturesBinder.detach(this.domElement)
    }
}

// noinspection JSUnusedLocalSymbols
function mapDispatchToProps(dispatch: Dispatch): DispatchProps {
    return {}
    // return {move: (direction => dispatch({type: "x:" + direction}))}//todo:
}

function mapStateToProps(state: AppStore): StateProps {
    return {
        room: state.serverState.room,
        rect: state.uiState.roomView.rect
    }
}

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(RoomView) as React.ComponentClass<OwnProps>