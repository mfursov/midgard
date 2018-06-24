package midgard.json

import org.junit.Test
import kotlin.test.assertFailsWith


class JSONTest {

    @Test
    fun checkDoubleNan() {
        assertFailsWith(IllegalArgumentException::class) {
            JSON.checkDouble(Double.NaN)
        }
    }

    @Test
    fun checkDoubleInfinity() {
        assertFailsWith(IllegalArgumentException::class) {
            JSON.checkDouble(Double.NEGATIVE_INFINITY)
        }
    }

    @Test
    fun toBooleanNotBoolean() {
        assertFailsWith(IllegalArgumentException::class) {
            JSON.toBoolean(1)
        }
    }

    @Test
    fun toDoubleNotDouble() {
        assertFailsWith(IllegalArgumentException::class) {
            JSON.toDouble(true)
        }
    }

    @Test
    fun toLongNotLong() {
        assertFailsWith(IllegalArgumentException::class) {
            JSON.toLong(2.2)
        }
    }

    @Test
    fun toObjectNotObject() {
        assertFailsWith(IllegalArgumentException::class) {
            JSON.toObject(JSONArray(1))
        }
    }

    @Test
    fun toArrayNotArray() {
        assertFailsWith(IllegalArgumentException::class) {
            JSON.toArray(arrayOf(1, 2, 3))
        }
    }
}
