package net.acomputerdog.ce2.gui;

import javassist.*;
import net.acomputerdog.ce2.disassembler.Disassembler;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ClassViewPanel extends JSplitPane {
    private final CtClass cls;

    private final JTree structureTree;
    private final DefaultTreeModel structureModel;
    private final DefaultMutableTreeNode structureRoot;

    private final JEditorPane disPane;
    private final Disassembler disassembler;
    //private Disassembly lastDisassembly;

    public ClassViewPanel(CtClass cls, Disassembler disassembler) {
        super();
        this.cls = cls;
        this.disassembler = disassembler;

        super.setDividerSize(7);

        structureRoot = new DefaultMutableTreeNode(cls.getSimpleName());
        structureModel = new DefaultTreeModel(structureRoot);
        structureTree = new JTree();
        structureTree.setModel(structureModel);
        disPane = new JEditorPane();
        disPane.setEditable(false);
        disPane.setEditorKit(new HTMLEditorKit());

        super.setLeftComponent(new JScrollPane(structureTree));
        super.setRightComponent(new JScrollPane(disPane));

        rebuildStructure();
        disassemble();
    }

    /*
    private String findCategory(DefaultMutableTreeNode node) {
        //root node has no parent
        if (node.getParent() == null) {
            return "root";
        }

        //make sure not to try and get category of a category
        if (node.getChildCount() != 0) {
            return null;
        }

        DefaultMutableTreeNode curr = node;
        while (curr.getParent() != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)curr.getParent();
            //if parent's parent is null, then this is a 1st level node (a category)
            if (parent.getParent() == null) {
                return String.valueOf(parent.getUserObject());
            } else {
                //keep walking up the tree
                curr = parent;
            }
        }
        return null;
    }
    */

    private void rebuildStructure() {
        addClass(structureRoot, this.cls);
        structureModel.reload();
    }

    private void addClass(DefaultMutableTreeNode root, CtClass cls) {
        root.removeAllChildren();

        DefaultMutableTreeNode fields = new DefaultMutableTreeNode("fields");
        DefaultMutableTreeNode cons = new DefaultMutableTreeNode("constructors");
        DefaultMutableTreeNode methods = new DefaultMutableTreeNode("methods");
        DefaultMutableTreeNode classes = new DefaultMutableTreeNode("classes");
        root.add(fields);
        root.add(cons);
        root.add(methods);
        root.add(classes);

        for (CtField field : cls.getFields()) {
            fields.add(new DefaultMutableTreeNode(field.getName()));
        }

        for (CtConstructor con : cls.getConstructors()) {
            cons.add(new DefaultMutableTreeNode(con.getName()));
        }

        for (CtMethod method : cls.getMethods()) {
            methods.add(new DefaultMutableTreeNode(method.getName()));
        }

        try {
            for (CtClass inner : cls.getNestedClasses()) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(inner.getSimpleName());
                root.add(node);
                addClass(node, inner);
            }
        } catch (NotFoundException ignored) {}

        if (fields.getChildCount() == 0) {
            fields.removeFromParent();
        }
        if (cons.getChildCount() == 0) {
            cons.removeFromParent();
        }
        if (methods.getChildCount() == 0) {
            methods.removeFromParent();
        }
        if (classes.getChildCount() == 0) {
            classes.removeFromParent();
        }
    }

    private void disassemble() {
        try {
            disPane.setText(disassembler.disassembleClass(cls));
        } catch (Exception e) {
            System.err.println("Exception disassembling!");
            e.printStackTrace();

            StringBuilder builder = new StringBuilder();
            builder.append("<font color='red'>An internal exception occurred while disassembling this class!");
            builder.append("<br>");
            builder.append(e.getClass().getName());
            builder.append(": ");
            builder.append(e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                builder.append("<br>");
                builder.append(el.toString());
            }
            builder.append("</font>");

            disPane.setText(builder.toString());
        }
    }
}
