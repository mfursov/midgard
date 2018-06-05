package midgard.area.generator.util

import midgard.area.generator.GenArea


@Suppress("unused")
/** Dumps area into plain text ASCII format.*/
class AsciiAreaDumper {

    fun dumpArea(area: GenArea): String {
        if (area.places.isEmpty()) {
            return "[empty]"
        }
        val result = StringBuilder()
        var p = area.places[0]
        while (true) {
            //result.append(p.id)
            if (p.exits.isEmpty()) {
                break
            }
            if (p.exits.size > 1) {
                TODO("Implement complete graph traversal")
            }
            val exitEntry = p.exits.entries.iterator().next()
            result.append(">>").append(exitEntry.key.name).append(">>")
            p = exitEntry.value
        }
        return result.toString()
    }

}