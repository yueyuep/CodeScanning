package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.Process;
import com.nwu.nisl.demo.Component.FileUtil;
import com.nwu.nisl.demo.Component.Utils;
import com.nwu.nisl.demo.pytools.CallPython;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
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
    @Value("${user.dir}")
    private String dir;

    private Process process;
    private CallPython callPython;
    private Utils utils;
    String savepath = "";
    String oldversion = "";
    String newversion = "";

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
                FileUtil.unZip(dir, savepath);
                logger.info("上传旧项目版本" + oldversion);

            } else if (dic.contains("new")) {
                newversion = dic.replace("new", "");
                /*解压上传的文件*/
                savepath = path + newversion;
                FileUtil.unZip(dir, savepath);
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
        if ("".equals(oldversion) || "".equals(newversion)) {
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

    @GetMapping("start/data/download")
    @ResponseBody
    public String downloadFile(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        String fileName = "res.txt";// 文件名
        File file = new File(dir + File.separator + fileName);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; fileName=" + fileName + ";filename*=utf-8''" + URLEncoder.encode(fileName, "UTF-8"));
        String res=FileUtil.download(file, response, request);
        return res;
    }
}
