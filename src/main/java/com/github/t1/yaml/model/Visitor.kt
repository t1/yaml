package com.github.t1.yaml.model

interface Visitor {
    fun visit(stream: Stream) {}

    fun leave(stream: Stream) {}


    fun visit(document: Document) {}

    fun visitPrefixComment(prefixComment: Comment) {}

    fun enterDirectives(document: Document) {}

    fun visit(directive: Directive) {}

    fun leaveDirectives(document: Document) {}

    fun enterBody(node: Node) {}

    fun leaveBody(node: Node) {}

    fun enterDocumentEnd() {}

    fun visitSuffixCommend(suffixComment: Comment) {}

    fun leaveDocumentEnd() {}

    fun leave(document: Document) {}


    fun visit(alias: Alias) {}


    fun visit(sequence: Sequence) {}

    fun enterSequenceItem(sequence: Sequence, item: Sequence.Item) {}

    fun leaveSequenceItem(sequence: Sequence, item: Sequence.Item) {}

    fun leave(sequence: Sequence) {}


    fun visit(scalar: Scalar) {}

    fun enterScalarLine(node: Scalar, line: Scalar.Line) {}

    fun visit(line: Scalar.Line) {}

    fun leaveScalarLine(node: Scalar, line: Scalar.Line) {}

    fun leave(scalar: Scalar) {}


    fun visit(mapping: Mapping) {}

    fun enterMappingEntry(mapping: Mapping, entry: Mapping.Entry) {}

    fun enterMappingKey(entry: Mapping.Entry, key: Node) {}

    fun leaveMappingKey(entry: Mapping.Entry, key: Node) {}

    fun enterMappingValue(entry: Mapping.Entry, key: Node) {}

    fun leaveMappingValue(entry: Mapping.Entry, key: Node) {}

    fun leaveMappingEntry(mapping: Mapping, entry: Mapping.Entry) {}

    fun leave(mapping: Mapping) {}
}
