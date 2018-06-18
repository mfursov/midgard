package midgard.json.performance

import midgard.json.JSONTokener

/**
 * Performance test for toString method. Use manual run from command line or IDE.
 */
object ToStringPT {

    @JvmStatic
    fun main(v: Array<String>) {
        val reader = ToStringPT::class.java.getResource("/sample-01.json").readText()
        val json = JSONTokener(reader).nextValue()
        val t0 = System.currentTimeMillis()
        for (i in 0 until 300_000) json.toString()
        println("Total time: " + (System.currentTimeMillis() - t0) / 1000L + " seconds")
    }
}
