package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.ScanGraph;
import com.nwu.nisl.demo.Services.LevelAnalyseServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Create by lp on 2019/11/20
 */
@RestController
@RequestMapping(value = "/analyse")
public class AnalyseLevel {
    private LevelAnalyseServices levelAnalyseServices;
    private ScanGraph scanGraph;

    @Autowired
    public AnalyseLevel(LevelAnalyseServices levelAnalyseServices, ScanGraph scanGraph) {
        this.levelAnalyseServices = levelAnalyseServices;
        this.scanGraph = scanGraph;
    }

    @GetMapping(value = "/connectLevelDiff")
    public Map<String, Object> level(@RequestParam(value = "version") String version,
                                     @RequestParam(value = "level") int level) {
        //返回<diifType，List<diffMethod>
        return levelAnalyseServices.getPartNodes(version, level);
    }




}
