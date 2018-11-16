package com.github.t1.yaml

import com.github.t1.yaml.dump.Canonicalizer
import com.github.t1.yaml.dump.Presenter
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Stream
import com.github.t1.yaml.parser.DocumentParser
import com.github.t1.yaml.parser.YamlParseException
import java.io.BufferedReader

import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.nio.charset.StandardCharsets.UTF_8

object Yaml {
    @JvmStatic fun parseFirst(inputStream: InputStream): Document = parseFirst(BufferedReader(InputStreamReader(inputStream, UTF_8)))

    @JvmStatic fun parseFirst(reader: Reader): Document = parseFirst(DocumentParser(reader))

    @JvmStatic fun parseFirst(yaml: String): Document = parseFirst(StringReader(yaml))

    private fun parseFirst(parser: DocumentParser): Document = parser.document()
        ?: throw YamlParseException("expected at least one document, but found none")


    @JvmStatic fun parseSingle(inputStream: InputStream): Document = parseSingle(BufferedReader(InputStreamReader(inputStream, UTF_8)))

    @JvmStatic fun parseSingle(reader: Reader): Document = parseSingle(DocumentParser(reader))

    @JvmStatic fun parseSingle(yaml: String): Document = parseSingle(StringReader(yaml))

    private fun parseSingle(parser: DocumentParser): Document {
        val document = parser.document()
            ?: throw YamlParseException("expected exactly one document, but found none")
        if (parser.more())
            throw YamlParseException("expected exactly one document, but found more: $parser")
        return document
    }


    @JvmStatic fun parseAll(inputStream: InputStream): Stream = parseAll(BufferedReader(InputStreamReader(inputStream, UTF_8)))

    @JvmStatic fun parseAll(yaml: String): Stream = parseAll(StringReader(yaml))

    @JvmStatic fun parseAll(reader: Reader): Stream = parseAll(DocumentParser(reader))

    private fun parseAll(parser: DocumentParser): Stream {
        val stream = Stream()
        while (parser.more())
            parser.document()?.let { stream.document(it) }
        return stream
    }


    @JvmStatic fun present(document: Document): String = present(Stream(mutableListOf(document)))
    @JvmStatic fun present(stream: Stream): String = Presenter().present(stream)

    @JvmStatic fun canonicalize(stream: Stream) = stream.guide(Canonicalizer())
}
