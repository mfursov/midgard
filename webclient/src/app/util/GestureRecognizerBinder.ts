import {GestureRecognizer, GrPoint, GrTrace} from "./GestureRecognizer"

let keyGen = Date.now()

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
    }

    attach(e: HTMLElement, callback: (eventName: string) => void) {
        e[this.key] = {points: [], callback} as GrData
        e.addEventListener("mousedown", this.pointerDownEvent)
        e.addEventListener("mousemove", this.pointerMoveEvent)
        e.addEventListener("mouseup", this.pointerUpEvent)
        e.addEventListener("touchstart", this.pointerDownEvent)
        e.addEventListener("touchmove", this.pointerMoveEvent)
        e.addEventListener("touchup", this.pointerUpEvent)
        e.addEventListener("resizeEvent", this.resizeEvent)
    }

    detach(e: HTMLElement) {
        e.removeEventListener("mousedown", this.pointerDownEvent)
        e.removeEventListener("mousemove", this.pointerMoveEvent)
        e.removeEventListener("mouseup", this.pointerUpEvent)
        e.removeEventListener("touchstart", this.pointerDownEvent)
        e.removeEventListener("touchmove", this.pointerMoveEvent)
        e.removeEventListener("touchup", this.pointerUpEvent)
        e.removeEventListener("resizeEvent", this.resizeEvent)
        e[this.key] = {points: [], callback: null} as GrData
    }

    private pointerDownEvent(event: MouseEvent | TouchEvent) {
        const d = this.getGrData(event.target as HTMLElement)
        if (!d) {
            return
        }
        const mouseEvent = event.type == "mousedown"
        const x = mouseEvent ? (event as MouseEvent).clientX : (event as TouchEvent).touches[0].clientX
        const y = mouseEvent ? (event as MouseEvent).clientY : (event as TouchEvent).touches[0].clientY
        const rect = (event.target as HTMLElement).getBoundingClientRect() as DOMRect
        d.points = [{
            x: x - rect.x - window.scrollX,
            y: y - rect.y - window.scrollY
        }]
    }

    private pointerMoveEvent(e: MouseEvent | TouchEvent) {
        const d = this.getGrData(e.target as HTMLElement)
        if (!d || d.points.length == 0) {
            return
        }
        const mouseEvent = e.type == "mousemove"
        const x = mouseEvent ? (e as MouseEvent).clientX : (e as TouchEvent).changedTouches[0].clientX
        const y = mouseEvent ? (e as MouseEvent).clientY : (e as TouchEvent).changedTouches[0].clientY
        const rect = (event.target as HTMLElement).getBoundingClientRect() as DOMRect
        d.points.push({
            x: x - rect.x - window.scrollX,
            y: y - rect.y - window.scrollY
        })
    }

    private pointerUpEvent(e: Event) {
        const d = this.getGrData(e.target as HTMLElement)
        if (!d || d.points.length == 0) {
            return
        }
        const rect = (event.target as HTMLElement).getBoundingClientRect() as DOMRect
        const trace: GrTrace = {rect, points: d.points, millis: 0} //todo:
        let result = null
        for (let i = 0; i < this.recognizers.length; i++) {
            console.log("recognize: " + i)

            result = this.recognizers[i].recognize(trace)
            if (result != null) {
                console.log("result: " + result.name)
                d.callback(result.name)
                break
            }
        }
        d.points = []
    }

    private resizeEvent(e: Event) {
        const element = e.target as HTMLElement
        const callback = this.getGrData(element).callback
        this.detach(element)
        this.attach(element, callback)
    }

    private getGrData(e: HTMLElement): GrData {
        return e[this.key] as GrData
    }
}

interface GrData {
    points: GrPoint[]
    callback: (eventName: string) => void
}
