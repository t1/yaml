package spec.generator

import spec.generator.Expression.ReferenceExpression
import java.util.TreeMap

class Spec(val productions: List<Production>) {
    private val index = TreeMap<String, Production>()

    init {
        index()
    }

    private fun index() {
        for (production in productions) {
            val previous = index.put(production.key, production)
            assert(previous == null) { "overwrite " + previous!!.key }
        }
        for (production in productions) {
            production.expression.guide(object : Expression.Visitor() {
                override fun visit(reference: ReferenceExpression) {
                    val ref = reference.ref
                    if (index[ref] == null) production.references.remove(ref)
                    else production.references[ref] = index[ref]!!
                }
            })
        }
    }

    operator fun get(key: String): Production = index[key] ?: throw RuntimeException("no production '$key' found")

}

data class Production(
    val counter: Int,
    val name: String,
    val args: List<String>,
    val expression: Expression) {

    val references = mutableMapOf<String, Production>()

    val key: String get() = name + argsKey
    val argsKey: String get() = if (args.isEmpty()) "" else args.joinToString(",", "(", ")")
}
