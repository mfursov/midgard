package midgard.web.client

import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onTouchStartFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import kotlin.browser.window

interface TestProps : RProps {
    var name: String
}

class TestView : RComponent<TestProps, RState>() {

//    var xDown = -1
//    var yDown = -1

    override fun RBuilder.render() {
        div(classes = "test-view-class") {
            attrs.onClickFunction = { window.alert("Click $it") }
            attrs.onTouchStartFunction = {
                // xDown = it.touches[0].clientX;
                // yDown = it.touches[0].clientY;
            }
            +"Hello, ${props.name}"
        }
    }
}

fun RBuilder.testView(name: String) = child(TestView::class) {
    attrs.name = name
}