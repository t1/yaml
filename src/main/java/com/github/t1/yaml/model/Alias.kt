package com.github.t1.yaml.model

class Alias : Node() {
    override fun guide(visitor: Visitor) {
        visitor.visit(this)
    }
}
