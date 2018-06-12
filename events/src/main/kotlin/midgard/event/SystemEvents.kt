package midgard.event

import midgard.Event

data class TickEvent(val tick: Long) : Event()