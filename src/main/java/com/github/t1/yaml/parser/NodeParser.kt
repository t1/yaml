package com.github.t1.yaml.parser

import com.github.t1.yaml.model.Collection.Style.BLOCK
import com.github.t1.yaml.model.Collection.Style.FLOW
import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Mapping
import com.github.t1.yaml.model.Mapping.Entry
import com.github.t1.yaml.model.Node
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Sequence
import com.github.t1.yaml.model.Sequence.Item
import com.github.t1.yaml.parser.Symbol.COLON
import com.github.t1.yaml.parser.Symbol.COMMENT
import com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_END
import com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_ENTRY
import com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_ITEM_END
import com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_START
import com.github.t1.yaml.parser.Symbol.MINUS
import com.github.t1.yaml.parser.Symbol.NL
import com.github.t1.yaml.parser.Symbol.QUESTION_MARK
import com.github.t1.yaml.parser.Symbol.SPACE
import com.github.t1.yaml.parser.Symbol.WS

internal class NodeParser(private val next: YamlScanner) {
    private val nesting = Nesting(this.next)

    override fun toString(): String = "NodeParser $next nesting: $nesting"

    fun node(): Node {
        nesting.expect()
        if (next.isFlowSequence)
            return flowSequence()
        if (next.isBlockSequence)
            return blockSequence()
        if (next.isFlowMapping)
            return flowMapping()
        return if (next.isBlockMapping) blockMapping() else scalar()
    }

    private fun flowSequence(): Node {
        next.expect(FLOW_SEQUENCE_START)
        val sequence = Sequence(style = FLOW)
        do
            sequence.item(flowSequenceItem())
        while (next.more() && next.accept(FLOW_SEQUENCE_ENTRY))
        next.expect(FLOW_SEQUENCE_END)
        next.skip(WS)
        return sequence
    }

    private fun flowSequenceItem(): Item {
        next.skip(SPACE)
        val line = next.readUntil(FLOW_SEQUENCE_ITEM_END) // TODO this must be a call to node()!
        next.skip(SPACE)
        return Item(node = Scalar().line(line))
    }

    private fun blockSequence(): Sequence {
        val sequence = Sequence(style = BLOCK)
        do
            sequence.item(blockSequenceItem())
        while (next.more() && nesting.accept())
        return sequence
    }

    private fun blockSequenceItem(): Item {
        next.expect(MINUS)
        val nlItem = next.accept(NL)
        if (!nlItem) {
            next.expect(SPACE)
            nesting.skipNext(true)
        }
        nesting.up()
        val item = Item(nl = nlItem, node = node())
        nesting.down()
        return item
    }

    private fun blockMapping(): Mapping {
        val mapping = Mapping()
        do {
            mapping.entry(blockMappingEntry())
        } while (next.more() && nesting.accept())
        return mapping
    }

    private fun blockMappingEntry(): Entry {
        val entry = Entry()
        blockMappingKey(entry)
        blockMappingValue(entry)
        return entry
    }

    private fun blockMappingKey(entry: Entry) {
        entry.hasMarkedKey = next.accept(QUESTION_MARK)
        if (entry.hasMarkedKey)
            next.expect(SPACE)
        entry.key = scalar() // TODO key node()
    }

    private fun blockMappingValue(entry: Entry) {
        next.expect(COLON)
        entry.hasNlAfterKey = next.accept(NL)
        if (!entry.hasNlAfterKey) {
            next.expect(SPACE)
            nesting.skipNext(true)
        }

        nesting.up()
        val value = node()
        nesting.down()
        entry.value = value

        if (value is Scalar && next.accept(COMMENT))
        // TODO comments for non-scalars
            comment(value)
    }

    private fun flowMapping(): Mapping {
        TODO("flow mapping not yet implemented")
    }

    private fun scalar(): Scalar = ScalarParser.of(next, nesting).scalar()

    private fun comment(scalar: Scalar) {
        next.accept(SPACE)
        val line = scalar.lastLine()
        line.comment(Comment(indent = line.rtrim(), text = next.readLine()))
    }
}
