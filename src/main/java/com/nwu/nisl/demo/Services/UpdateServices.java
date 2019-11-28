package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.CallGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private CallGraph callGraph;


    /**
     * @Author Kangaroo
     * @Description 返回新版本相对于旧版本的文件函数调用图 （且将不同的类型的变化标注出来）
     * @Date 2019/11/16 9:19
     * @Param [path, version]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @Transactional(readOnly = true)
    public Map<String,Object> updateNodes(String version){
        return callGraph.callGraphWithDiff(version);
    }

}
