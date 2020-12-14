package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.Node;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 *@Author:yueyue on 2020/12/14 9:28
 *@Param:
 *@return:
 *@Description:根据callgraph和dataflow分析关联节点
*/
@Component
public class CallGraph {
    @Autowired
    private ParseData parseData;
    @Autowired
    private DiffNode diffNode;
    @Autowired
    private ParseDiff parseDiff;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MethodRepository methodRepository;
    @Autowired
    private ScanGraph scanGraph;
    @Autowired
    private ConnectDiff connectDiff;

    @Value("${com.nwu.nisl.download.res}")
    private String outpath;

    public CallGraph() {
    }

    public Map<String, Object> callGraph(String version) {
        return callGraph(version, 0, false, false);
    }

    public Map<String, Object> callGraphWithDiff(String version) {
        return callGraph(version, 0, true, false);
    }

    public Map<String, Object> callGraphWithDiffAndLevel(String version, int level) {
        return callGraph(version, level, true, true);
    }

    public Map<String, Object> callGraph(String version, int levelNumber, Boolean showDiff, Boolean showLevel) {
        Map<String, Collection<File>> files = new HashMap<>();
        Map<String, Collection<Method>> methods = new HashMap<>();
        Map<String, Collection<Node>> nodes = new HashMap<>();

        // 文件函数调用图的所有节点
        Collection<File> allFiles = fileRepository.findFilesWithMethodByVersion(version);
        Collection<Method> temp = methodRepository.findMethodsWithCallByVersion(version);
        Collection<Method> allMethods = temp.stream().filter(method -> !method.getMethodCallMethods().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        files.put(NodeType.GENERAL_NODE, allFiles);
        methods.put(NodeType.GENERAL_NODE, allMethods);

        if (showDiff) {
            //显示变化
            Map<String, Object> map = diffNode.parseDiff();
            Map<String, Collection<File>> diffFiles = new HashMap<>();
            Map<String, Collection<Method>> diffMethods = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Map<String, Object> result = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) entry.getValue());
                diffFiles.put(entry.getKey(), (Collection<File>) result.get(NodeType.FILE));
                diffMethods.put(entry.getKey(), (Collection<Method>) result.get(NodeType.METHOD));
            }

            files.putAll(diffFiles);
            methods.putAll(diffMethods);
        }

        if (showLevel && levelNumber > 0) {
            //层次分析相关代码
            Map<String, Collection<Object>> level = scanGraph.levelNode(levelNumber);
            files.put(NodeType.LEVEL_ONE_NODE, level.get(NodeType.FILE).stream().map(x -> (File) x).collect(Collectors.toList()));
            methods.put(NodeType.LEVEL_ONE_NODE, level.get(NodeType.METHOD).stream().map(x -> (Method) x).collect(Collectors.toList()));
        }

        return parseData.graph(version, files, methods, nodes);
    }

    public Map<String, Object> getLevelPartNodes(String version, int level) {
        Map<String, Map<String, List<Object>>> typediff = scanGraph.initInstance(level);
        /*写入关联代码的结果*/
        change2txt(typediff);
        System.out.printf("done");
        return connectDiff.initstance(typediff);


    }

    public void change2txt(Map<String, Map<String, List<Object>>> typediff) {
        List<String> changeList = new ArrayList<>();
        List<String> connectList = new ArrayList<>();
        for (String changetype : typediff.keySet()) {
            Map<String, List<Object>> sp_change_type = typediff.get(changetype);
            for (String fileOrMethod : sp_change_type.keySet()) {
                List<Object> sp_fileOrMethod = sp_change_type.get(fileOrMethod);
                for (Object change : sp_fileOrMethod) {
                    if (change instanceof File) {
                        if (((File) change).getLevel() == 0) {
                            changeList.add(((File) change).getFileName());
                        } else {
                            connectList.add(((File) change).getFileName());
                        }
                    } else if (change instanceof Method) {
                        if (((Method) change).getLevel() == 0) {
                            changeList.add(((Method) change).getFileMethodName());
                        } else {
                            connectList.add(((Method) change).getFileMethodName());
                        }

                    }
                }
            }
        }
        //将changeList和connectList写入res.txt文件
        try {
            java.io.File file = new java.io.File(outpath + java.io.File.separator + "res.txt");
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outpath + java.io.File.separator + "res.txt", true)));

            out.write("---------------------changeNode---------------------" + "\r\n");
            for (String text : changeList) {
                out.write(text + "\r\n");

            }
            out.write("---------------------connectNode---------------------" + "\r\n");
            for (String text : connectList) {
                out.write(text + "\r\n");
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
