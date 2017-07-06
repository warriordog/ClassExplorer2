package net.acomputerdog.ce2.disassembler.html;

public enum HTMLFormat {
    TEXT("", ""),
    KEYWORD("<font color=\"blue\">", "</font>"),
    TYPE("<font color=\"green\">", "</font>"),
    BYTECODE("<font color=\"red\">", "</font>"),
    PRIMITIVE("<font color=\"#20B2AA\">", "</font>"),
    ARGUMENT("<font color=\"#E6B62C\">", "</font>"),
    VOID("<font color=\"#9733DD\">", "</font>"),
    STATIC_TEXT("<i>", "</i>")
    ;

    private final String startFormat;
    private final String endFormat;

    HTMLFormat(String startFormat, String endFormat) {
        this.startFormat = startFormat;
        this.endFormat = endFormat;
    }

    public void writeStart(HTMLBuilder builder) {
        builder.appendRawHTML(startFormat);
    }

    public void writeEnd(HTMLBuilder builder) {
        builder.appendRawHTML(endFormat);
    }
}
