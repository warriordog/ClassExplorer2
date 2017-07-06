package net.acomputerdog.ce2.disassembler.html;

public class HTMLBuilder {
    private final StringBuilder builder;

    private HTMLFormat currFormat = HTMLFormat.TEXT;
    private int indentLevel = 0;
    private int numLines = 0;
    private boolean printedFirstLine = false;

    public HTMLBuilder() {
        builder = new StringBuilder();
    }

    private void changeFormat(HTMLFormat format) {
        if (currFormat != format) {
            currFormat.writeEnd(this);
            format.writeStart(this);
            currFormat = format;
        }
    }

    public void setIndent(int level) {
        this.indentLevel = level;
    }

    public void increaseIndent() {
        indentLevel++;
    }

    public void decreaseIndent() {
        indentLevel--;
    }

    public void append(String str) {
        if (!printedFirstLine) {
            printLine();
        }

        str = str.replace("\n", "\\n");
        str = str.replace("<", "&#60;");
        str = str.replace(">", "&#62;");

        appendRawHTML(str);
    }

    public void appendRawHTML(String html) {
        builder.append(html);
    }

    public void addKeyword(String str) {
        changeFormat(HTMLFormat.KEYWORD);
        append(str);
    }

    public void addText(String str) {
        changeFormat(HTMLFormat.TEXT);
        append(str);
    }

    public void addType(String str) {
        changeFormat(HTMLFormat.TYPE);
        append(str);
    }

    public void addBytecode(String str) {
        changeFormat(HTMLFormat.BYTECODE);
        append(str);
    }

    public void addPrimitive(String str) {
        changeFormat(HTMLFormat.PRIMITIVE);
        append(str);
    }

    public void addArgument(String str) {
        changeFormat(HTMLFormat.ARGUMENT);
        append(str);
    }

    public void addVoid(String str) {
        changeFormat(HTMLFormat.VOID);
        append(str);
    }

    public void addStatic(String str) {
        changeFormat(HTMLFormat.STATIC_TEXT);
        append(str);
    }

    public void newLine() {
        newLine(1);
    }

    public void newLine(int count) {
        if (count < 0) {
            count = 0;
        }
        numLines += count;
        for (int i = 0; i < count; i++) {
            //add line number
            appendRawHTML("<br>\n");
            printLine();
        }
        for (int i = 0; i < indentLevel * 4; i++) {
            appendRawHTML("&nbsp;");
        }
    }

    private void printLine() {
        printedFirstLine = true;
        HTMLFormat old = currFormat;
        changeFormat(HTMLFormat.TEXT);
        appendRawHTML(String.format("%-4d| ", numLines).replace(" ", "&nbsp;"));
        changeFormat(old);
    }

    @Override
    public String toString() {
        currFormat.writeEnd(this);
        //append("</div>\n");
        return builder.toString();
    }
}
