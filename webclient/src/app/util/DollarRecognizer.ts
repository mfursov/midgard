// DollarRecognizer constants
import {GestureRecognizer, GrPoint, GrResult, GrTrace} from "./GestureRecognizer"

const NUM_POINTS = 64
const SQUARE_SIZE = 250.0
const ZERO_POINT = P(0, 0)
const DIAGONAL = Math.sqrt(SQUARE_SIZE * SQUARE_SIZE + SQUARE_SIZE * SQUARE_SIZE)
const HALF_DIAGONAL = 0.5 * DIAGONAL
const ANGLE_RANGE = deg2Rad(45.0)
const ANGLE_PRECISION = deg2Rad(2.0)
const PHI = 0.5 * (-1.0 + Math.sqrt(5.0)) // Golden Ratio

export enum Stroke {
    Triangle = "triangle",
    X = "x",
    Rectangle = "rectangle",
    Circle = "circle",
    Check = "check",
    Caret = "caret",
    ZigZag = "zig-zag",
    Arrow = "arrow",
    LeftSquareBracket = "left square bracket",
    RightSquareBracket = "right square bracket",
    V = "v",
    Delete = "delete",
    LeftCurlyBrace = "left curly brace",
    RightCurlyBrace = "right curly brace",
    Star = "star",
    PigTail = "pigtail"
}

export class StrokeDef {
    name: string
    points: GrPoint[]

    constructor(name: string, points: GrPoint[]) {
        this.name = name
        this.points = preprocessPoints(points)
    }
}

function P(x: number, y: number): GrPoint {
    return {x, y}
}

// http://depts.washington.edu/madlab/proj/dollar/index.html

const ALL_STROKES: StrokeDef[] = [
    new StrokeDef(Stroke.Triangle, [P(137, 139), P(135, 141), P(133, 144), P(132, 146), P(130, 149), P(128, 151), P(126, 155), P(123, 160), P(120, 166), P(116, 171), P(112, 177), P(107, 183), P(102, 188), P(100, 191), P(95, 195), P(90, 199), P(86, 203), P(82, 206), P(80, 209), P(75, 213), P(73, 213), P(70, 216), P(67, 219), P(64, 221), P(61, 223), P(60, 225), P(62, 226), P(65, 225), P(67, 226), P(74, 226), P(77, 227), P(85, 229), P(91, 230), P(99, 231), P(108, 232), P(116, 233), P(125, 233), P(134, 234), P(145, 233), P(153, 232), P(160, 233), P(170, 234), P(177, 235), P(179, 236), P(186, 237), P(193, 238), P(198, 239), P(200, 237), P(202, 239), P(204, 238), P(206, 234), P(205, 230), P(202, 222), P(197, 216), P(192, 207), P(186, 198), P(179, 189), P(174, 183), P(170, 178), P(164, 171), P(161, 168), P(154, 160), P(148, 155), P(143, 150), P(138, 148), P(136, 148)]),
    new StrokeDef(Stroke.X, [P(87, 142), P(89, 145), P(91, 148), P(93, 151), P(96, 155), P(98, 157), P(100, 160), P(102, 162), P(106, 167), P(108, 169), P(110, 171), P(115, 177), P(119, 183), P(123, 189), P(127, 193), P(129, 196), P(133, 200), P(137, 206), P(140, 209), P(143, 212), P(146, 215), P(151, 220), P(153, 222), P(155, 223), P(157, 225), P(158, 223), P(157, 218), P(155, 211), P(154, 208), P(152, 200), P(150, 189), P(148, 179), P(147, 170), P(147, 158), P(147, 148), P(147, 141), P(147, 136), P(144, 135), P(142, 137), P(140, 139), P(135, 145), P(131, 152), P(124, 163), P(116, 177), P(108, 191), P(100, 206), P(94, 217), P(91, 222), P(89, 225), P(87, 226), P(87, 224)]),
    new StrokeDef(Stroke.Rectangle, [P(78, 149), P(78, 153), P(78, 157), P(78, 160), P(79, 162), P(79, 164), P(79, 167), P(79, 169), P(79, 173), P(79, 178), P(79, 183), P(80, 189), P(80, 193), P(80, 198), P(80, 202), P(81, 208), P(81, 210), P(81, 216), P(82, 222), P(82, 224), P(82, 227), P(83, 229), P(83, 231), P(85, 230), P(88, 232), P(90, 233), P(92, 232), P(94, 233), P(99, 232), P(102, 233), P(106, 233), P(109, 234), P(117, 235), P(123, 236), P(126, 236), P(135, 237), P(142, 238), P(145, 238), P(152, 238), P(154, 239), P(165, 238), P(174, 237), P(179, 236), P(186, 235), P(191, 235), P(195, 233), P(197, 233), P(200, 233), P(201, 235), P(201, 233), P(199, 231), P(198, 226), P(198, 220), P(196, 207), P(195, 195), P(195, 181), P(195, 173), P(195, 163), P(194, 155), P(192, 145), P(192, 143), P(192, 138), P(191, 135), P(191, 133), P(191, 130), P(190, 128), P(188, 129), P(186, 129), P(181, 132), P(173, 131), P(162, 131), P(151, 132), P(149, 132), P(138, 132), P(136, 132), P(122, 131), P(120, 131), P(109, 130), P(107, 130), P(90, 132), P(81, 133), P(76, 133)]),
    new StrokeDef(Stroke.Circle, [P(127, 141), P(124, 140), P(120, 139), P(118, 139), P(116, 139), P(111, 140), P(109, 141), P(104, 144), P(100, 147), P(96, 152), P(93, 157), P(90, 163), P(87, 169), P(85, 175), P(83, 181), P(82, 190), P(82, 195), P(83, 200), P(84, 205), P(88, 213), P(91, 216), P(96, 219), P(103, 222), P(108, 224), P(111, 224), P(120, 224), P(133, 223), P(142, 222), P(152, 218), P(160, 214), P(167, 210), P(173, 204), P(178, 198), P(179, 196), P(182, 188), P(182, 177), P(178, 167), P(170, 150), P(163, 138), P(152, 130), P(143, 129), P(140, 131), P(129, 136), P(126, 139)]),
    new StrokeDef(Stroke.Check, [P(91, 185), P(93, 185), P(95, 185), P(97, 185), P(100, 188), P(102, 189), P(104, 190), P(106, 193), P(108, 195), P(110, 198), P(112, 201), P(114, 204), P(115, 207), P(117, 210), P(118, 212), P(120, 214), P(121, 217), P(122, 219), P(123, 222), P(124, 224), P(126, 226), P(127, 229), P(129, 231), P(130, 233), P(129, 231), P(129, 228), P(129, 226), P(129, 224), P(129, 221), P(129, 218), P(129, 212), P(129, 208), P(130, 198), P(132, 189), P(134, 182), P(137, 173), P(143, 164), P(147, 157), P(151, 151), P(155, 144), P(161, 137), P(165, 131), P(171, 122), P(174, 118), P(176, 114), P(177, 112), P(177, 114), P(175, 116), P(173, 118)]),
    new StrokeDef(Stroke.Caret, [P(79, 245), P(79, 242), P(79, 239), P(80, 237), P(80, 234), P(81, 232), P(82, 230), P(84, 224), P(86, 220), P(86, 218), P(87, 216), P(88, 213), P(90, 207), P(91, 202), P(92, 200), P(93, 194), P(94, 192), P(96, 189), P(97, 186), P(100, 179), P(102, 173), P(105, 165), P(107, 160), P(109, 158), P(112, 151), P(115, 144), P(117, 139), P(119, 136), P(119, 134), P(120, 132), P(121, 129), P(122, 127), P(124, 125), P(126, 124), P(129, 125), P(131, 127), P(132, 130), P(136, 139), P(141, 154), P(145, 166), P(151, 182), P(156, 193), P(157, 196), P(161, 209), P(162, 211), P(167, 223), P(169, 229), P(170, 231), P(173, 237), P(176, 242), P(177, 244), P(179, 250), P(181, 255), P(182, 257)]),
    new StrokeDef(Stroke.ZigZag, [P(307, 216), P(333, 186), P(356, 215), P(375, 186), P(399, 216), P(418, 186)]),
    new StrokeDef(Stroke.Arrow, [P(68, 222), P(70, 220), P(73, 218), P(75, 217), P(77, 215), P(80, 213), P(82, 212), P(84, 210), P(87, 209), P(89, 208), P(92, 206), P(95, 204), P(101, 201), P(106, 198), P(112, 194), P(118, 191), P(124, 187), P(127, 186), P(132, 183), P(138, 181), P(141, 180), P(146, 178), P(154, 173), P(159, 171), P(161, 170), P(166, 167), P(168, 167), P(171, 166), P(174, 164), P(177, 162), P(180, 160), P(182, 158), P(183, 156), P(181, 154), P(178, 153), P(171, 153), P(164, 153), P(160, 153), P(150, 154), P(147, 155), P(141, 157), P(137, 158), P(135, 158), P(137, 158), P(140, 157), P(143, 156), P(151, 154), P(160, 152), P(170, 149), P(179, 147), P(185, 145), P(192, 144), P(196, 144), P(198, 144), P(200, 144), P(201, 147), P(199, 149), P(194, 157), P(191, 160), P(186, 167), P(180, 176), P(177, 179), P(171, 187), P(169, 189), P(165, 194), P(164, 196)]),
    new StrokeDef(Stroke.LeftSquareBracket, [P(140, 124), P(138, 123), P(135, 122), P(133, 123), P(130, 123), P(128, 124), P(125, 125), P(122, 124), P(120, 124), P(118, 124), P(116, 125), P(113, 125), P(111, 125), P(108, 124), P(106, 125), P(104, 125), P(102, 124), P(100, 123), P(98, 123), P(95, 124), P(93, 123), P(90, 124), P(88, 124), P(85, 125), P(83, 126), P(81, 127), P(81, 129), P(82, 131), P(82, 134), P(83, 138), P(84, 141), P(84, 144), P(85, 148), P(85, 151), P(86, 156), P(86, 160), P(86, 164), P(86, 168), P(87, 171), P(87, 175), P(87, 179), P(87, 182), P(87, 186), P(88, 188), P(88, 195), P(88, 198), P(88, 201), P(88, 207), P(89, 211), P(89, 213), P(89, 217), P(89, 222), P(88, 225), P(88, 229), P(88, 231), P(88, 233), P(88, 235), P(89, 237), P(89, 240), P(89, 242), P(91, 241), P(94, 241), P(96, 240), P(98, 239), P(105, 240), P(109, 240), P(113, 239), P(116, 240), P(121, 239), P(130, 240), P(136, 237), P(139, 237), P(144, 238), P(151, 237), P(157, 236), P(159, 237)]),
    new StrokeDef(Stroke.RightSquareBracket, [P(112, 138), P(112, 136), P(115, 136), P(118, 137), P(120, 136), P(123, 136), P(125, 136), P(128, 136), P(131, 136), P(134, 135), P(137, 135), P(140, 134), P(143, 133), P(145, 132), P(147, 132), P(149, 132), P(152, 132), P(153, 134), P(154, 137), P(155, 141), P(156, 144), P(157, 152), P(158, 161), P(160, 170), P(162, 182), P(164, 192), P(166, 200), P(167, 209), P(168, 214), P(168, 216), P(169, 221), P(169, 223), P(169, 228), P(169, 231), P(166, 233), P(164, 234), P(161, 235), P(155, 236), P(147, 235), P(140, 233), P(131, 233), P(124, 233), P(117, 235), P(114, 238), P(112, 238)]),
    new StrokeDef(Stroke.V, [P(89, 164), P(90, 162), P(92, 162), P(94, 164), P(95, 166), P(96, 169), P(97, 171), P(99, 175), P(101, 178), P(103, 182), P(106, 189), P(108, 194), P(111, 199), P(114, 204), P(117, 209), P(119, 214), P(122, 218), P(124, 222), P(126, 225), P(128, 228), P(130, 229), P(133, 233), P(134, 236), P(136, 239), P(138, 240), P(139, 242), P(140, 244), P(142, 242), P(142, 240), P(142, 237), P(143, 235), P(143, 233), P(145, 229), P(146, 226), P(148, 217), P(149, 208), P(149, 205), P(151, 196), P(151, 193), P(153, 182), P(155, 172), P(157, 165), P(159, 160), P(162, 155), P(164, 150), P(165, 148), P(166, 146)]),
    new StrokeDef(Stroke.Delete, [P(123, 129), P(123, 131), P(124, 133), P(125, 136), P(127, 140), P(129, 142), P(133, 148), P(137, 154), P(143, 158), P(145, 161), P(148, 164), P(153, 170), P(158, 176), P(160, 178), P(164, 183), P(168, 188), P(171, 191), P(175, 196), P(178, 200), P(180, 202), P(181, 205), P(184, 208), P(186, 210), P(187, 213), P(188, 215), P(186, 212), P(183, 211), P(177, 208), P(169, 206), P(162, 205), P(154, 207), P(145, 209), P(137, 210), P(129, 214), P(122, 217), P(118, 218), P(111, 221), P(109, 222), P(110, 219), P(112, 217), P(118, 209), P(120, 207), P(128, 196), P(135, 187), P(138, 183), P(148, 167), P(157, 153), P(163, 145), P(165, 142), P(172, 133), P(177, 127), P(179, 127), P(180, 125)]),
    new StrokeDef(Stroke.LeftCurlyBrace, [P(150, 116), P(147, 117), P(145, 116), P(142, 116), P(139, 117), P(136, 117), P(133, 118), P(129, 121), P(126, 122), P(123, 123), P(120, 125), P(118, 127), P(115, 128), P(113, 129), P(112, 131), P(113, 134), P(115, 134), P(117, 135), P(120, 135), P(123, 137), P(126, 138), P(129, 140), P(135, 143), P(137, 144), P(139, 147), P(141, 149), P(140, 152), P(139, 155), P(134, 159), P(131, 161), P(124, 166), P(121, 166), P(117, 166), P(114, 167), P(112, 166), P(114, 164), P(116, 163), P(118, 163), P(120, 162), P(122, 163), P(125, 164), P(127, 165), P(129, 166), P(130, 168), P(129, 171), P(127, 175), P(125, 179), P(123, 184), P(121, 190), P(120, 194), P(119, 199), P(120, 202), P(123, 207), P(127, 211), P(133, 215), P(142, 219), P(148, 220), P(151, 221)]),
    new StrokeDef(Stroke.RightCurlyBrace, [P(117, 132), P(115, 132), P(115, 129), P(117, 129), P(119, 128), P(122, 127), P(125, 127), P(127, 127), P(130, 127), P(133, 129), P(136, 129), P(138, 130), P(140, 131), P(143, 134), P(144, 136), P(145, 139), P(145, 142), P(145, 145), P(145, 147), P(145, 149), P(144, 152), P(142, 157), P(141, 160), P(139, 163), P(137, 166), P(135, 167), P(133, 169), P(131, 172), P(128, 173), P(126, 176), P(125, 178), P(125, 180), P(125, 182), P(126, 184), P(128, 187), P(130, 187), P(132, 188), P(135, 189), P(140, 189), P(145, 189), P(150, 187), P(155, 186), P(157, 185), P(159, 184), P(156, 185), P(154, 185), P(149, 185), P(145, 187), P(141, 188), P(136, 191), P(134, 191), P(131, 192), P(129, 193), P(129, 195), P(129, 197), P(131, 200), P(133, 202), P(136, 206), P(139, 211), P(142, 215), P(145, 220), P(147, 225), P(148, 231), P(147, 239), P(144, 244), P(139, 248), P(134, 250), P(126, 253), P(119, 253), P(115, 253)]),
    new StrokeDef(Stroke.Star, [P(75, 250), P(75, 247), P(77, 244), P(78, 242), P(79, 239), P(80, 237), P(82, 234), P(82, 232), P(84, 229), P(85, 225), P(87, 222), P(88, 219), P(89, 216), P(91, 212), P(92, 208), P(94, 204), P(95, 201), P(96, 196), P(97, 194), P(98, 191), P(100, 185), P(102, 178), P(104, 173), P(104, 171), P(105, 164), P(106, 158), P(107, 156), P(107, 152), P(108, 145), P(109, 141), P(110, 139), P(112, 133), P(113, 131), P(116, 127), P(117, 125), P(119, 122), P(121, 121), P(123, 120), P(125, 122), P(125, 125), P(127, 130), P(128, 133), P(131, 143), P(136, 153), P(140, 163), P(144, 172), P(145, 175), P(151, 189), P(156, 201), P(161, 213), P(166, 225), P(169, 233), P(171, 236), P(174, 243), P(177, 247), P(178, 249), P(179, 251), P(180, 253), P(180, 255), P(179, 257), P(177, 257), P(174, 255), P(169, 250), P(164, 247), P(160, 245), P(149, 238), P(138, 230), P(127, 221), P(124, 220), P(112, 212), P(110, 210), P(96, 201), P(84, 195), P(74, 190), P(64, 182), P(55, 175), P(51, 172), P(49, 170), P(51, 169), P(56, 169), P(66, 169), P(78, 168), P(92, 166), P(107, 164), P(123, 161), P(140, 162), P(156, 162), P(171, 160), P(173, 160), P(186, 160), P(195, 160), P(198, 161), P(203, 163), P(208, 163), P(206, 164), P(200, 167), P(187, 172), P(174, 179), P(172, 181), P(153, 192), P(137, 201), P(123, 211), P(112, 220), P(99, 229), P(90, 237), P(80, 244), P(73, 250), P(69, 254), P(69, 252)]),
    new StrokeDef(Stroke.PigTail, [P(81, 219), P(84, 218), P(86, 220), P(88, 220), P(90, 220), P(92, 219), P(95, 220), P(97, 219), P(99, 220), P(102, 218), P(105, 217), P(107, 216), P(110, 216), P(113, 214), P(116, 212), P(118, 210), P(121, 208), P(124, 205), P(126, 202), P(129, 199), P(132, 196), P(136, 191), P(139, 187), P(142, 182), P(144, 179), P(146, 174), P(148, 170), P(149, 168), P(151, 162), P(152, 160), P(152, 157), P(152, 155), P(152, 151), P(152, 149), P(152, 146), P(149, 142), P(148, 139), P(145, 137), P(141, 135), P(139, 135), P(134, 136), P(130, 140), P(128, 142), P(126, 145), P(122, 150), P(119, 158), P(117, 163), P(115, 170), P(114, 175), P(117, 184), P(120, 190), P(125, 199), P(129, 203), P(133, 208), P(138, 213), P(145, 215), P(155, 218), P(164, 219), P(166, 219), P(177, 219), P(182, 218), P(192, 216), P(196, 213), P(199, 212), P(201, 211)])
]

function preprocessPoints(points: GrPoint[]): GrPoint[] {
    points = resample(points, NUM_POINTS)
    const radians = indicativeAngle(points)
    points = rotateBy(points, -radians)
    points = scaleTo(points, SQUARE_SIZE)
    points = translateTo(points, ZERO_POINT)
    return points
}

// Private helper functions from here on down
function resample(points: GrPoint[], nPoints: number): GrPoint[] {
    const pathLen = pathLength(points) / (nPoints - 1) // interval length
    let D = 0.0
    const newPoints = [points[0]]
    for (let i = 1; i < points.length; i++) {
        const d = distance(points[i - 1], points[i])
        if ((D + d) >= pathLen) {
            const qx = points[i - 1].x + ((pathLen - D) / d) * (points[i].x - points[i - 1].x)
            const qy = points[i - 1].y + ((pathLen - D) / d) * (points[i].y - points[i - 1].y)
            const q = P(qx, qy)
            newPoints.push(q)
            points.splice(i, 0, q) // insert 'q' at position i in points s.t. 'q' will be the next i
            D = 0.0
        }
        else D += d
    }
    if (newPoints.length === nPoints - 1) {
        // sometimes we fall a rounding-error short of adding the last point, so add it if so
        newPoints.push(P(points[points.length - 1].x, points[points.length - 1].y))
    }
    return newPoints
}

function indicativeAngle(points: GrPoint[]): number {
    const c = centroid(points)
    return Math.atan2(c.y - points[0].y, c.x - points[0].x)
}


// rotates points around centroid
function rotateBy(points: GrPoint[], radians: number) {
    const c = centroid(points)
    const cos = Math.cos(radians)
    const sin = Math.sin(radians)
    const newPoints = []
    for (let i = 0; i < points.length; i++) {
        const p = points[i]
        const qx = (p.x - c.x) * cos - (p.y - c.y) * sin + c.y
        const qy = (p.x - c.x) * sin + (p.y - c.y) * cos + c.y
        newPoints.push(P(qx, qy))
    }
    return newPoints
}

// non-uniform scale; assumes 2D gestures (i.e., no lines)
function scaleTo(points: GrPoint[], size: number): GrPoint[] {
    const B = boundingBox(points)
    const newPoints = []
    for (let i = 0; i < points.length; i++) {
        let p = points[i]
        const qx = p.x * (size / B.width)
        const qy = p.y * (size / B.height)
        newPoints.push(P(qx, qy))
    }
    return newPoints
}

// translates points' centroid
function translateTo(points: GrPoint[], pt: GrPoint): GrPoint[] {
    const c = centroid(points)
    const newPoints = []
    for (let i = 0; i < points.length; i++) {
        const p = points[i]
        const qx = p.x + pt.x - c.x
        const qy = p.y + pt.y - c.y
        newPoints.push(P(qx, qy))
    }
    return newPoints
}

function distanceAtBestAngle(points: GrPoint[], stroke: StrokeDef, a: number, b: number, threshold: number) {
    let x1 = PHI * a + (1.0 - PHI) * b
    let f1 = distanceAtAngle(points, stroke, x1)
    let x2 = (1.0 - PHI) * a + PHI * b
    let f2 = distanceAtAngle(points, stroke, x2)
    while (Math.abs(b - a) > threshold) {
        if (f1 < f2) {
            b = x2
            x2 = x1
            f2 = f1
            x1 = PHI * a + (1.0 - PHI) * b
            f1 = distanceAtAngle(points, stroke, x1)
        } else {
            a = x1
            x1 = x2
            f1 = f2
            x2 = (1.0 - PHI) * a + PHI * b
            f2 = distanceAtAngle(points, stroke, x2)
        }
    }
    return Math.min(f1, f2)
}

function distanceAtAngle(points: GrPoint[], stroke: StrokeDef, radians: number) {
    const newPoints = rotateBy(points, radians)
    return pathDistance(newPoints, stroke.points)
}

function centroid(points: GrPoint[]): GrPoint {
    let x = 0.0, y = 0.0
    for (let i = 0; i < points.length; i++) {
        x += points[i].x
        y += points[i].y
    }
    x /= points.length
    y /= points.length
    return P(x, y)
}

function boundingBox(points: GrPoint[]): DOMRect {
    let minX = +Infinity, maxX = -Infinity, minY = +Infinity, maxY = -Infinity
    for (let i = 0; i < points.length; i++) {
        minX = Math.min(minX, points[i].x)
        minY = Math.min(minY, points[i].y)
        maxX = Math.max(maxX, points[i].x)
        maxY = Math.max(maxY, points[i].y)
    }
    return new DOMRect(minX, minY, maxX - minX, maxY - minY)
}

function pathDistance(pts1: GrPoint[], pts2: GrPoint[]): number {
    let d = 0.0
    for (let i = 0; i < pts1.length; i++) {// assumes pts1.length == pts2.length
        d += distance(pts1[i], pts2[i])
    }
    return d / pts1.length
}

function pathLength(points: GrPoint[]): number {
    let d = 0.0
    for (let i = 1; i < points.length; i++) {
        d += distance(points[i - 1], points[i])
    }
    return d
}

function distance(p1: GrPoint, p2: GrPoint): number {
    const dx = p2.x - p1.x
    const dy = p2.y - p1.y
    return Math.sqrt(dx * dx + dy * dy)
}

function deg2Rad(d: number): number {
    return (d * Math.PI / 180.0)
}

export class DollarRecognizer implements GestureRecognizer {
    private readonly strokes: string[]

    constructor(strokes: string[] = ALL_STROKES.map(s => s.name)) {
        this.strokes = strokes
    }

    recognize(trace: GrTrace): GrResult | null {
        if (trace.points.length < 10) {
            return null
        }
        const strokes = ALL_STROKES.filter(s => this.strokes.indexOf(s.name) >= 0)
        if (strokes.length == 0) {
            return null
        }
        const points = preprocessPoints(trace.points)
        let bestStroke = null
        let bestScore = 0
        for (let i = 0; i < strokes.length; i++) {
            const stroke = strokes[i]
            const distance = distanceAtBestAngle(points, stroke, -ANGLE_RANGE, +ANGLE_RANGE, ANGLE_PRECISION)
            const score = 1.0 - distance / HALF_DIAGONAL // best (least) distance
            if (score > bestScore) {
                bestScore = score
                bestStroke = stroke
            }
        }
        return bestStroke == null ? null : {name: bestStroke.name, score: bestScore}
    }
}
