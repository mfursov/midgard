package midgard.db

import midgard.Room
import java.io.Reader
import java.io.Writer

interface Format {
    fun readRooms(reader: Reader): List<Room>
//    fun writeRoom(room: Room, w: Writer)
}