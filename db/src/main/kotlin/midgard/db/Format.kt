package midgard.db

import midgard.Room
import java.io.Reader
import java.io.Writer

interface Format {
    fun readRoom(reader: Reader): Room
    fun writeRoom(id: Room, w: Writer)
}