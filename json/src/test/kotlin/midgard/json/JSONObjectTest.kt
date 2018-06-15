package midgard.json

import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class JSONObjectTest {

    @Test
    fun testKeyset() {
        var x = JSONObject("{'a':1, 'b':2, 'c':3}")
        val k = TreeSet<String>()
        k.addAll(Arrays.asList("a", "b", "c"))
        assertEquals(x.keySet(), k)
        x = JSONObject("{}")
        assertEquals(x.keySet().size.toLong(), 0)
    }

    @Test
    fun testEmptyObject() {
        val o = JSONObject()
        assertTrue(o.isEmpty())

        assertEquals("{}", o.toString())
        assertEquals("{}", o.toString(5))
        try {
            o["foo"]
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o.getBoolean("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o.getDouble("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o.getArray("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o.getObject("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o.getLong("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o.getString("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        assertFalse(o.has("foo"))
        assertFalse(o.isNull("foo"))
        assertNull(o.opt("foo"))
        assertNull(o.optBoolean("foo"))
        //todo: assertEquals(Double.NaN, o.optDouble("foo"), 0.0)
        //todo: assertEquals(5.0, o.optDouble("foo", 5.0), 0.0)
        //todo: assertEquals(0, o.optLong("foo").toLong())
        //todo: assertEquals(5, o.optLong("foo", 5).toLong())
        assertEquals(null, o.optArray("foo"))
        assertEquals(null, o.optObject("foo"))
        //todo: assertEquals(0, o.optLong("foo"))
        //todo: assertEquals(Long.MAX_VALUE - 1, o.optLong("foo", Long.MAX_VALUE - 1))
        assertEquals(null, o.optString("foo"))
        assertNull(o.remove("foo"))
    }

    @Test
    fun testEqualsAndHashCode() {
        val a = JSONObject()
        val b = JSONObject()

        // JSON object doesn't override either equals or hashCode (!)
        assertFalse(a == b)
        assertEquals(a.hashCode().toLong(), System.identityHashCode(a).toLong())
    }

    @Test
    fun testSet() {
        val o = JSONObject()
        assertSame(o, o.set("foo", true))
        o["foo"] = false
        assertEquals(false, o["foo"])

        o["foo"] = 5.0
        assertEquals(5.0, o["foo"])
        o["foo"] = 0
        assertEquals(0L, o["foo"])
        o["bar"] = java.lang.Long.MAX_VALUE - 1
        assertEquals(java.lang.Long.MAX_VALUE - 1, o["bar"])
        o["baz"] = "x"
        assertEquals("x", o["baz"])
        o["bar"] = null
        assertNull(o["bar"])
    }

    @Test
    fun testPutNullDoesNotRemoves() {
        val o = JSONObject()
        o["foo"] = "bar"
        o["foo"] = null
        assertTrue(o.isNotEmpty())
        assertEquals(1, o.size())
        assertTrue(o.has("foo"))
        assertNull(o["foo"])
    }

    @Test
    fun testRemove() {
        val o = JSONObject()
        o["foo"] = "bar"
        assertEquals(null, o.remove(""))
        assertEquals(null, o.remove("bar"))
        assertEquals("bar", o.remove("foo"))
        assertEquals(null, o.remove("foo"))
    }

    @Test
    fun testBooleans() {
        val o = JSONObject()
        o["foo"] = true
        o["bar"] = false
        o["baz"] = "true"
        o["quux"] = "false"
        assertEquals(4, o.size().toLong())
        assertEquals(true, o.getBoolean("foo"))
        assertEquals(false, o.getBoolean("bar"))
        assertEquals(true, o.getBoolean("baz"))
        assertEquals(false, o.getBoolean("quux"))
        assertFalse(o.isNull("foo"))
        assertFalse(o.isNull("quux"))
        assertTrue(o.has("foo"))
        assertTrue(o.has("quux"))
        assertFalse(o.has("missing"))
        assertEquals(true, o.optBoolean("foo"))
        assertEquals(false, o.optBoolean("bar"))
        assertEquals(true, o.optBoolean("baz"))
        assertEquals(false, o.optBoolean("quux"))
        assertEquals(null, o.optBoolean("missing"))

        o["foo"] = "truE"
        o["bar"] = "FALSE"
        assertEquals(true, o.getBoolean("foo"))
        assertEquals(false, o.getBoolean("bar"))
        assertEquals(true, o.optBoolean("foo"))
        assertEquals(false, o.optBoolean("bar"))
    }

    @Test
    fun testCoerceStringToNumber() {
        val o = JSONObject()
        o["v1"] = "1"
        o["v2"] = "-1"
        assertEquals(1, o.getLong("v1"))
        assertEquals(-1, o.getLong("v2"))
    }

    @Test
    fun testCoerceStringToNumberWithFail() {
        val o = JSONObject()
        o["key"] = "not a number"
        try {
            o.getLong("key")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        //todo: assertEquals(0, o.optLong("key"))
    }

    // http://code.google.com/p/android/issues/detail?id=16411
    @Test
    fun testCoerceStringToBoolean() {
        val o = JSONObject()
        o["foo"] = "maybe"
        try {
            o.getBoolean("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        //todo: assertEquals(false, o.optBoolean("foo"))
    }

    @Test
    fun testNumbers() {
        val o = JSONObject()
        o["foo"] = Double.MIN_VALUE
        o["bar"] = 9223372036854775806L
        o["baz"] = Double.MAX_VALUE
        o["quux"] = -0.0
        assertEquals(4, o.size().toLong())

        val toString = o.toString()
        assertTrue(toString, toString.contains("\"foo\":4.9E-324"))
        assertTrue(toString, toString.contains("\"bar\":9223372036854775806"))
        assertTrue(toString, toString.contains("\"baz\":1.7976931348623157E308"))

        // toString() and getString() return different values for -0d!
        assertTrue(toString, toString.contains("\"quux\":-0}") // no trailing decimal point
                || toString.contains("\"quux\":-0,"))

        assertEquals(java.lang.Double.MIN_VALUE, o["foo"])
        assertEquals(9223372036854775806L, o["bar"])
        assertEquals(java.lang.Double.MAX_VALUE, o["baz"])
        assertEquals(-0.0, o["quux"])
        assertEquals(Double.MIN_VALUE, o.getDouble("foo"))
        assertEquals(Double.MAX_VALUE, o.getDouble("baz"))
        assertEquals(-0.0, o.getDouble("quux"))
        assertEquals(9223372036854775806L, o.getLong("bar"))
        assertEquals(java.lang.Double.MIN_VALUE, o.opt("foo"))
        assertEquals(9223372036854775806L, o.optLong("bar"))
        //todo: assertEquals(java.lang.Double.MAX_VALUE, o.optDouble("baz"), 0.0)
        assertEquals(java.lang.Double.MIN_VALUE, o.opt("foo"))
        assertEquals(9223372036854775806L, o.optLong("bar"))
        //todo: assertEquals(java.lang.Double.MAX_VALUE, o.optDouble("baz"), 0.0)
        //todo: assertEquals(java.lang.Double.MIN_VALUE, o.optDouble("foo", 5.0), 0.0)
        //todo: assertEquals(9223372036854775806L, o.optLong("bar", 1L))
        //todo: assertEquals(0, o.optLong("quux", -1).toLong())
    }

    @Test
    fun testFloats() {
        val o = JSONObject()
        try {
            o["foo"] = java.lang.Float.NaN.toDouble()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o["foo"] = java.lang.Float.NEGATIVE_INFINITY.toDouble()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o["foo"] = java.lang.Float.POSITIVE_INFINITY.toDouble()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testStrings() {
        val o = JSONObject()
        o["foo"] = "true"
        o["bar"] = "5.5"
        o["baz"] = "9223372036854775806"
        o["quux"] = "null"
        o["height"] = "5\"8' tall"

        assertTrue(o.toString().contains("\"foo\":\"true\""))
        assertTrue(o.toString().contains("\"bar\":\"5.5\""))
        assertTrue(o.toString().contains("\"baz\":\"9223372036854775806\""))
        assertTrue(o.toString().contains("\"quux\":\"null\""))
        assertTrue(o.toString().contains("\"height\":\"5\\\"8' tall\""))

        assertEquals("true", o["foo"])
        assertEquals("null", o.getString("quux"))
        assertEquals("5\"8' tall", o.getString("height"))
        assertEquals("true", o.opt("foo"))
        assertEquals("5.5", o.optString("bar"))
        assertEquals("true", o.optString("foo"))
        assertFalse(o.isNull("foo"))

        assertEquals(true, o.getBoolean("foo"))
        assertEquals(true, o.optBoolean("foo"))
        assertEquals(true, o.optBoolean("foo"))
        //todo: assertEquals(0, o.optLong("foo"))
        //todo: assertEquals(-2, o.optLong("foo", -2).toLong())

        assertEquals(5.5, o.getDouble("bar"))
        //todo: assertEquals(5, o.optLong("bar", 3).toLong())

        // The last digit of the string is a 6 but getLong returns a 7. It's probably parsing as a
        // double and then converting that to a long. This is consistent with JavaScript.
        assertEquals(9223372036854775806L, o.getLong("baz"))
        assertEquals(9.223372036854776E18, o.getDouble("baz"))

        assertFalse(o.isNull("quux"))
        try {
            o.getDouble("quux")
            fail()
        } catch (e: IllegalArgumentException) {
            // expected
        }

        //todo: assertEquals(java.lang.Double.NaN, o.optDouble("quux"), 0.0)
        //todo: assertEquals(-1.0, o.optDouble("quux", -1.0), 0.0)

        o["foo"] = "TRUE"
        assertEquals(true, o.getBoolean("foo"))
    }

    @Test
    fun testJSONObjects() {
        val o = JSONObject()

        val a = JSONArray()
        val b = JSONObject()
        o["foo"] = a
        o["bar"] = b

        assertSame(a, o.getArray("foo"))
        assertSame(b, o.getObject("bar"))
        try {
            o.getObject("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o.getArray("bar")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        assertEquals(a, o.optArray("foo"))
        assertEquals(b, o.optObject("bar"))
        //todo: assertEquals(null, `object`.optArray("bar"))
        //todo: assertEquals(null, o.optObject("foo"))
    }

    @Test
    fun testNullCoercionToString() {
        val o = JSONObject()
        o["foo"] = null
        assertNull(o.optString("foo"))
    }

    @Test
    fun testArrayCoercion() {
        val o = JSONObject()
        o["foo"] = "[true]"
        try {
            o.getArray("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testObjectCoercion() {
        val o = JSONObject()
        o["foo"] = "{}"
        try {
            o.getObject("foo")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testPutUnsupportedNumbers() {
        val o = JSONObject()
        try {
            o["foo"] = java.lang.Double.NaN
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o["foo"] = java.lang.Double.NEGATIVE_INFINITY
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o["foo"] = java.lang.Double.POSITIVE_INFINITY
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testPutUnsupportedNumbersAsObjects() {
        val o = JSONObject()
        try {
            o["foo"] = java.lang.Double.NaN
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o["foo"] = java.lang.Double.NEGATIVE_INFINITY
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            o["foo"] = java.lang.Double.POSITIVE_INFINITY
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testTokenerConstructor() {
        val o = JSONObject(JSONTokener("{\"foo\": false}"))
        assertEquals(1, o.size().toLong())
        assertEquals(false, o["foo"])
    }

    @Test
    fun testTokenerConstructorWrongType() {
        try {
            JSONObject(JSONTokener("[\"foo\", false]"))
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testTokenerConstructorNull() {
        try {
            JSONObject((null as JSONTokener?)!!)
            fail()
        } catch (ignored: NullPointerException) {
        }

    }

    @Test
    fun testTokenerConstructorParseFail() {
        try {
            JSONObject(JSONTokener("{"))
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testStringConstructor() {
        val o = JSONObject("{\"foo\": false}")
        assertEquals(1, o.size().toLong())
        assertEquals(false, o["foo"])
    }

    @Test
    fun testStringConstructorWrongType() {
        try {
            JSONObject("[\"foo\", false]")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testStringConstructorNull() {
        try {
            JSONObject((null as String?)!!)
            fail()
        } catch (ignored: NullPointerException) {
        }

    }

    @Test
    fun testStringConstructorParseFail() {
        try {
            JSONObject("{")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testEmptyStringKey() {
        val o = JSONObject()
        o[""] = 5
        assertEquals(5L, o[""])
        assertEquals("{\"\":5}", o.toString())
    }

    @Test
    fun testNullValue() {
        val o = JSONObject()
        o["foo"] = null

        // there are two ways to represent null; each behaves differently!
        assertTrue(o.has("foo"))
        assertTrue(o.isNull("foo"))

        assertNull(o.optString("foo"))
        assertNull(o.optLong("foo"))
        assertNull(o.optLong("foo"))
        assertNull(o.optBoolean("foo"))
        assertNull(o.optObject("foo"))
        assertNull(o.optArray("foo"))
        assertNull(o["foo"])
        assertNull(o.opt("foo"))
    }

    @Test
    fun testHas() {
        val o = JSONObject()
        o["foo"] = 5
        assertTrue(o.has("foo"))
        assertFalse(o.has("bar"))
    }

    @Test
    fun testToStringWithNulls() {
        val obj = JSONObject()
        obj["a"] = "A String"
        obj["n"] = 666
        obj["null"] = null

        obj["o"] = JSONObject().set("b", "B String").set("null", null).set("bool", false)

        assertEquals("{\"a\":\"A String\",\"n\":666,\"null\":null,\"o\":{\"b\":\"B String\",\"null\":null,\"bool\":false}}", obj.toString())
    }

    @Test
    fun testKeysEmptyObject() {
        val o = JSONObject()
        assertTrue(o.keySet().isEmpty())
    }

    @Test
    fun testKeys() {
        val o = JSONObject()
        o["foo"] = 5
        o["bar"] = 6
        o["foo"] = 7

        Assert.assertEquals(setOf("foo", "bar"), o.keySet())
    }

    // https://code.google.com/p/android/issues/detail?id=103641
    @Test
    fun testInvalidUnicodeEscape() {
        try {
            JSONObject("{\"q\":\"\\u\", \"r\":[]}")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }
}
