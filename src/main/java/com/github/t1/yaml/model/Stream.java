package com.github.t1.yaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

@Data
public class Stream {
    // TODO byte-order-marks; also test that all documents in a stream have the same encoding
    // see http://www.yaml.org/spec/1.2/spec.html#id2800168
    private List<Document> documents = new ArrayList<>();

    public Stream document(Document document) {
        this.documents.add(Objects.requireNonNull(document));
        return this;
    }

    private boolean hasDocuments() { return !documents.isEmpty(); }

    public Document lastDocument() { return documents.get(documents.size() - 1); }

    @Override public String toString() {
        return documents.stream().map(Document::toString).collect(joining());
    }

    public Stream canonicalize() {
        documents.removeIf(document -> document.node() == null);
        documents.forEach(Document::canonicalize);
        if (hasDocuments())
            lastDocument().hasDocumentEndMarker(false);
        return this;
    }
}
