package spec.generator

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

class SpecLoader {

    fun load(): Spec {
        return loadSpecFrom(if (Files.exists(CACHE)) loadHtmlFromCache() else fetchHtmlFromYamlOrg())
    }

    private fun loadSpecFrom(document: Document): Spec {
        val productions = ArrayList<Production>()
        for (set in document.select("table.productionset table.productionset tr"))
            productions.add(parse(set))
        return Spec(productions)
    }

    fun parse(set: Element): Production {
        val counter = parseCounter(set)

        val argsMatcher = lhsMatcher(set)
        val name = argsMatcher.group("name")
        val args = argsMatcher.group("args")

        val expression = expression(set, counter, name)

        return Production(counter, name, args, expression)
    }

    private fun expression(set: Element, counter: Int, name: String): Expression {
        val rhs = set.selectFirst("td.productionrhs")
        try {
            val parser = NodeExpressionParser(rhs.childNodes())
            return parser.expression()
        } catch (e: RuntimeException) {
            val info = StringBuilder()
            info.append("can't parse [").append(counter).append("][").append(name).append("]:\n\n").append(rhs).append("\n\n")
            for (i in 0 until rhs.childNodeSize())
                info.append("  ").append(i).append(": ").append(rhs.childNode(i)).append("\n")
            throw RuntimeException(info.toString(), e)
        } catch (e: AssertionError) {
            val info = StringBuilder()
            info.append("can't parse [").append(counter).append("][").append(name).append("]:\n\n").append(rhs).append("\n\n")
            for (i in 0 until rhs.childNodeSize())
                info.append("  ").append(i).append(": ").append(rhs.childNode(i)).append("\n")
            throw RuntimeException(info.toString(), e)
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
