package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Services.MainServices;
import com.nwu.nisl.demo.Services.NodeServices;
import org.python.antlr.ast.Str;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class FileController {
    //    @Autowired
//    private NodeServices nodeServices;
    @Autowired
    private MainServices mainServices;
    @Autowired
    private NodeServices nodeServices;

    public FileController() {
    }

    public FileController(MainServices mainServices) {
        this.mainServices = mainServices;
    }

    //组合映射的方式==@GetMapping(value="graph",method=RequestMethod.GET)
    @GetMapping("/graph")
    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
        System.out.println("start");
        return mainServices.getallNodes(limit = 80);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    // 获取前台节点的函数名和版本号
    public Map<String, Object> info(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String fileMethodName = httpServletRequest.getParameter("fileMethodName");
        String version = httpServletRequest.getParameter("version");
        //去除fieMEthodName后面的的数字
        String end = fileMethodName.split("-")[fileMethodName.split("-").length - 1];
        fileMethodName = fileMethodName.substring(0, fileMethodName.length() - end.length());
        //System.out.println("测试点");
        Map<String, Object> result = nodeServices.findAllByFileMethodName(fileMethodName, version);
        if (result.size() != 0) {
            //数据查找成功
            result.put("result", "SUCCESS");

        } else {
            result.put("result", "ERROR");
        }

        return result;

    }


}
