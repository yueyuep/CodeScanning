package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.PareJson;
import com.nwu.nisl.demo.Component.ParseData;
import com.nwu.nisl.demo.Component.Utils;
import com.nwu.nisl.demo.Entity.*;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.python.antlr.ast.Str;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author:lp on 2019/11/15 21:35
 * @Param: version
 * @return: d3数据格式
 * @Description:根据特定版本的数据
 */
@Service
public class CallGraphServices {
    //构造器注入依赖
    ParseData parseData;
    FileRepository fileRepository;
    MethodRepository methodRepository;
    Utils utils;

    @Autowired
    public CallGraphServices(ParseData parseData,
                             FileRepository fileRepository,
                             MethodRepository methodRepository,
                             Utils utils) {
        this.parseData = parseData;
        this.fileRepository = fileRepository;
        this.methodRepository = methodRepository;
        this.utils = utils;

    }

//    public Map<String, Object> callNodes(Collection<File> files, Collection<Method> methods) {
//        int count = 0;
//        List<Object> allNodes = new ArrayList<>();
//        files.forEach(file -> allNodes.add(file));
//        methods.forEach(method -> allNodes.add(method));
//
//        List<Map<String, Object>> jsonNodes = new ArrayList<>();
//        List<Map<String, Object>> jsonEdges = new ArrayList<>();
//
//        for (Object object : allNodes) {
//            int start;
//            Map<String, Object> temp = getNodeAttribute(object);
//            if (object instanceof File) {
//                if (jsonNodes.indexOf(temp) != -1) {
//                    start = jsonNodes.indexOf(temp);
//                } else {
//                    jsonNodes.add(temp);
//                    count++;
//                    start = count - 1;
//                }
//                for (HasMethod hasMethod : ((File) object).getMethods()) {
//                    Method targetMethod = hasMethod.getEndMethod();
//                    if (methods.contains(targetMethod)) {
//                        Map<String, Object> targetNode = getNodeAttribute(targetMethod);
//                        if (jsonNodes.indexOf(targetNode) == -1) {
//                            jsonNodes.add(targetNode);
//                            count++;
//                        }
//                        int end = jsonNodes.indexOf(targetNode);
//                        jsonEdges.add(getNodeCollection(start, end));
//                    }
//                }
//
//            } else if (object instanceof Method) {
//                if (jsonNodes.indexOf(temp) != -1) {
//                    start = jsonNodes.indexOf(temp);
//                } else {
//                    jsonNodes.add(temp);
//                    count++;
//                    start = count - 1;
//                }
//                for (MethodCallMethod methodCallMethod : ((Method) object).getMethodCallMethods()) {
//                    Method target = methodCallMethod.getEndMethod();
//                    Map<String, Object> targetMethod = getNodeAttribute(target);
//                    if (jsonNodes.indexOf(targetMethod) == -1) {
//                        jsonNodes.add(targetMethod);
//                        count++;
//                    }
//                    int end = jsonNodes.indexOf(targetMethod);
//                    jsonEdges.add(getNodeCollection(start, end));
//                }
//            }
//
//        }
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("nodes", jsonNodes);
//        map.put("links", jsonEdges);
//        return map;
//    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCallNodes(String version) {
        Collection<File> files = fileRepository.findFilesWithMethodByVersion(version);
        Collection<Method> methods = methodRepository.findMethodsWithCallByVersion(version);
        Collection<Method> temp = methods.stream().filter(method -> !method.getMethodCallMethods().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        return parseData.graph(files, temp, null);
    }
}
