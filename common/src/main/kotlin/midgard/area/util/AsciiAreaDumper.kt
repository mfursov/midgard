package midgard.area.util

import midgard.area.model.Area

/** Dumps area into plain text ASCII format.*/
class AsciiAreaDumper {

    fun dumpArea(area: Area): String {
        if (area.places.isEmpty()) {
            return "[empty]"
        }
        val result = StringBuilder()
        var p = area.places[0]
        while (true) {
            result.append(p.id)
            if (p.exits.isEmpty()) {
                break;
            }
            if (p.exits.size > 1) {
                TODO("Implement complete graph traversal")
            }
            val exitEntry = p.exits.entries.iterator().next()
            result.append(">>").append(exitEntry.key.name).append(">>")
            p = exitEntry.value.to
        }
        return result.toString()
    }

}