package com.github.t1.yaml

import com.github.t1.yaml.dump.Canonicalizer
import com.github.t1.yaml.dump.Presenter
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Stream
import com.github.t1.yaml.parser.DocumentParser
import com.github.t1.yaml.parser.YamlParseException

import java.io.InputStream
import java.io.Reader

object Yaml {
    fun parseFirst(inputStream: InputStream): Document = parseFirst(DocumentParser(inputStream))

    fun parseFirst(reader: Reader): Document = parseFirst(DocumentParser(reader))

    fun parseFirst(yaml: String): Document = parseFirst(DocumentParser(yaml))

    private fun parseFirst(parser: DocumentParser): Document = parser.document()
            .orElseThrow { YamlParseException("expected at least one document, but found none") }


    fun parseSingle(inputStream: InputStream): Document = parseSingle(DocumentParser(inputStream))

    fun parseSingle(reader: Reader): Document = parseSingle(DocumentParser(reader))

    fun parseSingle(yaml: String): Document = parseSingle(DocumentParser(yaml))

    private fun parseSingle(parser: DocumentParser): Document {
        val document = parser.document()
            .orElseThrow { YamlParseException("expected exactly one document, but found none") }
        if (parser.more())
            throw YamlParseException("expected exactly one document, but found more: $parser")
        return document
    }


    fun parseAll(inputStream: InputStream): Stream = parseAll(DocumentParser(inputStream))

    fun parseAll(yaml: String): Stream = parseAll(DocumentParser(yaml))

    fun parseAll(reader: Reader): Stream = parseAll(DocumentParser(reader))

    private fun parseAll(parser: DocumentParser): Stream {
        val stream = Stream()
        while (parser.more())
            parser.document().ifPresent { stream.document(it) }
        return stream
    }


    fun present(stream: Stream): String = Presenter().present(stream)

    fun canonicalize(stream: Stream) = stream.guide(Canonicalizer())
}
