package net.acomputerdog.ce2.disassembler.html;

import javassist.*;
import javassist.bytecode.*;
import net.acomputerdog.ce2.disassembler.Disassembler;
import net.acomputerdog.ce2.disassembler.fakeclass.FakeClass;

public class HTMLDisassembler implements Disassembler {

    private final ClassPool classPool;

    public HTMLDisassembler(ClassPool classPool) {
        this.classPool = classPool;
    }

    @Override
    public String disassembleClass(CtClass cls) {
        HTMLBuilder builder = new HTMLBuilder();
        FakeClass.loadMissingClasses(classPool, cls.getRefClasses());

        writeHTMLheader(builder);
        writeHeader(builder, cls);

        builder.setIndent(1);
        builder.newLine();
        writeFields(builder, cls);
        writeConstructors(builder, cls);
        writeMethods(builder, cls);
        builder.setIndent(0);
        builder.newLine();

        writeFooter(builder, cls);
        writeHTMLfooter(builder);

        return builder.toString();
        //return new Disassembly(builder.toString());
    }

    protected void writeHeader(HTMLBuilder builder, CtClass cls) {
        writePackage(builder, cls);
        builder.newLine();

        writeImports(builder, cls);
        builder.newLine();

        writeClassLine(builder, cls);
        builder.addText("{");
        builder.newLine();
    }

    protected void writePackage(HTMLBuilder b, CtClass cls) {
        b.addKeyword("package ");
        b.addText(cls.getPackageName());
        b.addText(";");
        b.newLine();
    }

    protected void writeImports(HTMLBuilder b, CtClass cls) {
        for (Object obj : cls.getRefClasses()) {
            String name = obj.toString();
            if (!name.equals(cls.getName())) {
                b.addKeyword("import ");
                b.addType(name);
                b.addText(";");
                b.newLine();
            }
        }
    }

    protected void writeClassLine(HTMLBuilder b, CtClass cls) {
        writeClassModifiers(b, cls);
        b.addType(cls.getSimpleName());
        b.addText(" ");

        CtClass parent = null;
        try {
            parent = cls.getSuperclass();
        } catch (NotFoundException ignored) {}
        if (parent != null && !"java.lang.Object".equals(parent.getName()) && !"java.lang.Enum".equals(parent.getName())) {
            b.addKeyword("extends ");
            b.addType(parent.getSimpleName());
            b.addText(" ");
        }

        CtClass[] interfaces = new CtClass[0];
        try {
            interfaces = cls.getInterfaces();
        } catch (NotFoundException ignored){}
        if (interfaces.length > 0) {
            b.addKeyword("implements ");
        }
        for (int i = 0; i < interfaces.length; i++) {
            if (i > 0) {
                b.addText(", ");
            }
            CtClass face = interfaces[i];
            b.addType(face.getSimpleName());
        }
        if (interfaces.length > 0) {
            b.addText(" ");
        }
    }

    protected void writeClassModifiers(HTMLBuilder b, CtClass cls) {
        int mod = cls.getModifiers();

        writeCommonModifiers(b, mod);

        if (!cls.isInterface() && Modifier.isAbstract(mod)) {
            b.addKeyword("abstract ");
        }

        if (Modifier.isStrict(mod)) {
            b.addKeyword("strictfp ");
        }

        if (Modifier.isInterface(mod)) {
            b.addKeyword("interface ");
        } else if (Modifier.isEnum(mod)) {
            b.addKeyword("enum ");
        } else if (Modifier.isAnnotation(mod)) {
            b.addKeyword("@interface ");
        } else {
            b.addKeyword("class ");
        }
    }

    protected void writeCommonModifiers(HTMLBuilder b, int mod) {
        if (Modifier.isPublic(mod)) {
            b.addKeyword("public ");
        } else if (Modifier.isPrivate(mod)) {
            b.addKeyword("private ");
        } else if (Modifier.isProtected(mod)) {
            b.addKeyword("protected ");
        }

        if (Modifier.isStatic(mod)) {
            b.addKeyword("static ");
        }

        if (Modifier.isFinal(mod)) {
            b.addKeyword("final ");
        }
    }

    protected void writeFieldModifiers(HTMLBuilder b, CtField field) {
        int mod = field.getModifiers();

        writeCommonModifiers(b, mod);

        if (Modifier.isTransient(mod)) {
            b.addKeyword("transient ");
        }

        if (Modifier.isVolatile(mod)) {
            b.addKeyword("volatile ");
        }
    }

    protected void writeMethodModifiers(HTMLBuilder b, CtMethod method) {
        int mod = method.getModifiers();

        writeCommonModifiers(b, mod);

        if (Modifier.isSynchronized(mod)) {
            b.addKeyword("synchronized ");
        }
        if (Modifier.isNative(mod)) {
            b.addKeyword("native ");
        }
        if (Modifier.isAbstract(mod)) {
            b.addKeyword("abstract ");
        }
        if (Modifier.isStrict(mod)) {
            b.addKeyword("strictpf ");
        }
    }

    protected void writeInterfaceMethodModifiers(HTMLBuilder b, CtMethod method) {
        int mod = method.getModifiers();

        if (Modifier.isStatic(mod)) {
            //in an interface, these will only occur if method is static
            writeMethodModifiers(b, method);
        } else {
            //the only modifier that can happen non-statically is strictfp
            if (Modifier.isStrict(mod)) {
                b.addKeyword("strictpf ");
            }
        }
    }

    protected void writeConstructorModifiers(HTMLBuilder b, CtConstructor constructor) {
        int mod = constructor.getModifiers();

        writeCommonModifiers(b, mod);

        if (Modifier.isSynchronized(mod)) {
            b.addKeyword("synchronized ");
        }
        if (Modifier.isStrict(mod)) {
            b.addKeyword("strictpf ");
        }
    }

    protected void writeFields(HTMLBuilder b, CtClass cls) {
        CtField[] fields = cls.getDeclaredFields();
        for (CtField field : fields) {
            writeFieldModifiers(b, field);
            try {
                writeType(b, field.getType());
                b.addText(" ");
            } catch (NotFoundException e) {
                b.addType("? ");
            }
            if (Modifier.isStatic(field.getModifiers())) {
                b.addStatic(field.getName());
            } else {
                b.addText(field.getName());
            }
            b.addText(";");
            b.newLine();
        }
        b.newLine();
    }

    protected void writeType(HTMLBuilder b, CtClass type){
        int arrayDepth = 0;
        try {
            while (type.isArray()) {
                type = type.getComponentType();
                arrayDepth++;
            }
            if (type.isPrimitive()) {
                String prim = getPrimitiveType(type);
                if ("void".equals(prim)) {
                    b.addVoid("void");
                } else {
                    b.addPrimitive(prim);
                }
            } else {
                b.addType(type.getSimpleName());
            }
        } catch (NotFoundException e) {
            b.addType("?");
        }
        for (int i = 0; i < arrayDepth; i++) {
            b.addText("[]");
        }
    }

    protected void writeConstructors(HTMLBuilder b, CtClass cls) {
        for (CtConstructor constructor : cls.getDeclaredConstructors()) {
            writeConstructorModifiers(b, constructor);
            b.addType(cls.getSimpleName());
            b.addText("(");
            try {
                writeMethodArgs(b, constructor.getParameterTypes());
            } catch (NotFoundException e) {
                b.addText("?");
            }
            b.addText(") {");

            writeByteCode(b, constructor.getMethodInfo());

            b.newLine();
            b.addText("}");
            b.newLine(2);
        }
    }

    protected void writeMethodArgs(HTMLBuilder b, CtClass[] args) {
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                b.addText(", ");
            }
            writeType(b, args[i]);
            b.addText(" ");
            b.addText("arg");
            b.addText(String.valueOf(i));
        }
    }

    protected void writeMethods(HTMLBuilder b, CtClass cls) {
        for (CtMethod method : cls.getDeclaredMethods()) {
            try {
                if (cls.isInterface()) {
                    writeInterfaceMethodModifiers(b, method);
                } else {
                    writeMethodModifiers(b, method);
                }
                try {
                    writeType(b, method.getReturnType());
                    b.addType(" ");
                } catch (NotFoundException e) {
                    b.addType("? ");
                }
                if (Modifier.isStatic(method.getModifiers())) {
                    b.addStatic(method.getName());
                } else {
                    b.addText(method.getName());
                }
                b.addText("(");
                try {
                    writeMethodArgs(b, method.getParameterTypes());
                } catch (NotFoundException e) {
                    b.addText("?");
                }
                if (Modifier.isAbstract(method.getModifiers()) || Modifier.isNative(method.getModifiers())) {
                    b.addText(");");
                } else {
                    b.addText(") {");

                    writeByteCode(b, method.getMethodInfo());

                    b.addText("}");
                    b.newLine(2);
                }
            } catch (Exception e) {
                b.newLine();
                b.addBytecode("Exception occurred disassembling this method!");
                b.newLine();
                System.err.println("Exception occurred disassembling method: " + method.getSignature());
                e.printStackTrace();
            }
        }
    }

    protected void writeByteCode(HTMLBuilder b, MethodInfo info) {
        b.increaseIndent();
        b.newLine();

        ConstPool pool = info.getConstPool();
        if (info.getCodeAttribute() != null) {
            CodeIterator it = info.getCodeAttribute().iterator();
            it.begin();
            try {
                while (it.hasNext()) {
                    int idx = it.next();
                    int op = it.byteAt(idx);

                    writeInstruction(b, pool, it, idx, op);
                    if (it.hasNext()) {
                        b.newLine();
                    }
                }
            } catch (BadBytecode | ArrayIndexOutOfBoundsException e) {
                b.addBytecode("An error occurred parsing bytecode!");
            }

            b.decreaseIndent();
            b.newLine();
        } else {
            b.newLine();
            b.addBytecode("Error: method is missing CodeAttribute!");
            b.newLine();
            if (info.getAttributes().isEmpty()) {
                b.addBytecode("Attributes list is empty!  Current stack: ");
                b.newLine();
                b.newLine();
                for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                    b.addBytecode(e.toString());
                    b.newLine();
                }
            } else {
                b.addBytecode("Attributes: ");
                b.newLine();
                for (Object obj : info.getAttributes()) {
                    b.addBytecode(String.valueOf(obj));
                    b.newLine();
                }
            }
        }
    }


    protected void writeInstruction(HTMLBuilder b, ConstPool pool, CodeIterator it, int off, int op) {
        String opName = Mnemonic.OPCODE[op];
        b.addBytecode(opName);

        int a1;
        int a2;
        int a3;
        switch (op) {
            //single byte value
            case 0x10:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x36:
            case 0x37:
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0xa9:
            case 0xbc:
                a1 = it.byteAt(off + 1);
                b.addArgument(" ");
                b.addArgument(String.valueOf(a1));
                break;
            //single two byte value
            case 0x11:
                a1 = it.s16bitAt(off + 1);
                b.addArgument(" ");
                b.addArgument(String.valueOf(a1));
                break;
            //single two byte offset
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:
            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xc6:
            case 0xc7:
                a1 = it.s16bitAt(off + 1);
                b.addArgument(" $");
                b.addArgument(String.valueOf(a1));
                break;
            //single one byte CP constant
            case 0x12:
                a1 = it.byteAt(off + 1);
                b.addArgument(" ");
                writeConstant(b, pool.getLdcValue(a1));
                break;
            //single two byte CP constant
            case 0x13:
            case 0x14:
                a1 = it.s16bitAt(off + 1);
                b.addArgument(" ");
                writeConstant(b, pool.getLdcValue(a1));
                break;
            //single 2 byte CP field
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
                a1 = it.s16bitAt(off + 1);
                b.addArgument(" ");
                if (op == 0xb2 || op == 0xb3) {
                    writeFieldRef(b, pool, a1, true);
                } else {
                    writeFieldRef(b, pool, a1, false);
                }
                break;
            //single 2 byte CP method
            case 0xb6:
            case 0xb7:
            case 0xb8:
                a1 = it.s16bitAt(off + 1);
                b.addArgument(" ");
                if (op == 0xb8) {
                    writeMethodRef(b, pool, a1, true);
                } else {
                    writeMethodRef(b, pool, a1, false);
                }
                break;
            //single 2 byte CP class
            case 0xbb:
            case 0xbd:
            case 0xc0:
            case 0xc1:
                a1 = it.s16bitAt(off + 1);
                b.addArgument(" ");
                writeClassRef(b, pool, a1);
                break;
            //double one byte values
            case 0x84:
                a1 = it.byteAt(off + 1);
                a2 = it.byteAt(off + 2);
                b.addArgument(" ");
                b.addArgument(String.valueOf(a1));
                b.addArgument(", ");
                b.addArgument(String.valueOf(a2));
                break;
            //tableswitch
            case 0xaa:
                b.addArgument(" tableargs");
                break;
            //lookupswitch
            case 0xab:
                b.addArgument(" lookupargs");
                break;
            //one two byte value, 2 one byte values
            case 0xb9:
            case 0xba:
                a1 = it.s16bitAt(off + 1);
                a2 = it.byteAt(off + 3);
                a3 = it.byteAt(off + 4);
                b.addArgument(" ");
                b.addArgument(String.valueOf(a1));
                b.addArgument(", ");
                b.addArgument(String.valueOf(a2));
                b.addArgument(", ");
                b.addArgument(String.valueOf(a3));
                break;
            //wide
            case 0xc4:
                b.addArgument(" wideargs");
                break;
            //1 two byte CP index, 1 single byte
            case 0xc5:
                b.addArgument(" 2b CP, 1b");
                break;
            //single four byte value
            case 0xc8:
            case 0xc9:
                a1 = it.s32bitAt(off + 1);
                b.addArgument(" ");
                b.addArgument(String.valueOf(a1));
                break;
            default:
                //bytecode with no arguments
                //System.err.print("Unknown opcode: " + op);
                break;
        }
        b.addText(";");
    }

    protected void writeClassRef(HTMLBuilder b, ConstPool pool, int a1) {
        String sig = pool.getClassInfo(a1);

        CtClass cls;
        if (sig.startsWith("[")) {
            cls = makeClassSig(sig);
        } else {
            cls =  FakeClass.getOrLoadClass(classPool, sig);
        }
        if (cls != null) {
            b.addType(cls.getSimpleName());
        } else {
            b.addType("?");
        }
    }

    protected void writeFieldRef(HTMLBuilder b, ConstPool pool, int a1, boolean isStatic) {
        String clsName = pool.getFieldrefClassName(a1);
        String field = pool.getFieldrefName(a1);
        String typeSig = pool.getFieldrefType(a1);

        CtClass cls = FakeClass.getOrLoadClass(classPool, clsName);
        CtClass type = makeClassSig(typeSig);

        if (cls != null) {
            b.addType(cls.getSimpleName());
        } else {
            b.addType("?");
        }
        b.addText(".");
        if (isStatic) {
            b.addStatic(field);
        } else {
            b.addText(field);
        }
        b.addText(" [");
        if (type != null) {
            writeType(b, type);
        } else {
            b.addType("?");
        }
        b.addText("]");
    }

    protected void writeMethodRef(HTMLBuilder b, ConstPool pool, int a1, boolean isStatic) {
        String clsName = pool.getMethodrefClassName(a1);
        String methodName = pool.getMethodrefName(a1);
        String methodSig = pool.getMethodrefType(a1);

        CtClass cls = FakeClass.getOrLoadClass(classPool, clsName);

        if (cls != null) {
            b.addType(cls.getSimpleName());
        } else {
            b.addType("?");
        }

        b.addText(".");
        if (isStatic) {
            b.addStatic(methodName);
        } else {
            b.addText(methodName);
        }
        b.addText("(");
        try {
            CtClass[] params = Descriptor.getParameterTypes(methodSig, classPool);
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    b.addText(", ");
                }
                writeType(b, params[i]);
            }
        } catch (NotFoundException e) {
            b.addType("?");
        }
        b.addText(") [");
        try {
            writeType(b, Descriptor.getReturnType(methodSig, classPool));
        } catch (NotFoundException e) {
            b.addType("?");
        }
        b.addText("]");
    }

    protected void writeConstant(HTMLBuilder b, Object obj) {
        if (obj == null) {
            b.addArgument("?");
        } else if (obj instanceof String) {
            b.addArgument("\"");
            b.addArgument((String)obj);
            b.addArgument("\"");
        } else {
            b.addPrimitive(obj.toString());
        }
    }

    protected CtClass makeClassSig(String sig) {
        return FakeClass.getOrLoadClass(classPool, Descriptor.toClassName(sig));
    }

    protected void writeFooter(HTMLBuilder b, CtClass cls) {
        b.addText("}");
        b.newLine();
    }

    protected void writeHTMLheader(HTMLBuilder b) {
        b.appendRawHTML("<div style='white-space:nowrap; font-family:\"Monospaced\"'>\n");
    }

    protected void writeHTMLfooter(HTMLBuilder b) {
        b.appendRawHTML("</div>");
    }

    protected String getPrimitiveType(CtClass cls) {
        if (CtClass.booleanType.equals(cls)) {
            return "boolean";
        } else if (CtClass.byteType.equals(cls)) {
            return "byte";
        } else if (CtClass.shortType.equals(cls)) {
            return "short";
        } else if (CtClass.intType.equals(cls)) {
            return "int";
        } else if (CtClass.longType.equals(cls)) {
            return "long";
        } else if (CtClass.floatType.equals(cls)) {
            return "float";
        } else if (CtClass.doubleType.equals(cls)) {
            return "double";
        } else if (CtClass.voidType.equals(cls)) {
            return "void";
        } else if (CtClass.charType.equals(cls)) {
            return "char";
        } else {
            return "?";
        }
    }
}
