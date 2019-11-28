package com.nwu.nisl.demo.json2csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.json2csv
 * @ClassName: Json2Csv
 * @Author: Kangaroo
 * @Description: 将数据从Json格式转为Csv
 * @Date: 2019/11/28 14:35
 * @Version: 1.0
 */
public class Json2Csv {
    private String sourcePath;
    private String destinationPath;

    private String file;
    private String method;
    private String node;
    private String file_method;
    private String method_method;
    private String method_node;
    private String node_method;
    private String node_node;

    public Json2Csv (String sourcePath, String destinationPath) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;

        this.file = new StringBuffer(destinationPath).append(File.separator).append(FileName.FILE_NAME).toString();
        this.method = new StringBuffer(destinationPath).append(File.separator).append(FileName.METHOD_NAME).toString();
        this.node = new StringBuffer(destinationPath).append(File.separator).append(FileName.NODE_NAME).toString();
        this.file_method = new StringBuffer(destinationPath).append(File.separator).append(FileName.FILE_METHOD_NAME).toString();
        this.method_method = new StringBuffer(destinationPath).append(File.separator).append(FileName.METHOD_METHOD_NAME).toString();
        this.method_node = new StringBuffer(destinationPath).append(File.separator).append(FileName.METHOD_NODE_NAME).toString();
        this.node_method = new StringBuffer(destinationPath).append(File.separator).append(FileName.NODE_METHOD_NAME).toString();
        this.node_node = new StringBuffer(destinationPath).append(File.separator).append(FileName.NODE_NODE_NAME).toString();
    }

    public void clear() {
        File file = new File(this.file);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateCsv() {

    }

}
