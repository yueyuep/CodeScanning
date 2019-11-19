package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.ProjectInformation;
import com.nwu.nisl.demo.Services.CallGraphServices;
import com.nwu.nisl.demo.Services.NodeServices;
import com.nwu.nisl.demo.Services.UpdateServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private ProjectInformation projectInformation;

    public NormalNodeController() {
    }


    @GetMapping(value = "/callMethod")
    public List<Object> callMethod(@RequestParam(value = "oldVersion") String oldVersion,
                                   @RequestParam(value = "newVersion") String newVersion,
                                   @RequestParam(value = "diffPath") String diffPath){
        List<Object> result = new ArrayList<>();
        projectInformation.setAttribute(oldVersion, newVersion, diffPath);
        // 可视化数据
        result.add(callGraphServices.getCallNodes(newVersion));
        // 项目基础数据（前端显示）
        result.addAll(projectInformation.getProjectInformation());

        return result;
    }

    @GetMapping(value = "/testCallMethod")
    public List<Object> callMethod(){
        List<Object> result = new ArrayList<>();
        projectInformation.setAttribute("0.9.22", "0.9.22", "");
        // 可视化数据
        result.add(callGraphServices.getCallNodes("0.9.22"));
        // 项目基础数据（前端显示）
        result.addAll(projectInformation.getProjectInformation());

        return result;
    }

    @GetMapping(value = "/testCallMethod2")
    public List<Object> callMethod2(){
        List<Object> result = new ArrayList<>();
        projectInformation.setAttribute("0.9.22", "0.9.23",
                "src/main/java/com/nwu/nisl/demo/Data/result.txt");
        // 可视化数据
        result.add(callGraphServices.getCallNodes("0.9.23"));
        // 项目基础数据（前端显示）
        result.addAll(projectInformation.getProjectInformation());

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
    public List<Object> diff(@RequestParam(value = "oldVersion") String oldVersion,
                             @RequestParam(value = "newVersion") String newVersion,
                             @RequestParam(value = "diffPath") String diffPath){
        List<Object> result = new ArrayList<>();
        projectInformation.setAttribute(oldVersion, newVersion, diffPath);
        // 可视化数据
        result.add(updateServices.updateNodes(diffPath, newVersion));
        // 项目基础数据（前端显示）
        result.addAll(projectInformation.getProjectInformation());
        return result;
    }

    @GetMapping(value = "/testDiff")
    public List<Object> testDiff(){
        String oldVersion = "0.9.22";
        String newVersion = "0.9.23";
        String diffPath = "src/main/java/com/nwu/nisl/demo/Data/result.txt";
        List<Object> result = new ArrayList<>();
        projectInformation.setAttribute(oldVersion, newVersion, diffPath);
        // 可视化数据
        result.add(updateServices.updateNodes(diffPath, newVersion));
        // 项目基础数据（前端显示）
        result.addAll(projectInformation.getProjectInformation());
        return result;
    }

}
