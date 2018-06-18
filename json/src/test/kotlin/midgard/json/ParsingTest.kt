package midgard.json

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.*

class ParsingTest {

    @Test
    fun testParsingNoObjects() {
        try {
            JSONTokener("").nextValue()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testParsingLiterals() {
        assertParsed(java.lang.Boolean.TRUE, "true")
        assertParsed(java.lang.Boolean.FALSE, "false")
        assertParsed(null, "null")
        assertParsed(null, "NULL")
        assertParsed(java.lang.Boolean.FALSE, "False")
        assertParsed(java.lang.Boolean.TRUE, "truE")
    }

    @Test
    fun testParsingQuotedStrings() {
        assertParsed("abc", "\"abc\"")
        assertParsed("123", "\"123\"")
        assertParsed("foo\nbar", "\"foo\\nbar\"")
        assertParsed("foo bar", "\"foo\\u0020bar\"")
        assertParsed("\"{}[]/\\:,=;#", "\"\\\"{}[]/\\\\:,=;#\"")
    }

    @Test
    fun testParsingSingleQuotedStrings() {
        assertParsed("abc", "'abc'")
        assertParsed("123", "'123'")
        assertParsed("foo\nbar", "'foo\\nbar'")
        assertParsed("foo bar", "'foo\\u0020bar'")
        assertParsed("\"{}[]/\\:,=;#", "'\\\"{}[]/\\\\:,=;#'")
    }

    @Test
    fun testParsingUnquotedStrings() {
        assertParsed("abc", "abc")
        assertParsed("123abc", "123abc")
        assertParsed("123e0x", "123e0x")
        assertParsed("123e", "123e")
        assertParsed("123ee21", "123ee21")
        assertParsed("0xFFFFFFFFFFFFFFFFF", "0xFFFFFFFFFFFFFFFFF")
    }

    /**
     * Unfortunately the original implementation attempts to figure out what
     * Java number type best suits an input value.
     */
    @Test
    fun testParsingNumbersThatAreBestRepresentedAsLongs() {
        assertParsed(9223372036854775807L, "9223372036854775807")
        assertParsed(9223372036854775806L, "9223372036854775806")
        assertParsed(-9223372036854775807L, "-9223372036854775807")
    }

    @Test
    fun testParsingNumbersThatAreBestRepresentedAsIntegers() {
        assertParsed(0L, "0")
        assertParsed(5L, "5")
        assertParsed(-2147483648L, "-2147483648")
        assertParsed(2147483647L, "2147483647")
    }

    @Test
    fun testParsingNegativeZero() {
        assertParsed(0L, "-0")
    }

    @Test
    fun testParsingIntegersWithAdditionalPrecisionYieldDoubles() {
        assertParsed(1.0, "1.00")
        assertParsed(1.0, "1.0")
        assertParsed(0.0, "0.0")
        assertParsed(-0.0, "-0.0")
    }

    @Test
    fun testParsingNumbersThatAreBestRepresentedAsDoubles() {
        assertParsed(9.223372036854776E18, "9223372036854775808")
        assertParsed(-9.223372036854776E18, "-9223372036854775809")
        assertParsed(1.7976931348623157E308, "1.7976931348623157e308")
        assertParsed(2.2250738585072014E-308, "2.2250738585072014E-308")
        assertParsed(4.9E-324, "4.9E-324")
        assertParsed(4.9E-324, "4.9e-324")
    }

    @Test
    fun testParsingOctalNumbers() {
        assertParsed(5L, "05")
        assertParsed(8L, "010")
        assertParsed(1046L, "02026")
    }

    @Test
    fun testParsingHexNumbers() {
        assertParsed(5L, "0x5")
        assertParsed(16L, "0x10")
        assertParsed(17L, "0X11")
        assertParsed(8230L, "0x2026")
        assertParsed(180150010L, "0xABCDEFA")
        assertParsed(2077093803L, "0x7BCDEFAB")
    }

    @Test
    fun testParsingLargeHexValues() {
        assertParsed(Integer.MAX_VALUE.toLong(), "0x7FFFFFFF")
        val message = "Hex values are parsed as Strings if their signed " + "value is greater than Integer.MAX_VALUE."
        assertParsed(message, 0x80000000L, "0x80000000")
    }

    @Test
    fun test64BitHexValues() {
        // note that this is different from the same test in the original Android
        // this is due to the fact that Long.parseLong doesn't correctly handle
        // the value -1 expressed as unsigned hex if you use the normal JDK. Presumably
        // the Android equivalent program does this better.
        assertParsed("Large hex longs shouldn't yield ints or strings",
                0xFFFFFFFFFFFFFFFL, "0xFFFFFFFFFFFFFFF")
    }

    @Test
    fun testParsingWithCommentsAndWhitespace() {
        assertParsed("baz", "  // foo bar \n baz")
        assertParsed("baz", "  // foo bar \r baz")
        assertParsed("baz", "  // foo bar \r\n baz")
        assertParsed("baz", "  # foo bar \n baz")
        assertParsed("baz", "  # foo bar \r baz")
        assertParsed("baz", "  # foo bar \r\n baz")
        assertParsed(5L, "  /* foo bar \n baz */ 5")
        assertParsed(5L, "  /* foo bar \n baz */ 5 // quux")
        assertParsed(5L, "  5   ")
        assertParsed(5L, "  5  \r\n\t ")
        assertParsed(5L, "\r\n\t   5 ")
    }

    @Test
    fun testParsingArrays() {
        assertParsed(JSONArray(), "[]")
        assertParsed(JSONArray().add(5).add(6).add(true), "[5,6,true]")
        assertParsed(JSONArray().add(5).add(6).add(JSONArray()), "[5,6,[]]")
        assertParsed(JSONArray().add(5).add(6).add(7).add(null), "[5,6,7,null]")
        assertParsed(JSONArray().add(null).add(null), "[null,null]")
    }

    @Test
    fun testParsingObjects() {
        assertParsed(JSONObject().set("foo", 5), "{\"foo\": 5}")
        assertParsed(JSONObject().set("foo", 5), "{foo: 5}")
        assertParsed(JSONObject().set("foo", 5).set("bar", "baz"), "{\"foo\": 5, \"bar\": \"baz\"}")
        assertParsed(JSONObject().set("foo", 5).set("bar", "baz"), "{\"foo\": 5; \"bar\": \"baz\"}")
        assertParsed(JSONObject().set("foo", JSONObject().set("foo", JSONArray().add(5).add(6))), "{\"foo\": {\"foo\": [5, 6]}}")
        assertParsed(JSONObject().set("foo", JSONObject().set("foo", JSONArray().add(5).add(6))), "{\"foo\":\n\t{\t \"foo\":[5,\r6]}}")
    }

    @Test
    fun testSyntaxProblemUnterminatedObject() {
        assertParseFail("{")
        assertParseFail("{\"foo\"")
        assertParseFail("{\"foo\":")
        assertParseFail("{\"foo\":bar")
        assertParseFail("{\"foo\":bar,")
        assertParseFail("{\"foo\":bar,\"baz\"")
        assertParseFail("{\"foo\":bar,\"baz\":")
        assertParseFail("{\"foo\":bar,\"baz\":true")
        assertParseFail("{\"foo\":bar,\"baz\":true,")
    }

    @Test
    fun testSyntaxProblemEmptyString() {
        assertParseFail("")
    }

    @Test
    fun testSyntaxProblemUnterminatedArray() {
        assertParseFail("[")
        assertParseFail("[,")
        assertParseFail("[,,")
        assertParseFail("[true")
        assertParseFail("[true,")
        assertParseFail("[true,,")
    }

    @Test
    fun testSyntaxProblemMalformedObject() {
        assertParseFail("{:}")
        assertParseFail("{\"key\":}")
        assertParseFail("{:true}")
        assertParseFail("{\"key\":true:}")
        assertParseFail("{null:true}")
        assertParseFail("{true:true}")
        assertParseFail("{0xFF:true}")
    }

    private fun assertParseFail(malformedJson: String) {
        try {
            JSONTokener(malformedJson).nextValue()
            fail("Successfully parsed: \"$malformedJson\"")
        } catch (ignored: IllegalArgumentException) {
        } catch (e: StackOverflowError) {
            fail("Stack overflowed on input: \"$malformedJson\"")
        }

    }

    private fun assertParsed(message: String, expectedArg: Any?, json: String) {
        var expected = expectedArg
        var actual = JSONTokener(json).nextValue()
        actual = canonical(actual)
        expected = canonical(expected)
        assertEquals("For input \"$json\" $message", expected, actual)
    }

    private fun assertParsed(expected: Any?, json: String) {
        assertParsed("", expected, json)
    }

    /**
     * Since they don't implement equals or hashCode properly, this recursively
     * replaces JSONObjects with an equivalent HashMap, and JSONArrays with the equivalent ArrayList.
     */
    private fun canonical(input: Any?): Any? {
        return when (input) {
            is JSONArray -> {
                val result = mutableListOf<Any?>()
                for (i in 0 until input.size()) {
                    result.add(canonical(input.opt(i)))
                }
                result
            }
            is JSONObject -> {
                val result = HashMap<String, Any?>()
                input.keySet().forEach { result[it] = canonical(input[it]) }
                result
            }
            else -> input
        }
    }
}
