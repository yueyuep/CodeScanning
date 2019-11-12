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
    public Map<String, Object> getNodeAttribute(Object object) {
        /*/
         获得所有类型结点的属性信息，分3类
         */
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
}
