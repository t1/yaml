# About Yaml

I _love_ [YAML](http://yaml.org). It's more compact than JSON and more powerful than XML... and that's saying a lot!

This project tries to add some missing features to YAML:

1. A **DOM** (document object model) so a document can be manipulated in a generic manner

1. **document-first**: many documents are carefully designed, include comments, structuring empty lines, sorted mappings, etc.
    The load-dump cycle as defined in the YAML spec would loose all that helpful albeit necessary information.

1. A **schema**, not for verification (that's actually mostly boring), but for documentation!
   This includes examples, and allows generic YAML editors to support specific document types.

# x

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