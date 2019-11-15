package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ParseData {
    @Autowired
    private Utils utils;

    // 函数重载，只返回和函数调用有关的文件节点和函数节点
    public Map<String, Object> graph(Collection<File> files,
                                     Collection<Method> methods,
                                     Collection<Node> nodes
                                     ){
        return graph(files, methods, nodes,
                Boolean.TRUE, Boolean.TRUE,
                Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    // 函数重载，只返回相关的内容节点，及其可能存在的函数调用节点
    public Map<String, Object> graph(Collection<File> files,
                                     Collection<Method> methods,
                                     Collection<Node> nodes,
                                     Boolean nodeSucc,
                                     Boolean nodeCallMethod
    ){
        return graph(files, methods, nodes,
                Boolean.FALSE, Boolean.FALSE, Boolean.FALSE,
                nodeSucc, nodeCallMethod);
    }


    /**
     * @Author Kangaroo
     * @Description 读取所有传入的节点类型，根据参数条件，创建对应的边关系，以特定的数据格式返回这些信息（节点，边）
     * @Date 2019/11/14 15:02
     * @Param [files, methods, nodes, fileHasMethod, methodCallMethod, methodHasNode, nodeCallMethod, nodeHasNode]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    public Map<String, Object> graph(Collection<File> files,
                                     Collection<Method> methods,
                                     Collection<Node> nodes,
                                     Boolean fileHasMethod,
                                     Boolean methodCallMethod,
                                     Boolean methodHasNode,
                                     Boolean nodeCallMethod,
                                     Boolean nodeHasNode){
        int count = 0;
        List<Object> allNodes = new ArrayList<>();
        // 添加顺序与下面代码逻辑有关联
        allNodes.addAll(nodes);
        allNodes.addAll(methods);
        allNodes.addAll(files);

        // 保存和函数调用有关的所有函数，A调用B，则 A 和 B都保存起来
        Set<Method> allCallMethod = new HashSet<>(methods);

        List<Map<String, Object>> jsonNodes = new ArrayList<>();
        List<Map<String, Object>> jsonEdges = new ArrayList<>();

        for (Object object: allNodes){
            // TODO
            // 生成调用图的时候，可能会添加函数节点不存在调用关系的文件节点
            List<Integer> res = getIndex(jsonNodes, object, count);
            int start = res.indexOf(0);
            count = res.indexOf(1);

            if (object instanceof File){
                if (fileHasMethod) {
                    for (Object node : ((File) object).getMethods()) {
                        Object target = ((HasMethod) node).getEndMethod();
                        // 判断文件的此函数节点是否于函数调用有关
                        if (allCallMethod.contains(target)) {
                            count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count);
                        }
                    }
                }
            } else if (object instanceof  Method){
                if (methodCallMethod) {
                    for (Object node : ((Method) object).getMethodCallMethods()) {
                        Object target = ((MethodCallMethod) node).getEndMethod();
                        // 保存被调用函数（上述提到的函数B）
                        allCallMethod.add((Method) target);
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count);
                    }
                }
                if (methodHasNode) {
                    for (Object node : ((Method) object).getHasNodes()) {
                        Object target = ((HasNode) node).getEndNode();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count);
                    }
                }
            } else if (object instanceof  Node){
                if (nodeCallMethod) {
                    for (Object node : ((Node) object).getNodeCallMethods()) {
                        Object target = ((NodeCallMethod) node).getEndMethod();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count);
                    }
                }
                if (nodeHasNode) {
                    for (Object node : ((Node) object).getSuccNodes()) {
                        Object target = ((SuccNode) node).getEndNode();
                        count = addEdgeRelationship(target, jsonNodes, jsonEdges, start, count);
                    }
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("nodes", jsonNodes);
        map.put("links", jsonEdges);
        //前台请求的数据
        return map;
    }

    /**
     * @Author Kangaroo
     * @Description 为 graph函数 服务，返回 obejct 在 jsonNodes中的索引值（若不在集合中，则插入）
     * @Date 2019/11/14 15:27
     * @Param [jsonNodes, object, count]
     * @return java.util.List<java.lang.Integer>
     **/
    private List<Integer> getIndex(List<Map<String, Object>> jsonNodes, Object object, int count){
        Map<String, Object> temp = utils.getNodeAttribute(object);
        int index = jsonNodes.indexOf(temp);
        if (index == -1){
            jsonNodes.add(temp);
            count++;
            index = count - 1;
        }
        return new ArrayList<Integer>(Arrays.asList(index, count));
    }

    /**
     * @Author Kangaroo
     * @Description 为 graph函数 服务，找到 object 在jsonNodes中的索引，并 jsonEdges 中保存边关系 （start -> end）
     * @Date 2019/11/14 15:29
     * @Param [object, jsonNodes, jsonEdges, start, count]
     * @return int
     **/
    private int addEdgeRelationship(Object object,
                                    List<Map<String, Object>> jsonNodes,
                                    List<Map<String, Object>> jsonEdges,
                                    int start,
                                    int count){
        List<Integer> res = getIndex(jsonNodes, object, count);
        int end = res.indexOf(0);
        count = res.indexOf(1);
        jsonEdges.add(utils.getEdgeRelationship(start, end));

        return count;
    }
}
