package pers.kaoru.rfsclient.core;

import java.io.*;
import java.util.LinkedList;

public class FileInfo implements Serializable {

    private final String name;
    private final Boolean isDirectory;
    private final Long size;
    private final Long last;

    public FileInfo(File file) {
        name = file.getName();
        isDirectory = file.isDirectory();
        size = file.length();
        last = file.lastModified();
    }

    public FileInfo(String name, Boolean isDirectory, Long size, Long last) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.size = size;
        this.last = last;
    }

    public String getName() {
        return name;
    }

    public Boolean isDirectory() {
        return isDirectory;
    }

    public Long getSize() {
        return size;
    }

    public Long getLast() {
        return last;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", isDirectory=" + isDirectory +
                ", size=" + size +
                ", last=" + last +
                '}';
    }

    public static LinkedList<FileInfo> FileInfoBuild(String string) {
        LinkedList<FileInfo> fileInfoList = new LinkedList<>();
        if (string.length() == 2) return fileInfoList;
        string = string.substring(1, string.length() - 2);
        String[] elements = string.split("\\}, ");
        for (String element : elements) {
            element = element.substring(9);
            String[] subElements = element.split(", ");

            String name = subElements[0].split("=")[1];
            name = name.substring(1, name.length() - 1);
            Boolean isDirectory = Boolean.parseBoolean(subElements[1].split("=")[1]);
            Long size = Long.parseLong(subElements[2].split("=")[1]);
            Long last = Long.parseLong(subElements[3].split("=")[1]);
            fileInfoList.add(new FileInfo(name, isDirectory, size, last));
        }
        return fileInfoList;
    }
}
