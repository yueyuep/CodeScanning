package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.ParseData;
import com.nwu.nisl.demo.Entity.*;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import com.nwu.nisl.demo.Repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class MainServices {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MethodRepository methodRepository;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private ParseData parseData;

    public MainServices() {
    }

    // TODO 只是显示文件和函数结点，如果访问函数下面的结点，则直接从数据库中进行读取
    // TODO 防止一次性读取数据过多
    public MainServices(FileRepository fileRepository, MethodRepository methodRepository, NodeRepository nodeRepository) {
        this.fileRepository = fileRepository;
        this.methodRepository = methodRepository;
        this.nodeRepository = nodeRepository;
    }

//    public Map<String, Object> graph(Collection<File> files, Collection<Method> methods, Collection<Node> nodes) {
//        int count = 0;
//        List<Object> allNodes = new ArrayList<>();
////        methods.forEach(method -> allNodes.add(method));
////        nodes.forEach(node -> allNodes.add(node));
//        files.forEach(file -> allNodes.add(file));
//
//        List<Map<String, Object>> json_nodes = new ArrayList<>();
//        List<Map<String, Object>> json_edges = new ArrayList<>();
//
//        for (Object object : allNodes) {
//            //遍历结点
//            int start;
//            Map<String, Object> temp = getNodeAttribute(object);
//            if (object instanceof Node) {
//                //建立后继关系，调用关系
//                if (json_nodes.indexOf(temp) != -1) {
//                    start = json_nodes.indexOf(temp);
//                } else {
//                    json_nodes.add(temp);
//                    count++;
//                    start = count - 1;
//                }
//
//                for (SuccNode succNode : ((Node) object).getSuccNodes()) {
//                    Node target = succNode.getEndNode();
//                    Map<String, Object> targetNode = getNodeAttribute(target);
//                    if (json_nodes.indexOf(targetNode) == -1) {
//                        json_nodes.add(targetNode);
//                        count++;
//
//                    }
//                    int end = json_nodes.indexOf(targetNode);
//                    json_edges.add(getNodeCollection(start, end));
//                }
//
//                //建立call关系
//                for (NodeCallMethod callMethod : ((Node) object).getNodeCallMethods()) {
//                    Method target = callMethod.getEndMethod();
//                    Map<String, Object> targetMethod = getNodeAttribute(target);
//                    if (json_nodes.indexOf(targetMethod) == -1) {
//                        json_nodes.add(targetMethod);
//
//                        count++;
//
//                    }
//                    int end = json_nodes.indexOf(targetMethod);
//                    json_edges.add(getNodeCollection(start, end));
//
//
//                }
//
//            } else if (object instanceof Method) {
//                //建立后继关系
//                if (json_nodes.indexOf(temp) != -1) {
//                    start = json_nodes.indexOf(temp);
//                } else {
//                    json_nodes.add(temp);
//                    count++;
//                    start = count - 1;
//                }
//
//                for (HasNode hasNode : ((Method) object).getHasNodes()) {
//                    Node target = hasNode.getEndNode();
//                    Map<String, Object> targetNode = getNodeAttribute(target);
//                    if (json_nodes.indexOf(targetNode) == -1) {
//                        json_nodes.add(targetNode);
//                        count++;
//
//                    }
//                    int end = json_nodes.indexOf(targetNode);
//                    json_edges.add(getNodeCollection(start, end));
//                }
//
//                for (MethodCallMethod methodCallMethod: ((Method) object).getMethodCallMethods()){
//                    Method target = methodCallMethod.getEndMethod();
//                    Map<String, Object> targetMethod = getNodeAttribute(target);
//                    if (json_nodes.indexOf(targetMethod) == -1){
//                        json_nodes.add(targetMethod);
//                        count++;
//                    }
//                    int end = json_nodes.indexOf(targetMethod);
//                    json_edges.add(getNodeCollection(start, end));
//                }
//
//            } else if (object instanceof File) {
//                //建立文件包含关系
//                if (json_nodes.indexOf(temp) != -1) {
//                    start = json_nodes.indexOf(temp);
//                } else {
//                    json_nodes.add(temp);
//                    count++;
//                    start = count - 1;
//                }
//                for (HasMethod hasMethod : ((File) object).getMethods()) {
//                    Method targetMethod = hasMethod.getEndMethod();
//                    Map<String, Object> targetNode = getNodeAttribute(targetMethod);
//                    if (json_nodes.indexOf(targetNode) == -1) {
//                        json_nodes.add(targetNode);
//                        count++;
//
//                    }
//                    int end = json_nodes.indexOf(targetNode);
//                    json_edges.add(getNodeCollection(start, end));
//                }
//
//            }
//            //遍历边关系
//        }
//        Map<String, Object> map = new HashMap<>();
//        map.put("nodes", json_nodes);
//        map.put("links", json_edges);
//        //前台请求的数据
//        return map;
//    }

//    @Transactional(readOnly = true)
//    public Map<String, Object> getAllNodes(int limit) {
//        Collection<File> files = fileRepository.findFilesByVersion("0.9.22");
//        Collection<Method> methods = null;//methodRepository.findMethod(limit);
//        Collection<Node> nodes = null;//nodeRepository.graph(limit);
//        return parseData.graph(files, methods, nodes);
//    }


}
