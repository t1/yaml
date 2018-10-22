package com.github.t1.yaml.model

import com.github.t1.yaml.model.Collection.Companion.DEFAULT_STYLE
import com.github.t1.yaml.model.Collection.Style

data class Sequence(
    override var anchor: String? = null,
    override var spacing: String? = null,
    override var lineWrapping: String? = null,
    override var style: Style = DEFAULT_STYLE,
    val items: MutableList<Item> = mutableListOf()
) : Collection {

    data class Item(
        var node: Node,
        var nl: Boolean = false
    )

    fun item(node: Node): Sequence = item(Item(node))

    fun item(item: Item): Sequence {
        items.add(item)
        return this
    }

    fun firstItem(): Item = items[0]

    fun lastItem(): Item = items[items.size - 1]

    override fun guide(visitor: Visitor) {
        visitor.visit(this)
        for (item in items) {
            visitor.enterSequenceItem(this, item)
            item.node.guide(visitor)
            visitor.leaveSequenceItem(this, item)
        }
        visitor.leave(this)
    }
}
