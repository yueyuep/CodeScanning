package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.ProjectInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Controller
 * @ClassName: InfromationController
 * @Author: Kangaroo
 * @Description: 返回项目基本信息
 * @Date: 2019/11/27 21:24
 * @Version: 1.0
 */
@RestController
@RequestMapping("/project")
public class InformationController {
    private ProjectInformation projectInformation;

    @Autowired
    public InformationController(ProjectInformation projectInformation) {
        this.projectInformation = projectInformation;
    }

    @GetMapping("/information")
    public Map<String, Object> projectInformation(@RequestParam(value = "version") String version) {

        projectInformation.setAttribute(version, version);
        return projectInformation.getProjectInformation();
    }

    @GetMapping("/informationCompare")
    public Map<String, Object> projectInformation(@RequestParam(value = "oldVersion") String oldVersion,
                                           @RequestParam(value = "newVersion") String newVersion) {

        projectInformation.setAttribute(oldVersion, newVersion);
        return projectInformation.getProjectInformation();
    }

    //-----------------------------------Test-------------------------------------------

    @GetMapping("/testInformation")
    public Map<String, Object> test1() {
        String version = "0.9.22";
        projectInformation.setAttribute(version, version);
        return projectInformation.getProjectInformation();
    }

    @GetMapping("/testInformationCompare")
    public Map<String, Object> test2( ) {
        String oldVersion = "0.9.22";
        String newVersion = "0.9.23";

        projectInformation.setAttribute(oldVersion, newVersion);
        return projectInformation.getProjectInformation();
    }

}
