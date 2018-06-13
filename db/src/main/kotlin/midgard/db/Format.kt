package midgard.db

import midgard.ObjId
import midgard.Room
import java.io.Reader
import java.io.Writer

interface Format {
    fun readRoom(reader: Reader): Room
    fun writeRoom(room: Room, w: Writer)
}