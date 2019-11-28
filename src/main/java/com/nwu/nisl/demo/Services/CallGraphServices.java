package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.CallGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
public class CallGraphServices {
    private CallGraph callGraph;

    @Autowired
    public CallGraphServices(CallGraph callGraph){
        this.callGraph = callGraph;
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

        return callGraph.callGraph(version);

    }
}
