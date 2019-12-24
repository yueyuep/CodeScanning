package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.Node;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Component
 * @ClassName: Graph
 * @Author: Kangaroo
 * @Description: 根据要求，返回对应的callGraph图
 * @Date: 2019/11/27 17:27
 * @Version: 1.0
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
//            diffNode.setPath(path);
            Map<String, Object> map = diffNode.parseDiff();
            Map<String, Collection<File>> diffFiles = new HashMap<>();
            Map<String, Collection<Method>> diffMethods = new HashMap<>();
            //TODO 这块解析变化节点有问题
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

    /*
     *Author:lp on 2019/11/29 22:14
     *Description:显示部分变化节点的callgraph图
     */
    public Map<String, Object> getLevelPartNodes(String version, int level) {
        //版本信息其实不需要
        Map<String, Map<String, List<Object>>> typediff = scanGraph.initInstance(level);
        return connectDiff.initstance(typediff);


    }

}
