package com.github.t1.yaml.model

data class Alias(
    override var anchor: String? = null,
    override var spacing: String? = null,
    override var lineWrapping: String? = null
) : Node {
    override fun guide(visitor: Visitor) {
        visitor.visit(this)
    }
}
