package com.github.t1.yaml.tools

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class CodePointReaderTest {
    private val reader = CodePointReader("abc")
    private lateinit var read: CodePoint

    @Test fun `should start at beginning`() {
        assertPosition(total = 0, lineNumber = 1, linePosition = 1, isStartOfFile = true)
    }

    @Test fun `should read first char`() {
        read()

        assertThat(read).isEqualTo(A)
        assertPosition(total = 1, lineNumber = 1, linePosition = 2, isStartOfFile = false)
    }

    @Test fun `should read second char`() {
        reader.read()

        read()

        assertThat(read).isEqualTo(B)
        assertPosition(total = 2, lineNumber = 1, linePosition = 3, isStartOfFile = false)
    }

    @Test fun `should read third char`() {
        reader.read()
        reader.read()

        read()

        assertThat(read).isEqualTo(C)
        assertPosition(total = 3, lineNumber = 1, linePosition = 4, isStartOfFile = false)
    }

    @Test fun `should read EOF char`() {
        reader.read()
        reader.read()
        reader.read()

        read()

        assertThat(read.isEof).isTrue()
        assertPosition(total = 3, lineNumber = 1, linePosition = 4, isStartOfFile = false)
    }

    @Test fun `should mark and reread first char`() {
        val first = reader.mark { reader.read() }

        read()

        assertThat(first).isEqualTo(A)
        assertThat(read).isEqualTo(A)
        assertPosition(total = 1, lineNumber = 1, linePosition = 2, isStartOfFile = false)
    }

    @Test fun `should mark first, read two, reset, and reread first char`() {
        val first = reader.mark { reader.read(2) }

        read()

        assertThat(first).isEqualTo(listOf(A, B))
        assertThat(read).isEqualTo(A)
        assertPosition(total = 1, lineNumber = 1, linePosition = 2, isStartOfFile = false)
    }

    @Test fun `should mark and reread second char`() {
        reader.read()
        val first = reader.mark { reader.read() }

        read()

        assertThat(first).isEqualTo(B)
        assertThat(read).isEqualTo(B)
        assertPosition(total = 2, lineNumber = 1, linePosition = 3, isStartOfFile = false)
    }

    @Test fun `should read again from mark`() {
        val first = reader.mark { reader.read(3) }
        val second = reader.mark { reader.read() }

        read()

        assertThat(first).isEqualTo(listOf(A, B, C))
        assertThat(second.info).isEqualTo(A.info)
        assertThat(read).isEqualTo(A)
        assertPosition(total = 1, lineNumber = 1, linePosition = 2, isStartOfFile = false)
    }

    @Test fun `should nest two marks`() {
        val a = reader.read()
        val outer = reader.mark {
            val b = reader.read()
            val c = reader.mark { reader.read() }
            listOf(b, c)
        }

        read()

        assertThat(a).isEqualTo(A)
        assertThat(outer).isEqualTo(listOf(B, C))
        assertThat(read).isEqualTo(B)
        assertPosition(total = 2, lineNumber = 1, linePosition = 3, isStartOfFile = false)
    }


    private fun read() {
        read = reader.read()
    }

    private fun assertPosition(total: Long, lineNumber: Long, linePosition: Long, isStartOfFile: Boolean) =
        softly {
            assertThat(reader.position.totalPosition).describedAs("totalPosition").isEqualTo(total)
            assertThat(reader.position.lineNumber).describedAs("lineNumber").isEqualTo(lineNumber)
            assertThat(reader.position.linePosition).describedAs("linePosition").isEqualTo(linePosition)
            assertThat(reader.isStartOfFile).describedAs("isStartOfFile").isEqualTo(isStartOfFile)
        }

    companion object {
        val A = CodePoint.of('a')
        val B = CodePoint.of('b')
        val C = CodePoint.of('c')

        fun softly(block: SoftAssertions.() -> Unit) = SoftAssertions().apply(block).assertAll()
    }
}
