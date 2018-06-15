package midgard.json

import junit.framework.TestCase

/**
 * This black box test was written without inspecting the non-free org.json sourcecode.
 */
class JSONTokenerTest : TestCase() {

    fun testEmptyString() {
        val backTokener = JSONTokener("")
        TestCase.assertEquals("at character 0 of ", backTokener.toString())
        try {
            JSONTokener("").nextString('"')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONTokener("").nextValue()
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }


        TestCase.assertEquals("at character 0 of ", JSONTokener("").toString())
    }

    fun testNextString() {
        TestCase.assertEquals("", JSONTokener("'").nextString('\''))
        TestCase.assertEquals("", JSONTokener("\"").nextString('\"'))
        TestCase.assertEquals("ABC", JSONTokener("ABC'DEF").nextString('\''))
        TestCase.assertEquals("ABC", JSONTokener("ABC'''DEF").nextString('\''))

        // nextString permits slash-escaping of arbitrary characters!
        TestCase.assertEquals("ABC", JSONTokener("A\\B\\C'DEF").nextString('\''))
    }

    fun testNextStringEscapedQuote() {
        try {
            JSONTokener("abc\\").nextString('"')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        // we're mixing Java escaping like \" and JavaScript escaping like \\\"
        // which makes these tests extra tricky to read!
        TestCase.assertEquals("abc\"def", JSONTokener("abc\\\"def\"ghi").nextString('"'))
        TestCase.assertEquals("abc\\def", JSONTokener("abc\\\\def\"ghi").nextString('"'))
        TestCase.assertEquals("abc/def", JSONTokener("abc\\/def\"ghi").nextString('"'))
        TestCase.assertEquals("abc\bdef", JSONTokener("abc\\bdef\"ghi").nextString('"'))
        //todo: TestCase.assertEquals("abc\fdef", JSONTokener("abc\\fdef\"ghi").nextString('"'))
        TestCase.assertEquals("abc\ndef", JSONTokener("abc\\ndef\"ghi").nextString('"'))
        TestCase.assertEquals("abc\rdef", JSONTokener("abc\\rdef\"ghi").nextString('"'))
        TestCase.assertEquals("abc\tdef", JSONTokener("abc\\tdef\"ghi").nextString('"'))
    }

    fun testNextStringUnicodeEscaped() {
        // we're mixing Java escaping like \\ and JavaScript escaping like \\u
        TestCase.assertEquals("abc def", JSONTokener("abc\\u0020def\"ghi").nextString('"'))
        TestCase.assertEquals("abcU0020def", JSONTokener("abc\\U0020def\"ghi").nextString('"'))

        // JSON requires 4 hex characters after a unicode escape
        try {
            JSONTokener("abc\\u002\"").nextString('"')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONTokener("abc\\u").nextString('"')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONTokener("abc\\u    \"").nextString('"')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        TestCase.assertEquals("abc\"def", JSONTokener("abc\\u0022def\"ghi").nextString('"'))
        try {
            JSONTokener("abc\\u000G\"").nextString('"')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    fun testNextStringNonQuote() {
        TestCase.assertEquals("AB", JSONTokener("ABC").nextString('C'))
        TestCase.assertEquals("ABCD", JSONTokener("AB\\CDC").nextString('C'))
        TestCase.assertEquals("AB\nC", JSONTokener("AB\\nCn").nextString('n'))
    }

    fun testBomIgnoredAsFirstCharacterOfDocument() {
        val tokener = JSONTokener("\ufeff[]")
        val array = tokener.nextValue() as JSONArray?
        TestCase.assertEquals(0, array!!.size())
    }

    fun testBomTreatedAsCharacterInRestOfDocument() {
        val tokener = JSONTokener("[\ufeff]")
        val array = tokener.nextValue() as JSONArray?
        TestCase.assertEquals(1, array!!.size())
    }

}
