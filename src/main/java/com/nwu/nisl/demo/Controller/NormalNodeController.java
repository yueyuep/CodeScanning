package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Services.CallGraphServices;
import com.nwu.nisl.demo.Services.MainServices;
import com.nwu.nisl.demo.Services.NodeServices;
import org.python.antlr.ast.Str;
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
    private MainServices mainServices;
    @Autowired
    private CallGraphServices callGraphServices;

    public NormalNodeController() {
    }

    public NormalNodeController(MainServices mainServices) {
        this.mainServices = mainServices;
    }

//    @GetMapping("/grapha")
//    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
//        return mainServices.getAllNodes(limit = 80);
//    }

    @GetMapping(value = "/callMethod")
    public Map<String, Object> callMethod(@RequestParam(value = "version") String version) {
        return callGraphServices.getCallNodes(version);
    }


    @GetMapping(value = "/node")
    public Map<String, Object> node(@RequestParam(value = "fileMethodName") String fileMethodName,
                                    @RequestParam(value = "num") Integer num,
                                    @RequestParam(value = "version") String version) {
        return nodeServices.findAllByFileMethodName(fileMethodName, num, version);
    }
}
