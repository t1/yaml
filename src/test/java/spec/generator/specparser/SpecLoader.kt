package spec.generator.specparser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import spec.generator.Expression
import spec.generator.Production
import spec.generator.Spec
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

class SpecLoader {

    fun load(): Spec {
        return load(if (Files.exists(CACHE)) loadHtmlFromCache() else fetchHtmlFromYamlOrg())
    }

    fun load(document: Document): Spec {
        val productions = ArrayList<Production>()
        for (set in document.select("table.productionset table.productionset tr"))
            productions.add(parse(set))
        return Spec(productions)
    }

    private fun parse(set: Element): Production {
        val counter = parseCounter(set)
        val argsMatcher = lhsMatcher(set)
        var name = argsMatcher.group("name")
        val args = argsMatcher.group("args")?.split(",")?.toMutableList() ?: mutableListOf()
        if (args.size > 0 && (args[0].startsWith("<") || args[0].startsWith("≤"))) {
            name += args[0].substring(0, 1)
            args[0] = args[0].substring(1)
        }
        val expression = expression(set, counter, name)
        return Production(counter, name, args, expression)
    }

    private fun expression(set: Element, counter: Int, name: String): Expression {
        val rhs = set.selectFirst("td.productionrhs")
        try {
            val parser = NodeExpressionParser(rhs.childNodes())
            return parser.expression()
        } catch (e: Throwable) {
            var info = "can't parse [$counter][$name]:\n\n$rhs\n\n"
            for (i in 0 until rhs.childNodeSize())
                info += "  $i: ${rhs.childNode(i)}\n"
            throw RuntimeException(info, e)
        }
    }

    private fun parseCounter(set: Element): Int {
        var text = set.selectFirst("td.productioncounter").text()
        assert(text.startsWith("["))
        assert(text.endsWith("]"))
        text = text.substring(1, text.length - 1)
        return Integer.parseInt(text)
    }

    private fun lhsMatcher(set: Element): Matcher {
        val lhs = set.selectFirst("td.productionlhs").text()
        val argsMatcher = Pattern.compile("(?<name>.*?)(\\((?<args>.*)\\))?").matcher(lhs)
        if (!argsMatcher.matches())
            throw RuntimeException("unexpected lhs")
        return argsMatcher
    }

    companion object {
        private val CACHE = Paths.get("target", "spec.html")

        private fun loadHtmlFromCache(): Document {
            println("load spec from $CACHE")
            return Jsoup.parse(CACHE.toFile(), "UTF-8")
        }

        private fun fetchHtmlFromYamlOrg(): Document {
            println("fetch spec from yaml.org")
            val document = Jsoup.connect("http://yaml.org/spec/1.2/spec.html").get()
            Files.write(CACHE, document.toString().toByteArray())
            return document
        }
    }
}
