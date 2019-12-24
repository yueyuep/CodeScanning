package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Entity.Node;
import com.nwu.nisl.neo4j.FileName;
import org.python.antlr.ast.Str;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Utils {
    @Value("${com.nwu.nisl.data.csv}")
    String csvurl;
    @Value("${com.nwu.nisl.data.source}")
    String sourceurl;
    @Value("${com.nwu.nisl.data.json}")
    String jsonurl;

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
            map.put("level", ((Node) object).getLevel());
        } else if (object instanceof Method) {
            map.put("fileMethodName", ((Method) object).getFileMethodName());
            map.put("version", ((Method) object).getVersion());
            map.put("nodeType", ((Method) object).getNodeType());
            map.put("changed", changed);
            map.put("type", type);
            map.put("level", ((Method) object).getLevel());
        } else if (object instanceof File) {
            map.put("fileName", ((File) object).getFileName());
            map.put("version", ((File) object).getVersion());
            map.put("nodeType", ((File) object).getNodeType());
            map.put("changed", changed);
            map.put("type", type);
            map.put("level", ((File) object).getLevel());
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

    /**
     * Author:lp on 2019/12/13 16:36
     * Param:
     * return:
     * Description:确保H:\CodeScanning\tools\csvdata路径下存在被写入的文件
     */
    public void makeFile() throws Exception {
        List<String> fileurl = Arrays.asList(
                csvurl + "//" + FileName.FILE_NAME,
                csvurl + "//" + FileName.METHOD_NAME,
                csvurl + "//" + FileName.FILE_METHOD_NAME,
                csvurl + "//" + FileName.METHOD_METHOD_NAME,
                csvurl + "//" + FileName.METHOD_NODE_NAME,
                csvurl + "//" + FileName.NODE_METHOD_NAME,
                csvurl + "//" + FileName.NODE_NODE_NAME
        );
        for (String path : fileurl) {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                file.mkdir();
            }
        }


    }

    /**
     * Author:lp on 2019/12/13 17:17
     * Param:
     * return:
     * Description:判断source、jsondata路径下对应的版本数据是否存在
     */

    public boolean exisversion(String version, boolean isSource) {
        String path;
        if (isSource) {
            //原数据文件
            path = sourceurl + "\\" + version;
        } else {
            //json数据文件
            path = jsonurl + "\\" + version;
        }
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            return true;
        } else return false;


    }


}
