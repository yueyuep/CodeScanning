package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.HasMethod;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.MethodCallMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Author:lp on 2019/11/29 22:26
 * Param:
 * return:
 * Description:给三种不同变化的类型添加变化标注，
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
        int i = 0;
        for (Object object : allNodes) {
            if (typeDiff.get("add").get("file").contains(object) || typeDiff.get("add").get("method").contains(object)) {
                startNode = utils.getNodeAttribute(object, "yes", "addConnectDiff");
                if (object instanceof File) {
                    startNode.put("hasMethod", ((File) object).getMethods());
                } else if (object instanceof Method)
                    startNode.put("methodCallMethods", ((Method) object).getMethodCallMethods());
            } else if (typeDiff.get("delete").get("file").contains(object) || typeDiff.get("delete").get("method").contains(object)) {
                startNode = utils.getNodeAttribute(object, "yes", "deleteConnectDiff");
                if (object instanceof File) {
                    startNode.put("hasMethod", ((File) object).getMethods());
                } else if (object instanceof Method)
                    startNode.put("methodCallMethods", ((Method) object).getMethodCallMethods());

            } else if (typeDiff.get("modify").get("file").contains(object) || typeDiff.get("modify").get("method").contains(object)) {
                startNode = utils.getNodeAttribute(object, "yes", "modifyConnectDiff");
                if (object instanceof File) {
                    startNode.put("hasMethod", ((File) object).getMethods());
                } else if (object instanceof Method)
                    startNode.put("methodCallMethods", ((Method) object).getMethodCallMethods());

            } else {
                //continue
            }

            //判断我们的startNode是否已经被计算过。如果计算过，则直接跳过
            if (isSameNode(nodes, startNode)) {
                continue;
            }

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

    public boolean isSameNode(List<Map<String, Object>> maps, Map<String, Object> pnode) {
        //判断pnode文件名是否在pnode中出现，只是名字是否重复
        String pname = "";
        if (pnode.get("nodeType") == "method") {
            pname = pnode.get("fileMethodName").toString();
        } else {
            pname = pnode.get("fileName").toString();
        }
        for (Map<String, Object> target : maps) {
            if (target.get("nodeType") == "method" && target.get("fileMethodName").equals(pname)) return true;
            if (target.get("nodeType") == "file" && target.get("fileName").equals(pname)) return true;

        }
        return false;

    }


}
