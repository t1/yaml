package com.github.t1.yaml.dump

import com.github.t1.yaml.model.Alias
import com.github.t1.yaml.model.Collection.Style.BLOCK
import com.github.t1.yaml.model.Collection.Style.FLOW
import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Directive
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Mapping
import com.github.t1.yaml.model.Mapping.Entry
import com.github.t1.yaml.model.Node
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED
import com.github.t1.yaml.model.Scalar.Style.PLAIN
import com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED
import com.github.t1.yaml.model.Sequence
import com.github.t1.yaml.model.Sequence.Item
import com.github.t1.yaml.model.Stream
import com.github.t1.yaml.model.Visitor
import com.github.t1.yaml.tools.spaces

class Presenter {
    private val out = StringBuilder()
    private val visitor = object : Visitor {
        var skipNextIndent: Boolean = false
        var skipIndent: Boolean = false
        var indent = 0

        private fun indent(): String {
            if (skipIndent)
                return ""
            if (skipNextIndent) {
                skipNextIndent = false
                return ""
            }
            return spaces(indent * 2)
        }

        override fun visit(directive: Directive) {
            out.append("%").append(directive.name).append(" ").append(directive.parameters).append(NL)
        }

        override fun leaveDirectives(document: Document) {
            out.append("---").append(NL)
        }

        override fun visitPrefixComment(prefixComment: Comment) {
            append(prefixComment).append(NL)
        }

        override fun leaveBody(node: Node) {
            nl()
        }

        override fun enterDocumentEnd() {
            out.append("...")
        }

        override fun visitSuffixCommend(suffixComment: Comment) {
            append(suffixComment)
        }

        override fun leaveDocumentEnd() {
            nl()
        }


        override fun visit(alias: Alias) {
            out.append(alias)
        }


        override fun visit(scalar: Scalar) {
            if (scalar.tag != null)
                out.append(scalar.tag).append(' ')
            out.append(scalar.style.quote)
        }

        override fun enterScalarLine(node: Scalar, line: Line) {
            out.append(indent()).append(spaces(line.indent))
            out.append(when (node.style) {
                PLAIN -> line.text
                SINGLE_QUOTED -> line.text.replace("'", "''")
                DOUBLE_QUOTED -> line.text
            })
        }

        override fun leaveScalarLine(scalar: Scalar, line: Line) {
            out.append(scalar.style.quote)
            if (line.comment != null)
                append(line.comment!!)
            if (line != scalar.lastLine)
                nl()
        }


        override fun enterSequenceItem(sequence: Sequence, item: Item) {
            when (sequence.style) {
                FLOW -> {
                    skipIndent = true
                    out.append(if (item === sequence.firstItem) "[" else ", ")
                }
                BLOCK -> {
                    out.append(indent())
                    skipNextIndent = !item.nl
                    indent++
                    out.append("-").append(if (item.nl) "\n" else " ")
                }
            }
        }

        override fun leaveSequenceItem(sequence: Sequence, item: Item) {
            when (sequence.style) {
                FLOW -> {
                    if (item === sequence.lastItem)
                        out.append("]")
                    skipIndent = false
                }
                BLOCK -> {
                    if (item !== sequence.lastItem)
                        nl()
                    indent--
                }
            }
        }


        override fun enterMappingEntry(mapping: Mapping, entry: Entry) {
            when (mapping.style) {
                FLOW -> out.append(if (entry === mapping.firstEntry) "{" else ",")
                BLOCK -> {
                }
            }
        }

        override fun enterMappingKey(entry: Entry, key: Node) {
            out.append(if (entry.hasMarkedKey) "? " else "")
        }

        override fun enterMappingValue(entry: Entry, key: Node) {
            indent++
            skipNextIndent = !entry.hasNlAfterKey
            out.append(":").append(if (entry.hasNlAfterKey) NL else ' ')
        }

        override fun leaveMappingValue(entry: Entry, key: Node) {
            indent--
        }

        override fun leaveMappingEntry(mapping: Mapping, entry: Entry) {
            when (mapping.style) {
                FLOW -> if (entry === mapping.lastEntry) out.append("}")
                BLOCK -> if (entry !== mapping.lastEntry) nl()
            }
        }

        private fun append(comment: Comment): StringBuilder {
            return out.append(spaces(comment.indent)).append("# ").append(comment.text)
        }

        private fun nl() {
            out.append(NL)
        }
    }

    fun present(stream: Stream): String {
        stream.guide(visitor)
        return out.toString()
    }

    companion object {
        private const val NL = '\n'
    }
}
