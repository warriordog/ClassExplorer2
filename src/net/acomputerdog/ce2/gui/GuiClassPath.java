package net.acomputerdog.ce2.gui;

import net.acomputerdog.ce2.CEClassPath;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class GuiClassPath extends JFrame {
    private JPanel mainPanel;
    private JButton backButton;
    private JList<ClassPathEntry> cpList;
    private JButton addButton;
    private JButton removeButton;

    private DefaultListModel<ClassPathEntry> cpListModel;

    public GuiClassPath(JFrame parent, CEClassPath classPath) {
        super("Edit classpath");
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setContentPane(mainPanel);
        super.setMinimumSize(new Dimension(500, 400));
        super.pack();

        parent.setEnabled(false);

        cpListModel = new DefaultListModel<>();
        cpList.setModel(cpListModel);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setEnabled(true);
            }
        });

        addButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Add files or directories");
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("JAR files", "jar"));
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("class files", "class"));

            if (chooser.showDialog(GuiClassPath.this, "open") == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();
                for (File file : files) {
                    ClassPathEntry entry = new ClassPathEntry(file);
                    if (!cpListModel.contains(entry)) {
                        cpListModel.addElement(entry);
                        classPath.addPath(file);
                    }
                }
            }
        });

        removeButton.addActionListener(e -> {
            List<ClassPathEntry> entries = cpList.getSelectedValuesList();
            for (ClassPathEntry entry : entries) {
                cpListModel.removeElement(entry);
                classPath.removePath(entry.path);
            }
        });

        backButton.addActionListener(e -> GuiClassPath.this.dispatchEvent(new WindowEvent(GuiClassPath.this, WindowEvent.WINDOW_CLOSING)));

        for (File f : classPath.getPaths()) {
            cpListModel.addElement(new ClassPathEntry(f));
        }
    }

    private static class ClassPathEntry {
        private final File path;
        private final String display;

        private ClassPathEntry(File path) {
            if (path == null) {
                throw new IllegalArgumentException();
            }
            this.path = path;

            if (path.exists()) {
                if (path.isFile()) {
                    display = path.getAbsolutePath();
                } else {
                    display = path.getAbsolutePath() + "/";
                }
            } else {
                display = path.getPath();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClassPathEntry that = (ClassPathEntry) o;

            return path.equals(that.path);
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
