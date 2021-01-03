package com.github.t1.yaml.model

data class Sequence(
    override var style: Style = Style.BLOCK,
    val items: MutableList<Item> = mutableListOf()
) : Collection(style) {

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
