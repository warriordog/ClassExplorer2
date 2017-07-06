package net.acomputerdog.ce2;

import javassist.ClassPath;
import javassist.NotFoundException;
import net.acomputerdog.ce2.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CEClassPath implements ClassPath {
    private List<File> paths = new ArrayList<>();

    public CEClassPath() {

    }

    public void addPath(File path) {
        paths.add(path);
    }

    public void removePath(File path) {
        paths.removeIf(file -> file.equals(path));
    }

    public List<File> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    @Override
    public InputStream openClassfile(String classname) throws NotFoundException {
        URL url = find(classname);
        if (url == null) {
            throw new NotFoundException("Class could not be found");
        }
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            return conn.getInputStream();
        } catch (IOException e) {
            throw new NotFoundException("Class could not be loaded", e);
        }
    }

    @Override
    public URL find(String classname) {
        for (File path : paths) {
            if (path.isDirectory()) {
                File sub = new File(path, classToPath(classname));
                if (sub.isFile()) {
                    URL url = FileUtils.toUrl(sub);
                    if (url != null) {
                        return url;
                    }
                }
            } else if (path.isFile()) {
                if (FileUtils.isJar(path)) {
                    URL url = searchJar(path, classname);
                    if (url != null) {
                        return url;
                    }
                } else if (FileUtils.isClass(path) && pathMatches(path, classname)) {
                    return FileUtils.toUrl(path);
                }
            } else {
                System.out.println("Skipping invalid classpath \"" + path.getPath() + "\".");
            }
        }
        return null;
    }

    @Override
    public void close() {

    }

    private static URL searchJar(File jarFile, String cls) {
        if (jarFile == null || cls == null) {
            return null;
        }
        URL jarURL = FileUtils.toUrl(jarFile);
        if (jarURL == null) {
            return  null;
        }
        try (ZipFile jar = new ZipFile(jarFile)) {
            String clsPath = classToPath(cls);
            ZipEntry entry = jar.getEntry(clsPath);
            if (entry != null) {
                return FileUtils.urlInJar(jarURL.toString(), clsPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private static boolean pathMatches(File path, String classname) {
        if (path == null || classname == null) {
            return false;
        }
        String clsPath = classToPath(classname);
        return path.getPath().endsWith(clsPath);
    }

    private static String classToPath(String cls) {
        if (cls == null) {
            return null;
        }
        return cls.replace('.', File.separatorChar).concat(".class");
    }

}
