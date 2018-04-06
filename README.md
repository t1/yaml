We are not perfectly strict:

| In this situation | The Spec says | We say |
| --- | --- | --- |
| x | y | z |


# Comments

Comments are preserved, before documents, in documents, and after the document-end-marker (suffix).

# Canonical

`canonicalize` does:
* Adds YAML directive
* Removes comments
* Adds tags
* Removes documents without a node

# Normalization

Do as few normalizations as possible, but some are:

* A document always ends with a newline character