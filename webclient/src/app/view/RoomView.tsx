import * as React from "react"
import {MouseEvent} from "react"
import {AppStore} from "../Reducer"
import * as ReactRedux from "react-redux"
import {ExitDirection, RoomInfo} from "../reducer/ServerStateReducer"
import {Dispatch} from "redux"
import {ServerInterface} from "../ServerInterface"
import {GestureRecognizerBinder} from "../util/GestureRecognizerBinder"
import {SimpleSwipeRecognizer, Swipe} from "../util/SimpleSwipeRecognizer"
import {DollarRecognizer, Stroke} from "../util/DollarRecognizer"
import {Rect} from "../reducer/UiStateReducer"
import {newDebugMessage} from "../reducer/DebugStateReducer"

type StateProps = {
    room: RoomInfo,
    rect: Rect
}

type DispatchProps = {
    debug: (message: string) => void
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
        this.gesturesBinder.attach(this.domElement, eventName => {
            console.log("gesture: " + eventName)
            this.props.debug("Gesture: " + eventName)
            let direction = null
            if (eventName === Swipe.Up) {
                direction = ExitDirection.North
            } else if (eventName == Swipe.Down) {
                direction = ExitDirection.South
            } else if (eventName == Swipe.Left) {
                direction = ExitDirection.West
            } else if (eventName == Swipe.Right) {
                direction = ExitDirection.East
            }
            if (direction != null) {
                this.props.server.move(direction)
            }
        })
    }

    componentWillUnmount() {
        this.gesturesBinder.detach(this.domElement)
    }
}

function mapDispatchToProps(dispatch: Dispatch): DispatchProps {
    return {
        debug: message => dispatch(newDebugMessage(message))
    }
}

function mapStateToProps(state: AppStore): StateProps {
    return {
        room: state.serverState.room,
        rect: state.uiState.roomView.rect
    }
}

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(RoomView) as React.ComponentClass<OwnProps>