package com.github.t1.yaml.model;

import com.github.t1.yaml.tools.CodePoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED;
import static com.github.t1.yaml.model.Scalar.Style.PLAIN;
import static com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED;

@EqualsAndHashCode(callSuper = true)
public @Data class Scalar extends Node {
    @RequiredArgsConstructor
    public enum Style {
        PLAIN(""), SINGLE_QUOTED("\'"), DOUBLE_QUOTED("\"");

        @Getter final String quote;
    }

    public static @Data class Line {
        int indent = 0;
        String text = "";
        Comment comment;

        public int rtrim() {
            int spaces = 0;
            int count = CodePoint.Companion.count(text);
            while (spaces < count && CodePoint.Companion.at(count - spaces - 1, text).is(Character::isSpaceChar))
                spaces++;
            this.text = text.substring(0, count - spaces);
            return spaces;
        }
    }

    private String tag;
    private Style style = PLAIN;
    private final List<Line> lines = new ArrayList<>();

    public Scalar line(String line) { return line(new Line().text(line)); }

    public Scalar line(Line line) {
        lines.add(line);
        return this;
    }

    public Scalar comment(Comment comment) {
        lastLine().comment(comment);
        return this;
    }

    public boolean isEmpty() { return lines.isEmpty(); }

    public Line lastLine() {
        if (lines.isEmpty())
            line("");
        return lines.get(lines.size() - 1);
    }

    @Override public void guide(Visitor visitor) {
        visitor.visit(this);
        for (Line line : lines) {
            visitor.enterScalarLine(this, line);
            visitor.visit(line);
            visitor.leaveScalarLine(this, line);
        }
        visitor.leave(this);
    }

    public Scalar plain() { return style(PLAIN); }

    public Scalar singleQuoted() { return style(SINGLE_QUOTED); }

    public Scalar doubleQuoted() { return style(DOUBLE_QUOTED); }
}
