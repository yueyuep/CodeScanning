package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.ParseData;
import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class CallGraphServices {
    //配置路径当中
    @Autowired
    private ParseData parseData;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MethodRepository methodRepository;

    public CallGraphServices(){

    }

    /**
     * @Author Kangaroo
     * @Description 返回项目的文件函数调用图 （只包含文件节点和函数节点）
     * @Date 2019/11/16 9:17
     * @Param [version]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @Transactional(readOnly = true)
    public Map<String, Object> getCallNodes(String version) {
        Collection<File> files = fileRepository.findFilesWithMethodByVersion(version);
        Collection<Method> methods = methodRepository.findMethodsWithCallByVersion(version);
        Collection<Method> temp = methods.stream().filter(method -> !method.getMethodCallMethods().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        return parseData.graph(version, files, temp, null);
    }
}
