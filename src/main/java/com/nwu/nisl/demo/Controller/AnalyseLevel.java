package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Services.LevelAnalyse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Create by lp on 2019/11/20
 */
@Controller
@RequestMapping(value = "/analyse")
public class AnalyseLevel {
    private LevelAnalyse levelAnalyse;

    @Autowired
    public AnalyseLevel(LevelAnalyse levelAnalyse) {
        this.levelAnalyse = levelAnalyse;
    }

    @GetMapping(value = "/analyse/test")
    public Map<String, List<Object>> level(@RequestParam(value = "level") String level) {
        int pLevel = 0;
        if (level.equals("One"))
            pLevel = 1;
        //返回<diifType，List<diffMethod>
        return levelAnalyse.initInstance(1);

    }


}
