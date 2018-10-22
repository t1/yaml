package com.github.t1.yaml.model

abstract class Node(
    var anchor: String? = null,
    var spacing: String? = null,
    var lineWrapping: String? = null
) {
    abstract fun guide(visitor: Visitor)
}
