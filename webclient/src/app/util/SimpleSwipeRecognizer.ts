import {GestureRecognizer, GrResult, GrTrace} from "./GestureRecognizer"

export const enum Swipe {
    Left = "swipe left",
    Right = "swipe right",
    Up = "swipe up",
    Down = "swipe down"
}

export const ALL_SWIPES = [Swipe.Left, Swipe.Right, Swipe.Up, Swipe.Down]

export class SimpleSwipeRecognizer implements GestureRecognizer {
    private readonly swipes: Swipe[]

    constructor(swipes: Swipe[] = ALL_SWIPES) {
        this.swipes = swipes
    }

    recognize(trace: GrTrace): GrResult | null {
        const {rect, points, millis} = trace
        if (points.length < 2 || ALL_SWIPES.some(swipe => this.swipes.indexOf(swipe) === -1)) {
            return null
        }
        const restraint = rect.width / 10

        let x = points[0].x
        let y = points[0].y

        let left = true
        let right = true
        let up = true
        let down = true

        for (let i = 1; i < points.length; i++) {
            const x1 = points[i].x
            const y1 = points[i].y
            const dx = x1 - x
            const dy = y1 - y
            const adx = Math.abs(dx)
            const ady = Math.abs(dy)

            if (right && ady >= restraint || dx <= 0 || adx <= ady) {
                right = false
            }
            if (left && ady >= restraint || dx >= 0 || adx <= ady) {
                left = false
            }
            if (up && adx >= restraint || dy >= 0 || ady <= adx) {
                up = false
            }
            if (down && adx >= restraint || dy <= 0 || ady <= adx) {
                down = false
            }
            if (!(right || left || up || down)) {
                return null
            }
            x = x1
            y = y1
        }
        const name = right ? Swipe.Right : left ? Swipe.Left : up ? Swipe.Up : Swipe.Down
        return this.swipes.indexOf(name) >= 0 ? {name, score: 1} : null
    }
}
