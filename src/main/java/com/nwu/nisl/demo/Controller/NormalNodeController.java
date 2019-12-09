package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.Process;
import com.nwu.nisl.demo.Component.ProjectInformation;
import com.nwu.nisl.demo.Services.CallGraphServices;
import com.nwu.nisl.demo.Services.NodeServices;
import com.nwu.nisl.demo.Services.StartProcessServices;
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
    private StartProcessServices startProcessServices;

    @Autowired
    public NormalNodeController(NodeServices nodeServices, CallGraphServices callGraphServices,
                                UpdateServices updateServices, ProjectInformation projectInformation, StartProcessServices startProcessServices) {
        this.nodeServices = nodeServices;
        this.callGraphServices = callGraphServices;
        this.updateServices = updateServices;
        this.projectInformation = projectInformation;
        this.startProcessServices=startProcessServices;
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

    // TODO
    // 目前：获取diff文件路径、当前项目的版本号
    // 后续考虑是否需要传入当前版本号，和之前的版本号，然后diff文件在函数里面生成
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


    //-----------------------------------Test-------------------------------------------

    @GetMapping(value = "/testCallMethod")

    public Map<String, Object> callMethod() {
        Map<String, Object> result = new HashMap<>();
        projectInformation.setAttribute("0.9.22", "0.9.22");
        // 可视化数据
        result.putAll(callGraphServices.getCallNodes("0.9.22"));
        // 项目基础数据（前端显示）
        result.putAll(projectInformation.getProjectInformation());

        return result;
    }

    @GetMapping(value = "/testCallMethod2")
    public Map<String, Object> callMethod2() {
        Map<String, Object> result = new HashMap<>();
        projectInformation.setAttribute("0.9.22", "0.9.23");
        // 可视化数据
        result.putAll(callGraphServices.getCallNodes("0.9.23"));
        // 项目基础数据（前端显示）
        result.putAll(projectInformation.getProjectInformation());

        return result;
    }


    @GetMapping(value = "/testDiff")
    public Map<String, Object> testDiff() {
        String oldVersion = "0.9.22";
        String newVersion = "0.9.23";
        Map<String, Object> result = new HashMap<>();
        projectInformation.setAttribute(oldVersion, newVersion);
        // 可视化数据
        result.putAll(updateServices.updateNodes(newVersion));
        // 项目基础数据（前端显示）
        result.putAll(projectInformation.getProjectInformation());
        return result;
    }

    //测试我们的process程序
    @GetMapping(value = "/testprocess")
    public void test() {
        try {
            startProcessServices.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
