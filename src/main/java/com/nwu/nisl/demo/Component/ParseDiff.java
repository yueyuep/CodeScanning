package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Component
 * @ClassName: ParseDiff
 * @Author: Kangaroo
 * @Description: 解析项目间利用相似性进行比较的结果
 * @Date: 2019/11/14 20:02
 * @Version: 1.0
 */
@Component
public class ParseDiff {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MethodRepository methodRepository;

    public ParseDiff() {}

    /**
     * @Author Kangaroo
     * @Description 根据参数字段 (版本号，fileMethodName)，返回数据库中对应的函数节点对象
     * @Date 2019/11/16 9:29
     * @Param [normalDiff]
     * @return java.util.Collection<com.nwu.nisl.demo.Entity.Method>
     **/
    public Collection<Method> getMethodInstance(Map<String, List<String>> normalDiff) {
        Collection<Method> methods = new ArrayList<>();
        for (String version: normalDiff.keySet()){
            for (String fileMethodName: normalDiff.get(version)){
                methods.add(methodRepository.findMethodByFileMethodNameAndVersion(fileMethodName, version));
            }
        }
        return methods;
    }

    /**
     * @Author Kangaroo
     * @Description 根据参数字段 (版本号，fileName)，返回数据库中对应的文件节点对象
     * @Date 2019/11/16 9:32
     * @Param [diff]
     * @return java.util.Collection<com.nwu.nisl.demo.Entity.File>
     **/
    public Collection<File> getFileInstance(Map<String, List<String>> diff) {
        Collection<File> files = new ArrayList<>();
        for (String version: diff.keySet()) {
            for (String fileName: diff.get(version)) {
                files.add(fileRepository.findFileByFileNameAndVersion(fileName, version));
            }
        }
        return files;
    }


    /**
     * @Author Kangaroo
     * @Description 解析diff文件中 adddiff和deleteddiff 行，返回其在数据库中对应的文件、函数节点对象
     * @Date 2019/11/16 9:37
     * @Param [diff]
     * @return java.util.List<java.lang.Object>
     **/
    public List<Object> getFileAndMethodInstance(Map<String, Map<String, List<String>>> diff) {
        List<Object> res = new ArrayList<>();

        Map<String, List<String>> fileDiff = new HashMap<>();
        Map<String, List<String>> methodDiff = new HashMap<>();

        for (String version: diff.keySet()){
            for (String flag: diff.get(version).keySet()) {
                if (flag.equals("file")) {
                    if (!fileDiff.keySet().contains(version)) {
                        fileDiff.put(version, new ArrayList<>());
                    }
                    fileDiff.get(version).addAll(diff.get(version).get(flag));
                } else if (flag.equals("method")) {
                    if (!methodDiff.keySet().contains(version)) {
                        methodDiff.put(version, new ArrayList<>());
                    }
                    methodDiff.get(version).addAll(diff.get(version).get(flag));
                }
            }
        }
        res.add(getFileInstance(fileDiff));
        res.add(getMethodInstance(methodDiff));

        return res;
    }

}
