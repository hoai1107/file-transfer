package org.example.server;

public class ServerVariable {
    private static String rootDirectory = System.getProperty("user.dir");

    public static String getRootDirectory() {
        return rootDirectory;
    }

    public static void setRootDirectory(String dir) {
        rootDirectory = dir;
    }
}
