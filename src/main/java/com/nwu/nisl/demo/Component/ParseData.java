package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ParseData {
    @Autowired
    private Utils utils;

    // 函数重载，只返回和函数调用有关的文件、函数节点
    public Map<String, Object> graph(String version,
                                     Collection<File> files,
                                     Collection<Method> methods,
                                     Collection<Node> nodes
                                     ){
        List<Collection<File>> fileList = new ArrayList<>();
        fileList.add(files);
        List<Collection<Method>> methodList = new ArrayList<>();
        methodList.add(methods);
        List<Collection<Node>> nodeList = new ArrayList<>();
        nodeList.add(nodes);

        return graph(version, fileList, methodList, nodeList,
                Boolean.TRUE, Boolean.TRUE,
                Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    // 函数重载，只返回相关的内容节点，及其可能存在的函数调用节点
    public Map<String, Object> graph(String version,
                                     Collection<File> files,
                                     Collection<Method> methods,
                                     Collection<Node> nodes,
                                     Boolean nodeSucc,
                                     Boolean nodeCallMethod
    ){
        List<Collection<File>> fileList = new ArrayList<>();
        fileList.add(files);
        List<Collection<Method>> methodList = new ArrayList<>();
        methodList.add(methods);
        List<Collection<Node>> nodeList = new ArrayList<>();
        nodeList.add(nodes);

        return graph(version, fileList, methodList, nodeList,
                Boolean.FALSE, Boolean.FALSE, Boolean.FALSE,
                nodeSucc, nodeCallMethod);
    }

    /**
     * @Author Kangaroo
     * @Description 读取所有传入的节点类型，根据参数条件，创建对应的边关系，以特定的数据格式返回这些信息（节点，边）
     * @Date 2019/11/14 15:02
     * @Param [version, files, methods, nodes, fileHasMethod, methodCallMethod, methodHasNode, nodeCallMethod, nodeHasNode]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
        public Map<String, Object> graph(String version,
                                         List<Collection<File>> files,
                                         List<Collection<Method>> methods,
                                         List<Collection<Node>> nodes,
                                         Boolean fileHasMethod,
                                         Boolean methodCallMethod,
                                         Boolean methodHasNode,
                                         Boolean nodeCallMethod,
                                         Boolean nodeHasNode){
        int count = 0;
        List<Object> allNodes = new ArrayList<>();

        // 添加顺序与下面代码逻辑有关联
        // addAll 不能添加null, add 可以添加null
        for (Collection<Node> node: nodes) if (node != null) allNodes.addAll(node);
        for (Collection<Method> method: methods) if (method != null) allNodes.addAll(method);
        for (Collection<File> file: files) if (file != null) allNodes.addAll(file);

        // 去重且顺序不发生变化
        LinkedHashSet<Object> set = new LinkedHashSet<>(allNodes);
        allNodes.clear();
        allNodes.addAll(set);

        List<Map<String, Object>> jsonNodes = new ArrayList<>();
        List<Map<String, Object>> jsonEdges = new ArrayList<>();

        for (Object object: allNodes){
            // TODO
            // 生成调用图的时候，可能会添加函数节点不存在调用关系的文件节点
            List<Integer> res = getIndex(jsonNodes, object, count, judgeChanged(object, version, files, methods, nodes));
            int start = res.get(0);
            count = res.get(1);

            if (object instanceof File){
                if (fileHasMethod) {
                    for (Object node : ((File) object).getMethods()) {
                        Object target = ((HasMethod) node).getEndMethod();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count, judgeChanged(target, version, files, methods, nodes), "hasMethod");
                    }
                }
            } else if (object instanceof  Method){
                if (methodCallMethod) {
                    for (Object node : ((Method) object).getMethodCallMethods()) {
                        Object target = ((MethodCallMethod) node).getEndMethod();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count, judgeChanged(target, version, files, methods, nodes), "methodCallMethod");
                    }
                }
                if (methodHasNode) {
                    for (Object node : ((Method) object).getHasNodes()) {
                        Object target = ((HasNode) node).getEndNode();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count, judgeChanged(target, version, files, methods, nodes), "hasNode");
                    }
                }
            } else if (object instanceof  Node){
                if (nodeCallMethod) {
                    for (Object node : ((Node) object).getNodeCallMethods()) {
                        Object target = ((NodeCallMethod) node).getEndMethod();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count, judgeChanged(target, version, files, methods, nodes), "nodeCallMethod");
                    }
                }
                if (nodeHasNode) {
                    for (Object node : ((Node) object).getSuccNodes()) {
                        Object target = ((SuccNode) node).getEndNode();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count, judgeChanged(target, version, files, methods, nodes), "succNode");
                    }
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("nodes", jsonNodes);
        map.put("links", jsonEdges);

        return map;
    }

    /**
     * @Author Kangaroo
     * @Description 为 graph函数 服务，返回 object 在 jsonNodes中的索引值，变化后的count（若不在集合中，则插入）
     * @Date 2019/11/14 15:27
     * @Param [jsonNodes, object, count, type]
     * @return java.util.List<java.lang.Integer>
     **/
    private List<Integer> getIndex(List<Map<String, Object>> jsonNodes, Object object, int count, String type){
        Map<String, Object> temp;
        if ("".equals(type)) {
            temp = utils.getNodeAttribute(object, "", type);
        } else {
            temp = utils.getNodeAttribute(object, "yes", type);
        }
        int index = jsonNodes.indexOf(temp);
        if (index == -1){
            jsonNodes.add(temp);
            count++;
            index = count - 1;
        }
        return new ArrayList<>(Arrays.asList(index, count));
    }

    /**
     * @Author Kangaroo
     * @Description 为 graph函数 服务，找到 object 在jsonNodes中的索引，并 jsonEdges 中保存边关系，返回count （start -> end）
     * @Date 2019/11/14 15:29
     * @Param [object, jsonNodes, jsonEdges, start, count, nodeType, edgeType]
     * @return int
     **/
    private int addEdgeRelationship(Object object,
                                    List<Map<String, Object>> jsonNodes,
                                    List<Map<String, Object>> jsonEdges,
                                    int start,
                                    int count,
                                    String nodeType,
                                    String edgeType){
        List<Integer> res = getIndex(jsonNodes, object, count, nodeType);
        int end = res.get(0);
        count = res.get(1);
        jsonEdges.add(utils.getEdgeRelationship(start, end, edgeType));

        return count;
    }

    /**
     * @Author Kangaroo
     * @Description 判断 object 对象是否在在项目变更中发生变化，若发生变化，则返回变化类型
     * @Date 2019/11/16 9:57
     * @Param [object, version, files, methods, nodes]
     * @return java.lang.String
     **/
    private String judgeChanged(Object object,
                                String version,
                                List<Collection<File>> files,
                                List<Collection<Method>> methods,
                                List<Collection<Node>> nodes) {
        if (object instanceof File) {
            File temp = (File) object;
            if (files.size() > 1) {
                if (files.get(1).contains(temp)) {
                    return "add";
                } else if (!temp.getVersion().equals(version)) {
                    return "deleted";
                }
            }
        } else if (object instanceof Method) {
            Method temp = (Method) object;
            if (methods.size() > 1){
                if (methods.get(1).contains(temp)) {
                    return "add";
                } else if (methods.get(2).contains(temp)) {
                    return "modify";
                } else if (!temp.getVersion().equals(version)) {
                    return "deleted";
                }
            }
        } else  if (object instanceof Node) {

        }
        return "";
    }
}
