package com.github.t1.yaml.tools

import java.io.Reader
import java.util.ArrayList

class CodePointReader(private val reader: Reader) {

    fun mark(readAheadLimit: Int): Mark = Mark(readAheadLimit)

    fun read(count: Int): List<CodePoint> {
        val out = ArrayList<CodePoint>()
        for (i in 0 until count)
            out.add(read())
        return out
    }

    fun read(): CodePoint = CodePoint.of(reader.read())

    inner class Mark(readAheadLimit: Int) : AutoCloseable {
        init {
            reader.mark(readAheadLimit)
        }

        override fun close() {
            reader.reset()
        }
    }
}
