package com.github.t1.yaml.model

data class Mapping(
    val entries: MutableList<Entry> = mutableListOf()
) : Collection() {

    fun entry(key: String, value: String): Mapping = entry(Scalar().line(key), value)

    fun entry(key: Scalar, value: String): Mapping = entry(key, Scalar().line(value))

    fun entry(key: String, value: Scalar): Mapping = entry(Scalar().line(key), value)

    fun entry(key: String, value: Node): Mapping = entry(Scalar().line(key), value)

    fun entry(key: Node, value: Node): Mapping = entry(Entry(key, value))

    fun entry(entry: Entry): Mapping {
        entries.add(entry)
        return this
    }

    fun lastEntry(): Entry = entries[entries.size - 1]

    override fun guide(visitor: Visitor) {
        visitor.visit(this)
        for (entry in entries) {
            visitor.enterMappingEntry(this, entry)
            entry.guide(visitor)
            visitor.leaveMappingEntry(this, entry)
        }
        visitor.leave(this)
    }

    data class Entry(
        var key: Node? = null,
        var value: Node? = null,
        var hasMarkedKey: Boolean = false,
        var hasNlAfterKey: Boolean = false
    ) {
        fun guide(visitor: Visitor) {
            visitor.enterMappingKey(this, key!!)
            key!!.guide(visitor)
            visitor.leaveMappingKey(this, key!!)
            visitor.enterMappingValue(this, value!!)
            value!!.guide(visitor)
            visitor.leaveMappingValue(this, value!!)
        }
    }
}
