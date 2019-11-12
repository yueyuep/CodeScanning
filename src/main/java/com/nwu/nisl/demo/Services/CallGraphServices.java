package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.PareJson;
import com.nwu.nisl.demo.Entity.*;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.python.antlr.ast.Str;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

//只显示callgraph，在数据库中读取的
@Service
public class CallGraphServices {
    //配置路径当中
    @Autowired
    private PareJson pareJson;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MethodRepository methodRepository;

    private String version = "0.9.22";

    public CallGraphServices(PareJson pareJson){

    }

    public Map<String, Object> callNodes(Collection<File> files, Collection<Method> methods){
        int count = 0;
        List<Object> allNodes = new ArrayList<>();
        files.forEach(file -> allNodes.add(file));
        methods.forEach(method -> allNodes.add(method));

        List<Map<String, Object>> jsonNodes = new ArrayList<>();
        List<Map<String, Object>> jsonEdges = new ArrayList<>();

        for (Object object: allNodes){
            int start;
            Map<String, Object> temp = getNodeAttribute(object);
            if (object instanceof File){
                if (jsonNodes.indexOf(temp) != -1){
                    start = jsonNodes.indexOf(temp);
                } else {
                    jsonNodes.add(temp);
                    count++;
                    start = count - 1;
                }
                for (HasMethod hasMethod: ((File) object).getMethods()){
                    Method targetMethod = hasMethod.getEndMethod();
                    if (methods.contains(targetMethod)){
                        Map<String, Object> targetNode = getNodeAttribute(targetMethod);
                        if (jsonNodes.indexOf(targetNode) == -1){
                            jsonNodes.add(targetNode);
                            count++;
                        }
                        int end = jsonNodes.indexOf(targetNode);
                        jsonEdges.add(getNodeCollection(start, end));
                    }
                }

            } else if (object instanceof Method){
                if (jsonNodes.indexOf(temp) != -1){
                    start = jsonNodes.indexOf(temp);
                } else {
                    jsonNodes.add(temp);
                    count++;
                    start = count - 1;
                }
                for (MethodCallMethod methodCallMethod: ((Method) object).getMethodCallMethods()){
                    Method target = methodCallMethod.getEndMethod();
                    Map<String, Object> targetMethod = getNodeAttribute(target);
                    if (jsonNodes.indexOf(targetMethod) == -1){
                        jsonNodes.add(targetMethod);
                        count++;
                    }
                    int end = jsonNodes.indexOf(targetMethod);
                    jsonEdges.add(getNodeCollection(start, end));
                }
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put("nodes", jsonNodes);
        map.put("links", jsonEdges);
        return map;
    }

    public Map<String, Object> getNodeAttribute(Object object) {
        Map<String, Object> map = new HashMap<>();

        if (object instanceof Method) {
            map.put("fileName", ((Method) object).getFileMethodName());
            map.put("version", ((Method) object).getVersion());
            map.put("num", String.valueOf(((Method)object).getNum()));
            //map.put("nodeType", ((Method) object).getNodeType());
        } else if (object instanceof File) {
            map.put("fileName", ((File) object).getFileName());
            map.put("version", ((File) object).getVersion());
            //map.put("nodeType", ((File) object).getNodeType());

        }

        return map;
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

    @Transactional(readOnly = true)
    public Map<String, Object> getCallNodes() {
        Collection<File> files = fileRepository.findFilesWithMethodByVersion(version);
        Collection<Method> methods = methodRepository.findMethodsWithCallByVersion(version);
        Collection<Method> t = methods.stream().filter(method -> !method.getMethodCallMethods().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        return callNodes(files, t);
    }
}
