package com.nwu.nisl.parse.neo4j;

import com.nwu.nisl.parse.graph.AST2Graph;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParameterCompare {
    @Getter
    @Setter
    private List<String> methodDeclationOfParameterList = new ArrayList<>();
    @Getter
    @Setter
    private List<String> methodCallExprOfParameterList = new ArrayList<>();
    private MethodCallExpr methodCallExpr;
    private MethodDeclaration methodDeclaration;

    public ParameterCompare(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public ParameterCompare(MethodCallExpr methodCallExpr) {
        this.methodCallExpr = methodCallExpr;
    }

    public ParameterCompare(MethodDeclaration methodDeclaration, MethodCallExpr methodCallExpr) {
        this.methodDeclaration = methodDeclaration;
        this.methodCallExpr = methodCallExpr;
    }

    boolean isSameParameter() {
        //比较两个参数列表字段是否相等（数量&&内容相等）
        if (methodCallExprOfParameterList.size() != methodDeclationOfParameterList.size()) {
            //参数数量不等
            return false;
        }
        for (String basepar : methodDeclationOfParameterList) {
            for (String targerpar : methodCallExprOfParameterList) {
                if (basepar.equals(targerpar)) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<String> getParOfMethodDeclation() {
        List<String> paramelist = new ArrayList<>();
        NodeList<com.github.javaparser.ast.body.Parameter> parameterNodeList = methodDeclaration.getParameters();
        for (com.github.javaparser.ast.body.Parameter parameter : parameterNodeList) {
            paramelist.add(parameter.getTypeAsString());
        }
        return paramelist;
    }

    private List<String> getParOfMethodCallexpr() {
        List<String> list = new ArrayList<>();
        //函数调用的是具体的变量，需要定位到变量的类型。
        NodeList<Expression> nodeList = methodCallExpr.getArguments();
        return list;
    }

    String getTypeOfVariable(String variable) {
        // 寻找变量的类型
        String lists = "";
        return lists;
    }

    public static void main(String[] args) {
        String src = "H:\\CodeGraph\\Project\\Sourcedata\\test\\GsonCompatibilityMode.java";
        File file = new File(src);
        AST2Graph ast2Graph = AST2Graph.newInstance(file.getPath());
        List<MethodCallExpr> methodCallExprs = ast2Graph.getCompilationUnit().findAll(MethodCallExpr.class);
        for (MethodCallExpr methodCallExpr : methodCallExprs) {
            ParameterCompare parameter = new ParameterCompare(methodCallExpr);
            parameter.getParOfMethodCallexpr();
        }


    }
}

