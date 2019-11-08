package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Services.MainServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class NormalNodeController {
//    @Autowired
//    private NodeServices nodeServices;
    @Autowired
    private MainServices mainServices;

    public NormalNodeController() {
    }

    public NormalNodeController(MainServices mainServices) {
        this.mainServices=mainServices;
    }

    @GetMapping("/graph")
    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
        System.out.println("start");
        return mainServices.getallNodes(limit = 80);
    }


}
