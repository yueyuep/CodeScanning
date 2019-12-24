package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:lp on 2019/12/2 16:43
 * @Param:
 * @return:
 * @Description: 解析diffResult结果，3种变化类型。每一种分别进行标注
 */
@Component
public class ScanGraph {
    private DiffNode diffNode;
    private ParseDiff parseDiff;
    private MethodRepository methodRepository;
    private FileRepository fileRepository;

    @Autowired
    public ScanGraph(DiffNode diffNode, ParseDiff parseDiff, MethodRepository methodRepository, FileRepository fileRepository) {
        this.diffNode = diffNode;
        this.parseDiff = parseDiff;
        this.methodRepository = methodRepository;
        this.fileRepository = fileRepository;
    }

    public Map<String, Map<String, List<Object>>> initInstance(int level) {
        // TODO
        // 1. 传入diff文件所在位置
        // 2. diff文件一直保存在固定的位置
        //diffNode.setPath(path);

        Map<String, Object> diffMap = diffNode.parseDiff();
        //得到不同变化类型的文件函数节点，供我们进行查找
        // diffMap: {"difftype":"",<version,flag,[method/file]>} 其中flag标记我们的变化部分是函数节点还是文件节点
        // TODO @1:直接把数据传送到前台，在本地数据的基础上添加标识符,不需要再重新在读取数据
        // TODO @2:对这部分数据直接在在数据库中进行更改，type、change属性进行更改
        // TODO 需要比较两种的执行效率。
        return handle(level, diffMap);
    }

    public Map<String, Collection<Object>> levelNode(int level) {
        Map<String, Map<String, List<Object>>> levelMap = initInstance(level);
        Map<String, Collection<Object>> res = new HashMap<>();
        res.put(NodeType.FILE, new ArrayList<>());
        res.put(NodeType.METHOD, new ArrayList<>());

        for (Map<String, List<Object>> map : levelMap.values()) {
            for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
                res.get(entry.getKey()).addAll(entry.getValue());
            }
        }

        return res;
    }

    public Map<String, Map<String, List<Object>>> handle(int level, Map<String, Object> diffMap) {
        // 变化类型， 文件/函数  对象
        Map<String, Map<String, List<Object>>> diffCollections = new HashMap<>();
        for (Map.Entry<String, Object> entry : diffMap.entrySet()) {
            Map<String, Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get(entry.getKey()));
            Map<String, List<Object>> adjFileMethodCollections = getAdjacent(level, fileMethodCollections);
            diffCollections.put(entry.getKey(), adjFileMethodCollections);
        }
        return diffCollections;
    }

    // TODO
    // level扫描到的节点，不应该包含变化的节点
    //根据层次寻找改变函数被其他节点调用的地方，给与每层节点添加层次关系
    /*
     * 0 层：代表变化的层次
     * 1层:代表邻层
     * 。。依次类比
     * */
    public Map<String, List<Object>> getAdjacent(int level, Map<String, Object> fileMethodCollections) {
        //根据file、method求出这些所在level的关系节点
        Collection<Method> tmpmethod = new ArrayList<>();
        Collection<File> tmpfile = new ArrayList<>();
        Collection<File> connectFiles = (Collection<File>) fileMethodCollections.get(NodeType.FILE);
        Collection<Method> connectMethods = (Collection<Method>) fileMethodCollections.get(NodeType.METHOD);
        HashMap<Long, File> mapconnectFiles = new HashMap<>();
        HashMap<Long, Method> mapconnectMethods = new HashMap<>();
        //haspMap
        connectFiles.forEach(file -> mapconnectFiles.put(file.getId(), file));
        connectMethods.forEach(method -> mapconnectMethods.put(method.getId(), method));
        //setLevel==0
        mapconnectFiles.values().forEach(file -> file.setLevel(0));
        mapconnectMethods.values().forEach(method -> method.setLevel(0));


        // fileMethodCollections:diffType-> <fileDiff,methodDiff>
        for (int i = 0; i < level; i++) {
            //clear cache
            tmpmethod.clear();
            tmpfile.clear();
            for (File file : mapconnectFiles.values()) {
                //文件的变化只有删除、增加,通过查询结果来显示。
                if (file.getMethods().size() != 0) {

                    tmpmethod.addAll(file.getMethods().stream().map(hasMethod -> hasMethod.getEndMethod()).collect(Collectors.toList()));
                    //以hashmap方式存储
                }

                //需要过滤掉已经存在的节点
                //目前只是添加变化的文件
            }
            for (Method method : mapconnectMethods.values()) {
                //寻找被人调用他的函数节点。
                String version = method.getVersion();
                String fileMethodName = method.getFileMethodName();
                tmpmethod.addAll(methodRepository.findConnect(version, fileMethodName));
                tmpfile.addAll(fileRepository.findConnect(version, fileMethodName));

            }
            for (File file : tmpfile) {
                if (!mapconnectFiles.keySet().contains(file.getId())) {
                    file = copyfile(file);
                    file.setLevel(i + 1);
                    mapconnectFiles.put(file.getId(), file);
                }


            }
            for (Method method : tmpmethod) {

                if (!mapconnectMethods.keySet().contains(method.getId())) {
                    method = copymethod(method);
                    method.setLevel(i + 1);
                    mapconnectMethods.put(method.getId(), method);
                }

            }

        }

        Map<String, List<Object>> adjFileMethod = new HashMap<>();
        adjFileMethod.put(NodeType.FILE, new ArrayList<>());
        adjFileMethod.get(NodeType.FILE).addAll(mapconnectFiles.values());
        adjFileMethod.put(NodeType.METHOD, new ArrayList<>());
        adjFileMethod.get(NodeType.METHOD).addAll(mapconnectMethods.values());
        return adjFileMethod;
    }

    public Collection<Method> setMethodLevel(Collection<Method> methods, int level) {
        for (Method method : methods) {
            //如果是新生成的节点，则给当前节点添加当前level
            method.setLevel(level);
        }
        return methods;
    }

    public Collection<File> setFileLevel(Collection<File> files, int level) {
        for (File file : files) {
            //如果是新生成的节点，则给当前节点添加当前level
            file.setLevel(level);
        }
        return files;
    }
    //file、method的复制，深复制

    public File copyfile(File file) {
        File file1 = new File();
        file1.setLevel(file.getLevel());
        file1.setFileName(file.getFileName());
        file1.setVersion(file.getVersion());
        file1.setMethods(file.getMethods());
        file1.setNodeType(file.getNodeType());
        file1.setId(file.getId());
        return file1;
    }

    public Method copymethod(Method method) {
        Method method1 = new Method();
        method1.setLevel(method.getLevel());
        method1.setVersion(method.getVersion());
        method1.setFileMethodName(method.getFileMethodName());
        method1.setHasNodes(method.getHasNodes());
        method1.setNodeType(method.getNodeType());
        method1.setMethodCallMethods(method.getMethodCallMethods());
        method1.setNum(method.getNum());
        method1.setId(method.getId());
        return method1;

    }


}
