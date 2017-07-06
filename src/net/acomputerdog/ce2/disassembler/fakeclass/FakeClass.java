package net.acomputerdog.ce2.disassembler.fakeclass;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.Collection;

public class FakeClass {

    public static CtClass getOrLoadClass(ClassPool pool, String name) {
        try {
            //if this works then class exists
            return pool.get(name);
        } catch (NotFoundException e) {
            //if not then we need to load a fake class
            if (!"java.lang.Object".equals(name)) {
                return pool.makeClass(name);
            } else {
                return null;
            }
        }
    }

    public static void loadMissingClasses(ClassPool pool, Collection objects) {
        for (Object obj : objects) {
            if (obj instanceof String) {
                String clsName = (String)obj;
                getOrLoadClass(pool, clsName);
            }
        }
    }
}
