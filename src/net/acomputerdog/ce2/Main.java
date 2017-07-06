package net.acomputerdog.ce2;

import javassist.ClassPool;
import net.acomputerdog.ce2.disassembler.Disassembler;
import net.acomputerdog.ce2.disassembler.html.HTMLDisassembler;
import net.acomputerdog.ce2.gui.GuiMain;

public class Main {

    public static void main(String[] args) {
        CEClassPath classPath = new CEClassPath();
        //include system classpath
        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(classPath);

        Disassembler disassembler = new HTMLDisassembler(classPool);

        new GuiMain(classPath, classPool, disassembler);
    }
}
