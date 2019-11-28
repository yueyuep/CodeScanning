package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Services.LevelAnalyse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Create by lp on 2019/11/20
 */
@RestController
@RequestMapping(value = "/analyse")
public class AnalyseLevel {
    private LevelAnalyse levelAnalyse;

    @Autowired
    public AnalyseLevel(LevelAnalyse levelAnalyse) {
        this.levelAnalyse = levelAnalyse;
    }

    @GetMapping(value = "/analyse/test")
    public Map<String, Object> level(@RequestParam(value = "version") String version,
                                     @RequestParam(value = "level") int level) {
        //返回<diifType，List<diffMethod>
        return levelAnalyse.getLevelNodes(version, level);
    }


    //-----------------------------------Test-------------------------------------------

    @GetMapping(value = "/test")
    public Map<String, Object> level() {
        return levelAnalyse.getLevelNodes("0.9.23", 1);
    }


}
