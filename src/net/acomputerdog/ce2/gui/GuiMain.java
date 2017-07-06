package net.acomputerdog.ce2.gui;

import javassist.ClassPool;
import javassist.CtClass;
import net.acomputerdog.ce2.CEClassPath;
import net.acomputerdog.ce2.disassembler.Disassembler;
import net.acomputerdog.ce2.util.FileUtils;
import net.acomputerdog.ce2.util.IterableEnumeration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GuiMain extends JFrame {
    private static final String PATTERN_DOT = Pattern.quote(".");

    private JPanel mainPanel;
    private JToolBar toolbar;
    private JButton editCPButton;

    private JTree classTree;
    private JTabbedPane classTabs;
    private JLabel statusLabel;
    private DefaultTreeModel classModel;
    private DefaultMutableTreeNode classRoot;

    private final CEClassPath classPath;
    private final ClassPool classPool;

    private final Disassembler disassembler;

    public GuiMain(CEClassPath classPath, ClassPool classPool, Disassembler disassembler) {
        super("Class Explorer 2");
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        super.setContentPane(mainPanel);
        super.setMinimumSize(new Dimension(800, 600));

        this.classPath = classPath;
        this.classPool = classPool;

        this.disassembler = disassembler;

        this.classRoot = new DefaultMutableTreeNode(new ClassTreeItem("root"));
        this.classModel = new DefaultTreeModel(classRoot);
        classTree.setModel(classModel);

        editCPButton.addActionListener(e -> {
            GuiClassPath gcp = new GuiClassPath(this, this.classPath);

            gcp.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    statusLabel.setText("Indexing...");
                    classTabs.removeAll();

                    buildClassTree();
                    classModel.reload();
                    statusLabel.setText("Ready.");
                }
            });

            statusLabel.setText("Waiting...");
            gcp.setVisible(true);
            statusLabel.setText("Ready.");
        });
        classTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)classTree.getLastSelectedPathComponent();
                    if (node != null) {
                        ClassTreeItem item = (ClassTreeItem) node.getUserObject();
                        if (item.cls != null) {
                            String name = item.display;
                            int tabIdx = classTabs.indexOfTab(name);
                            if (tabIdx != -1) {
                                classTabs.setSelectedIndex(tabIdx);
                            } else {
                                statusLabel.setText("Decompiling...");
                                classTabs.addTab(name, new ClassViewPanel(item.cls, disassembler));

                                JPanel tabTitlePane = new JPanel(new FlowLayout());
                                tabTitlePane.add(new JLabel(name));

                                JButton close = new JButton();
                                close.setText("x");
                                close.setMargin(new Insets(0, 0, 0, 0));
                                close.setBorder(BorderFactory.createEmptyBorder());
                                close.addActionListener(e2 -> {
                                    //must be separate in case tabs are rearranged
                                    classTabs.removeTabAt(classTabs.indexOfTab(name));
                                });
                                tabTitlePane.add(close);

                                int idx = classTabs.indexOfTab(name);
                                classTabs.setTabComponentAt(idx, tabTitlePane);
                                classTabs.setSelectedIndex(idx);

                                statusLabel.setText("Ready.");
                            }
                        }
                    }
                }
            }
        });

        statusLabel.setText("Ready.");
        super.pack();
        super.setVisible(true);
    }

    private void buildClassTree() {
        classRoot.removeAllChildren();
        for (File file : classPath.getPaths()) {
            addFileToTree(file);
        }
    }

    private void addFileToTree(File file) {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File sub : contents) {
                    addFileToTree(sub);
                }
            }
        } else if (file.isFile()) {
            if (FileUtils.isClass(file)) {
                addURLToTree(FileUtils.toUrl(file));
            } else if (FileUtils.isJar(file)) {
                addJarToTree(file);
            }
        }
    }

    private void addJarToTree(File file) {
        try (ZipFile zip = new ZipFile(file)){
            URL zipURL = FileUtils.toUrl(file);
            if (zipURL != null) {
                String zipURLString = zipURL.toString();
                for (ZipEntry entry : new IterableEnumeration<>(zip.entries())) {
                    String name = entry.getName();
                    if (FileUtils.isClass(name)) {
                        try {
                            addURLToTree(FileUtils.urlInJar(zipURLString, name));
                        } catch (MalformedURLException e) {
                            System.err.println("Malformed URL");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("IOException reading zip!");
            e.printStackTrace();
        }
    }

    private void addURLToTree(URL url) {
        if (url != null) {
            try {
                URLConnection conn = url.openConnection();
                conn.connect();
                CtClass cls = classPool.makeClass(conn.getInputStream());
                addClassToTree(cls);
                conn.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addClassToTree(CtClass cls) {
        String name = cls.getName();
        String[] parts = name.split(PATTERN_DOT);

        DefaultMutableTreeNode currNode = classRoot;
        for (String part : parts) {
            //look for existing node
            boolean didFind = false;
            for (Object node : new IterableEnumeration(currNode.children())) {
                DefaultMutableTreeNode sub = (DefaultMutableTreeNode)node;
                ClassTreeItem item = (ClassTreeItem)sub.getUserObject();
                if (item.display.equals(part)) {
                    currNode = sub;
                    didFind = true;
                    break;
                }
            }
            //create new one if necessary
            if (!didFind) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                node.setUserObject(new ClassTreeItem(part));
                currNode.add(node);
                currNode = node;
            }
        }
        ((ClassTreeItem)currNode.getUserObject()).cls = cls;
    }

    private static class ClassTreeItem {
        private CtClass cls;
        private final String display;

        private ClassTreeItem(CtClass cls) {
            if (cls == null) {
                throw new IllegalArgumentException();
            }
            this.cls = cls;
            this.display = cls.getSimpleName();
        }

        private ClassTreeItem(String display) {
            if (display == null) {
                throw new IllegalArgumentException();
            }

            this.cls = null;
            this.display = display;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClassTreeItem that = (ClassTreeItem) o;

            return display.equals(that.display);
        }

        @Override
        public int hashCode() {
            return display.hashCode();
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
