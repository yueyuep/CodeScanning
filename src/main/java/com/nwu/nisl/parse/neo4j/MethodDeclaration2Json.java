package com.nwu.nisl.parse.neo4j;

import com.nwu.nisl.parse.graph.AST2Graph;
import com.nwu.nisl.parse.graph.Graph2Json;
import com.nwu.nisl.parse.graph.RangeNode;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.graph.MutableNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 将外部类中的函数转换成json格式存储起来
 * 格式如下：
 * 1、classMethod:outclassMethod
 * 2、Version:0.9.22
 * 3、MethodName:main
 * 4、succs:
 * 5、callMethodNameReferTo：
 * 5、featurestring:
 */

public class MethodDeclaration2Json extends Graph2Json {
    @Expose
    @SerializedName(value = "Version")
    public String Version;
    @Setter
    @Getter
    @Expose
    @SerializedName(value = "MethodName")
    public String MethodName;//前面加上outer前缀
    @Expose
    @SerializedName(value = "callMethodNameReferTo")
    public HashMap<Integer, String> callMethodNameReferTo = new HashMap<>();//<结点，文件名_内部类_函数名>
    @Expose
    @SerializedName(value = "num")
    public int num;
    @Expose
    @SerializedName(value = "succs")
    public List<List<Integer>> succs = new ArrayList<>();
    @Expose
    @SerializedName(value = "nodeAttribute")
    public List<String> nodeAttribute = new ArrayList<>();
    private String fileName = "";
    private MethodDeclaration methodDeclaration;
    private MethodDeclaration pmethodDeclaration;
    private MutableNetwork mutableNetwork;
    private HashMap<String, HashMap<MethodDeclaration, String>> CalledMethod = new HashMap<>();
    //<外部类，外部类中所有的方法声明>
    private HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> outclassMethods = new HashMap<>();
    //<内部类，内部类中所有的方法声明>
    private HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> innerclassMethods = new HashMap<>();

    public MethodDeclaration2Json(File pfile, String version, String methodName, HashMap<String, HashMap<MethodDeclaration, String>> CalledMethod, MethodDeclaration methodDeclaration) {
        //每个函数的处理
        this.fileName = pfile.getName();
        this.Version = version;
        this.CalledMethod = CalledMethod;//<文件名，调用的函数名>
        this.methodDeclaration = methodDeclaration;
        //这个函数分出去，需要根据pmethodDeclation的不同来设置。
        //this.MethodName = FunctionParse.getClassOfMethod(methodDeclaration).concat("_") + methodName;//获得类名_函数名

    }

    public void newInstanceJson(AST2Graph ast2Graph) {
        ast2Graph.initNetwork();
        ast2Graph.constructNetwork(this.methodDeclaration);
        this.mutableNetwork = ast2Graph.getNetwork();
        initSuccessors();

    }

    @Override
    public void initSuccessors() {
        List<Integer> temp = new ArrayList<>();
        Map<Object, Integer> vistedMethodCallex = new HashMap<>();
        Map<Object, Integer> nodeMap = new HashMap<>();//节点索引图
        int nodeIndex = 0;
        //======================添加文件节点作为root节点，用标号0来表示===========================
//        RangeNode MethodRoot=RangeNode.newInstance(methodDeclaration);
//        nodeMap.put(methodDeclaration,0);
        //对函数体中的所有节点进行排序。构造键值对，按照键值来表示我们的节点信息。
        for (Object node : mutableNetwork.nodes()) {
            nodeMap.put(node, nodeIndex);
            nodeIndex++;
            //+++++++++++++++++++++++++++++++++++++++++构建节点调用函数位置关系++++++++++++++++++++++++++++++++++++++++
            if (!vistedMethodCallex.containsKey(node) && node instanceof RangeNode) {
                //可以传过来需要构建函数调用的结点
                List<MethodCallExpr> mNode = new ArrayList<>();
                //((RangeNode) node).getmNode().findAll(MethodCallExpr.class).stream().forEach(methodCallExpr -> mNode.add(methodCallExpr));//获得所有函数调用
                //当callMethod得到的结果为零的时候，一定是不存在函数调用的。直接过滤掉。
                if (((RangeNode) node).getmNode() instanceof MethodCallExpr) {
                    MethodCallExpr methodCallExpr = ((MethodCallExpr) ((RangeNode) node).getmNode()).asMethodCallExpr();
                    //如果当前结点是函数调用的结点
                    int index = nodeMap.get(node);
                    vistedMethodCallex.put(methodCallExpr, index);
                    for (String pfileName : CalledMethod.keySet()) {
                        for (MethodDeclaration methodDeclaration : CalledMethod.get(pfileName).keySet()) {
                            // TODO MethodCallExpr和MethodDeclation的比较这部分逻辑问题
                            if (methodDeclaration.getNameAsString().equals(methodCallExpr.getNameAsString()) && methodDeclaration.getParameters().size() == methodCallExpr.getArguments().size()) {
                                //得到所在的类名
                                String calssName = CalledMethod.get(pfileName).get(methodDeclaration);
                                //<结点，文件名_类名_outer_函数名>
                                String res = pfileName.concat("-").concat(methodDeclaration.getNameAsString()).concat(calssName).concat(new GraphParse().getMethodParameter(methodDeclaration));
//                                callMethodNameReferTo.put(index, pfileName.concat("_").concat(calssName).concat("_").concat(methodDeclaration.getNameAsString()));
                                callMethodNameReferTo.put(index, res);
                            }
                        }
                    }
                }
            }
        }

        num = nodeIndex;
        for (Object node : mutableNetwork.nodes()) {   //针对每一个节点进行构图
            temp = (List<Integer>) mutableNetwork.successors(node).stream()
                    .map(o -> nodeMap.get(o))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Integer pindex = nodeMap.get(node);
            temp = temp.stream().filter(t -> !(t == pindex)).collect(Collectors.toList());//过滤掉存在自环节点
            //ParseExpression parseExpression=new ParseExpression(Utils.Object2Node(node));
            //图网络添加后两维的特征
            succs.add(temp);
            addAttribute(node);

        }
    }

    @Override
    public void saveToJson(String fileName) {
        BufferedWriter writer = null;
        //只对有注解的进行序列化和反序列化。
        Gson gson = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().excludeFieldsWithModifiers(Modifier.PRIVATE).create();
        String string = gson.toJson(this);
        try {
            System.out.println("写入文件");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true)));
            writer.write(string + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addAttribute(Object node) {
        if (node instanceof RangeNode) {
            nodeAttribute.add(super.travelNode(((RangeNode) node).getNode()));
        } else if (node instanceof String) {
            nodeAttribute.add(node.toString());
        } else {
            nodeAttribute.add(node.toString());
        }
    }
}
