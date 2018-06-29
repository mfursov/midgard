package midgard.web.client

import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.p
import midgard.common.CommonData
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import react.dom.div
import react.dom.render
import kotlin.browser.document
import kotlin.browser.window


var socket: WebSocket? = null

fun connect() {
    console.log("Begin connect")
    val s = WebSocket("ws://" + window.location.host + "/console")

    s.onerror = { console.log("socket error") }

    s.onopen = { write("Connected"); }

    s.onclose = {
        val e = it as CloseEvent
        val explanation = when {
            e.reason.isNotEmpty() -> "reason: ${e.reason}"
            else -> "without a reason specified"
        }
        write("Disconnected with close code " + e.code + " and " + explanation)
        window.setTimeout({ connect() }, 5000)
    }

    s.onmessage = { received((it as MessageEvent).data.toString()) }
    socket = s
}

fun received(message: String) {
    write(message)
}

fun write(message: String) {
    val line = document.createElement("p") as HTMLElement
    line.className = "message"
    line.textContent = message

    val messagesDiv = document.getElementById("messages") ?: return
    messagesDiv.appendChild(line)
    messagesDiv.scrollTop = line.offsetTop.toDouble()
}

fun onSend() {
    val input = document.getElementById("commandInput") as? HTMLInputElement ?: return
    val text = input.value
    val s = socket
    if (text.isNotEmpty() && s != null) {
        s.send(text)
        input.value = ""
    }

}

fun start2() {
    connect()

    val sendButton = document.getElementById("sendButton") as? HTMLElement ?: throw IllegalStateException("send button not found!")
    sendButton.onclick = { onSend() }

    val input = document.getElementById("commandInput") as? HTMLElement ?: throw IllegalStateException("input field not found!")
    input.onkeydown = {
        if ((it as KeyboardEvent).keyCode == 13) {
            onSend()
        }
    }
}

fun main(args: Array<String>) {
    val data = CommonData("hello_common_data");
    val myDiv = document.create.div("panel") {
        p {
            +"Here is "
            a("http://kotlinlang.org") { +"official Kotlin site" }
        }
    }

    render(document.body) {
        div {
            +"Hello React!"
        }
    }
    document.body!!.append(myDiv)

    //start()
}

