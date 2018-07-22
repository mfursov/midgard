import * as React from "react"
import {MouseEvent} from "react"
import {AppStore} from "../Reducer"
import * as ReactRedux from "react-redux"
import {RoomInfo} from "../reducer/ServerStateReducer"
import {Dispatch} from "redux"
import {ServerInterface} from "../ServerInterface"
import * as Hammer from "hammerjs"

type StateProps = {
    room: RoomInfo
}

type DispatchProps = {
    // move: (direction: ExitDirection) => void
}

type OwnProps = {
    server: ServerInterface
}

class RoomView extends React.Component<StateProps & OwnProps & DispatchProps, {}> {
    hammer: any
    domElement: HTMLElement

    render(): React.ReactNode {
        return (
            <div ref={ref => this.domElement = ref} className="room-view">
                <div>Room: {this.props.room.name}</div>
                <div>Exits:</div>
                {
                    this.props.room.exits.map(exitInfo => {
                        return (
                            <div key={`exit-${exitInfo.direction}`}>
                                <a href="#" onClick={(e: MouseEvent) => {
                                    e.preventDefault()
                                    this.props.server.move(exitInfo.direction)
                                }}>
                                    {`${exitInfo.direction} to ${exitInfo.name}`}
                                </a>
                            </div>)
                    })
                }
            </div>
        )
    }

    componentDidMount() {
        const options = {}
        const hammer = new Hammer(this.domElement, options)
        hammer.get("swipe").set({direction: Hammer.DIRECTION_ALL})
        hammer.on("swipe", () => console.log("swipe!"))

        this.hammer = hammer
    }

    componentWillUnmount() {
        if (this.hammer) {
            this.hammer.stop()
            this.hammer.destroy()
        }
        this.hammer = null
    }
}

// noinspection JSUnusedLocalSymbols
function mapDispatchToProps(dispatch: Dispatch): DispatchProps {
    return {}
    // return {move: (direction => dispatch({type: "x:" + direction}))}//todo:
}

function mapStateToProps(state: AppStore): StateProps {
    return {room: state.serverState.room}
}

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(RoomView) as React.ComponentClass<OwnProps>