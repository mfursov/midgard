/**
 * Interface used by client code to trigger server actions.
 */
import {ExitDirection} from "./reducer/ServerStateReducer"

export interface ServerInterface {
    setWebSocket: (socket: WebSocket) => void
    move: (direction: ExitDirection) => void
}

let socket: WebSocket = null

function setWebSocket(socketParam: WebSocket) {
    socket = socketParam
}

function move(direction: ExitDirection) {
    const request = {type: "MoveAction", payload: {direction}}
    socket.send(JSON.stringify(request))
}

export default {
    setWebSocket,
    move
} as ServerInterface
