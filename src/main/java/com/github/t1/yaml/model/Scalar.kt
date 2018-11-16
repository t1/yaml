package com.github.t1.yaml.model

import com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED
import com.github.t1.yaml.model.Scalar.Style.PLAIN
import com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED

data class Scalar(
    override var anchor: String? = null,
    override var spacing: String? = null,
    override var lineWrapping: String? = null,
    var tag: String? = null,
    var style: Style = PLAIN,
    val lines: MutableList<Line> = mutableListOf()
) : Node {

    companion object {
        @JvmStatic fun scalar() = Scalar()
    }

    val isEmpty: Boolean get() = lines.isEmpty()

    override fun toString(): String =
        "Scalar(tag=" + this.tag + ", style=" + this.style + ", lines=" + this.lines + ")"

    enum class Style(val quote: String) {
        PLAIN(""), SINGLE_QUOTED("\'"), DOUBLE_QUOTED("\"");
    }

    data class Line(
        var indent: Int = 0,
        var text: String = "",
        var comment: Comment? = null
    ) {
        fun indent(indent: Int): Line {
            this.indent = indent
            return this
        }

        fun text(text: String): Line {
            this.text = text
            return this
        }

        fun comment(comment: Comment): Line {
            this.comment = comment
            return this
        }

        override fun toString(): String =
            "Scalar.Line(indent=" + this.indent + ", text=" + this.text + ", comment=" + this.comment + ")"
    }

    fun line(line: String): Scalar = line(Line().text(line))

    fun line(line: Line): Scalar {
        lines.add(line)
        return this
    }

    fun comment(comment: Comment): Scalar {
        lastLine.comment(comment)
        return this
    }

    val lastLine
        get(): Line {
            if (lines.isEmpty())
                line("")
            return lines[lines.size - 1]
        }

    override fun guide(visitor: Visitor) {
        visitor.visit(this)
        for (line in lines) {
            visitor.enterScalarLine(this, line)
            visitor.visit(line)
            visitor.leaveScalarLine(this, line)
        }
        visitor.leave(this)
    }

    fun plain(): Scalar = style(PLAIN)

    fun singleQuoted(): Scalar = style(SINGLE_QUOTED)

    fun doubleQuoted(): Scalar = style(DOUBLE_QUOTED)

    fun style(style: Style): Scalar {
        this.style = style
        return this
    }
}
