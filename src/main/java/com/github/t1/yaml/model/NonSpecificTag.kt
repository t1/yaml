package com.github.t1.yaml.model

data class NonSpecificTag(
    override val name: String,
    override val kind: String
) : Tag
