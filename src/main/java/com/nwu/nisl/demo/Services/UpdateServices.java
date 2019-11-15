package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.DiffNode;
import com.nwu.nisl.demo.Component.ParseDiff;
import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    private DiffNode diffNode;
    @Autowired
    private ParseDiff parseDiff;


    @Transactional(readOnly = true)
    public Map<String,Object> updateNodes(String path, String version){
        diffNode.setPath(path);
        List<Object> list = diffNode.ToList();

        Collection<File> files = parseDiff.getFileInstance((Map<String, List<String>>) list.get(0));
        Collection<Method> methods = new ArrayList<>();
        for (Object object: list.subList(1, list.size())) {
            List<Object> res = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) object);
            files.addAll((Collection<File>) res.get(0));
            methods.addAll((Collection<Method>) res.get(1));
        }

        return null;
    }

}
