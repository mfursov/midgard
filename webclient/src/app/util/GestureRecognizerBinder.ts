import {GestureRecognizer, GrPoint, GrTrace} from "./GestureRecognizer"

let keyGen = Date.now()

interface GestureTraceListener {
    onStart: (x: number, y: number) => void,
    onMove: (x: number, y: number) => void,
    onEnd: (x: number, y: number) => void
}

function getEventPoint(event: MouseEvent | TouchEvent): GrPoint {
    const mouseEvent = event.type.indexOf("mouse") === 0
    const x = mouseEvent ? (event as MouseEvent).clientX : (event as TouchEvent).changedTouches[0].clientX
    const y = mouseEvent ? (event as MouseEvent).clientY : (event as TouchEvent).changedTouches[0].clientY
    const rect = (event.target as HTMLElement).getBoundingClientRect() as DOMRect
    return {
        x: x - rect.x - window.scrollX,
        y: y - rect.y - window.scrollY
    }
}

export class GestureRecognizerBinder {
    private readonly key: string
    private readonly recognizers: GestureRecognizer[]

    constructor(recognizers: GestureRecognizer[], key: string = "gr-" + (++keyGen)) {
        this.key = key
        this.recognizers = recognizers
        this.pointerDownEvent = this.pointerDownEvent.bind(this)
        this.pointerMoveEvent = this.pointerMoveEvent.bind(this)
        this.pointerUpEvent = this.pointerUpEvent.bind(this)
        this.resizeEvent = this.resizeEvent.bind(this)
        this.pointerLeaveEvent = this.pointerLeaveEvent.bind(this)
    }

    attach(e: HTMLElement, callback: (eventName: string) => void, traceListener?: GestureTraceListener) {
        e[this.key] = {points: [], callback, traceListener} as GrData
        e.addEventListener("mousedown", this.pointerDownEvent)
        e.addEventListener("mousemove", this.pointerMoveEvent)
        e.addEventListener("mouseup", this.pointerUpEvent)
        e.addEventListener("touchstart", this.pointerDownEvent)
        e.addEventListener("touchmove", this.pointerMoveEvent)
        e.addEventListener("touchend", this.pointerUpEvent)
        e.addEventListener("mouseleave", this.pointerLeaveEvent)
        e.addEventListener("resizeEvent", this.resizeEvent)
    }

    detach(e: HTMLElement) {
        e.removeEventListener("mousedown", this.pointerDownEvent)
        e.removeEventListener("mousemove", this.pointerMoveEvent)
        e.removeEventListener("mouseup", this.pointerUpEvent)
        e.removeEventListener("touchstart", this.pointerDownEvent)
        e.removeEventListener("touchmove", this.pointerMoveEvent)
        e.removeEventListener("touchend", this.pointerUpEvent)
        e.removeEventListener("mouseleave", this.pointerLeaveEvent)
        e.removeEventListener("resizeEvent", this.resizeEvent)
        e[this.key] = {points: [], callback: null} as GrData
    }

    private pointerDownEvent(event: MouseEvent | TouchEvent) {
        const d = this.getGrData(event.target as HTMLElement)
        if (!d) {
            return
        }
        const point = getEventPoint(event)
        d.points.push(point)
        d.traceListener && d.traceListener.onStart(point.x, point.y)
    }

    private pointerMoveEvent(e: MouseEvent | TouchEvent) {
        const d = this.getGrData(e.target as HTMLElement)
        if (!d || d.points.length == 0) {
            return
        }
        const point = getEventPoint(e)
        d.points.push(point)
        d.traceListener && d.traceListener.onMove(point.x, point.y)
        console.log("move: " + point.x + ":" + point.y)
    }

    private pointerUpEvent(e: MouseEvent | TouchEvent) {
        const d = this.getGrData(e.target as HTMLElement)
        if (!d || d.points.length == 0) {
            return
        }
        const point = getEventPoint(e)
        d.points.push(point)
        const rect = (event.target as HTMLElement).getBoundingClientRect() as DOMRect
        const trace: GrTrace = {rect, points: d.points, millis: 0} //todo:
        for (const r of this.recognizers) {
            console.log("check: "+ r)
            const result = r.recognize(trace)
            if (result != null) {
                d.callback(result.name)
                break
            }
        }
        d.traceListener && d.traceListener.onEnd(point.x, point.y)
        d.points = []
    }

    private pointerLeaveEvent(e: Event) {
        const d = this.getGrData(e.target as HTMLElement)
        if (d && d.points.length > 0) {
            const lastPoint = d.points[d.points.length - 1]
            d.traceListener && d.traceListener.onEnd(lastPoint.x, lastPoint.y)
            d.points = []
        }
    }

    private resizeEvent(e: Event) {
        const element = e.target as HTMLElement
        const gr = this.getGrData(element)
        const callback = gr.callback
        this.detach(element)
        this.attach(element, callback, gr.traceListener)
    }

    private getGrData(e: HTMLElement): GrData {
        return e[this.key] as GrData
    }
}

interface GrData {
    points: GrPoint[]
    callback: (eventName: string) => void
    traceListener?: GestureTraceListener
}
