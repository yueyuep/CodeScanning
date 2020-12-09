package com.nwu.nisl.parse.graph;
//处理java源码，包含获取AST解析树

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class ParseUtil {
    private String mSrcFilePath;
    private CompilationUnit mCompilationUnit;
    private List<MethodDeclaration> mMethodDeclarations;//方法声明列表
    private MethodDeclaration mMethodDeclaration;
    private List<SimpleName> mSimpleNamesOfClass; //返回class类型的实体名称
    private ArrayList<VariableDeclarator> mFieldVariableDeclarators = new ArrayList<>();//变量声明列表
    private ArrayList<SimpleName> mVariableDeclSimpleNames = new ArrayList<>();

    // 父节点为类或者接口的函数声明
    private List<MethodDeclaration> methodDeclarations = new ArrayList<>();

    public ParseUtil(String srcFilePath) throws FileNotFoundException {
        //构造方法，负责解析java语法树
        mSrcFilePath = srcFilePath;

        try {
            mCompilationUnit = JavaParser.parse(new FileInputStream(mSrcFilePath));
        } catch (Exception e) {
            System.out.println(mSrcFilePath + "\n" + e);
        }

        mCompilationUnit = JavaParser.parse(new FileInputStream(mSrcFilePath));//编译单元

        mMethodDeclarations = mCompilationUnit.findAll(MethodDeclaration.class);
        //注意lambda表达式的用法
        mCompilationUnit.findAll(VariableDeclarator.class).forEach(variableDeclarator ->
                mVariableDeclSimpleNames.add(variableDeclarator.getName()));

        mSimpleNamesOfClass = mCompilationUnit.findAll(ClassOrInterfaceType.class).stream()
                .filter(classOrInterfaceType -> !mVariableDeclSimpleNames.contains(classOrInterfaceType.getName()))
                .map(ClassOrInterfaceType::getName).collect(Collectors.toList());
        mCompilationUnit.findAll(FieldDeclaration.class).forEach((FieldDeclaration fieldDeclaration) ->
            mFieldVariableDeclarators.addAll(new ArrayList<>(fieldDeclaration.findAll(VariableDeclarator.class))));

        mMethodDeclarations.stream().filter(methodDeclaration -> methodDeclaration.getParentNode().get()
                instanceof ClassOrInterfaceDeclaration).forEach(methodDeclaration -> methodDeclarations.add(methodDeclaration));

//        mMethodDeclarations.forEach(methodDeclaration -> {if (methodDeclaration.getParentNode().get()
//                instanceof ClassOrInterfaceDeclaration){methodDeclarations.add(methodDeclaration);}});

    }

    public MethodDeclaration findMethodDeclarationContains(Node node) {
        if (node instanceof MethodDeclaration) {
            return (MethodDeclaration) node;
        }
        for (MethodDeclaration methodDeclaration : mMethodDeclarations) {
            if (isRangeContains(methodDeclaration, node)) {
                return methodDeclaration;
            }
        }
        return null;
    }

    public void renameVariableFieldName(List<Node> nodeList) {
        int i = 1;
        int j = 1;
        ArrayList<NameExpr> nameExprs = new ArrayList<NameExpr>();
        ArrayList<FieldAccessExpr> fieldAccessExprs = new ArrayList<FieldAccessExpr>();
        ArrayList<VariableDeclarator> variableDeclarators = new ArrayList<VariableDeclarator>();
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        nodeList.forEach(node -> {
            nameExprs.addAll(node.findAll(NameExpr.class).stream().filter(nameExpr -> notBelongToClassNames(nameExpr)).collect(Collectors.toList()));
            fieldAccessExprs.addAll(node.findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
            variableDeclarators.addAll(node.findAll(VariableDeclarator.class).stream().filter(variableDeclarator -> !mFieldVariableDeclarators.contains(variableDeclarator)).collect(Collectors.toList()));
            parameters.addAll(new ArrayList<>(node.findAll(Parameter.class)));
        });
        ArrayList<VariableDeclarator> variableDeclarators_done = new ArrayList<>();
        HashMap<String,String> name_done = new HashMap<String,String>();
        ArrayList<Node> nodes = new ArrayList<>();
        parameters.sort(new NodeComparator());
        variableDeclarators.sort(new NodeComparator());
        mFieldVariableDeclarators.sort(new NodeComparator());
        HashMap<String,String> field_done = new HashMap<String,String>();
        HashMap<String,String> variableDeclarator_done = new HashMap<String,String>();
//        System.out.println(fieldAccessExprs);
        for (VariableDeclarator variableDeclarator: mFieldVariableDeclarators) {
            Boolean isGet = Boolean.FALSE;
            for (FieldAccessExpr fieldAccessExpr: fieldAccessExprs) {
                if (Objects.equals(variableDeclarator.getNameAsString(), fieldAccessExpr.getNameAsString())) {
                    fieldAccessExpr.setName("var" + i);
                    isGet = Boolean.TRUE;
//                    field_done.put(fieldAccessExpr.getNameAsString(), "field" +j);
                }
            }
            if (isGet) {
                variableDeclarator.setName("var" + i);
                i++;
            }
        }
        for (Parameter parameter: parameters) {
            Boolean isGet = Boolean.FALSE;
            for (NameExpr nameExpr: nameExprs) {
                if (Objects.equals(parameter.getNameAsString(), nameExpr.getNameAsString())) {
                    nameExpr.setName("var" + i);
                    isGet = Boolean.TRUE;
                }
            }
            if (isGet) {
                parameter.setName("var" + i);
                i++;
            }
        }
        for (VariableDeclarator variableDeclarator: variableDeclarators) {
            Boolean isGet = Boolean.FALSE;
            for (NameExpr nameExpr: nameExprs) {
                if (Objects.equals(variableDeclarator.getNameAsString(), nameExpr.getNameAsString())) {
                    nameExpr.setName("var" + i);
                    isGet = Boolean.TRUE;
                }
            }
            if (isGet) {
                variableDeclarator_done.put(variableDeclarator.getNameAsString(), "var" +i);
                variableDeclarators_done.add(variableDeclarator);
                variableDeclarator.setName("var" + i);
                i++;
            }
        }
        for (VariableDeclarator variableDeclarator: variableDeclarators) {
            if (!variableDeclarators_done.contains(variableDeclarator)) {
                if (variableDeclarator_done.containsKey(variableDeclarator.getNameAsString())) {
                    variableDeclarator.setName((String) variableDeclarator_done.get(variableDeclarator.getNameAsString()));
                    continue;
                }
                variableDeclarator.setName("unused");
            }
        }
    }

    public void setMethodDeclaration(MethodDeclaration methodDeclaration) {
        mMethodDeclaration = methodDeclaration;
    }

    public List<MethodDeclaration> getMethodDeclarations() {
        return mMethodDeclarations;
    }

    public List<MethodDeclaration> getmethodDeclarations() {
        return methodDeclarations;
    }

    public MethodDeclaration getMethodDeclaration() {
        return mMethodDeclaration;
    }

    public CompilationUnit getCompilationUnit() {
        return mCompilationUnit;
    }

    public String getSrcFilePath() {
        return mSrcFilePath;
    }

    public List<SimpleName> getSimpleNamesOfClass() {
        return mSimpleNamesOfClass;
    }

    public ArrayList<VariableDeclarator> getFieldVariableDeclarators() {
        return mFieldVariableDeclarators;
    }

    public List<Node> getAssignsOrStmtsContains(Node parent, SimpleName variableName) {
        List<Node> containNodes = new ArrayList<>();
        List<Class> nodeTypes = Arrays.asList(IfStmt.class, WhileStmt.class, DoStmt.class,
                ForStmt.class, ForEachStmt.class, SwitchStmt.class, TryStmt.class, LambdaExpr.class);
        for (Class type : nodeTypes) {
            containNodes.addAll(getNodesContains(parent, variableName, type));
        }
        List<Node> toRemove = new ArrayList<>();
        for (Node node : containNodes) {
            for (Node other : containNodes) {
                if (node.equals(other)) {
                    continue;
                }
                if (isParentContains(node, other, Node.class)) {
                    toRemove.add(other);
                } else if (isParentContains(other, node, Node.class)) {
                    toRemove.add(node);
                }
            }
        }
        containNodes.removeAll(toRemove);

        containNodes.addAll(getSpecificAssignExpr(parent, variableName).stream()
                .filter(assignExpr -> !isParentsContains(containNodes, assignExpr, AssignExpr.class))
                .collect(Collectors.toList()));
        containNodes.sort(new NodeComparator());
        return containNodes;
    }

    private  <T extends Node> boolean isParentsContains(List<Node> parents, T child, Class<T> tClass) {
        for (Node parent : parents) {
            if (isParentContains(parent, child, tClass)) {
                return true;
            }
        }

        return false;
    }

    private  <T extends Node> boolean isParentContains(Node parent, T child, Class<T> tClass) {
        return new ArrayList<>(parent.findAll(tClass)).contains(child);
    }

    public <T extends Node> List<T> getNodesContains(Node parent, SimpleName simpleName, Class<T> tClass) {
        return parent.findAll(tClass)
                .stream()
                .filter(node ->
                        node.findAll(NameExpr.class)
                                .stream()
                                .map(NameExpr::getName)
                                .collect(Collectors.toList())
                                .contains(simpleName))
                .collect(Collectors.toList());
    }

    public <T extends Node> List<T> getSpecificChildNodes(Node parent, Class<T> tClass) {
        return new ArrayList<>(parent.findAll(tClass));
    }

    public List<NameExpr> getVariableNames(Node parentNode) {
        return parentNode.findAll(NameExpr.class)
                .stream()
                .filter(this::notBelongToClassNames)
                .collect(Collectors.toList());
    }

    public List<NameExpr> getSpecificVariableFlowsLastNodes(Node parent, SimpleName variableName, Node before) {
        return parent.findAll(NameExpr.class)
                .stream()
                .filter(nameExpr ->
                        notBelongToClassNames(nameExpr)
                                && nameExpr.getName().equals(variableName)
                                && NodePositionComparator.isAfterPosition(nameExpr, before))
                .sorted(new NodeComparator())
                .collect(Collectors.toList());
    }

    public List<NameExpr> getSpecificVariableFlowsStartNodes(Node parent, SimpleName variableName, Node after) {
        return parent.findAll(NameExpr.class)
                .stream()
                .filter(nameExpr ->
                        notBelongToClassNames(nameExpr)
                                && nameExpr.getName().equals(variableName)
                                && NodePositionComparator.isBeforePosition(nameExpr, after))
                .sorted(new NodeComparator())
                .collect(Collectors.toList());
    }

    public List<NameExpr> getSpecificVariableFlowsBetweenNodes(Node parent, SimpleName variableName, Node before, Node after) {
        return parent.findAll(NameExpr.class)
                .stream()
                .filter(nameExpr ->
                        notBelongToClassNames(nameExpr)
                        && nameExpr.getName().equals(variableName)
                        && NodePositionComparator.isBeforePosition(nameExpr, after)
                        && NodePositionComparator.isAfterPosition(nameExpr, before))
                .sorted(new NodeComparator())
                .collect(Collectors.toList());
    }

    public boolean notBelongToClassNames(NameExpr nameExpr) {
        return !mSimpleNamesOfClass.contains(nameExpr.getName());
    }

    public boolean notBelongToClassNames(SimpleName simpleName) {
        return !mSimpleNamesOfClass.contains(simpleName);
    }

    public List<NameExpr> getSpecificVariableFlows(Node parentNode, SimpleName specificVariableName) {
        return parentNode.findAll(NameExpr.class)
                .stream()
                .filter(nameExpr -> notBelongToClassNames(nameExpr)
                        && nameExpr.getName().equals(specificVariableName))
                .sorted(new NodeComparator())
                .collect(Collectors.toList());
    }

    public List<NameExpr> getSpecificVariableFlowsUntilFirstWrite(Node parentNode, SimpleName variableName) {
        // TODO: improve
        List<AssignExpr> assignExprs = getSpecificAssignExpr(parentNode, variableName);
        if (assignExprs.isEmpty()) {
            return parentNode.findAll(NameExpr.class)
                    .stream()
                    .filter(nameExpr -> notBelongToClassNames(nameExpr)
                            && nameExpr.getName().equals(variableName))
                    .sorted(new NodeComparator())
                    .collect(Collectors.toList());

        } else {
            AssignExpr firstAssign = assignExprs.get(0);
            List<NameExpr> result = parentNode.findAll(NameExpr.class)
                    .stream()
                    .filter(nameExpr -> notBelongToClassNames(nameExpr)
                            && nameExpr.getName().equals(variableName)
                            && NodePositionComparator.isBeforePosition(nameExpr, firstAssign))
                    .sorted(new NodeComparator())
                    .collect(Collectors.toList());
            result.addAll(getSpecificVariableFlows(firstAssign.getValue(), variableName));
            result.addAll(getSpecificVariableFlows(firstAssign.getTarget(), variableName));
            return result;
        }
    }

    public List<AssignExpr> getAssignExpr(Node parentNode) {
        return parentNode.findAll(AssignExpr.class)
                .stream()
                .sorted(new NodeComparator()).collect(Collectors.toList());
    }

    public List<AssignExpr> getSpecificAssignExpr(Node parentNode, SimpleName variableName) {
        return parentNode.findAll(AssignExpr.class)
                .stream()
                .filter(assignExpr -> !getSpecificVariableFlows(assignExpr.getTarget(), variableName).isEmpty())
                .sorted(new NodeComparator()).collect(Collectors.toList());
    }

    private boolean isNameExprInAssignTarget(Node parentNode, NameExpr variableNameExpr) {
        for (AssignExpr assignExpr : getAssignExpr(parentNode)) {
            for (NameExpr nameExpr : assignExpr.getTarget()
                    .findAll(NameExpr.class)
                    .stream()
                    .filter(nameExpr -> notBelongToClassNames(nameExpr))
                    .collect(Collectors.toList())) {
                if (isSamePosition(nameExpr, variableNameExpr)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<NameExpr> getVariableNamesExceptInAssign(Node parentNode, SimpleName specificVariableName) {
        return getSpecificVariableFlows(parentNode, specificVariableName)
                .stream()
                .filter(nameExpr -> !isNameExprInAssignTarget(parentNode, nameExpr))
                .collect(Collectors.toList());
    }

    private boolean isSamePosition(Node a, Node b) {
        return  a.getRange().equals(b.getRange());
    }

    public boolean isNodeInThisLine(Node a, int line) {
        return a.getBegin().get().line == line && line == a.getEnd().get().line;
    }

    public boolean simpleNameIsVariable(SimpleName node) {
        return mCompilationUnit.findAll(VariableDeclarator.class).stream().map(VariableDeclarator::getName).collect(Collectors.toList()).contains(node);
    }

    public List<MethodDeclaration> getMethodDeclarationIn(List<Integer> lines) {
        List<MethodDeclaration> methods = new ArrayList<>();
        for (Integer line : lines) {
            for (MethodDeclaration methodDeclaration : mMethodDeclarations) {
                if (methodDeclaration.getBegin().get().line <= line
                        && methodDeclaration.getEnd().get().line >= line) {
                    if (!methods.contains(methodDeclaration)) {
                        methods.add(methodDeclaration);
                    }
                    break;
                }
            }
        }
        return methods;
    }

    public boolean isRangeContains(Node nodeBig, Node nodeSmall) {
        if (nodeBig.getRange().isPresent() && nodeSmall.getRange().isPresent()) {
            return NodePositionComparator.ifSmaller(nodeBig.getRange().get().begin, nodeSmall.getRange().get().begin) &&
                    NodePositionComparator.ifSmaller(nodeSmall.getRange().get().end, nodeBig.getRange().get().end);
        }
        return false;
    }
}
