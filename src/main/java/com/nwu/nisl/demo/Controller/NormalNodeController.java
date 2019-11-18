package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Services.CallGraphServices;
import com.nwu.nisl.demo.Services.NodeServices;
import com.nwu.nisl.demo.Services.UpdateServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class NormalNodeController {
    @Autowired
    private NodeServices nodeServices;
    @Autowired
    private CallGraphServices callGraphServices;
    @Autowired
    private UpdateServices updateServices;

    public NormalNodeController() {
    }

//    @GetMapping("/grapha")
//    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
//        return mainServices.getAllNodes(limit = 80);
//    }

    @GetMapping(value = "/callMethod")
    public Map<String, Object> callMethod(@RequestParam(value = "version") String version){
        return callGraphServices.getCallNodes(version);
    }

    @GetMapping(value = "/testCallMethod")
    public Map<String, Object> callMethod(){
        return callGraphServices.getCallNodes("0.9.23");
    }

    @GetMapping(value = "/node")
    public Map<String, Object> node(@RequestParam(value = "fileMethodName") String fileMethodName,
                                    @RequestParam(value = "num") Integer num,
                                    @RequestParam(value = "version") String version) {
        return nodeServices.findAllByFileMethodName(fileMethodName, num, version);
    }

    // TODO
    // 目前：获取diff文件路径、当前项目的版本号
    // 后续考虑是否需要传入当前版本号，和之前的版本号，然后diff文件在函数里面生成
    @GetMapping(value = "/diff")
    public Map<String, Object> diff(@RequestParam(value = "path") String path,
                                    @RequestParam(value = "version") String version){
        return updateServices.updateNodes(path, version);
    }

    @GetMapping(value = "/testDiff")
    public Map<String, Object> testDiff(){
        return updateServices.updateNodes("src\\main\\java\\com\\nwu\\nisl\\demo\\Data\\result.txt", "0.9.23");
    }


}
