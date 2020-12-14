package com.nwu.nisl.demo.Controller;

import com.nwu.nisl.demo.Component.Process;
import com.nwu.nisl.demo.Component.Utils;
import com.nwu.nisl.demo.pytools.CallPython;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lp on 2019/12/10
 */
@Controller
@RequestMapping(value = "/start")
public class Router {
    @Value("${web.socket.ip}")
    private String ip;


    @GetMapping(value = "/main")
    public String main(Model model) {
        //启动start页面
        model.addAttribute("ipConfig", ip);
        return "main";
    }


}
