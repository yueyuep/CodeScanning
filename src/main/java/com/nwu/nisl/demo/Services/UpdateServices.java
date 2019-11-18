package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.DiffNode;
import com.nwu.nisl.demo.Component.ParseData;
import com.nwu.nisl.demo.Component.ParseDiff;
import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.Node;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Services
 * @ClassName: UpdateServices
 * @Author: Kangaroo
 * @Description: 获取版本变化后待可视化的数据
 * @Date: 2019/11/14 20:52
 * @Version: 1.0
 */
@Service
public class UpdateServices {
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


    /**
     * @Author Kangaroo
     * @Description 返回新版本相对于旧版本的文件函数调用图 （且将不同的类型的变化标注出来）
     * @Date 2019/11/16 9:19
     * @Param [path, version]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @Transactional(readOnly = true)
    public Map<String,Object> updateNodes(String path, String version){
        diffNode.setPath(path);

        // list 包含的类型， 不同类型保存的顺序和后面代码逻辑关系十分密切
        // Map<String, Map<String, List<String>>> addDiff
        // Map<String, Map<String, List<String>>> normalDiff
        // Map<String, Map<String, List<String>>> deletedDiff
        List<Object> list = diffNode.diffToList();

        List<Collection<File>> diffFiles = new ArrayList<>();
        List<Collection<Method>> diffMethods = new ArrayList<>();
        for (Object object: list) {
            List<Object> result = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) object);
            diffFiles.add((Collection<File>) result.get(0));
            diffMethods.add((Collection<Method>) result.get(1));
        }

        // 文件函数调用图的所有节点
        Collection<File> allFiles = fileRepository.findFilesWithMethodByVersion(version);
        Collection<Method> temp = methodRepository.findMethodsWithCallByVersion(version);
        Collection<Method> allMethods = temp.stream().filter(method -> !method.getMethodCallMethods().isEmpty()).collect(Collectors.toCollection(ArrayList::new));

        // Files: allFiles, addFiles, normalFiles (虽然这个值为null，为了保持代码复用性，简洁), deletedFiles;
        // Methods: allMethods, addMethods, normalMethods, deletedMethods
        List<Collection<File>> files = new ArrayList<>();
        files.add(allFiles);
        diffFiles.forEach(tempDiffFiles -> files.add(tempDiffFiles));
        List<Collection<Method>> methods = new ArrayList<>();
        methods.add(allMethods);
        diffMethods.forEach(tempDiffMethods -> methods.add(tempDiffMethods));
        List<Collection<Node>> nodes = new ArrayList<>();

        return parseData.graph(version, files, methods, nodes, true, true, false, false, false);
    }

}
