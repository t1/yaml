package com.github.t1.yaml.model

interface Node {
    var anchor: String?
    var spacing: String?
    var lineWrapping: String?

    fun guide(visitor: Visitor)
}
