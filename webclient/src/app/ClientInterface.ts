/**
 * Interface used by server code to trigger client actions.
 */
import {startApp} from "./App"

export interface InitContext {
    appElementId: string
}

export interface ClientInterface {
    init(ctx: InitContext);
}

function init(ctx: InitContext) {
    startApp(ctx.appElementId)
}

export default {
    init
} as ClientInterface
