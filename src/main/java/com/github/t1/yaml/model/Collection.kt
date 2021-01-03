package com.github.t1.yaml.model

import com.github.t1.yaml.model.Collection.Style.BLOCK

interface Collection : Node {
    var style: Style

    enum class Style {
        FLOW, BLOCK
    }

    companion object {
        val DEFAULT_STYLE = BLOCK
    }
}
