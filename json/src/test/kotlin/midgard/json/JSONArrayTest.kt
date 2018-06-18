package midgard.json

import org.junit.Assert.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JSONArrayTest {

    @Test
    fun testEmptyArray() {
        val array = JSONArray()
        assertTrue(array.isEmpty())
        try {
            array[0]
            fail()
        } catch (ignored: IndexOutOfBoundsException) {
        }

        try {
            array.getBoolean(0)
            fail()
        } catch (ignored: IndexOutOfBoundsException) {
        }

        assertEquals("[]", array.toString())
        assertEquals("[]", array.toString(4))

        // out of bounds is co-opted with defaulting
        assertFalse(array.isNull(0))
        assertNull(array.opt(0))
        assertNull(array.optBoolean(0))
    }

    @Test
    fun testEqualsAndHashCode() {
        val a = JSONArray()
        val b = JSONArray()
        assertEquals(a, b)
        assertEquals("equals() not consistent with hashCode()", a.hashCode(), b.hashCode())

        a.add(true)
        a.add(false)
        b.add(true)
        b.add(false)
        assertTrue(a.isNotEmpty())
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())

        b.add(true)
        assertNotEquals(a, b)
        assertTrue(a.hashCode() != b.hashCode())
    }

    @Test
    fun testBooleans() {
        val array = JSONArray()
        array.add(true)  // 0
        array.add(false) // 1
        array.resize(4)

        array[2] = false
        array[3] = false
        array[2] = true
        assertEquals("[true,false,true,false]", array.toString())
        assertEquals(4, array.size())
        assertEquals(true, array[0])
        assertEquals(false, array[1])
        assertEquals(true, array[2])
        assertEquals(false, array[3])
        assertFalse(array.isNull(0))
        assertFalse(array.isNull(1))
        assertFalse(array.isNull(2))
        assertFalse(array.isNull(3))
        assertTrue(array.optBoolean(0)!!)
        assertFalse(array.optBoolean(3)!!)
        assertEquals("[\n     true,\n     false,\n     true,\n     false\n]", array.toString(5))

        var other = JSONArray()
        other.add(true)
        other.add(false)
        other.add(true)
        other.add(false)
        assertEquals(array, other)
        other.add(true)
        assertNotEquals(array, other)

        other = JSONArray()
        other.add("true")
        other.add("false")
        assertNotEquals(array, other)
        assertNotEquals(other, array)
        assertTrue(other.getBoolean(0))
    }


    @Test
    fun testCoerceStringToNumber() {
        val array = JSONArray()
        array.add("1")
        array.add("-1")
        assertEquals(1, array.getLong(0))
        assertEquals(-1, array.getLong(1))
    }

    @Test
    fun testCoerceStringToNumberWithFail() {
        val array = JSONArray()
        array.add("not a number")
        try {
            array.getLong(0)
            fail()
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun testCoerceStringToBoolean() {
        val array = JSONArray(1)
        array[0] = "maybe"
        try {
            array.getBoolean(0)
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        array[0] = "true"
        assertTrue(array.getBoolean(0))

        array[0] = "false"
        assertFalse(array.getBoolean(0))
    }

    @Test
    fun testNulls() {
        val array = JSONArray()
        array.resize(4)

        array[3] = null
        assertEquals(4, array.size())
        assertEquals("[null,null,null,null]", array.toString())

        assertNull(array.opt(0))
        assertNull(array.opt(1))
        assertNull(array.opt(2))
        assertNull(array[3])
        assertTrue(array.isNull(0))
        assertTrue(array.isNull(1))
        assertTrue(array.isNull(2))
        assertTrue(array.isNull(3))
        assertEquals(null, array.optString(0))
        assertEquals(null, array.optObject(1))
        assertEquals(null, array.optArray(2))
        assertEquals(null, array.optString(3))
    }

    @Test
    fun testParseNullYieldsJSONObjectNull() {
        val array = JSONArray("[\"null\",null]")
        array.add(null)
        assertEquals("null", array[0])
        assertNull(array[1])
        assertNull(array[2])

        assertEquals("null", array.optString(0))
        assertNull(array.optString(1))
        assertNull(array.optString(2))
    }

    @Test
    fun testNumbers() {
        val array = JSONArray()
        array.add(Double.MIN_VALUE) // 0
        array.add(9223372036854775806L) // 1
        array.add(Double.MAX_VALUE) // 2
        array.add(-0.0) // 3

        val objElement = JSONObject()
        array.add(objElement)

        val arrElement = JSONArray()
        array.add(arrElement)
        array.add(Integer.MIN_VALUE)
        assertEquals(7, array.size())

        // toString() and getString(int) return different values for -0d
        assertEquals("[4.9E-324,9223372036854775806,1.7976931348623157E308,-0.0,{},[],-2147483648]", array.toString())

        assertEquals(Double.MIN_VALUE, array[0])
        assertEquals(9223372036854775806L, array[1])
        assertEquals(Double.MAX_VALUE, array[2])
        assertEquals(-0.0, array[3])
        assertEquals(Double.MIN_VALUE, array.getDouble(0), 0.0)
        assertEquals(Double.MAX_VALUE, array.getDouble(2), 0.0)
        assertEquals(-0.0, array.getDouble(3), 0.0)
        assertEquals(9223372036854775806L, array.getLong(1))
        assertEquals(Double.MIN_VALUE, array.opt(0))
        assertEquals(java.lang.Double.MIN_VALUE, array.optDouble(0)!!, 0.0)
        assertEquals(objElement, array.getObject(4))
        assertEquals(arrElement, array.getArray(5))
        assertEquals(Integer.MIN_VALUE.toLong(), array.getLong(6))

        val other = JSONArray()
        other.add(java.lang.Double.MIN_VALUE)
        other.add(9223372036854775806L)
        other.add(java.lang.Double.MAX_VALUE)
        other.add(-0.0)
        other.add(objElement)
        other.add(arrElement)
        other.add(Integer.MIN_VALUE)
        assertEquals(array, other)
        other[0] = 0L
        other[6] = Integer.MIN_VALUE
        assertNotEquals(array, other)
    }

    @Test
    fun testStrings() {
        val array = JSONArray(1)
        array[0] = "true"
        array.add("5.5") // 1
        array.add("9223372036854775806") // 2
        array.add("null") // 3
        array.add("5\"8' tall") // 4
        assertEquals(5, array.size())
        assertEquals("[\"true\",\"5.5\",\"9223372036854775806\",\"null\",\"5\\\"8' tall\"]", array.toString())
        assertEquals("true", array[0])
        assertEquals("null", array.getString(3))
        assertEquals("5\"8' tall", array.getString(4))
        assertEquals("true", array.opt(0))
        assertEquals("5.5", array.optString(1))
        assertFalse(array.isNull(0))
        assertFalse(array.isNull(3))

        assertTrue(array.getBoolean(0))
        assertTrue(array.optBoolean(0)!!)
        assertEquals(5.5, array.getDouble(1), 0.0)
        assertEquals(9223372036854775806L, array.getLong(2))

        assertFalse(array.isNull(3))
        try {
            array.getDouble(3)
            fail()
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun testSetUnsupportedNumbers() {
        val array = JSONArray()
        try {
            array.add(Double.NaN)
            fail()
        } catch (e: IllegalArgumentException) {
        }
        try {
            array[0] = Double.NEGATIVE_INFINITY
            fail()
        } catch (e: IllegalArgumentException) {
        }
        try {
            array[0] = Double.POSITIVE_INFINITY
            fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun testToStringWithNulls() {
        val array = JSONArray("[" +
                JSONObject()
                        .set("a", "A String")
                        .set("n", 666)
                        .set("null", null)
                        .set("o", JSONObject()
                                .set("b", "B String")
                                .set("null", null)
                                .set("bool", false))
                        .toString()
                + ", " + JSONObject()
                .set("a", "A String")
                .set("n", 666)
                .set("null", null)
                .toString()
                + "]")
        assertEquals("[{\"a\":\"A String\",\"n\":666,\"null\":null,\"o\":{\"b\":\"B String\",\"null\":null,\"bool\":false}},{\"a\":\"A String\",\"n\":666,\"null\":null}]", array.toString())
    }

    @Test
    fun testArrays() {
        val a = JSONArray(1)
        a[0] = a
        assertSame(a, a[0])
    }

    @Test
    fun testTokenerConstructor() {
        val o = JSONArray(JSONTokener("[false]"))
        assertEquals(1, o.size())
        assertEquals(false, o[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTokenerConstructorWrongType() {
        JSONArray(JSONTokener("{\"foo\": false}"))
    }

    @Test(expected = NullPointerException::class)
    fun testTokenerConstructorNull() {
        JSONArray((null as JSONTokener?)!!)
    }

    @Test
    fun testTokenerConstructorParseFail() {
        try {
            JSONArray(JSONTokener("["))
            fail()
        } catch (ignored: IllegalArgumentException) {
        } catch (e: StackOverflowError) {
            fail("Stack overflowed on input: \"[\"")
        }

    }

    @Test
    fun testStringConstructor() {
        val o = JSONArray("[false]")
        assertEquals(1, o.size())
        assertEquals(false, o[0])
    }

    @Test
    fun testStringConstructorWrongType() {
        try {
            JSONArray("{\"foo\": false}")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test(expected = NullPointerException::class)
    fun testStringConstructorNull() {
        JSONArray((null as String?)!!)
    }

    @Test
    fun testStringConstructorParseFail() {
        try {
            JSONArray("[")
            fail()
        } catch (ignored: IllegalArgumentException) {
        } catch (e: StackOverflowError) {
            fail("Stack overflowed on input: \"[\"")
        }

    }

    @Test
    fun testAccessOutOfBounds() {
        val array = JSONArray()
        array.add("foo")
        assertNull(array.opt(3))
        assertNull(array.opt(-3))
        assertEquals(null, array.optString(3))
        assertEquals(null, array.optString(-3))
        try {
            array[3]
            fail()
        } catch (ignored: IndexOutOfBoundsException) {
        }

        try {
            array[-3]
            fail()
        } catch (ignored: IndexOutOfBoundsException) {
        }

        try {
            array.getString(3)
            fail()
        } catch (ignored: IndexOutOfBoundsException) {
        }

        try {
            array.getString(-3)
            fail()
        } catch (ignored: IndexOutOfBoundsException) {
        }

    }

    @Test
    fun testRemove() {
        val a = JSONArray()
        assertNull(a.remove(-1))
        assertNull(a.remove(0))

        a.add("hello")
        assertNull(a.remove(-1))
        assertNull(a.remove(1))
        assertEquals("hello", a.remove(0))
        assertNull(a.remove(0))
    }

    @Test
    fun testResize() {
        val a = JSONArray()
        a.add(1)
        assertEquals(1, a.size())

        a.resize(1)
        assertEquals(1, a.size())

        a.resize(10)
        assertEquals(10, a.size())

        assertEquals(1L, a[0])
        assertTrue(a.isNull(9))
        a.clear()

        assertTrue(a.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testResizeBadRange() {
        JSONArray().resize(-1)
    }

    @Test
    fun testResizeNoOp() {
        val a = JSONArray()
        a.add(1)
        a.resize(1)
        assertEquals(1L, a[0])
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testResizeNoAccess() {
        val a = JSONArray()
        a.resize(10)
        a.resize(2)
        a[3]
    }

    @Test(expected = NullPointerException::class)
    fun testGetNull() {
        JSONArray(1).getLong(0)
    }
}
