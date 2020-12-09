package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.Process;
import com.nwu.nisl.demo.Component.ProjectInformation;
import com.nwu.nisl.demo.Services.CallGraphServices;
import com.nwu.nisl.demo.Services.NodeServices;
import com.nwu.nisl.demo.Services.UpdateServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class NormalNodeController {
    private NodeServices nodeServices;
    private CallGraphServices callGraphServices;
    private UpdateServices updateServices;
    private ProjectInformation projectInformation;
    private Process process;

    @Autowired
    public NormalNodeController(NodeServices nodeServices, CallGraphServices callGraphServices,
                                UpdateServices updateServices, ProjectInformation projectInformation, Process process) {
        this.nodeServices = nodeServices;
        this.callGraphServices = callGraphServices;
        this.updateServices = updateServices;
        this.projectInformation = projectInformation;
        this.process = process;
    }


    @GetMapping(value = "/callMethod")
    public Map<String, Object> callMethod(@RequestParam(value = "version") String version) {
        Map<String, Object> result = new HashMap<>();
        projectInformation.setAttribute(version, version);
        // 可视化数据
        result.putAll(callGraphServices.getCallNodes(version));
        // 项目基础数据（前端显示）
        result.putAll(projectInformation.getProjectInformation());

        return result;
    }

    @GetMapping(value = "/node")
    public Map<String, Object> node(@RequestParam(value = "fileMethodName") String fileMethodName,
                                    @RequestParam(value = "num") Integer num,
                                    @RequestParam(value = "version") String version) {
        return nodeServices.findAllByFileMethodName(fileMethodName, num, version);
    }

    // 获取diff文件路径、当前项目的版本号
    @GetMapping(value = "/diff")
    public Map<String, Object> diff(@RequestParam(value = "oldVersion") String oldVersion,
                                    @RequestParam(value = "newVersion") String newVersion) {
        Map<String, Object> result = new HashMap<>();
        projectInformation.setAttribute(oldVersion, newVersion);
        // 可视化数据
        result.putAll(updateServices.updateNodes(newVersion));
        // 项目基础数据（前端显示）
        result.putAll(projectInformation.getProjectInformation());
        return result;
    }

}
