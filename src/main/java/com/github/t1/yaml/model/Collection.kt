package com.github.t1.yaml.model

import com.github.t1.yaml.model.Collection.Style.BLOCK

abstract class Collection(open var style: Style = BLOCK) : Node() {
    enum class Style {
        FLOW, BLOCK
    }
}
