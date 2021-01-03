package com.github.t1.yaml.model

import java.util.Objects

data class Document(
    val directives: MutableList<Directive> = mutableListOf(),
    var hasDirectivesEndMarker: Boolean = false,

    val prefixComments: MutableList<Comment> = mutableListOf(),

    var node: Node? = null,

    var hasDocumentEndMarker: Boolean = false,
    var suffixComment: Comment? = null
) {
    constructor(node: Node) : this(directives = mutableListOf(), node = node)

    companion object {
        @JvmStatic fun document(node: Node) = Document(node)
    }

    val isEmpty: Boolean get() = node == null && !hasDirectivesEndMarker

    fun directive(directive: Directive): Document {
        directives.add(Objects.requireNonNull(directive))
        hasDirectivesEndMarker = true
        return this
    }

    fun prefixComment(comment: Comment): Document {
        prefixComments.add(Objects.requireNonNull(comment))
        return this
    }

    fun node(node: Node): Document {
        assert(this.node == null) { "Already has node ${this.node}" }
        this.node = node
        return this
    }

    fun hasDirectives(): Boolean {
        return !directives.isEmpty()
    }


    fun guide(visitor: Visitor) {
        visitor.visit(this)

        if (!directives.isEmpty() || hasDirectivesEndMarker)
            guideToDirectives(visitor)

        for (prefixComment in prefixComments)
            visitor.visitPrefixComment(prefixComment)

        if (node != null)
            guideToBody(visitor)

        if (hasDocumentEndMarker)
            guideToDocumentEnd(visitor)

        visitor.leave(this)
    }

    private fun guideToDirectives(visitor: Visitor) {
        visitor.enterDirectives(this)
        for (directive in directives)
            visitor.visit(directive)
        visitor.leaveDirectives(this)
    }

    private fun guideToBody(visitor: Visitor) {
        visitor.enterBody(node!!)
        node!!.guide(visitor)
        visitor.leaveBody(node!!)
    }

    private fun guideToDocumentEnd(visitor: Visitor) {
        visitor.enterDocumentEnd()
        if (suffixComment != null)
            visitor.visitSuffixCommend(suffixComment!!)
        visitor.leaveDocumentEnd()
    }
}
