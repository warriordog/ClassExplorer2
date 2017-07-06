package net.acomputerdog.ce2.disassembler;

import java.util.Collections;
import java.util.Map;

//you can't scroll to a line in a JEditorPane...
@Deprecated
public class Disassembly {
    private final String contents;
    private final Map<String, Integer> memberMap;

    public Disassembly(String contents, Map<String, Integer> memberMap) {
        this.contents = contents;
        this.memberMap = memberMap;
    }

    public String getContents() {
        return contents;
    }

    public Map<String, Integer> getMemberMap() {
        return Collections.unmodifiableMap(memberMap);
    }
}
