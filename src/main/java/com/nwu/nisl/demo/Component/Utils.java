package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.Node;
import org.python.antlr.ast.Str;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;



@Component
public class Utils {

    public Utils() {
    }

    /**
     * @Author Kangaroo
     * @Description 根据 object 的类型，返回相应的属性
     * @Date 2019/11/14 15:32
     * @Param [object]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    public Map<String, Object> getNodeAttribute(Object object) {
        Map<String, Object> map = new HashMap<>();
        if (object instanceof Node) {

            map.put("fileMethodName", ((Node) object).getFileMethodName());
            map.put("version", ((Node) object).getVersion());
            map.put("attribute", ((Node) object).getAttribute());
            map.put("nodeType", ((Node) object).getNodeType());

        } else if (object instanceof Method) {
            map.put("fileMethodName", ((Method) object).getFileMethodName());
            map.put("version", ((Method) object).getVersion());
            map.put("nodeType", ((Method) object).getNodeType());
            //包含的文件内容先不显示

        } else if (object instanceof File) {
            //包含的文件内容先不显示
            map.put("fileName", ((File) object).getFileName());
            map.put("version", ((File) object).getVersion());
            map.put("nodeType", ((File) object).getNodeType());

        } else {
            //无操作
        }
        return map;
    }

    /**
     * @Author Kangaroo
     * @Description 建立相关边的字段 （start -> end）
     * @Date 2019/11/14 14:46
     * @Param [start, end]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    public Map<String, Object> getEdgeRelationship(int start, int end){
        Map<String, Object> map = new HashMap<>();
        map.put("source", start);
        map.put("target", end);
        return map;
    }
}
