package midgard.json

import org.junit.Assert.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
        //todo: assertFalse(array.optBoolean(0))
        //todo: assertTrue(array.optBoolean(0, true))
    }

    @Test
    fun testEqualsAndHashCode() {
        val a = JSONArray()
        val b = JSONArray()
        assertEquals(a, b)
        assertEquals("equals() not consistent with hashCode()", a.hashCode().toLong(), b.hashCode().toLong())

        a.add(true)
        a.add(false)
        b.add(true)
        b.add(false)
        assertTrue(a.isNotEmpty())
        assertEquals(a, b)
        assertEquals(a.hashCode().toLong(), b.hashCode().toLong())

        b.add(true)
        assertNotEquals(a, b)
        assertTrue(a.hashCode() != b.hashCode())
    }

    @Test
    fun testBooleans() {
        val array = JSONArray()
        array.add(true)
        array.add(false)
        array[2] = false
        array[3] = false
        array[2] = true
        assertEquals("[true,false,true,false]", array.toString())
        assertEquals(4, array.size().toLong())
        assertEquals(true, array[0])
        assertEquals(false, array[1])
        assertEquals(true, array[2])
        assertEquals(false, array[3])
        assertFalse(array.isNull(0))
        assertFalse(array.isNull(1))
        assertFalse(array.isNull(2))
        assertFalse(array.isNull(3))
        //todo: assertTrue(array.optBoolean(0))
        //todo: assertFalse(array.optBoolean(1, true))
        //todo: assertTrue(array.optBoolean(2, false))
        //todo: assertFalse(array.optBoolean(3))
        //todo: assertEquals("true", array.getString(0))
        //todo: assertEquals("false", array.getString(1))
        //todo: assertEquals("true", array.optString(2))
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
        other.add("truE")
        other.add("FALSE")
        assertNotEquals(array, other)
        assertNotEquals(other, array)
        assertTrue(other.getBoolean(0))
        //todo: assertFalse(other.optBoolean(1, true))
        //todo: assertTrue(other.optBoolean(2))
        assertFalse(other.getBoolean(3))
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

        //todo: assertEquals(0, array.optLong(0))
    }


    // http://code.google.com/p/android/issues/detail?id=16411
    @Test
    fun testCoerceStringToBoolean() {
        val array = JSONArray()
        array.add("maybe")
        try {
            array.getBoolean(0)
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        //todo: assertFalse(array.optBoolean(0))
        //todo: assertTrue(array.optBoolean(0, true))
    }

    @Test
    fun testNulls() {
        val array = JSONArray()
        array.setNull(3)
        assertEquals(4, array.size().toLong())
        assertEquals("[null,null,null,null]", array.toString())

        assertNull(array.opt(0))
        assertNull(array.opt(1))
        assertNull(array.opt(2))
        assertNull(array.opt(3))
        assertTrue(array.isNull(0))
        assertTrue(array.isNull(1))
        assertTrue(array.isNull(2))
        assertTrue(array.isNull(3))
        assertEquals(null, array.optString(0))
        assertEquals(null, array.optObject(1))
        assertEquals(null, array.optArray(2))
        assertEquals(null, array.optString(3))
    }

    /**
     * Our behaviour is questioned by this bug:
     * http://code.google.com/p/android/issues/detail?id=7257
     */
    @Test
    fun testParseNullYieldsJSONObjectNull() {
        val array = JSONArray("[\"null\",null]")
        array.addNull()
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
        array.add(Double.MIN_VALUE)
        array.add(9223372036854775806L)
        array.add(Double.MAX_VALUE)
        array.add(-0.0)
        val objElement = JSONObject()
        array.add(objElement)
        val arrElement = JSONArray()
        array.add(arrElement)
        array.add(Integer.MIN_VALUE)
        assertEquals(7, array.size().toLong())

        // toString() and getString(int) return different values for -0d
        assertEquals("[4.9E-324,9223372036854775806,1.7976931348623157E308,-0,{},[],-2147483648]", array.toString())

        assertEquals(Double.MIN_VALUE, array[0])
        assertEquals(9223372036854775806L, array[1])
        assertEquals(Double.MAX_VALUE, array[2])
        assertEquals(-0.0, array[3])
        //todo: assertEquals(java.lang.Double.MIN_VALUE, array.getDouble(0), 0.0)
        //todo: assertEquals(9.223372036854776E18, array.getDouble(1), 0.0)
        //todo: assertEquals(java.lang.Double.MAX_VALUE, array.getDouble(2), 0.0)
        //todo: assertEquals(-0.0, array.getDouble(3), 0.0)
        assertEquals(9223372036854775806L, array.getLong(1))
        assertEquals(Double.MIN_VALUE, array.opt(0))
        //todo: assertEquals(java.lang.Double.MIN_VALUE, array.optDouble(0), 0.0)
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
        val array = JSONArray()
        array.add("true")
        array.add("5.5")
        array.add("9223372036854775806")
        array.add("null")
        array.add("5\"8' tall")
        assertEquals(5, array.size().toLong())
        assertEquals("[\"true\",\"5.5\",\"9223372036854775806\",\"null\",\"5\\\"8' tall\"]", array.toString())

        // although the documentation doesn't mention it, join() escapes text and wraps
        // strings in quotes
        assertEquals("\"true\" \"5.5\" \"9223372036854775806\" \"null\" \"5\\\"8' tall\"", array.join(" "))

        assertEquals("true", array[0])
        assertEquals("null", array.getString(3))
        assertEquals("5\"8' tall", array.getString(4))
        assertEquals("true", array.opt(0))
        assertEquals("5.5", array.optString(1))
        assertFalse(array.isNull(0))
        assertFalse(array.isNull(3))

        assertTrue(array.getBoolean(0))
        //todo: assertTrue(array.optBoolean(0))
        //todo: assertTrue(array.optBoolean(0, false))
        //todo: assertEquals(0, array.optLong(0))
        //todo: assertEquals(-2, array.optLong(0))

        //todo: assertEquals(5.5, array.getDouble(1), 0.0)

        assertEquals(9223372036854775806L, array.getLong(2))
        //todo: assertEquals(9.223372036854776E18, array.getDouble(2), 0.0)
        //todo: assertEquals(Integer.MAX_VALUE.toLong(), array.getLong(2))

        assertFalse(array.isNull(3))
        try {
            array.getDouble(3)
            fail()
        } catch (e: IllegalArgumentException) {
            // expected
        }

        //todo: assertEquals(java.lang.Double.NaN, array.optDouble(3), 0.0)
        //todo: assertEquals(-1.0, array.optDouble(3, -1.0), 0.0)
    }

    @Test
    fun testJoin() {
        val array = JSONArray()
        array.addNull()
        assertEquals("null", array.join(" & "))
        array.add("\"")
        assertEquals("null & \"\\\"\"", array.join(" & "))
        array.add(5)
        assertEquals("null & \"\\\"\" & 5", array.join(" & "))
        array.add(true)
        assertEquals("null & \"\\\"\" & 5 & true", array.join(" & "))
    }

    @Test
    fun testJoinWithSpecialCharacters() {
        val array = JSONArray("[5, 6]")
        assertEquals("5\"6", array.join("\""))
    }

    @Test
    fun testPutUnsupportedNumbers() {
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
                        .setNull("null")
                        .set("o", JSONObject()
                                .set("b", "B String")
                                .setNull("null")
                                .set("bool", false))
                        .toString()
                + ", " + JSONObject()
                .set("a", "A String")
                .set("n", 666)
                .setNull("null")
                .toString()
                + "]")
        assertEquals("[{\"a\":\"A String\",\"n\":666,\"null\":null,\"o\":{\"b\":\"B String\",\"null\":null,\"bool\":false}},{\"a\":\"A String\",\"n\":666,\"null\":null}]", array.toString())
    }

    @Test
    fun testTokenerConstructor() {
        val `object` = JSONArray(JSONTokener("[false]"))
        assertEquals(1, `object`.size().toLong())
        assertEquals(false, `object`[0])
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
        val `object` = JSONArray("[false]")
        assertEquals(1, `object`.size().toLong())
        assertEquals(false, `object`[0])
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
    fun test_remove() {
        val a = JSONArray()
        assertNull(a.remove(-1))
        assertNull(a.remove(0))

        a.add("hello")
        assertNull(a.remove(-1))
        assertNull(a.remove(1))
        assertEquals("hello", a.remove(0))
        assertNull(a.remove(0))
    }

}
