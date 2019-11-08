package com.nwu.nisl.demo.Component;

import com.google.gson.Gson;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
//负责解析callgraph json数据
public class PareJson {
    @Value("${com.nwu.nisl.demo.callgraph}")
    private String url;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    MethodRepository methodRepository;
    @Autowired
    Utils utils;

    public PareJson() {
    }

    public List<HashMap<String, Object>> Pare() {
        List<HashMap<String, Object>> lineData = new ArrayList<>();
        String line;
        File file = new File(url);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                Gson gson = new Gson();
                HashMap<String, Object> map = new HashMap<>();
                //反射机制
                String string = line.toString();
                lineData.add(gson.fromJson(string, map.getClass()));


            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return JsonToD3(lineData);
    }

    public List<HashMap<String, Object>> JsonToD3(List<HashMap<String, Object>> lineData) {
        List<HashMap<String, Object>> nodes = new ArrayList<>();
        List<HashMap<String, Object>> edges = new ArrayList<>();
        int count = 0;
        for (HashMap<String, Object> line : lineData) {

            try {
                String linekey = line.keySet().iterator().next();
                Object linevalue = line.get(linekey);
                if (linevalue != null) {
                    //文件：hasMethod关系
                    if (linekey.contains(".java")) {
                        //数据库读取文件的信息，以及被调用函数的信息
                        // TODO 需要函数名和版本号,版本号取上面文件的版本号

                        com.nwu.nisl.demo.Entity.File file = fileRepository.findByFileName(linekey);
                        Map<String, Object> file_info = utils.getNodeAttribute(file);


                        // TODO 依次遍历文件所包含函数结点


                    }
                    //函数：callMethod
                    else {
                        //数据库读取函数的的具体信息
                        // TODO 需要函数名和版本号,版本号取上面文件的版本号
                        String methodName = linekey;
                        //TODO 版本号
                        String version = null;
                        Method method = methodRepository.findByfileMethodNameAndVersion(linekey, version);
                        Map<String, Object> file_info = utils.getNodeAttribute(method);

                        // TODO 依次遍历函数所调用函数结点


                    }
                }


            } catch (Exception e) {
                System.out.println("There is no key");
            }

        }

        return null;
    }


}
