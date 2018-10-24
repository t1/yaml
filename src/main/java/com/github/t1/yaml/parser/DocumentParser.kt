package com.github.t1.yaml.parser

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Directive
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER
import com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER
import com.github.t1.yaml.parser.Marker.INDENTED_COMMENT
import com.github.t1.yaml.parser.YamlSymbol.BOM
import com.github.t1.yaml.parser.YamlSymbol.COMMENT
import com.github.t1.yaml.parser.YamlSymbol.PERCENT
import com.github.t1.yaml.tools.NL
import com.github.t1.yaml.tools.SPACE
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Optional

class DocumentParser(reader: Reader) {

    private val next: YamlScanner = YamlScanner(reader)
    private var document: Document? = null

    constructor(yaml: String) : this(StringReader(yaml))

    constructor(inputStream: InputStream) : this(BufferedReader(InputStreamReader(inputStream, UTF_8)))

    fun document(): Optional<Document> {
        this.document = Document()

        next.accept(BOM)

        if (next.end())
            return Optional.empty()

        directives()
        prefixComments()
        node()
        documentEnd()

        return Optional.of(document!!)
    }

    private fun directives() {
        if (next.accept(PERCENT))
            document!!.directive(directive())

        if (next.accept(DIRECTIVES_END_MARKER)) {
            document!!.hasDirectivesEndMarker = true
            next.expect(NL)
        }
    }

    private fun directive(): Directive {
        return Directive(next.readWord(), next.readLine())
    }


    private fun prefixComments() {
        while (next.peek(INDENTED_COMMENT))
            document!!.prefixComment(comment())
    }

    private fun comment(): Comment {
        return Comment(indent = next.count(SPACE), text = next.expect(COMMENT).skip(SPACE).readLine())
    }


    private fun node() {
        val parser = NodeParser(next)
        if (next.more())
            document!!.node(parser.node())
    }

    private fun documentEnd() {
        if (next.accept(DOCUMENT_END_MARKER)) {
            document!!.hasDocumentEndMarker = true
            if (next.peek(SPACE))
                document!!.suffixComment = comment()
            else
                next.accept(NL)
        }
    }

    fun more(): Boolean {
        return next.anyMore()
    }

    override fun toString(): String {
        return next.toString()
    }
}
