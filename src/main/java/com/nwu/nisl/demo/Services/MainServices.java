package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.Utils;
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
    private Utils utils;

    public MainServices() {
    }

    // TODO 只是显示文件和函数结点，如果访问函数下面的结点，则直接从数据库中进行读取
    // TODO 防止一次性读取数据过多
    public MainServices(FileRepository fileRepository, MethodRepository methodRepository, NodeRepository nodeRepository) {
        this.fileRepository = fileRepository;
        this.methodRepository = methodRepository;
        this.nodeRepository = nodeRepository;
    }

    public Map<String, Object> graph(Collection<Method> methods, Collection<Node> nodes, Collection<File> files) {
        int count = 0;
        List<Object> allnodes = new ArrayList<>();
        methods.forEach(method -> allnodes.add(method));
        nodes.forEach(node -> allnodes.add(node));
        files.forEach(file -> allnodes.add(file));
        List<Map<String, Object>> json_nodes = new ArrayList<>();
        List<Map<String, Object>> json_edges = new ArrayList<>();
        for (Object object : allnodes) {
            //遍历结点
            int start;
            Map<String, Object> temp = utils.getNodeAttribute(object);
            if (object instanceof Node) {
                //建立后继关系，调用关系
                if (json_nodes.indexOf(temp) != -1) {
                    start = json_nodes.indexOf(temp);
                } else {
                    json_nodes.add(temp);
                    count++;
                    start = count - 1;
                }

                for (SuccNode succNode : ((Node) object).getSuccNodes()) {
                    Node target = succNode.getEndnode();
                    Map<String, Object> targetNode = utils.getNodeAttribute(target);
                    if (json_nodes.indexOf(targetNode) == -1) {
                        json_nodes.add(targetNode);
                        count++;

                    }
                    int end = json_nodes.indexOf(targetNode);
                    json_edges.add(getNodeCollection(start, end));
                }
                //建立call关系
                for (CallMethod callMethod : ((Node) object).getCallMethods()) {
                    Method target = callMethod.getEndmethod();
                    Map<String, Object> targetMethod =utils.getNodeAttribute(target);
                    if (json_nodes.indexOf(targetMethod) == -1) {
                        json_nodes.add(targetMethod);

                        count++;

                    }
                    int end = json_nodes.indexOf(targetMethod);
                    json_edges.add(getNodeCollection(start, end));


                }

            } else if (object instanceof Method) {
                //建立后继关系
                if (json_nodes.indexOf(temp) != -1) {
                    start = json_nodes.indexOf(temp);
                } else {
                    json_nodes.add(temp);
                    count++;
                    start = count - 1;
                }
                for (HasNode hasNode : ((Method) object).getHasNodes()) {
                    Node target = hasNode.getEndNode();
                    Map<String, Object> targetNode = utils.getNodeAttribute(target);
                    if (json_nodes.indexOf(targetNode) == -1) {
                        json_nodes.add(targetNode);
                        count++;

                    }
                    int end = json_nodes.indexOf(targetNode);
                    json_edges.add(getNodeCollection(start, end));
                }

            } else if (object instanceof File) {
                //建立文件包含关系
                if (json_nodes.indexOf(temp) != -1) {
                    start = json_nodes.indexOf(temp);
                } else {
                    json_nodes.add(temp);
                    count++;
                    start = count - 1;
                }
                for (HasMethod hasMethod : ((File) object).getMethods()) {
                    Method targetMethod = hasMethod.getEndMethod();
                    Map<String, Object> targetNode = utils.getNodeAttribute(targetMethod);
                    if (json_nodes.indexOf(targetNode) == -1) {
                        json_nodes.add(targetNode);
                        count++;

                    }
                    int end = json_nodes.indexOf(targetNode);
                    json_edges.add(getNodeCollection(start, end));
                }

            }
            //遍历边关系
        }
        Map<String, Object> map = new HashMap<>();
        map.put("nodes", json_nodes);
        map.put("links", json_edges);
        //前台请求的数据
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getallNodes(int limit) {
        Collection<Method> methods = methodRepository.findMethod(limit);
        Collection<Node> nodes = nodeRepository.graph(limit);
        Collection<File> files = fileRepository.findFiles(limit);
        return graph(methods, nodes, files);
    }



    public Map<String, Object> getNodeCollection(int start, int end) {
        /*
        获取结点的边关系
         */
        Map<String, Object> map = new HashMap<>();
        map.put("source", start);
        map.put("target", end);
        return map;

    }

}
