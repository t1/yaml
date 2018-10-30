package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.YamlTokens.`b-break`
import com.github.t1.yaml.parser.YamlTokens.`c-byte-order-mark`
import com.github.t1.yaml.parser.YamlTokens.`c-directives-end`
import com.github.t1.yaml.parser.YamlTokens.`c-document-end`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Scanner
import java.io.Reader

internal class YamlScanner(reader: Reader) : Scanner(CodePointReader(reader)) {

    override fun read(): CodePoint {
        if (!isStartOfFile && peek(`c-byte-order-mark`))
            throw YamlParseException("A BOM must not appear inside a document")
        return super.read()
    }

    fun readWord(): String = readUntilAndSkip(WS)

    fun readLine(): String = readUntilAndSkip(`b-break`)

    override fun more(): Boolean {
        return (anyMore()
            && !peek(`c-document-end`)
            && !peek(`c-directives-end`)) // of next document
    }

    fun anyMore(): Boolean = super.more()
}
