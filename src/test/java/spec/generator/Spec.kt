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
                    val target = index[reference.key]
                    if (target == null) externalRefs += reference.key
                    else production.addReference(reference, target)
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

    val referencedProductions = mutableMapOf<String, Production>()
    val referencedExpressions = mutableSetOf<ReferenceExpression>()

    fun addReference(reference: ReferenceExpression, target: Production) {
        referencedExpressions += reference
        referencedProductions[reference.key] = target
    }

    val hasArgs: Boolean get() = args.isNotEmpty()
    val key: String get() = name + argsKey
    val argsKey: String get() = if (args.isEmpty()) "" else args.joinToString(",", "(", ")")

    override fun toString() = "`$counter` : $key:\n$expression"
}
