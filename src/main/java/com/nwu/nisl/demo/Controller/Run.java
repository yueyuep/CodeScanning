package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.Message;
import com.nwu.nisl.demo.Component.Process;
import com.nwu.nisl.demo.Component.Unzip;
import com.nwu.nisl.demo.Component.Utils;
import com.nwu.nisl.demo.Entity.HasMethod;
import com.nwu.nisl.demo.pytools.CallPython;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.HashMap;

/**
 * Create by yueyue on 2020/12/10
 */
@Controller
@RequestMapping("/")
public class Run {
    Logger logger = LoggerFactory.getLogger(Run.class);
    @Value("${com.nwu.nisl.data.source}")
    private String path;

    private Process process;
    private CallPython callPython;
    private Utils utils;
    String savepath = "";
    String oldversion;
    String newversion;

    public Run(Process process, CallPython callPython, Utils utils) {
        this.process = process;
        this.callPython = callPython;
        this.utils = utils;
    }

    @PostMapping("start/data/upload")
    @ResponseBody
    public JSONObject upload(@RequestParam("file") MultipartFile file) {
        path = path + File.separator;
        File sfile = new File(path);
        JSONObject jsonObject = new JSONObject();
        if (!sfile.exists()) {
            sfile.mkdir();

        }
        File dir = new File(path + file.getOriginalFilename());
        try {
            file.transferTo(dir);
            jsonObject.put("res", 200);
            /*获取目录*/
            String dic = file.getOriginalFilename().replace(".zip", "");
            /*获取版本号*/
            if (dic.contains("old")) {
                oldversion = dic.replace("old", "");
                /*解压上传的文件*/
                savepath = path + oldversion;
                Unzip.unZip(dir, savepath);
                logger.info("上传旧项目版本" + oldversion);

            } else if (dic.contains("new")) {
                newversion = dic.replace("new", "");
                /*解压上传的文件*/
                savepath = path + newversion;
                Unzip.unZip(dir, savepath);
                logger.info("上传新项目版本" + newversion);
            } else {
                /**/
            }
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("res", 100);
            return jsonObject;
        }
    }

    /*启动4个处理过程跳转主页面*/
    @GetMapping(value = "start/data/parse")
    @ResponseBody
    public HashMap<String, Object> parse() {
        HashMap<String, Object> msg = new HashMap<>();
        if (oldversion.equals("") || newversion.equals("")) {
            logger.info("请上传完整版本数据");
            msg.put("status", "needile");
            return msg;
        }
        try {
            /*解析*/
            process.first(oldversion, newversion);
            /*json2csv*/
            process.second(oldversion, newversion);
            /*数据存储*/
            process.third();
            /*相似性分析*/
            callPython.execute(oldversion, newversion);
        } catch (Exception e) {
            e.printStackTrace();
            msg.put("status", "exception");
            return msg;

        }
        msg.put("status", "succes");
        msg.put("oldversion", oldversion);
        msg.put("newversion", newversion);
        return msg;

    }

    @GetMapping("start/data/show")
    @ResponseBody
    public HashMap<String, Object> show() {
        logger.info("页面跳转");
        HashMap<String, Object> msg = new HashMap<>();
        /*状态信息*/
        msg.put("status", "succes");
        return msg;
    }
}
