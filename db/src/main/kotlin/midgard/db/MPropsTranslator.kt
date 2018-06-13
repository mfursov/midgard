package midgard.db

import com.github.mprops.MPropsParser
import java.io.File
import java.io.FileReader

class MPropsTranslator(val dataDir: String) : Translator {

    val map: Map<String, String> = MPropsParser().parse(FileReader(File("$dataDir/tr.mproperties")))

    override fun tr(messageId: String) = map[messageId] ?: "???"

}
