package com.github.t1.yaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

@Data
public class Stream {
    private DocumentPrefix prefix;
    private List<Document> documents = new ArrayList<>();

    public DocumentPrefix prefix() {
        if (prefix == null)
            prefix = new DocumentPrefix();
        return prefix;
    }

    public Stream document(Document document) {
        this.documents.add(document);
        return this;
    }

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        if (prefix != null)
            out.append(prefix.toString());
        out.append(documents.stream().map(Document::toString).collect(joining("\n---\n")));
        return out.toString();
    }

    public Stream canonicalize() {
        prefix = null; // The recommended encoding is UTF-8 which doesn't need a BOM, and prefix comments are not shown in the examples
        documents.forEach(Document::canonicalize);
        return this;
    }
}
