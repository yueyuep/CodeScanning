package com.nwu.nisl.demo.Component;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Component
 * @ClassName: NodeType
 * @Author: Kangaroo
 * @Description: 保存callGraph中Node的类型
 * @Date: 2019/11/27 9:50
 * @Version: 1.0
 */
public interface NodeType {

    String FILE = "file";
    String METHOD = "method";

    /** 一般节点 **/
    String GENERAL_NODE = "";

    /** 变化节点类型 **/
    String ADD_NODE = "add";
    String DELETE_NODE = "delete";
    String MODIFY_NODE = "modify";

    String LEVEL_PREFIX = "level";
    String LEVEL_ONE_NODE = "levelOne";
    String LEVEL_TWO_NODE = "levelTwo";
    String LEVEL_THREE_NODE = "levelThree";

    String HAS_CHANGED = "yes";
    String NOT_CHANGE = "no";
}
