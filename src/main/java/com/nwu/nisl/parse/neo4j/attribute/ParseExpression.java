package com.nwu.nisl.parse.neo4j.attribute;

import com.nwu.nisl.parse.graph.AST2Graph;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParseExpression {
    public static void main(String[] args) {
        String Cat="../CodeGraph/Project/test/";
        File file=new File(Cat).listFiles()[0];

        AST2Graph ast2Graph= AST2Graph.newInstance(file.getPath());
        MethodDeclaration methodDeclaration=ast2Graph.getMethodDeclarations().get(0);
        List<BinaryExpr> ifStmts=methodDeclaration.findAll(BinaryExpr.class);
        ExpressionStmt expression=methodDeclaration.findAll(ExpressionStmt.class).get(0);
       String myclass=expression.getTokenRange().get().getBegin().getText();
       ParseExpression parseExpression=new ParseExpression(expression);
       String s=parseExpression.set2List();
    }
   @Expose
   @SerializedName(value = "NodeClass")
    private String NodeClass="";
    @Expose
   @SerializedName(value = "VariableCLASS")
    private String VariableCLASS="";//变量类型
    @Expose
   @SerializedName(value = "VariableName")
    private List<String> VariableName=new ArrayList<>();//变量名称
     @Expose
   @SerializedName(value = "InitVariableValue")
    private  List<String> InitVariableValue=new ArrayList<>();//变量的初始值
      @Expose
   @SerializedName(value = "OPerator")
    private List<String> OPerator=new ArrayList<>();//运算符
      @Expose
   @SerializedName(value = "Expressions")
    private String Expressions="";//语句内容
public ParseExpression(){}
public ParseExpression(Node node ){
    if (node instanceof ExpressionStmt){
        this.NodeClass=Graph.EXPSTMT;
        Expressions=((ExpressionStmt) node).getExpression().toString().replace("\"","\'");
        List<VariableDeclarationExpr>variableDeclarationExprs=node.findAll(VariableDeclarationExpr.class);
        for(VariableDeclarationExpr variableDeclarationExpr:variableDeclarationExprs){
           VariableDeclarator variableDeclarator=variableDeclarationExpr.getVariable(0);
            VariableCLASS=variableDeclarator.getTypeAsString();
            VariableName.add(variableDeclarator.getNameAsString());
            if (variableDeclarator.getInitializer().isPresent()){
                String value=variableDeclarator.getInitializer().get().toString().replace("\"","\'");
                InitVariableValue.add(value);
            }






        }

    }
    else if (node instanceof TryStmt){
        this.NodeClass=Graph.TRYBLOCK;
    }
    else if (node instanceof IfStmt){
        this.NodeClass=Graph.IFBLOCK;

    }

    else if (node instanceof Parameter){
        this.NodeClass=Graph.PARAMETER;
    }

    else if(node instanceof BinaryExpr){
        this.NodeClass=Graph.BINARYEXPR;//条件节点
        this.OPerator.add(((BinaryExpr) node).getOperator().toString());
        this.VariableName.add(((BinaryExpr) node).getLeft().toString());
        Expressions=node.toString();


    }
    else if(node instanceof WhileStmt){
        this.NodeClass=Graph.WHILEBLOCK;
    }
    else if(node instanceof ForEachStmt){
         this.NodeClass=Graph.FORBLOCK;
    }
    else if(node instanceof ReturnStmt){
        this.NodeClass=Graph.RETURNBLOCK;
    }
    else if (node instanceof ReturnStmt){
        this.NodeClass=Graph.RETURNBLOCK;
    }
    else if (node instanceof DoStmt ){
        this.NodeClass=Graph.DOBLOCK;
    }
    else if (node instanceof SwitchStmt){
        this.NodeClass=Graph.SWITHBLOCK;
    }
    else if(node instanceof ContinueStmt){
        this.NodeClass=Graph.CONTINUEBLOCK;
    }
    else if (node instanceof AssertStmt){
        this.NodeClass=Graph.ASSERTBLOCK;
    }
    else if (node instanceof BreakStmt){
        this.NodeClass=Graph.BREAKBLOCK;
    }
    else if(node instanceof SynchronizedStmt){
        this.NodeClass=Graph.SYNCHRONIZEDBLOCK;
    }
    else if(node instanceof LabeledStmt){
        this.NodeClass=Graph.LABLEBLOCK;
    }
    else if(node instanceof ThrowStmt){
        this.NodeClass=Graph.THROWBLOCK;
    }
    else if (node instanceof ForEachStmt){
        this.NodeClass=Graph.FOREACHBLOCK;

    }
    else {
        this.NodeClass=Graph.BLOCK;
    }



    }


 public  String set2List(){
    Gson gson=new GsonBuilder().disableHtmlEscaping().create();
    String s=gson.toJson(this);
    return  s;
}

    public List<String> getInitVariableValue() {
        return InitVariableValue;
    }

    public List<String> getOPerator() {
        return OPerator;
    }

    public List<String> getVariableName() {
        return VariableName;
    }

    public String getExpressions() {
        return Expressions;
    }

    public String getNodeClass() {
        return NodeClass;
    }

    public String getVariableCLASS() {
        return VariableCLASS;
    }

}


