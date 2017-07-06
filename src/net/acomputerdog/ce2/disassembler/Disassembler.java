package net.acomputerdog.ce2.disassembler;

import javassist.CtClass;

public interface Disassembler {
    String disassembleClass(CtClass cls);
}
