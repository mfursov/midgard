export interface GrResult {
    name: string
    score: number
}

export interface GrPoint {
    x: number,
    y: number
}

export interface GrTrace {
    rect: DOMRect
    points: GrPoint[]
    millis: number
}

export interface GestureRecognizer {
    recognize: (trace: GrTrace) => GrResult | null
}
