package midgard.json

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * This black box test was written without inspecting the non-free org.json sourcecode.
 */
class JSONTokenerTest {

    @Test
    fun testEmptyString() {
        val backTokener = JSONTokener("")
        assertEquals("at character 0 of ", backTokener.toString())
        try {
            JSONTokener("").nextString('"')
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONTokener("").nextValue()
            fail()
        } catch (ignored: IllegalArgumentException) {
        }


        assertEquals("at character 0 of ", JSONTokener("").toString())
    }

    @Test
    fun testNextString() {
        assertEquals("", JSONTokener("'").nextString('\''))
        assertEquals("", JSONTokener("\"").nextString('\"'))
        assertEquals("ABC", JSONTokener("ABC'DEF").nextString('\''))
        assertEquals("ABC", JSONTokener("ABC'''DEF").nextString('\''))

        // nextString permits slash-escaping of arbitrary characters!
        assertEquals("ABC", JSONTokener("A\\B\\C'DEF").nextString('\''))
    }

    @Test
    fun testNextStringEscapedQuote() {
        try {
            JSONTokener("abc\\").nextString('"')
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        // we're mixing Java escaping like \" and JavaScript escaping like \\\"
        // which makes these tests extra tricky to read!
        assertEquals("abc\"def", JSONTokener("abc\\\"def\"ghi").nextString('"'))
        assertEquals("abc\\def", JSONTokener("abc\\\\def\"ghi").nextString('"'))
        assertEquals("abc/def", JSONTokener("abc\\/def\"ghi").nextString('"'))
        assertEquals("abc\bdef", JSONTokener("abc\\bdef\"ghi").nextString('"'))
        //todo: TestCase.assertEquals("abc\fdef", JSONTokener("abc\\fdef\"ghi").nextString('"'))
        assertEquals("abc\ndef", JSONTokener("abc\\ndef\"ghi").nextString('"'))
        assertEquals("abc\rdef", JSONTokener("abc\\rdef\"ghi").nextString('"'))
        assertEquals("abc\tdef", JSONTokener("abc\\tdef\"ghi").nextString('"'))
    }

    @Test
    fun testNextStringUnicodeEscaped() {
        // we're mixing Java escaping like \\ and JavaScript escaping like \\u
        assertEquals("abc def", JSONTokener("abc\\u0020def\"ghi").nextString('"'))
        assertEquals("abcU0020def", JSONTokener("abc\\U0020def\"ghi").nextString('"'))

        // JSON requires 4 hex characters after a unicode escape
        try {
            JSONTokener("abc\\u002\"").nextString('"')
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONTokener("abc\\u").nextString('"')
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONTokener("abc\\u    \"").nextString('"')
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

        assertEquals("abc\"def", JSONTokener("abc\\u0022def\"ghi").nextString('"'))
        try {
            JSONTokener("abc\\u000G\"").nextString('"')
            fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    @Test
    fun testNextStringNonQuote() {
        assertEquals("AB", JSONTokener("ABC").nextString('C'))
        assertEquals("ABCD", JSONTokener("AB\\CDC").nextString('C'))
        assertEquals("AB\nC", JSONTokener("AB\\nCn").nextString('n'))
    }

    @Test
    fun testBomIgnoredAsFirstCharacterOfDocument() {
        val tokener = JSONTokener("\ufeff[]")
        val array = tokener.nextValue() as JSONArray?
        assertEquals(0, array!!.size())
    }

    @Test
    fun testBomTreatedAsCharacterInRestOfDocument() {
        val tokener = JSONTokener("[\ufeff]")
        val array = tokener.nextValue() as JSONArray?
        assertEquals(1, array!!.size())
    }
}
