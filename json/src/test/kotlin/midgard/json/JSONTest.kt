package midgard.json

import org.junit.Test

class JSONTest {

    @Test(expected = IllegalArgumentException::class)
    fun checkDoubleNan() {
        JSON.checkDouble(Double.NaN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkDoubleInfinity() {
        JSON.checkDouble(Double.NEGATIVE_INFINITY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun toBooleanNotBoolean() {
        JSON.toBoolean(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun toDoubleNotDouble() {
        JSON.toDouble(true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun toLongNotLong() {
        JSON.toLong(2.2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun toObjectNotObject() {
        JSON.toObject(JSONArray(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun toArrayNotArray() {
        JSON.toArray(arrayOf(1, 2, 3))
    }
}