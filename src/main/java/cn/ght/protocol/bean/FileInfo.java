package cn.ght.protocol.bean;

/**
 * Created by zxd on 2018/1/15.
 */

public class FileInfo {

    public static final int FILE_TYPE_NONE = -1;

    public static final int FILE_TYPE_DIST = 0;

    public static final int FILE_TYPE_DIRECTORY = 1;

    public static final int FILE_TYPE_FILE = 2;

    private String fileName;

    private int fileType;

    public FileInfo() {
    }

    public FileInfo(String fileName, int fileType) {
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }
}
