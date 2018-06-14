package midgard.json

import junit.framework.AssertionFailedError
import junit.framework.TestCase

/**
 * This black box test was written without inspecting the non-free org.json sourcecode.
 */
class JSONTokenerTest : TestCase() {

    fun testEmptyString() {
        val backTokener = JSONTokener("")
        TestCase.assertEquals(" at character 0 of ", backTokener.toString())
        TestCase.assertEquals('\u0000', JSONTokener("").next())
        try {
            JSONTokener("").next(3)
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        try {
            JSONTokener("").next('A')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

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


        TestCase.assertEquals(" at character 0 of ", JSONTokener("").toString())
    }

    fun testCharacterNavigation() {
        val abcdeTokener = JSONTokener("ABCDE")
        TestCase.assertEquals('A', abcdeTokener.next())
        TestCase.assertEquals('B', abcdeTokener.next('B'))
        TestCase.assertEquals("CD", abcdeTokener.next(2))
        try {
            abcdeTokener.next(2)
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        TestCase.assertEquals('E', abcdeTokener.next())
    }

    fun testBackNextAndMore() {
        val abcTokener = JSONTokener("ABC")
        abcTokener.next()
        abcTokener.next()
        abcTokener.next()
        abcTokener.next()
        TestCase.assertEquals('\u0000', abcTokener.next())
    }

    fun testNextMatching() {
        val abcdTokener = JSONTokener("ABCD")
        TestCase.assertEquals('A', abcdTokener.next('A'))
        try {
            abcdTokener.next('C') // although it failed, this op consumes a character of input
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        TestCase.assertEquals('C', abcdTokener.next('C'))
        TestCase.assertEquals('D', abcdTokener.next('D'))
        try {
            abcdTokener.next('E')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    fun testNextN() {
        val abcdeTokener = JSONTokener("ABCDEF")
        TestCase.assertEquals("", abcdeTokener.next(0))
        try {
            abcdeTokener.next(7)
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        TestCase.assertEquals("ABC", abcdeTokener.next(3))
        try {
            abcdeTokener.next(4)
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

    }

    fun testNextNWithAllRemaining() {
        val tokener = JSONTokener("ABCDEF")
        tokener.next(3)
        try {
            tokener.next(3)
        } catch (e: IllegalArgumentException) {
            val error = AssertionFailedError("off-by-one error?")
            error.initCause(e)
            throw error
        }

    }

    fun testNext0() {
        val tokener = JSONTokener("ABCDEF")
        tokener.next(5)
        tokener.next()
        try {
            tokener.next(0)
        } catch (e: IllegalArgumentException) {
            val error = AssertionFailedError("Returning an empty string should be valid")
            error.initCause(e)
            throw error
        }

    }

    fun testNextString() {
        TestCase.assertEquals("", JSONTokener("'").nextString('\''))
        TestCase.assertEquals("", JSONTokener("\"").nextString('\"'))
        TestCase.assertEquals("ABC", JSONTokener("ABC'DEF").nextString('\''))
        TestCase.assertEquals("ABC", JSONTokener("ABC'''DEF").nextString('\''))

        // nextString permits slash-escaping of arbitrary characters!
        TestCase.assertEquals("ABC", JSONTokener("A\\B\\C'DEF").nextString('\''))

        val tokener = JSONTokener(" 'abc' 'def' \"ghi\"")
        tokener.next()
        TestCase.assertEquals('\'', tokener.next())
        TestCase.assertEquals("abc", tokener.nextString('\''))
        tokener.next()
        TestCase.assertEquals('\'', tokener.next())
        TestCase.assertEquals("def", tokener.nextString('\''))
        tokener.next()
        TestCase.assertEquals('"', tokener.next())
        TestCase.assertEquals("ghi", tokener.nextString('\"'))
    }

    fun testNextStringNoDelimiter() {
        try {
            JSONTokener("").nextString('\'')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

        val tokener = JSONTokener(" 'abc")
        tokener.next()
        tokener.next()
        try {
            tokener.next('\'')
            TestCase.fail()
        } catch (ignored: IllegalArgumentException) {
        }

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
