package com.github.t1.yaml.model

data class Directive(
    val name: String? = null,
    val parameters: String? = null
) {

    fun matchName(that: Directive): Boolean = name == that.name

    companion object {
        val YAML_VERSION = Directive("YAML", "1.2")
    }
}
