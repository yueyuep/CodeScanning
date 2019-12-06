package com.nwu.nisl.parse.owaspbench;

import com.nwu.nisl.parse.graph.RangeNode;
import com.nwu.nisl.parse.graph.Util;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.google.common.graph.MutableNetwork;

import java.util.ArrayList;

/*
    ******* 向量生成器 *******
    * 0-控制流出度，
    * 1-控制流入度，
    * 2-方法调用数目，
    * 3-是否控制节点，
    * 4-是否是声明语句，
    * 5-是否是赋值语句，
    * 6-是否是比较语句（是否有关系运算符），
    * 7-是几元运算符。
 */

public class VecGenerator {//根据节点进行向量生成

    ArrayList<ArrayList<Integer>> vecs = new ArrayList<ArrayList<Integer>>();
    MutableNetwork<Object, String> CFG;
    public VecGenerator(MutableNetwork CFG){
        this.CFG = CFG;
    }

    public void run(){
        for (Object node: CFG.nodes()
             ) {
            vecs.add(this.getVecOfNode(node));
        }
    }

    public ArrayList<Integer> getVecOfNode(Object nodeObject) {//从节点->向量表示方法
        ArrayList<Integer> vec = new ArrayList<>(8);//8维度的矩阵。
        int outDegree;
        int inDegree;
        int NOMethodCalled;
        int isControlNode;
        int isDeclaration;
        int isAssignExpr;
        int hasRelationOp;
        int isBinaryExpr;
        outDegree = CFG.outDegree(nodeObject);
        inDegree = CFG.inDegree(nodeObject);
        vec.add(outDegree);
        vec.add(inDegree);
        if (nodeObject instanceof RangeNode) {
            Node node = ((RangeNode) nodeObject).getNode();
            NOMethodCalled = calNMC(node);
            isControlNode = calCtN(node);
            isDeclaration = calDcl(node);
            isAssignExpr = calAsE(node);
            hasRelationOp = calROp(node);
            isBinaryExpr = calBnE(node);
            vec.add(NOMethodCalled);
            vec.add(isControlNode);
            vec.add(isDeclaration);
            vec.add(isAssignExpr);
            vec.add(hasRelationOp);
            vec.add(isBinaryExpr);
        } else {
            vec.add(0);
            vec.add(0);
            vec.add(0);
            vec.add(0);
            vec.add(0);
            vec.add(0);
        }
//        System.out.println("Node:"+node.getRange().get().begin+" vec"+vec.toString());
        return vec;
    }

    private int calROp(Node node) {
        int count = 0;
        if(!node.getChildNodes().isEmpty()){
            for(Node cnode: node.getChildNodes()){
                switch (getTail(cnode.getClass().toString())){
                    case "BinaryExpr":
                        String opName = ((BinaryExpr)cnode).getOperator().name();
                        switch (opName){
                            case "GREATER":count += 1;break;
                            case "LESS":count += 2;break;
                            case "EQUALS":count += 3;break;
                            case "GREATER_EQUALS":count += 4;break;
                            case "LESS_EQUALS":count += 5;break;
                            case "NOT_EQUALS":count += 6;break;
                            default:break;
                        }
                    default:break;
                }
                count = calROp(cnode)+count;
            }
        }
        return count;
    }

    // TODO: 暂时只能判断是否二元表达式
    private int calBnE(Node node) {
        int count = 0;
        if(!node.getChildNodes().isEmpty()){
            for(Node cnode: node.getChildNodes()){
                switch (getTail(cnode.getClass().toString())){
                    case "UnaryExpr": count += 1;break;
                    case "BinaryExpr":{
                        if(cnode.getParentNode().get().toString().contains("?"))
                            count += 3;
                        else
                            count += 2;
                        break;
                    }
                    default:break;
                }
                count = calBnE(cnode)+count;
            }
        }
        return count;
    }

    private int calAsE(Node node) {
        int count = 0;
        if(!node.getChildNodes().isEmpty()){
            for(Node cnode: node.getChildNodes()){
                switch (getTail(cnode.getClass().toString())){
                    case "AssignExpr":
                        count++;
                    default:break;
                }
                count = calAsE(cnode)+count;
            }
        }
        return count;
    }

    //TODO: 暂时只处理变量声明语句
    private int calDcl(Node node) {
        int count = 0;
        if(!node.getChildNodes().isEmpty()){
            for(Node cnode: node.getChildNodes()){
                switch (getTail(cnode.getClass().toString())){
                    case "VariableDeclarationExpr":
                        count++;
                    default:break;
                }
                count = calDcl(cnode)+count;
            }
        }
        return count;
    }

    private int calCtN(Node node) {
        int count;
        switch (getTail(node.getClass().toString())){
            case "ExpressionStmt":count=0;break;
            case "IfStmt":count=1;break;
            case "SwitchStmt":count=2;break;
            case "WhileStmt":count=3;break;
            case "DoStmt":count=4;break;
            case "ForStmt":count=5;break;
            case "BreakStmt":count=6;break;
            case "ContinueStmt":count=7;break;
            case "SwitchEntryStmt":count=8;break;
            default:count=-1;break;
        }
        return count;
    }

    private int calNMC(Node node) {//递归的求当前节点的函数声明个数。
        int count = 0;
        if(!node.getChildNodes().isEmpty()){
            for(Node cnode: node.getChildNodes()){
                if(getTail(cnode.getClass().toString()).equals("MethodCallExpr")){
                    count++;
                }
                count = calNMC(cnode)+count;
            }
        }
        return count;
    }

    private String getTail(String s){
        return Util.getClassLastName(s);
    }
}
