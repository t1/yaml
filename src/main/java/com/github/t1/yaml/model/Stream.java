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

    @Override public String toString() {
        return documents.stream().map(Document::toString).collect(joining("\n"));
    }

    public Stream canonicalize() {
        documents.forEach(Document::canonicalize);
        return this;
    }
}
