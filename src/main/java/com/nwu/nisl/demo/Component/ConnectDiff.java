package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.HasMethod;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.MethodCallMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 *@Author:yueyue on 2020/12/14 9:30
 *@Param:
 *@return:
 *@Description:关联节点
*/
@Component
public class ConnectDiff {
    private Utils utils;
    private ParseData parseData;

    @Autowired
    public ConnectDiff(Utils utils, ParseData parseData) {
        this.parseData = parseData;
        this.utils = utils;
    }

    public Map<String, Object> initstance(Map<String, Map<String, List<Object>>> typeDiff) {
        Collection<Object> allNodes = new ArrayList<>();
        //把我们的文件和函数节点全部放在一起。
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        allNodes.addAll(addAllNodes(typeDiff, "add"));
        allNodes.addAll(addAllNodes(typeDiff, "delete"));
        allNodes.addAll(addAllNodes(typeDiff, "modify"));
        Map<String, Object> startNode = new HashMap<>();
        Map<String, Object> endNode = new HashMap<>();
        //需要去掉重复元素
        removeSameNode(allNodes);
        // 存在同一个节点，但是被不同的变化所引用，导致同一节点被加注不同的变化，处理完成后，我们需要进行过滤
        int i = 0;
        for (Object object : allNodes) {
            if (isSameNode(typeDiff.get("add"), object)) {
                startNode = utils.getNodeAttribute(object, "yes", "addConnectDiff");
                if (object instanceof File) {
                    startNode.put("hasMethod", ((File) object).getMethods());
                } else if (object instanceof Method) {
                    startNode.put("methodCallMethods", ((Method) object).getMethodCallMethods());
                }

            } else if (isSameNode(typeDiff.get("delete"), object)) {
                startNode = utils.getNodeAttribute(object, "yes", "deleteConnectDiff");
                if (object instanceof File) {
                    startNode.put("hasMethod", ((File) object).getMethods());
                } else if (object instanceof Method) {
                    startNode.put("methodCallMethods", ((Method) object).getMethodCallMethods());

                }

            } else if (isSameNode(typeDiff.get("modify"), object)) {
                startNode = utils.getNodeAttribute(object, "yes", "modifyConnectDiff");
                if (object instanceof File) {
                    startNode.put("hasMethod", ((File) object).getMethods());
                } else if (object instanceof Method) {
                    startNode.put("methodCallMethods", ((Method) object).getMethodCallMethods());
                }


            } else {
                //其他情况
            }
            //判断我们的startNode是否已经被计算过。如果计算过，则直接跳过

            if (!nodes.contains(startNode)) {
                nodes.add(startNode);

            }
            int start = nodes.indexOf(startNode);
            //File
            if (startNode.get("nodeType").equals("file")) {
                for (HasMethod hasMethod : (ArrayList<HasMethod>) startNode.get("hasMethod")) {
                    endNode = utils.getNodeAttribute(hasMethod.getEndMethod(), "yes", startNode.get("type").toString());
                    endNode.put("methodCallMethods", hasMethod.getEndMethod().getMethodCallMethods());
                    if (!nodes.contains(endNode)) {
                        nodes.add(endNode);
                    }
                    int target = nodes.indexOf(endNode);
                    edges.add(utils.getEdgeRelationship(start, target, "hasMethod"));

                }

            } else if (startNode.get("nodeType").equals("method")) {
                //Method
                for (MethodCallMethod methodCallMethod : (ArrayList<MethodCallMethod>) startNode.get("methodCallMethods")) {
                    endNode = utils.getNodeAttribute(methodCallMethod.getEndMethod(), "yes", startNode.get("type").toString());
                    endNode.put("methodCallMethods", methodCallMethod.getEndMethod().getMethodCallMethods());
                    if (!nodes.contains(endNode)) {
                        nodes.add(endNode);
                    }
                    int target = nodes.indexOf(endNode);
                    edges.add(utils.getEdgeRelationship(start, target, "methodCallMethod"));

                }

            }
            i++;
        }
        //移除无用的键值对
        nodes = removeKey(nodes);
        //去除重复引用的问题
        Map<String, Object> map = new HashMap<>();
        map.put("nodes", nodes);
        map.put("links", edges);

        return map;
    }


    public Collection<Object> addAllNodes(Map<String, Map<String, List<Object>>> typeDiff, String diff) {
        Collection<Object> all = new ArrayList<>();
        all.addAll(typeDiff.get(diff).get("file"));
        all.addAll(typeDiff.get(diff).get("method"));
        return all;

    }

    List<Map<String, Object>> removeKey(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            node.remove("methodCallMethods");
            node.remove("hasMethod");
        }
        return nodes;
    }

    public void removeSameNode(Collection<Object> objects) {
        HashMap<Integer, Object> tmp = new HashMap<>();
        for (Object object : objects) {
            if (object instanceof File) {
                tmp.put((int) ((File) object).getId(), object);
            }
            if (object instanceof Method) {
                tmp.put((int) ((Method) object).getId(), object);
            }
        }
        objects.clear();
        objects.addAll(tmp.values());
    }

    public boolean isSameNode(Map<String, List<Object>> maps, Object pnode) {
        //判断pnode文件名是否在map中出现，通过名字来判断
        String pname = "";
        String nodeType = "";
        if (pnode instanceof Method) {
            pname = ((Method) pnode).getFileMethodName();
            nodeType = "method";
        } else if (pnode instanceof File) {
            pname = ((File) pnode).getFileName();
            nodeType = "file";
        }
        for (Object object : maps.get(nodeType)) {
            if (object instanceof File && ((File) object).getFileName() == pname) return true;
            if (object instanceof Method && ((Method) object).getFileMethodName() == pname) return true;
        }
        return false;


    }


}
