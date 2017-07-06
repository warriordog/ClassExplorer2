package net.acomputerdog.ce2.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUtils {

    public static URL urlInJar(String jarURL, String entry) throws MalformedURLException {
        return new URL("jar:" + jarURL + "!/" + entry);
    }

    public static URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isClass(String name) {
        return extensionMatches(name, ".class");
    }

    public static boolean isJar(String name) {
        return extensionMatches(name, ".jar") || extensionMatches(name, ".zip");
    }

    public static boolean isClass(File file) {
    return extensionMatches(file, ".class");
}

    public static boolean isJar(File file) {
        return extensionMatches(file, ".jar") || extensionMatches(file, ".zip");
    }

    private static boolean extensionMatches(File file, String ext) {
        if (file == null || ext == null) {
            return false;
        }
        String path = file.getPath();
        return extensionMatches(path, ext);
    }

    private static boolean extensionMatches(String name, String ext) {
        if (name == null || ext == null) {
            return false;
        }

        int start = name.length() - ext.length();
        if (start < 0) {
            return false;
        }
        String sub = name.substring(start);
        return ext.equalsIgnoreCase(sub);

    }
}
