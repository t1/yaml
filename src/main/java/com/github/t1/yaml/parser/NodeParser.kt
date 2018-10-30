package com.github.t1.yaml.parser

import com.github.t1.yaml.model.Collection.Style.FLOW
import com.github.t1.yaml.model.Mapping
import com.github.t1.yaml.model.Mapping.Entry
import com.github.t1.yaml.model.Node
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Sequence
import com.github.t1.yaml.model.Sequence.Item
import com.github.t1.yaml.parser.ScalarParser.Mode.KEY
import com.github.t1.yaml.parser.ScalarParser.Mode.VALUE
import com.github.t1.yaml.tools.symbol

internal class NodeParser(private val next: YamlScanner) {
    private val nesting = Nesting(this.next)

    override fun toString(): String = "NodeParser $next nesting: $nesting"

    fun node(): Node {
        nesting.expect()
        return when {
            next.peek(`c-sequence-start`) -> flowSequence()
            next.peek(BLOCK_SEQUENCE_START) -> blockSequence()
            next.peek(`c-mapping-start`) -> flowMapping()
            next.peek(BLOCK_MAPPING_START) -> blockMapping()
            else -> scalar()
        }
    }

    private fun flowSequence(): Node {
        next.expect(`c-sequence-start`)
        val sequence = Sequence(style = FLOW)
        do {
            sequence.item(flowSequenceItem())
        } while (next.more() && next.accept(`c-collect-entry`))
        next.expect(`c-sequence-end`)
        next.skip(WS)
        return sequence
    }

    private fun flowSequenceItem(): Item {
        next.skip(`s-space`)
        val line = next.readUntil(symbol(',').or(symbol(']'))) // TODO this must be a call to node()!
        next.skip(`s-space`)
        return Item(node = Scalar().line(line))
    }

    private fun blockSequence(): Sequence {
        val sequence = Sequence()
        do
            sequence.item(blockSequenceItem())
        while (next.more() && nesting.accept())
        return sequence
    }

    private fun blockSequenceItem(): Item {
        next.expect(`c-sequence-entry`)
        val nlItem = next.accept(`b-break`)
        if (!nlItem) {
            next.expect(`s-space`)
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
        entry.hasMarkedKey = next.accept(`c-mapping-key`)
        if (entry.hasMarkedKey)
            next.expect(`s-space`)
        entry.key = scalar() // TODO key node()
    }

    private fun blockMappingValue(entry: Entry) {
        next.expect(`c-mapping-value`)
        entry.hasNlAfterKey = next.accept(`b-break`)
        if (!entry.hasNlAfterKey) {
            next.expect(`s-space`)
            nesting.skipNext(true)
        }

        nesting.up()
        entry.value = node()
        nesting.down()
    }

    private fun flowMapping(): Mapping {
        next.expect(`c-mapping-start`)
        val mapping = Mapping(style = FLOW)
        do {
            mapping.entry(flowMappingEntry())
        } while (next.more() && next.accept(`c-collect-entry`))
        next.expect(`c-mapping-end`)
        return mapping
    }

    private fun flowMappingEntry(): Entry {
        val entry = Entry()
        flowMappingKey(entry)
        flowMappingValue(entry)
        return entry
    }

    private fun flowMappingKey(entry: Entry) {
        entry.hasMarkedKey = next.accept(`c-mapping-key`)
        if (entry.hasMarkedKey)
            next.expect(`s-space`)
        entry.key = ScalarParser.of(next, nesting, mode = KEY).scalar()
        // TODO key node()
    }

    private fun flowMappingValue(entry: Entry) {
        next.expect(`c-mapping-value`)
        entry.hasNlAfterKey = next.accept(`b-break`)
        if (!entry.hasNlAfterKey) {
            next.expect(`s-space`)
            nesting.skipNext(true)
        }

        nesting.up()
        entry.value = ScalarParser.of(next, nesting, mode = VALUE).scalar()
        // TODO value node()
        nesting.down()
    }

    private fun scalar(): Scalar = ScalarParser.of(next, nesting).scalar()
}
