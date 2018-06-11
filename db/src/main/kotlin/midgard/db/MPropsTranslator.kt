package midgard.db

import com.github.mprops.MPropsParser
import java.io.File
import java.io.FileReader

class MPropsTranslator : Translator {

    val map: Map<String, String> = MPropsParser().parse(FileReader(File("$DATA_DIR/tr.mproperties")))

    override fun tr(messageId: String) = map[messageId] ?: "???"

}
