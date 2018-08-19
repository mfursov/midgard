export function animateTrail(x: number, y: number) {
    const sizeInt = 7//getRandomInt(3, 5)
    const particle = document.createElement("div")
    particle.classList.add("trail")
    particle.style.background = "#ffffff"
    particle.style.height = sizeInt + "px"
    particle.style.width = sizeInt + "px"
    particle.style.left = x + "px"
    particle.style.top = y + "px"
    for (const eventType of ["webkitAnimationEnd", "mozAnimationEnd", "MSAnimationEnd", "oanimationend", "animationend"]) {
        particle.addEventListener(eventType, () => {
            particle.remove()
        }, {once: true})
    }
    document.body.appendChild(particle)
}
