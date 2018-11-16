package com.github.t1.yaml.parser

import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Scanner
import com.github.t1.yaml.tools.whitespace
import java.io.Reader

internal class YamlScanner(reader: Reader) : Scanner(CodePointReader(reader)) {

    override fun read(): CodePoint {
        if (!isStartOfFile && peek(`c-byte-order-mark`))
            throw YamlParseException("A BOM must not appear inside a document")
        return super.read()
    }

    fun readWord(): String = readUntilAndSkip(whitespace)

    fun readLine(): String = readUntilAndSkip(`b-break`)

    override fun more(): Boolean {
        return (anyMore()
            && !peek(`c-document-end`)
            && !peek(`c-directives-end`)) // of next document
    }

    fun anyMore(): Boolean = super.more()
}
