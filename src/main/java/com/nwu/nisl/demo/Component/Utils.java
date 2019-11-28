package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.Node;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Utils {

    public Utils() {
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Author Kangaroo
     * @Description 根据 object 的类型，返回节点相应的属性
     * @Date 2019/11/14 15:32
     * @Param [object, changed, type]
     **/
    public Map<String, Object> getNodeAttribute(Object object, String changed, String type) {
        // change: 字段目前可取的取值："no" "yes"
        // type: 字段目前可取的值："" "add" "modify" "delete", "levelOne" ...
        // 以上两个字段定义在 NodeType 接口中
        Map<String, Object> map = new HashMap<>();
        if (object instanceof Node) {
            map.put("fileMethodName", ((Node) object).getFileMethodName());
            map.put("version", ((Node) object).getVersion());
            map.put("attribute", ((Node) object).getAttribute());
            map.put("nodeType", ((Node) object).getNodeType());
            map.put("changed", changed);
            map.put("type", type);
        } else if (object instanceof Method) {
            map.put("fileMethodName", ((Method) object).getFileMethodName());
            map.put("version", ((Method) object).getVersion());
            map.put("nodeType", ((Method) object).getNodeType());
            map.put("changed", changed);
            map.put("type", type);
        } else if (object instanceof File) {
            map.put("fileName", ((File) object).getFileName());
            map.put("version", ((File) object).getVersion());
            map.put("nodeType", ((File) object).getNodeType());
            map.put("changed", changed);
            map.put("type", type);
        }
        return map;
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Author Kangaroo
     * @Description 建立相关边的字段 （start -> end）
     * @Date 2019/11/14 14:46
     * @Param [start, end, type]
     **/
    public Map<String, Object> getEdgeRelationship(int start, int end, String type) {
        // type: 字段目前可取的值 "hasMethod" "methodCallMethod" "hasNode" "succNode" "nodeCallMethod"
        Map<String, Object> map = new HashMap<>();
        map.put("source", start);
        map.put("target", end);
        map.put("type", type);
        return map;
    }


}
