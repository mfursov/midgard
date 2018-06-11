package midgard.db

import midgard.Place
import java.io.Reader
import java.io.Writer

interface Format {
    fun writePlace(id: Place, w: Writer)
    fun readPlace(reader: Reader): Place
}