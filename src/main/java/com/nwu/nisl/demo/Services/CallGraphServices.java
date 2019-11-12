package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.PareJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//只显示callgraph，在数据库中读取的
@Service
public class CallGraphServices {
    //配置路径当中
    @Autowired

    public CallGraphServices(PareJson pareJson){

    }

}
