package com.github.t1.yaml.tools

// 1000 should be enough
private const val SPACES = "" +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    " +
    "                                                                                                    "

fun spaces(indent: Int): String {
    assert(SPACES.length == 1000) { "actually " + SPACES.length }
    assert(indent <= SPACES.length)
    return SPACES.substring(0, indent)
}
