# About

We _love_ [YAML](http://yaml.org). It's more compact than JSON and more powerful than XML... and that's saying a lot for both comparisons!

This project is not only a fully compliant YAML 1.2 implementation, it also adds some important features:

1. A **DOM** (document object model) so a document can be manipulated in a generic manner

1. **document-first**: many documents are carefully designed, include comments, structuring empty lines, sorted mappings, etc.
    The load-dump cycle as defined in the YAML spec looses all this helpful, albeit technically unnecessary information.

1. A **schema**, not for verification (that's actually mostly boring), but for documentation!
   This includes examples, and allows generic YAML editors to support specific document types.
   Documents may want to adhere to more than one schema, so we would also need **namespaces**,
   but that may be something for the future.

# Status and Plan

* **DOM**: beta - missing mainly documentation and a more formal specification
* **Load-Dump**: alpha - simple documents may work
* **Schema**: ideas only

# DOM

## Comments

Comments are preserved, before documents, in documents, and after the document-end-marker (suffix).

## Canonical

`canonicalize` does:
* Adds YAML directive
* Removes comments
* Adds tags
* Removes documents without a node
* Concatenates multi-line literals

## Normalization

We do as little normalizations as possible, but some are:

* A document always ends with a newline character.
* New lines of scalars are indented at least as much as the appropriate for the current nesting,
  i.e. the second and subsequent lines of a scalar value in a block mapping is indented by two.
