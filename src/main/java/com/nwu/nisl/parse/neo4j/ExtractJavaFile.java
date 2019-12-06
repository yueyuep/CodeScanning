package com.nwu.nisl.parse.neo4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.*;
import org.springframework.util.FileSystemUtils;

/**
 * 获取当前目录下的所有java文件
 * 删除非java文件，test文件夹
 * @author Kangaroo
 */
class ExtractJavaFile {
    private static Logger logger = Logger.getLogger(ExtractJavaFile.class);

    private File dir = null;
    private List<File> fileList = new ArrayList<>();

    public ExtractJavaFile(File dir){
        this.dir = dir;
    }

    public void getFileList(File dir){
        if (!dir.exists()||!dir.isDirectory()){
            return;
        } else {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".java")) {
                        fileList.add(file);
                    } else {
                        file.delete();
                    }
                } else {
                    if ("test".equals(file.getName())) {
                        FileSystemUtils.deleteRecursively(file);
                    } else {
                        getFileList(file);
                    }
                }
            }
        }
    }

    public File[] getFile() {
        int size = fileList.size();
        logger.info("The number of. java files is: " + size + " in " + this.dir.getName() + " project");

        return fileList.toArray(new File[size]);
    }

}
