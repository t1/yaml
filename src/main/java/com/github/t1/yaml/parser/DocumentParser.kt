package com.github.t1.yaml.parser

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Directive
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.parser.YamlTokens.`b-break`
import com.github.t1.yaml.parser.YamlTokens.`c-byte-order-mark`
import com.github.t1.yaml.parser.YamlTokens.`c-comment`
import com.github.t1.yaml.parser.YamlTokens.`c-directive`
import com.github.t1.yaml.parser.YamlTokens.`c-directives-end`
import com.github.t1.yaml.parser.YamlTokens.`c-document-end`
import com.github.t1.yaml.parser.YamlTokens.`s-space`
import java.io.Reader

class DocumentParser(reader: Reader) {

    private val next: YamlScanner = YamlScanner(reader)
    private var document: Document? = null

    fun document(): Document? {
        this.document = Document()

        next.accept(`c-byte-order-mark`)

        if (next.end()) return null

        directives()
        prefixComments()
        node()
        documentEnd()

        return document
    }

    private fun directives() {
        if (next.accept(`c-directive`))
            document!!.directive(directive())

        if (next.accept(`c-directives-end`)) {
            document!!.hasDirectivesEndMarker = true
            next.expect(`b-break`)
        }
    }

    private fun directive(): Directive =
        Directive(next.readWord(), next.readLine())


    private fun prefixComments() {
        while (next.peek(INDENTED_COMMENT))
            document!!.prefixComment(comment())
    }

    private fun comment(): Comment =
        Comment(indent = next.count(`s-space`), text = next.run {
            expect(`c-comment`)
            skip(`s-space`)
            return@run readLine()
        })


    private fun node() {
        if (next.more())
            document!!.node(NodeParser(next).node())
    }

    private fun documentEnd() {
        if (next.accept(`c-document-end`)) {
            document!!.hasDocumentEndMarker = true
            if (next.peek(`s-space`))
                document!!.suffixComment = comment()
            else
                next.accept(`b-break`)
        }
    }

    fun more(): Boolean = next.anyMore()

    override fun toString() = next.toString()
}
