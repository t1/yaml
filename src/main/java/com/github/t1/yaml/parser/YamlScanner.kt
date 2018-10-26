package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.YamlTokens.`c-byte-order-mark`
import com.github.t1.yaml.parser.YamlTokens.`c-directives-end`
import com.github.t1.yaml.parser.YamlTokens.`c-document-end`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.Scanner
import java.io.Reader

internal class YamlScanner(reader: Reader) : Scanner(MAX_LOOK_AHEAD, reader) {

    override fun read(): CodePoint {
        if (!isStartOfFile && peek(`c-byte-order-mark`))
            throw YamlParseException("A BOM must not appear inside a document")
        return super.read()
    }

    override fun more(): Boolean {
        return (anyMore()
            && !peek(`c-document-end`)
            && !peek(`c-directives-end`)) // of next document
    }

    fun anyMore(): Boolean = super.more()

    companion object {
        /** As specified in http://www.yaml.org/spec/1.2/spec.html#id2790832  */
        private const val MAX_LOOK_AHEAD = 1024
    }
}
