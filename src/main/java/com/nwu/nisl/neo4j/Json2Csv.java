package com.nwu.nisl.neo4j;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

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
    private String separator = "?";

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

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
    private static Logger logger = LoggerFactory.getLogger(Json2Csv.class);
    public Json2Csv(String sourcePath, String destinationPath) {

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

    /**
     * 清空文件内容
     **/
    public void clear() {
        String[] fileList = new String[]{this.file, this.method, this.node,
                this.file_method, this.method_method, this.method_node,
                this.node_method, this.node_node};

        for (String file : fileList) {
            try {
                new FileWriter(file, false).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void generateCsv() throws IOException {
        // 遍历文件夹，获取所有以 .java 结尾的文件
        Path filePath = FileSystems.getDefault().getPath(sourcePath);
        List<File> allFiles = Files.walk(filePath)
                .filter(s -> s.toString().endsWith(".txt"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : allFiles) {
            logger.info("Json to csv :" + file.getName());
            List<String> lines = Files.lines(Paths.get(file.getAbsolutePath())).map(String::trim).collect(Collectors.toList());

            // 处理主体 （非第一行）
            for (String json : lines.subList(1, lines.size())) {
                Map line = new Gson().fromJson(json, Map.class);

                String version = (String) line.get("version");
                String fileMethodName = String.join(separator,
                        version,
                        (String) line.get("fileName"),
                        (String) line.get("methodName"))
                        .replace(",", ".");
                String num = String.valueOf(((Double) line.get("num")).intValue());

                // 读取fileMethodName、version、num字段，保存到 method.csv
                // 自动换行
                Files.write(Paths.get(this.method),
                        Arrays.asList(String.join(",", fileMethodName, version, num)),
                        StandardOpenOption.APPEND);

                if (!((Map<String, String>) line.get("callMethodNameReferTo")).isEmpty()) {
                    // 遍历，保存边的关系到 self.node_method
                    BufferedWriter writer = new BufferedWriter(new FileWriter(this.node_method, true));
                    for (Map.Entry<String, String> map : ((Map<String, String>) line.get("callMethodNameReferTo")).entrySet()) {
                        writer.append(String.join(",",
                                String.join(separator, fileMethodName, map.getKey()),
                                String.join(separator, version, map.getValue()).replace(",", "."),
                                "nodeCallMethod") + "\n");
                    }
                    writer.close();

                    BufferedWriter writer1 = new BufferedWriter(new FileWriter(this.method_method, true));
                    for (String call : ((Map<String, String>) line.get("callMethodNameReferTo")).values()) {
                        writer1.append(String.join(",",
                                fileMethodName,
                                String.join(separator, version, call).replace(",", "."),
                                "methodCallMethod") + "\n");
                    }
                    writer1.close();

//                    for (Map.Entry<String, String> map: ((Map<String, String>) line.get("callMethodNameReferTo")).entrySet()) {
//                        Files.write(Paths.get(this.node_method),
//                                Arrays.asList(String.join(",",
//                                        String.join(separator, fileMethodName, map.getKey()),
//                                        String.join(separator, version, map.getValue()).replace(",", "."),
//                                        "nodeCallMethod") + "\n"),
//                                StandardOpenOption.APPEND);
//                    }
//
//                    for (String call: ((Map<String, String>) line.get("callMethodNameReferTo")).values()) {
//                        Files.write(Paths.get(this.method_method),
//                                Arrays.asList(String.join(",",
//                                        fileMethodName,
//                                        String.join(separator, version, call).replace(",", "."),
//                                        "methodCallMethod") + "\n"),
//                                StandardOpenOption.APPEND);
//                    }
                }


                if (!num.isEmpty()) {
                    // 保存边的关系到 methodNode
                    Files.write(Paths.get(method_node),
                            Arrays.asList(String.join(",",
                                    fileMethodName,
                                    String.join(separator, fileMethodName, "0"),
                                    "hasNode")),
                            StandardOpenOption.APPEND);

                    // 遍历attribute属性，保存到self.node
                    int index = 0;
                    BufferedWriter writer = new BufferedWriter(new FileWriter(this.node, true));
                    for (String attr : (List<String>) line.get("attribute")) {
                        writer.append(String.join(",",
                                String.join(separator, fileMethodName, String.valueOf(index)),
                                version,
                                attr.replace(",", ".")) + "\n");
                        index++;
                    }
                    writer.close();

                    // 遍历succs属性，保存边的关系到self.node_node
                    int idx = 0;
                    BufferedWriter writer1 = new BufferedWriter(new FileWriter(this.node_node, true));
                    for (List<Object> succ : (List<List<Object>>) line.get("succs")) {
                        for (Object next : succ) {
                            writer1.append(String.join(",",
                                    String.join(separator, fileMethodName, String.valueOf(idx)),
                                    String.join(separator, fileMethodName, String.valueOf(((Double) next).intValue())),
                                    "succNode") + "\n");
                        }
                        idx++;
                    }
                    writer1.close();

                }

            }

            // 处理基本信息 （第一行）
            Map line = new Gson().fromJson(lines.get(0), Map.class);
            String version = (String) line.get("version");
            String fileName = (String) line.get("fileName");

            // 读取 fileName 和 version 字段，并写入到 file.csv
            Files.write(Paths.get(this.file),
                    Arrays.asList(String.join(",",
                            String.join(separator, version, fileName),
                            version)),
                    StandardOpenOption.APPEND);

//            for (String methodName: (List<String>) line.get("hasMethodName")) {
//                Files.write(Paths.get(this.file_method),
//                        Arrays.asList(String.join(",",
//                                String.join(separator, version, fileName),
//                                String.join(separator, version, fileName, methodName).replace(",", "."),
//                                "hasMethod") + "\n"),
//                        StandardOpenOption.APPEND);
//            }
            if (!((List<String>) line.get("hasMethodName")).isEmpty()) {
                // 读取此字段，保存边的关系到 self.file_method
                BufferedWriter writer = new BufferedWriter(new FileWriter(this.file_method, true));
                for (String methodName : (List<String>) line.get("hasMethodName")) {
                    writer.append(String.join(",",
                            String.join(separator, version, fileName),
                            String.join(separator, version, fileName, methodName).replace(",", "."),
                            "hasMethod") + "\n");
                }
                writer.close();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        System.out.println("Please enter the version number: ");
        Scanner scanner = new Scanner(System.in);
        String version = scanner.next();

        String sourcePath = "Project\\target" + File.separator + version;
        String targetPath = "src\\main\\java\\com\\nwu\\nisl\\neo4j\\data";
        Json2Csv json2Csv = new Json2Csv(sourcePath, targetPath);
        json2Csv.clear();
//        json2Csv.generateCsv();

        System.out.println("Please enter the version number: ");
        Scanner scanner1 = new Scanner(System.in);
        String version1 = scanner1.next();
        json2Csv.setSourcePath("Project\\target" + File.separator + version1);
//        json2Csv.generateCsv();
    }

}
