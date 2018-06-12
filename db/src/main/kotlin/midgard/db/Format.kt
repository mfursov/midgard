package midgard.db

import midgard.Room
import java.io.Reader
import java.io.Writer

interface Format {
    fun writePlace(id: Room, w: Writer)
    fun readRoom(reader: Reader): Room
}