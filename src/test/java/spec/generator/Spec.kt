package spec.generator

import spec.generator.Expression.ReferenceExpression
import java.util.TreeMap

class Spec(val productions: List<Production>) {
    val externalRefs = mutableSetOf<String>()
    private val index = TreeMap<String, Production>()

    init {
        index()
        resolveReferences()
    }

    private fun index() {
        for (production in productions) {
            val previous = index.put(production.key, production)
            assert(previous == null) { "overwrite " + previous!!.key }
        }
    }

    private fun resolveReferences() {
        for (production in productions) {
            production.expression.guide(object : OrIgnoreVisitor() {
                override fun visit(reference: ReferenceExpression) {
                    if (index[reference.key] == null) externalRefs += reference.key
                    else production.references[reference.key] = index[reference.key]!!
                }
            })
        }
    }

    operator fun get(key: String): Production = index[key]
        ?: throw RuntimeException("no production '$key' found")
}

data class Production(
    val counter: Int,
    val name: String,
    val args: List<String>,
    val expression: Expression) {

    val references = mutableMapOf<String, Production>()

    val key: String get() = name + argsKey
    val argsKey: String get() = if (args.isEmpty()) "" else args.joinToString(",", "(", ")")

    override fun toString() = "`$counter` : $key:\n$expression"
}
