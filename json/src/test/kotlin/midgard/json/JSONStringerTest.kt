package midgard.json

import org.junit.Assert.*
import org.junit.Test

class JSONStringerTest {

    @Test
    fun testEmptyStringer() {
        // why isn't this the empty string?
        //todo: assertNull(JSONStringer().toString())
    }

    @Test
    fun testValueJSONNull() {
        val stringer = JSONStringer()
        stringer.array()
        stringer.value(null)
        stringer.endArray()
        assertEquals("[null]", stringer.toString())
    }

    @Test
    fun testEmptyObject() {
        val stringer = JSONStringer()
        stringer.startObject()
        stringer.endObject()
        assertEquals("{}", stringer.toString())
    }

    @Test
    fun testEmptyArray() {
        val stringer = JSONStringer()
        stringer.array()
        stringer.endArray()
        assertEquals("[]", stringer.toString())
    }

    @Test
    fun testArray() {
        val stringer = JSONStringer()
        stringer.array()
        stringer.value(false)
        stringer.value(5.0)
        stringer.value(5L)
        stringer.value("five")
        stringer.value(null)
        stringer.endArray()
        assertEquals("[false,5,5,\"five\",null]", stringer.toString())
    }

    @Test
    fun testValueObjectMethods() {
        val stringer = JSONStringer()
        stringer.array()
        stringer.value(java.lang.Boolean.FALSE)
        stringer.value(java.lang.Double.valueOf(5.0))
        stringer.value(java.lang.Long.valueOf(5L))
        stringer.endArray()
        assertEquals("[false,5,5]", stringer.toString())
    }

    @Test
    fun testKeyValue() {
        val stringer = JSONStringer()
        stringer.startObject()
        stringer.key("a").value(false)
        stringer.key("b").value(5.0)
        stringer.key("c").value(5L)
        stringer.key("d").value("five")
        stringer.key("e").value(null)
        stringer.endObject()
        assertEquals("{\"a\":false," +
                "\"b\":5," +
                "\"c\":5," +
                "\"d\":\"five\"," +
                "\"e\":null}", stringer.toString())
    }

    @Test
    fun testCustomNameStringer() {
        val stringer = object : JSONStringer() {
            override fun createKey(name: String): JSONStringer {
                out.append(name)
                return this
            }
        }
        stringer.startObject()
        stringer.key("a").value(false)
        stringer.key("b").value(5.0)
        stringer.key("c").value(5L)
        stringer.key("d").value("five")
        stringer.key("e").value(null)
        stringer.endObject()
        assertEquals("{a:false," +
                "b:5," +
                "c:5," +
                "d:\"five\"," +
                "e:null}", stringer.toString())
    }

    /**
     * Test what happens when extreme values are emitted. Such values are likely
     * to be rounded during parsing.
     */
    @Test
    fun testNumericRepresentations() {
        val stringer = JSONStringer()
        stringer.array()
        stringer.value(java.lang.Long.MAX_VALUE)
        stringer.value(java.lang.Double.MIN_VALUE)
        stringer.endArray()
        assertEquals("[9223372036854775807,4.9E-324]", stringer.toString())
    }

    @Test
    fun testWeirdNumbers() {
        try {
            JSONStringer().array().value(java.lang.Double.NaN)
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().array().value(java.lang.Double.NEGATIVE_INFINITY)
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().array().value(java.lang.Double.POSITIVE_INFINITY)
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        val stringer = JSONStringer()
        stringer.array()
        stringer.value(-0.0)
        stringer.value(0.0)
        stringer.endArray()
        assertEquals("[-0,0]", stringer.toString())
    }

    @Test
    fun testMismatchedScopes() {
        try {
            JSONStringer().key("a")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().value("a")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().endObject()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().endArray()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().array().endObject()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().startObject().endArray()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().startObject().key("a").key("a")
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONStringer().startObject().value(false)
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testRepeatedKey() {
        val stringer = JSONStringer()
        stringer.startObject()
        stringer.key("a").value(true)
        stringer.key("a").value(false)
        stringer.endObject()
        // JSONStringer doesn't attempt to detect duplicates
        assertEquals("{\"a\":true,\"a\":false}", stringer.toString())
    }

    @Test
    fun testEmptyKey() {
        val stringer = JSONStringer()
        stringer.startObject()
        stringer.key("").value(false)
        stringer.endObject()
        assertEquals("{\"\":false}", stringer.toString()) // legit behaviour!
    }

    @Test
    fun testEscaping() {
        assertEscapedAllWays("a", "a")
        assertEscapedAllWays("a\\\"", "a\"")
        assertEscapedAllWays("\\\"", "\"")
        assertEscapedAllWays(":", ":")
        assertEscapedAllWays(",", ",")
        assertEscapedAllWays("\\b", "\b")
        //todo: assertEscapedAllWays("\\f", "\f")
        assertEscapedAllWays("\\n", "\n")
        assertEscapedAllWays("\\r", "\r")
        assertEscapedAllWays("\\t", "\t")
        assertEscapedAllWays(" ", " ")
        assertEscapedAllWays("\\\\", "\\")
        assertEscapedAllWays("{", "{")
        assertEscapedAllWays("}", "}")
        assertEscapedAllWays("[", "[")
        assertEscapedAllWays("]", "]")
        assertEscapedAllWays("\\u0000", "\u0000")
        assertEscapedAllWays("\\u0019", "\u0019")
        assertEscapedAllWays(" ", "\u0020")
        assertEscapedAllWays("<\\/foo>", "</foo>")
    }

    private fun assertEscapedAllWays(escaped: String, original: String) {
        assertEquals("{\"$escaped\":false}",
                JSONStringer().startObject().key(original).value(false).endObject().toString())
        assertEquals("{\"a\":\"$escaped\"}",
                JSONStringer().startObject().key("a").value(original).endObject().toString())
        assertEquals("[\"$escaped\"]",
                JSONStringer().array().value(original).endArray().toString())
    }

    @Test
    fun testJSONArrayAsValue() {
        val array = JSONArray()
        array.add(false)
        val stringer = JSONStringer()
        stringer.array()
        stringer.value(array)
        stringer.endArray()
        assertEquals("[[false]]", stringer.toString())
    }

    @Test
    fun testJSONObjectAsValue() {
        val `object` = JSONObject()
        `object`["a"] = false
        val stringer = JSONStringer()
        stringer.startObject()
        stringer.key("b").value(`object`)
        stringer.endObject()
        assertEquals("{\"b\":{\"a\":false}}", stringer.toString())
    }

    @Test
    fun testArrayNestingMaxDepthSupports20() {
        var stringer = JSONStringer()
        for (i in 0..19) {
            stringer.array()
        }
        for (i in 0..19) {
            stringer.endArray()
        }
        assertEquals("[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]", stringer.toString())

        stringer = JSONStringer()
        for (i in 0..19) {
            stringer.array()
        }
    }

    @Test
    fun testObjectNestingMaxDepthSupports20() {
        var stringer = JSONStringer()
        for (i in 0..19) {
            stringer.startObject()
            stringer.key("a")
        }
        stringer.value(false)
        for (i in 0..19) {
            stringer.endObject()
        }
        assertEquals("{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":" +
                "{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":false" +
                "}}}}}}}}}}}}}}}}}}}}", stringer.toString())

        stringer = JSONStringer()
        for (i in 0..19) {
            stringer.startObject()
            stringer.key("a")
        }
    }

    @Test
    fun testMixedMaxDepthSupports20() {
        var stringer = JSONStringer()
        run {
            var i = 0
            while (i < 20) {
                stringer.array()
                stringer.startObject()
                stringer.key("a")
                i += 2
            }
        }
        stringer.value(false)
        run {
            var i = 0
            while (i < 20) {
                stringer.endObject()
                stringer.endArray()
                i += 2
            }
        }
        assertEquals("[{\"a\":[{\"a\":[{\"a\":[{\"a\":[{\"a\":" +
                "[{\"a\":[{\"a\":[{\"a\":[{\"a\":[{\"a\":false" +
                "}]}]}]}]}]}]}]}]}]}]", stringer.toString())

        stringer = JSONStringer()
        var i = 0
        while (i < 20) {
            stringer.array()
            stringer.startObject()
            stringer.key("a")
            i += 2
        }
    }

    @Test
    fun testMaxDepthWithArrayValue() {
        val array = JSONArray()
        array.add(false)

        val stringer = JSONStringer()
        for (i in 0..19) {
            stringer.array()
        }
        stringer.value(array)
        for (i in 0..19) {
            stringer.endArray()
        }
        assertEquals("[[[[[[[[[[[[[[[[[[[[[false]]]]]]]]]]]]]]]]]]]]]", stringer.toString())
    }

    @Test
    fun testMaxDepthWithObjectValue() {
        val `object` = JSONObject()
        `object`["a"] = false
        val stringer = JSONStringer()
        for (i in 0..19) {
            stringer.startObject()
            stringer.key("b")
        }
        stringer.value(`object`)
        for (i in 0..19) {
            stringer.endObject()
        }
        assertEquals("{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":" +
                "{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":{\"b\":" +
                "{\"a\":false}}}}}}}}}}}}}}}}}}}}}", stringer.toString())
    }

    @Test
    fun testMultipleRoots() {
        val stringer = JSONStringer()
        stringer.array()
        stringer.endArray()
        try {
            stringer.startObject()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testIndent() {
        val o = JSONObject().set("b", JSONObject().set("a", false))
        val result = "{\"b\":{\"a\":false}}"
        assertEquals(result, o.toString(JSONStringer(-1)))
        assertEquals(result, o.toString(JSONStringer()))
        assertEquals("{\n   \"b\": {\n      \"a\": false\n   }\n}", o.toString(JSONStringer(3)))
    }

}
