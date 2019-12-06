package com.nwu.nisl.parse.neo4j.attribute;

import java.util.ArrayList;
import java.util.List;

public class NodeAttribute {
    public NodeAttribute(){
        //无参构造
    }
    private String NodeClass;
    private String VariableCLASS;//变量类型
    private List<String> VariableName=new ArrayList<>();//变量名称
    private  List<String> InitVariableValue=new ArrayList<>();//变量的初始值
    private List<String> OPerator=new ArrayList<>();//运算符
    private String Expressions;//语句内容
}

