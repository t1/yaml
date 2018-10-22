package com.github.t1.yaml.model

import java.util.Objects

data class Stream(
    // TODO byte-order-marks; also test that all documents in a stream have the same encoding
    // see http://www.yaml.org/spec/1.2/spec.html#id2800168
    val documents: MutableList<Document> = mutableListOf()
) {
    fun document(document: Document): Stream {
        this.documents.add(Objects.requireNonNull(document))
        fixDocumentEndMarkers()
        return this
    }

    fun fixDocumentEndMarkers() {
        for (i in 1 until documents.size)
            if (documents[i].hasDirectives() && !documents[i - 1].hasDocumentEndMarker)
                documents[i - 1].hasDocumentEndMarker = true
    }

    fun hasDocuments(): Boolean {
        return !documents.isEmpty()
    }

    fun lastDocument(): Document {
        return documents[documents.size - 1]
    }

    fun guide(visitor: Visitor) {
        visitor.visit(this)
        for (document in documents)
            document.guide(visitor)
        visitor.leave(this)
    }
}
