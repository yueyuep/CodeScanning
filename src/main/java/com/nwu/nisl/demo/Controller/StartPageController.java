package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.Process;
import com.nwu.nisl.demo.Component.Utils;
import com.nwu.nisl.demo.pytools.CallPython;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Create by lp on 2019/12/10
 */
@Controller
@RequestMapping(value = "/start")
public class StartPageController {
    private Process process;
    private CallPython callPython;
    private Utils utils;

    @Autowired
    public StartPageController(Process process, CallPython callPython, Utils utils) {
        this.process = process;
        this.callPython = callPython;
        this.utils = utils;

    }

    @GetMapping(value = "/main")
    public String main() {
        //启动start页面
        return "start2";


    }


    @GetMapping(value = "/stage1")
    @ResponseBody
    public Map<String, Object> stage1(@RequestParam("oldversion") String oldversion, @RequestParam("newversion") String newversion) {
        //调用stage1,解析我们的java数据
        Map<String, Object> response = new HashMap<>();
        if (!(utils.exisversion(oldversion, true) && utils.exisversion(newversion, true))) {
            response.put("reponse", "versionError");
            return response;
        }

        try {
            process.first(oldversion, newversion);
        } catch (Exception e) {
            response.put("reponse", "error");
            return response;

        }
        response.put("reponse", "succs");
        return response;

    }

    @GetMapping(value = "/stage2")
    @ResponseBody
    public Map<String, Object> stage2(@RequestParam("oldversion") String oldversion, @RequestParam("newversion") String newversion) {
        //调用stage2 将我们的图数据存储成csv格式
        Map<String, Object> response = new HashMap<>();
        if (!(utils.exisversion(oldversion, true) && utils.exisversion(newversion, true))) {
            response.put("reponse", "versionError");
            return response;
        }
        try {

            process.second(oldversion, newversion);
        } catch (Exception e) {
            response.put("reponse", "error");
            return response;

        }
        response.put("reponse", "succs");
        return response;


    }

    @GetMapping(value = "/stage3")
    @ResponseBody
    public Map<String, Object> stage3(@RequestParam("oldversion") String oldversion, @RequestParam("newversion") String newversion) {
        //不处理参数
        //调用stage3 csv数据格式批量存储到数据库中，开启数据库服务
        Map<String, Object> response = new HashMap<>();
        try {
            process.third();
        } catch (Exception e) {
            response.put("reponse", "error");
            return response;

        }
        response.put("reponse", "succs");
        return response;


    }

    @GetMapping(value = "/stage4")
    @ResponseBody
    public Map<String, Object> stage4(@RequestParam("oldversion") String oldversion, @RequestParam("newversion") String newversion) {
        //调用stage4 生成我们的相似分析结果，供后面的结果展示

        Map<String, Object> response = new HashMap<>();
        try {
            callPython.execute(oldversion, newversion);
        } catch (Exception e) {
            response.put("reponse", "error");
            return response;

        }
        response.put("reponse", "succs");
        return response;

    }

    @GetMapping(value = "/stage5")
    @ResponseBody
    public Map<String, Object> jump(@RequestParam("oldversion") String oldversion, @RequestParam("newversion") String newversion) {
        //全部处理完成，跳转到index.html页面
        ///不需要参数，只是触发调转页面
        Map<String, Object> response = new HashMap<>();
        return response;

    }


}
