package com.github.t1.yaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public @Data class Stream {
    // TODO byte-order-marks; also test that all documents in a stream have the same encoding
    // see http://www.yaml.org/spec/1.2/spec.html#id2800168
    private List<Document> documents = new ArrayList<>();

    public Stream document(Document document) {
        this.documents.add(Objects.requireNonNull(document));
        fixDocumentEndMarkers();
        return this;
    }

    private void fixDocumentEndMarkers() {
        for (int i = 1; i < documents.size(); i++) {
            if (documents.get(i).hasDirectives() && !documents.get(i - 1).hasDocumentEndMarker())
                documents.get(i - 1).hasDocumentEndMarker(true);
        }
    }

    private boolean hasDocuments() { return !documents.isEmpty(); }

    public Document lastDocument() { return documents.get(documents.size() - 1); }

    public Stream canonicalize() {
        documents.removeIf(Document::isEmpty);
        documents.forEach(Document::canonicalize);
        fixDocumentEndMarkers();
        if (hasDocuments())
            lastDocument().hasDocumentEndMarker(false);
        return this;
    }
}
