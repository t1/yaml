package com.github.t1.yaml.model

data class ScalarTag(
    override val name: String,
    override val kind: String,
    val format: String
) : Tag
