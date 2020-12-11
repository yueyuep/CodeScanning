package com.nwu.nisl.parse.graph;

import com.github.javaparser.Range;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.google.common.graph.*;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
//这个是在解析语法树之后进行处理，ParseUtil中包含了很多解析java语言的各种接口函数

public class AST2Graph extends ParseUtil implements Graph {//实现了图的接口，图模型上的修改
    public List<Range> mVisitedNodes = new ArrayList<>();
    public String mParseResult;
    public MutableValueGraph<Object, String> mGraph;//多值网络
    public MutableNetwork<Object, String> mNetwork;//多网络
    public Node mLastUse;
    public Node mLastWrite;
    public int mEdgeNumber;
    public Set<RangeNode> mDataFlowNodes = new HashSet<>();
    // For CFG
    public ArrayList<Node> mPreNodes = new ArrayList<>();
    public ArrayDeque<Node> mBreakNodes = new ArrayDeque<>();
    public ArrayDeque<Node> mContinueNodes = new ArrayDeque<>();
    public ArrayList<Node> mPreTempNodes = new ArrayList<>();
    // For CG
    public ArrayList<MethodDeclaration> mCalledMethodDecls = new ArrayList<>();

    public AST2Graph(String srcFilePath) throws FileNotFoundException {
        //调用构造方法，ParseUtil中，解析java代码，获得对应的方法声明
        //和方法声明
        super(srcFilePath);
    }


    //下面三个函数共同完成网络的初始化。
    public void initNetwork() {
        RangeNode.nodeCacheClear();
        initNetworkWithoutRangeNode();
    }

    public void initNetworkWithoutRangeNode() {
        mVisitedNodes.clear();
        mParseResult = "";
        //允许存在平行边，允许自环
        mNetwork = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
        mGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();

        mEdgeNumber = 0;
        mDataFlowNodes.clear();
        initCFG();
    }

    public void initCFG() {
        mLastUse = null;
        mLastWrite = null;
        mPreNodes.clear();
        mBreakNodes.clear();
        mContinueNodes.clear();
        mPreTempNodes.clear();
    }

    public static AST2Graph newInstance(String srcFilePath) {//静态实例对象。
        try {
            return new AST2Graph(srcFilePath);
        } catch (FileNotFoundException e) {
            System.out.println("断点1");
            e.printStackTrace();
            logInfo("Not Found File " + srcFilePath);
            return null;
        } catch (Exception e) {
            System.out.println("断点2");
            e.printStackTrace();
            logInfo(e + "\n" + srcFilePath);
            return null;
        }
    }

//    public static void main(String[] args) {
//        String srcFilePath = "data/BenchmarkTest00159.java";
//       // ExtractSubGraphs.ExtractSQLI(srcFilePath, srcFilePath.replace(".java", "__.txt"));
////        String srcFilePath = "data/tsExpStmt.java";
//        //调用上面的方法创建实例对象
//        AST2Graph ast2Graph = AST2Graph.newInstance(srcFilePath);
//        if (ast2Graph == null) {
//            return;
//        }
//        List<MethodDeclaration> methodDeclarations = ast2Graph.getMethodDeclarations();
//        if (methodDeclarations.size() <= 0) {
//            logInfo("There is no method declaration.");
//            System.exit(0);
//        }
//        //初始化网络
//        ast2Graph.initNetwork();
//        MethodDeclaration method = ast2Graph.getCompilationUnit().findAll(MethodDeclaration.class).stream()
//                .filter(methodDeclaration -> methodDeclaration.getNameAsString().equals("doPost"))
//                .collect(Collectors.toList()).get(0);
//        ast2Graph.constructNetwork(method);
//        MutableNetwork<Object, String> network = ast2Graph.getNetwork();
////        ast2Graph.renameNetworkVar();
//        for (Object nodeU : network.nodes()) {
//            for (Object nodeV : network.adjacentNodes(nodeU)) {
//                for (String edge : network.edgesConnecting(nodeU, nodeV)) {
//                    System.out.println("=============");
//                    System.out.println(edge);
//                    System.out.println(nodeU);
//                    System.out.println(nodeV);
//                }
//            }
//        }
//        for (String edge : network.edges()) {
//            System.out.println(edge);
//            for (Object pair : network.incidentNodes(edge)) {
////                System.out.println(((RangeNode)pair).getOptionalRange());
//            }
//        }
//        Graph2Json graph2Json = Graph2Json.newInstance(ast2Graph.mNetwork);
//        graph2Json.saveToJson(srcFilePath.replace(".java", "_.txt"));
//    }//main结束

    public void constructNetwork(Node node) {
        travelNodeForCFG(node);//控制流信息的构造（if/switch/for/do 类似语句的处理）
        travelNode(node);
        travelCalled();//完成上面两个函数的递归调用。
    }

    public void renameNetworkVar() {
        ArrayList<Node> nodes = new ArrayList<>();
        for (Object node : mNetwork.nodes()) {
            if (node instanceof RangeNode) {
                nodes.add(((RangeNode) node).getNode());
            }
        }
        renameVariableFieldName(nodes);
    }

    public <T extends Node> boolean travelNode(T nodeRoot) {//遍历节点,针对不同的函数声明，使用不同的处理
        if (!nodeRoot.getRange().isPresent() || mVisitedNodes.contains(nodeRoot.getRange().get())) {
            return false;
        }
        List<Node> nodes = nodeRoot.findAll(Node.class);
        for (Node node : nodes) {//遍历所有节点
            if (!node.getRange().isPresent() || mVisitedNodes.contains(node.getRange().get())) {
                continue;
            }
            node.getRange().ifPresent(range -> mVisitedNodes.add(range));

            String nodeClassPackage = node.getClass().toString();
//            String[] nodeClassPackageSplit = node.getClass().toString().split("\\.");
            String nodeClass = Util.getClassLastName(nodeClassPackage);

            node.removeComment();

            if (isContain(nodeClassPackage, "Comment")) {
                addChildNodesToVisited(node);
            } else if (isContain(nodeClassPackage, new String[]{"VoidType", "UnknownType"})) {
                stringPrint(nodeClass);
            } else if (isContain(nodeClassPackage, "WildcardType")) {
                WildcardType wildcardType = (WildcardType) node;
                stringPrint(nodeClass);
                addChildNodeList(wildcardType, wildcardType.getAnnotations());
//                travelNode(wildcardType.getElementType());
                wildcardType.getSuperType().ifPresent(c -> {
                    stringPrint("SuperType");
                    addNextToken(nodeClass, "SuperType");
                    addNextToken("SuperType", wildcardType.getSuperType().get());
//                    travelNode(c);
                });
                wildcardType.getExtendedType().ifPresent(c -> {
                    stringPrint("ExtendedType");
                    addNextToken(nodeClass, "ExtendedType");
                    addNextToken("ExtendedType", wildcardType.getExtendedType().get());
//                    travelNode(c);
                });
            } else if (isContain(nodeClassPackage, "UnionType")) {
                UnionType unionType = (UnionType) node;
                addChildNodeList(unionType, unionType.getAnnotations());
                addChildNextTokenList(unionType, unionType.getElements());
                processNodeListNodeTravel(unionType.getAnnotations());
                processNodeListNodeTravel(unionType.getElements());
            } else if (isContain(nodeClassPackage, "IntersectionType")) {
                IntersectionType intersectionType = (IntersectionType) node;
                stringPrint(nodeClass);
                addChildNodeList(intersectionType, intersectionType.getAnnotations());
                addChildNextTokenList(intersectionType, intersectionType.getElements());
                processNodeListNodeTravel(intersectionType.getAnnotations());
                processNodeListNodeTravel(intersectionType.getElements());
            } else if (isContain(nodeClassPackage, "ArrayType")) {
                ArrayType arrayType = (ArrayType) node;
                stringPrint(nodeClass);
                addChildNodeList(arrayType, arrayType.getAnnotations());
                addChildToken(arrayType, arrayType.getComponentType());
                addNextNodeToken(arrayType.getComponentType(), arrayType.getElementType());
                addChildToken(arrayType, arrayType.getElementType());
                addChildNodesToVisited(arrayType.getComponentType());
                addChildNodesToVisited(arrayType.getElementType());
                processNodeListNodeTravel(arrayType.getAnnotations());
                travelNode(arrayType.getComponentType());
                travelNode(arrayType.getElementType());
                stringPrint(String.valueOf(arrayType.getArrayLevel()));
            } else if (isContain(nodeClassPackage, "Annotation")) {
            } else if (isContain(nodeClassPackage, "MarkerAnnotationExpr")) {
                stringPrint(nodeClass);
                addChildToken(node, node.toString());
                nodePrint(node);
                addChildNodesToVisited(node);
            } else if (isContain(nodeClassPackage, "InitializerDeclaration")) {
                InitializerDeclaration initializerDeclaration = (InitializerDeclaration) node;
                stringPrint("InitializerDeclaration");
                addChildNodeList(initializerDeclaration, initializerDeclaration.getAnnotations());
                addChildNode(initializerDeclaration, initializerDeclaration.getBody());
                processNodeListNodeTravel(initializerDeclaration.getAnnotations());
                travelNode(initializerDeclaration.getBody());
                stringPrint("InitializerDeclaration END");
            } else if (isContain(nodeClassPackage, "AnnotationMemberDeclaration")) {
                AnnotationMemberDeclaration annotationMemberDeclaration = (AnnotationMemberDeclaration) node;
                stringPrint(nodeClass);
                addChildNodeList(annotationMemberDeclaration, annotationMemberDeclaration.getAnnotations());
                addChildModifiers(annotationMemberDeclaration, annotationMemberDeclaration.getModifiers());
                addChildToken(annotationMemberDeclaration, annotationMemberDeclaration.getType());
                addChildToken(annotationMemberDeclaration, annotationMemberDeclaration.getName());
                addNextNodeToken(annotationMemberDeclaration.getType(), annotationMemberDeclaration.getName());
                annotationMemberDeclaration.getDefaultValue().ifPresent((d) ->
                        addChildToken(annotationMemberDeclaration, annotationMemberDeclaration.getDefaultValue().get()));
                processNodeListNodeTravel(annotationMemberDeclaration.getAnnotations());
                processModifiers(annotationMemberDeclaration.getModifiers());
                travelNode(annotationMemberDeclaration.getType());
                travelNode(annotationMemberDeclaration.getName());
                annotationMemberDeclaration.getDefaultValue().ifPresent((c) -> stringPrint("default"));
                processOptionalNodeTravel(annotationMemberDeclaration.getDefaultValue());
            } else if (isContain(nodeClassPackage, "AnnotationDeclaration")) {
                AnnotationDeclaration annotationDeclaration = (AnnotationDeclaration) node;
                stringPrint(nodeClass);
                addChildNodeList(annotationDeclaration, annotationDeclaration.getAnnotations());
                addChildModifiers(annotationDeclaration, annotationDeclaration.getModifiers());
                addChildToken(annotationDeclaration, annotationDeclaration.getName());
                addChildNodeList(annotationDeclaration, annotationDeclaration.getMembers());
                addChildNodeList(annotationDeclaration, annotationDeclaration.getFields());
                addChildNodeList(annotationDeclaration, annotationDeclaration.getMethods());
                processNodeListNodeTravel(annotationDeclaration.getAnnotations());
                processModifiers(annotationDeclaration.getModifiers());
                travelNode(annotationDeclaration.getName());
                processNodeListNodeTravel(annotationDeclaration.getMembers());
                processNodeListNodeTravel(annotationDeclaration.getFields());
                processNodeListNodeTravel(annotationDeclaration.getMethods());
            } else if (isContain(nodeClassPackage, "FieldDeclaration")) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
                addChildTokenList(fieldDeclaration, fieldDeclaration.getAnnotations());
                addChildModifiers(fieldDeclaration, fieldDeclaration.getModifiers());
//                addNextNodeToken(fieldDeclaration.getModifiers(), fieldDeclaration.getElementType());
                addChildNodeList(fieldDeclaration, fieldDeclaration.getVariables());

                stringPrint(nodeClass);
                processNodeListNodeTravel(fieldDeclaration.getAnnotations());
                processModifiers(fieldDeclaration.getModifiers());
                processNodeListNodeTravel(fieldDeclaration.getVariables());
            } else if (isContain(nodeClassPackage, "ClassOrInterfaceDeclaration")) {
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) node;
                stringPrint(nodeClass);
               addChildTokenList(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getAnnotations());
                addChildModifiers(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getModifiers());
                addChildToken(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getName());
                addChildNextTokenList(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getImplementedTypes());
                addChildNodeList(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getMembers());
                addChildNodeList(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getFields());
                addChildNodeList(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getConstructors());
                addChildNodeList(classOrInterfaceDeclaration, classOrInterfaceDeclaration.getMethods());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getAnnotations());
                processModifiers(classOrInterfaceDeclaration.getModifiers());
                travelNode(classOrInterfaceDeclaration.getName());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getTypeParameters());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getExtendedTypes());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getImplementedTypes());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getMembers());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getFields());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getConstructors());
                processNodeListNodeTravel(classOrInterfaceDeclaration.getMethods());
            } else if (isContain(nodeClassPackage, "EnumDeclaration")) {
                EnumDeclaration enumDeclaration = (EnumDeclaration) node;
                stringPrint(nodeClass);
                addChildTokenList(enumDeclaration, enumDeclaration.getAnnotations());
                addChildModifiers(enumDeclaration, enumDeclaration.getModifiers());
                addChildToken(enumDeclaration, enumDeclaration.getName());
                addChildNextTokenList(enumDeclaration, enumDeclaration.getImplementedTypes());
                addChildNextTokenList(enumDeclaration, enumDeclaration.getEntries());
                addChildNodeList(enumDeclaration, enumDeclaration.getMembers());
                addChildNodeList(enumDeclaration, enumDeclaration.getFields());
                addChildNodeList(enumDeclaration, enumDeclaration.getConstructors());
                addChildNodeList(enumDeclaration, enumDeclaration.getMethods());
                processNodeListNodeTravel(enumDeclaration.getAnnotations());
                processModifiers(enumDeclaration.getModifiers());
                travelNode(enumDeclaration.getName());
                processNodeListNodeTravel(enumDeclaration.getImplementedTypes());
                processNodeListNodeTravel(enumDeclaration.getEntries());
                processNodeListNodeTravel(enumDeclaration.getMembers());
                processNodeListNodeTravel(enumDeclaration.getFields());
                processNodeListNodeTravel(enumDeclaration.getConstructors());
                processNodeListNodeTravel(enumDeclaration.getMethods());
            } else if (isContain(nodeClassPackage, "EnumConstantDeclaration")) {
                EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) node;
                nodePrint(enumConstantDeclaration.getName());
                addChildNodeList(enumConstantDeclaration, enumConstantDeclaration.getAnnotations());
                addChildToken(enumConstantDeclaration, enumConstantDeclaration.getName());
                addChildNodeList(enumConstantDeclaration, enumConstantDeclaration.getArguments());
                addChildNodeList(enumConstantDeclaration, enumConstantDeclaration.getClassBody());
                addChildNodesToVisited(enumConstantDeclaration.getName());
                processNodeListNodeTravel(enumConstantDeclaration.getAnnotations());
                processNodeListNodeTravel(enumConstantDeclaration.getArguments());
                enumConstantDeclaration.getClassBody().forEach(this::travelNode);
            } else if (isContain(nodeClassPackage, "MethodDeclaration")) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) node;
                addChildNodeList(methodDeclaration, methodDeclaration.getAnnotations());
                addChildModifiers(methodDeclaration, methodDeclaration.getModifiers());
                addChildToken(methodDeclaration, methodDeclaration.getType());
                addChildToken(methodDeclaration, methodDeclaration.getName());
                addNextNodeToken(methodDeclaration.getType(), methodDeclaration.getName());
                addChildNodeList(methodDeclaration, methodDeclaration.getParameters());
                addChildNodeListThrows(methodDeclaration, methodDeclaration.getThrownExceptions());
                addChildOptionalNode(methodDeclaration, methodDeclaration.getBody());
                addReturnsTo(methodDeclaration);
                processNodeListNodeTravel(methodDeclaration.getAnnotations());
                stringPrint(nodeClass);
                processModifiers(methodDeclaration.getModifiers());
                travelNode(methodDeclaration.getType());
                travelNode(methodDeclaration.getName());
                methodDeclaration.getParameters().forEach(parameter -> {
                    addDataFlowVarLinksForSimpleName(methodDeclaration, parameter.getName(), parameter);
                });
                processNodeListNodeTravel(methodDeclaration.getParameters());
                processThrows(methodDeclaration.getThrownExceptions());
                processOptionalNodeTravel(methodDeclaration.getBody());
            } else if (isContain(nodeClassPackage, "ConstructorDeclaration")) {
                ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) node;
                stringPrint(nodeClass);
                addChildNodeList(constructorDeclaration, constructorDeclaration.getAnnotations());
                addChildModifiers(constructorDeclaration, constructorDeclaration.getModifiers());
                addChildToken(constructorDeclaration, constructorDeclaration.getName());
                addChildNodeList(constructorDeclaration, constructorDeclaration.getParameters());
                addChildNodeListThrows(constructorDeclaration, constructorDeclaration.getThrownExceptions());
                addChildNode(constructorDeclaration, constructorDeclaration.getBody());
                processNodeListNodeTravel(constructorDeclaration.getAnnotations());
                processModifiers(constructorDeclaration.getModifiers());
                travelNode(constructorDeclaration.getName());
                processNodeListNodeTravel(constructorDeclaration.getParameters());
                processThrows(constructorDeclaration.getThrownExceptions());
                travelNode(constructorDeclaration.getBody());
            } else if (isContain(nodeClassPackage, "LocalClassDeclarationStmt")) {
                LocalClassDeclarationStmt localClassDeclarationStmt = (LocalClassDeclarationStmt) node;
                addChildNode(localClassDeclarationStmt, localClassDeclarationStmt.getClassDeclaration());
            } else if (isContain(nodeClassPackage, "ExplicitConstructorInvocationStmt")) {
                ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt = (ExplicitConstructorInvocationStmt) node;
                stringPrint("ExplicitConstructorInvocationStmt");
                addChildOptionalNodeList(explicitConstructorInvocationStmt, explicitConstructorInvocationStmt.getTypeArguments());
                addChildNodeList(explicitConstructorInvocationStmt, explicitConstructorInvocationStmt.getArguments());
                addChildOptionalNode(explicitConstructorInvocationStmt, explicitConstructorInvocationStmt.getExpression());
                processOptionalNodeListNodeTravel(explicitConstructorInvocationStmt.getTypeArguments());
                processNodeListNodeTravel(explicitConstructorInvocationStmt.getArguments());
                processOptionalNodeTravel(explicitConstructorInvocationStmt.getExpression());
                stringPrint("ExplicitConstructorInvocationStmt END");
            } else if (isContain(nodeClassPackage, "BreakStmt")) {
                addChildToken(node, node.toString());
                stringPrint(nodeClass);
            } else if (isContain(nodeClassPackage, "ContinueStmt")) {
                addChildToken(node, node.toString());
                stringPrint(nodeClass);
            } else if (isContain(nodeClassPackage, "AssertStmt")) {
                addChildNode(node, ((AssertStmt) node).getCheck());
                travelNode(((AssertStmt) node).getCheck());
                stringPrint(nodeClass);
            } else if (isContain(nodeClassPackage, "ExpressionStmt")) {
                ExpressionStmt expressionStmt = (ExpressionStmt) node;
                addChildNode(expressionStmt, expressionStmt.getExpression());
                if (expressionStmt.getExpression().isVariableDeclarationExpr()) {
                    expressionStmt.getParentNode().ifPresent(parent ->
                            addDataFlowVarLinks(parent, (VariableDeclarationExpr) expressionStmt.getExpression()));
                }
            } else if (isContain(nodeClassPackage, "WhileStmt")) {
                WhileStmt whileStmt = (WhileStmt) node;
                addChildNode(whileStmt, whileStmt.getCondition());
                addChildNode(whileStmt, whileStmt.getBody());
            } else if (isContain(nodeClassPackage, "TryStmt")) {
                TryStmt tryStmt = (TryStmt) node;
                addChildNode(tryStmt, tryStmt.getTryBlock());
                addChildNodeList(tryStmt, tryStmt.getCatchClauses());
                addChildOptionalNode(tryStmt, tryStmt.getFinallyBlock());
            } else if (isContain(nodeClassPackage, "ThrowStmt")) {
                ThrowStmt throwStmt = (ThrowStmt) node;
                addChildNode(throwStmt, throwStmt.getExpression());
            } else if (isContain(nodeClassPackage, "SynchronizedStmt")) {
                SynchronizedStmt synchronizedStmt = (SynchronizedStmt) node;
                addChildNode(synchronizedStmt, synchronizedStmt.getExpression());
                addChildNode(synchronizedStmt, synchronizedStmt.getBody());
            } else if (isContain(nodeClassPackage, "SwitchStmt")) {
                SwitchStmt switchStmt = (SwitchStmt) node;
                addChildNode(switchStmt, switchStmt.getSelector());
                addChildNodeList(switchStmt, switchStmt.getEntries());
                processNodeListNodeTravel(switchStmt.getEntries());
            } else if (isContain(nodeClassPackage, "SwitchEntryStmt")) {
                SwitchEntryStmt switchEntryStmt = (SwitchEntryStmt) node;
                addChildToken(switchEntryStmt, nodeClass);
                addChildOptionalNode(switchEntryStmt, switchEntryStmt.getLabel());
                addChildNodeList(switchEntryStmt, switchEntryStmt.getStatements());
            } else if (isContain(nodeClassPackage, "ReturnStmt")) {
                ReturnStmt returnStmt = (ReturnStmt) node;
                addChildToken(returnStmt, nodeClass);
                addChildOptionalNode(returnStmt, returnStmt.getExpression());
            } else if (isContain(nodeClassPackage, "LabeledStmt")) {
                LabeledStmt labeledStmt = (LabeledStmt) node;
                addChildToken(labeledStmt, labeledStmt.getLabel());
                addChildNode(labeledStmt, labeledStmt.getStatement());
            } else if (isContain(nodeClassPackage, "IfStmt")) {
                IfStmt ifStmt = (IfStmt) node;
                addChildNode(ifStmt, ifStmt.getCondition());
                addChildNode(ifStmt, ifStmt.getThenStmt());
                addChildOptionalNode(ifStmt, ifStmt.getElseStmt());
                addGuardedVariable(ifStmt);
            } else if (isContain(nodeClassPackage, "ForStmt")) {
                ForStmt forStmt = (ForStmt) node;
                addChildNodeList(forStmt, forStmt.getInitialization());
                addChildOptionalNode(forStmt, forStmt.getCompare());
                addChildNodeList(forStmt, forStmt.getUpdate());
                addChildNode(forStmt, forStmt.getBody());
                forStmt.getInitialization().forEach(expression ->
                        expression.ifVariableDeclarationExpr(variableDeclarationExpr -> addDataFlowVarLinks(forStmt, variableDeclarationExpr))
                );
            } else if (isContain(nodeClassPackage, "ForEachStmt")) {
                ForEachStmt foreachStmt = (ForEachStmt) node;
                addChildNode(foreachStmt, foreachStmt.getVariable());
                addChildNode(foreachStmt, foreachStmt.getIterable());
                addChildNode(foreachStmt, foreachStmt.getBody());
                addDataFlowVarLinks(foreachStmt, foreachStmt.getVariable());
            } else if (isContain(nodeClassPackage, "DoStmt")) {
                DoStmt doStmt = (DoStmt) node;
                addChildNode(doStmt, doStmt.getBody());
                addChildNode(doStmt, doStmt.getCondition());
            } else if (isContain(nodeClassPackage, "BlockStmt")) {
                BlockStmt blockStmt = (BlockStmt) node;
                addChildNodeList(blockStmt, blockStmt.getStatements());
            } else if (isContain(nodeClassPackage, "CatchClause")) {
                CatchClause catchClause = (CatchClause) node;
                addChildNode(catchClause, catchClause.getParameter());
                addChildNode(catchClause, catchClause.getBody());
                stringPrint(nodeClass);
                travelNode(catchClause.getParameter());
                travelNode(catchClause.getBody());
            } else if (isContain(nodeClassPackage, "stmt")) {
                Statement statement = (Statement) node;
                addChildNodeList(statement, statement.getChildNodes());
                stringPrint(nodeClass);
                travelNode(node);
                stringPrint(nodeClass + " END");
            } else if (isContain(nodeClassPackage, "TypeParameter")) {
                TypeParameter parameter = (TypeParameter) node;
                addChildNodeList(parameter, parameter.getAnnotations());
                addChildToken(parameter, parameter.getElementType());
                parameter.getAnnotations().forEach(this::travelNode);
                if (parameter.getElementType().isUnknownType()) {
                    stringPrint("UnknownType");
                    stringPrint(parameter.getNameAsString());
                } else {
                    travelNode(parameter.getElementType());
                    travelNode(parameter.getName());
                }
            } else if (isContain(nodeClassPackage, "ParameterCompare")) {
                Parameter parameter = (Parameter) node;
                addChildNodeList(parameter, parameter.getAnnotations());
                parameter.getAnnotations().forEach(this::travelNode);
                addChildToken(parameter, parameter.getType());
                addChildModifiers(parameter, parameter.getModifiers());
                addChildNodeList(parameter, parameter.getVarArgsAnnotations());
                addChildToken(parameter, parameter.getName());
                if (parameter.getType().isUnknownType()) {
                    stringPrint("UnknownType");
                    addChildNode(parameter, parameter.getType());
                    stringPrint(parameter.getNameAsString());
                } else {
                    travelNode(parameter.getType());
                    processModifiers(parameter.getModifiers());
                    processNodeListNodeTravel(parameter.getVarArgsAnnotations());
                    travelNode(parameter.getName());
                }
            } else if (isContain(nodeClassPackage, "ClassOrInterfaceType")) {
                ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) node;
                addChildOptionalNode(classOrInterfaceType, classOrInterfaceType.getScope());
                addChildToken(classOrInterfaceType, classOrInterfaceType.getName());
                addChildOptionalNodeList(classOrInterfaceType, classOrInterfaceType.getTypeArguments());
                processOptionalNodeTravel(classOrInterfaceType.getScope());
                nodePrint(classOrInterfaceType.getName());
                processOptionalNodeListNodeTravel(classOrInterfaceType.getTypeArguments());
            } else if (isContain(nodeClassPackage, "VariableDeclarationExpr")) {
                stringPrint(nodeClass);
                VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) node;
                addChildNodeList(variableDeclarationExpr, variableDeclarationExpr.getAnnotations());
                addChildModifiers(variableDeclarationExpr, variableDeclarationExpr.getModifiers());
                addChildNodeList(variableDeclarationExpr, variableDeclarationExpr.getVariables());
                if (!variableDeclarationExpr.getModifiers().isEmpty()) {
                    stringPrint(variableDeclarationExpr.getModifiers().toString());
                }
                processModifiers(variableDeclarationExpr.getModifiers());
                travelNode(variableDeclarationExpr);
            } else if (isContain(nodeClassPackage, "VariableDeclarator")) {
                stringPrint(nodeClass);
                VariableDeclarator variableDeclarator = (VariableDeclarator) node;
                addChildToken(variableDeclarator, variableDeclarator.getType());
                addChildToken(variableDeclarator, variableDeclarator.getName());
                addNextNodeToken(variableDeclarator.getType(), variableDeclarator.getName());
                addChildOptionalNode(variableDeclarator, variableDeclarator.getInitializer());
                addChildNodesToVisited(variableDeclarator.getType());
                addChildNodesToVisited(variableDeclarator.getNameAsExpression());
                nodePrint(variableDeclarator.getType());
                stringPrint(variableDeclarator.getNameAsString());
                variableDeclarator.getInitializer().ifPresent((consumer) -> {
                    stringPrint("=");
                    travelNode(consumer);
                });
            } else if (isContain(nodeClassPackage, new String[]{"SimpleName", "NameExpr", "Name"})) {
                nodePrint(node);
            } else if (isContain(nodeClassPackage, "Binary")) {
                BinaryExpr binaryExpr = (BinaryExpr) node;
                addChildNode(binaryExpr, binaryExpr.getLeft());
                addChildTokenForObjectV(binaryExpr, binaryExpr.getOperator());
                addChildNode(binaryExpr, binaryExpr.getRight());
                travelNode(binaryExpr.getLeft());
                stringPrint(binaryExpr.getOperator().toString());
                travelNode(binaryExpr.getRight());
            } else if (isContain(nodeClassPackage, "Unary")) {
                UnaryExpr unaryExpr = (UnaryExpr) node;
                addChildNode(unaryExpr, unaryExpr.getExpression());
                addChildTokenForObjectV(unaryExpr, unaryExpr.getOperator());
                travelNode(unaryExpr.getExpression());
                stringPrint(unaryExpr.getOperator().asString());
            } else if (isContain(nodeClassPackage, "CastExpr")) {
                CastExpr castExpr = (CastExpr) node;
                addChildToken(castExpr, castExpr.getType());
                addChildNode(castExpr, castExpr.getExpression());
                stringPrint(nodeClass);
                travelNode(castExpr.getType());
                travelNode(castExpr.getExpression());
            } else if (isContain(nodeClassPackage, "ClassExpr")) {
                nodePrint(node);
                ClassExpr classExpr = (ClassExpr) node;
                addChildToken(classExpr, classExpr.getType());
                addChildNodesToVisited(node);
            } else if (isContain(nodeClassPackage, "MethodCallExpr")) {
                MethodCallExpr methodCallExpr = (MethodCallExpr) node;
                addChildOptionalNode(methodCallExpr, methodCallExpr.getScope());
                addChildToken(methodCallExpr, methodCallExpr.getName());
                addChildNodeList(methodCallExpr, methodCallExpr.getArguments());
                addChildOptionalNodeList(methodCallExpr, methodCallExpr.getTypeArguments());
                processOptionalNodeTravel(methodCallExpr.getScope());
                for (MethodDeclaration called : getMethodDeclarations()) {
                    if (methodCallExpr.getNameAsString().equals(called.getNameAsString())
                            && methodCallExpr.getArguments().size() == called.getParameters().size()) {
                        addFormalArgs(methodCallExpr, called);
                        addMethodCall(methodCallExpr, called);
                        break;
                    }
                }
                stringPrint(nodeClass);
                travelNode(methodCallExpr.getName());
                methodCallExpr.getArguments().forEach(this::travelNode);
            } else if (isContain(nodeClassPackage, "MethodReferenceExpr")) {
                MethodReferenceExpr methodReferenceExpr = (MethodReferenceExpr) node;
                stringPrint(nodeClass);
                addChildNode(methodReferenceExpr, methodReferenceExpr.getScope());
                addChildToken(methodReferenceExpr, methodReferenceExpr.getIdentifier());
                addChildOptionalNodeList(methodReferenceExpr, methodReferenceExpr.getTypeArguments());
                processOptionalPrint(methodReferenceExpr.getTypeArguments());
                travelNode(methodReferenceExpr.getScope());
                for (MethodDeclaration called : getMethodDeclarations()) {
                    if (methodReferenceExpr.getIdentifier().equals(called.getNameAsString())) {
                        addMethodCall(methodReferenceExpr, called);
                        break;
                    }
                }
                stringPrint(methodReferenceExpr.getIdentifier());
                addChildNodesToVisited(node);
            } else if (isContain(nodeClassPackage, "TypeExpr")) {
                TypeExpr typeExpr = (TypeExpr) node;
                addChildToken(typeExpr, typeExpr.getType());
                stringPrint(nodeClass);
                nodePrint(node);
            } else if (isContain(nodeClassPackage, "LiteralExpr")) {
                processLiteralExpr(node);
            } else if (isContain(nodeClassPackage, "AssignExpr")) {
                AssignExpr assignExpr = (AssignExpr) node;
                addChildNode(assignExpr, assignExpr.getTarget());
                addChildTokenForObjectV(assignExpr, assignExpr.getOperator());
                addChildNode(assignExpr, assignExpr.getValue());
                stringPrint(nodeClass);
                travelNode(assignExpr.getTarget());
                stringPrint("=");
                travelNode(assignExpr.getValue());
            } else if (isContain(nodeClassPackage, "FieldAccessExpr")) {
                FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
                addChildNode(fieldAccessExpr, fieldAccessExpr.getScope());
                addChildToken(fieldAccessExpr, fieldAccessExpr.getName());
                addChildOptionalNodeList(fieldAccessExpr, fieldAccessExpr.getTypeArguments());
                travelNode(fieldAccessExpr.getScope());
                stringPrint("FieldAccess");
                travelNode(fieldAccessExpr.getNameAsExpression());
            } else if (isContain(nodeClassPackage, "ArrayAccessExpr")) {
                ArrayAccessExpr arrayAccessExpr = (ArrayAccessExpr) node;
                addChildNode(arrayAccessExpr, arrayAccessExpr.getName());
                addChildNode(arrayAccessExpr, arrayAccessExpr.getIndex());
                stringPrint(nodeClass);
                travelNode(arrayAccessExpr.getName());
                nodePrint(arrayAccessExpr.getIndex());
            } else if (isContain(nodeClassPackage, "ArrayCreationExpr")) {
                ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) node;
                addChildNode(arrayCreationExpr, arrayCreationExpr.getElementType());
                addChildNextTokenList(arrayCreationExpr, arrayCreationExpr.getLevels());
                addChildOptionalNode(arrayCreationExpr, arrayCreationExpr.getInitializer());
                stringPrint(nodeClass);
                nodePrint(arrayCreationExpr.getElementType());
                addChildNodesToVisited(arrayCreationExpr.getElementType());
                if (arrayCreationExpr.getLevels().isEmpty()) {
                    stringPrint("ArrayCreationLevel Empty");
                } else {
                    stringPrint("ArrayCreationLevel NotEmpty");
                    arrayCreationExpr.getLevels().forEach(this::travelNode);
                }
                if (arrayCreationExpr.getInitializer().isPresent()) {
                    stringPrint("ArrayInitializerExpr");
                    processOptionalNodeTravel(arrayCreationExpr.getInitializer());
                    stringPrint("ArrayInitializerExpr END");
                    addChildNodesToVisited(arrayCreationExpr.getInitializer().get());
                }
            } else if (isContain(nodeClassPackage, "ArrayInitializerExpr")) {
                ArrayInitializerExpr arrayInitializerExpr = (ArrayInitializerExpr) node;
                addChildNodeList(arrayInitializerExpr, arrayInitializerExpr.getValues());
                processNodeListNodeTravel(arrayInitializerExpr.getValues());
            } else if (isContain(nodeClassPackage, "ArrayCreationLevel")) {
                ArrayCreationLevel arrayCreationLevel = (ArrayCreationLevel) node;
                if (arrayCreationLevel.getDimension().isPresent()) {
                    stringPrint("Dimension NotEmpty");
                    travelNode(arrayCreationLevel.getDimension().get());
                } else {
                    stringPrint("Dimension Empty");
                }
            } else if (isContain(nodeClassPackage, "ObjectCreationExpr")) {
                ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) node;
                addChildOptionalNodeList(objectCreationExpr, objectCreationExpr.getTypeArguments());
                addChildOptionalNode(objectCreationExpr, objectCreationExpr.getScope());
                addNextNodeToken(objectCreationExpr, objectCreationExpr.getType());
                addChildNodeList(objectCreationExpr, objectCreationExpr.getArguments());
                stringPrint(nodeClass);
                nodePrint(objectCreationExpr.getType());
                addChildNodesToVisited(objectCreationExpr.getType());
//                stringPrint(objectCreationExpr.getArguments().toString());
                stringPrint(objectCreationExpr.getScope().toString());
                stringPrint(objectCreationExpr.getTypeArguments().toString());
                travelNode(node);
//                objectCreationExpr.
            } else if (isContain(nodeClassPackage, "LambdaExpr")) {
                LambdaExpr lambdaExpr = (LambdaExpr) node;
                addChildNodeList(lambdaExpr, lambdaExpr.getParameters());
                addChildNode(lambdaExpr, lambdaExpr.getBody());
                addChildOptionalNode(lambdaExpr, lambdaExpr.getExpressionBody());
                stringPrint(nodeClass);
                processNodeListNodeTravel(lambdaExpr.getParameters());
                travelNode(lambdaExpr.getBody());
                processOptionalNodeTravel(lambdaExpr.getExpressionBody());
            } else if (isContain(nodeClassPackage, "EnclosedExpr")) {
                EnclosedExpr enclosedExpr = (EnclosedExpr) node;
                addChildNode(enclosedExpr, enclosedExpr.getInner());
                stringPrint(nodeClass);
                travelNode(enclosedExpr.getInner());
            } else if (isContain(nodeClassPackage, "InstanceOfExpr")) {
                InstanceOfExpr instanceOfExpr = (InstanceOfExpr) node;
                addChildNode(instanceOfExpr, instanceOfExpr.getExpression());
                addChildToken(instanceOfExpr, nodeClass);
                addChildToken(instanceOfExpr, instanceOfExpr.getType());
                addNextToken(nodeClass, instanceOfExpr.getType());
                travelNode(instanceOfExpr.getExpression());
                stringPrint(nodeClass);
                travelNode(instanceOfExpr.getType());
            } else if (isContain(nodeClassPackage, "MemberValuePair")) {
                MemberValuePair memberValuePair = (MemberValuePair) node;
                addChildToken(memberValuePair, memberValuePair.getName());
                addChildNode(memberValuePair, memberValuePair.getValue());
                stringPrint(nodeClass);
                travelNode(memberValuePair.getName());
                travelNode(memberValuePair.getValue());
            } else if (isContain(nodeClassPackage, "SuperExpr")) {
                SuperExpr superExpr = (SuperExpr) node;
                addChildToken(superExpr, nodeClass);
                addChildOptionalNode(superExpr, superExpr.getClassExpr());
            } else if (isContain(nodeClassPackage, "ThisExpr")) {
                ThisExpr thisExpr = (ThisExpr) node;
                addChildToken(thisExpr, nodeClass);
                addChildOptionalNode(thisExpr, thisExpr.getClassExpr());
                stringPrint(nodeClass);
            } else {
                addChildNodeList(node, node.getChildNodes());
                stringPrint(nodeClass);
                nodePrint(node);
                travelNode(node);
            }
        }
        return false;
    }

    public void addFormalArgs(MethodCallExpr caller, MethodDeclaration called) {
        List<Parameter> parameters = called.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            putEdge(RangeNode.newInstance(caller.getArguments().get(i)),
                    RangeNode.newInstance(parameters.get(i).getName()), EDGE_FORMAL_ARG_NAME);
        }
    }

    public void addMethodCall(Node caller, MethodDeclaration called) {
        putEdge(RangeNode.newInstance(caller), RangeNode.newInstance(called), EDGE_METHOD_CALL);
        mCalledMethodDecls.add(called);
    }

    public void travelCalled() {
        if (mCalledMethodDecls.isEmpty()) {
            return;
        }
        MethodDeclaration called = mCalledMethodDecls.remove(0);
        if (called.getRange().isPresent() || !mVisitedNodes.contains(called.getRange().get())) {
            initCFG();
            travelNodeForCFG(called);
            travelNode(called);
        }
        travelCalled();
    }

    public void travelNodeForCFG(Node parentNode) {//对函数声明中的所有节点，进行控制流图构图(主要包括条件语句、循环语句等)
        // 只要当前传入结点的孩子结点不为空，就对每一个孩子结点进行分析
        if (!parentNode.getChildNodes().isEmpty()) {
            for (Node node : parentNode.getChildNodes()) {//深度遍历所有节点的孩子节点
                switch (node.getClass().toString().substring("class com.github.javaparser.ast.stmt.".length())) {
                    case "TryStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                        TryStmt tryStmt = (TryStmt) node;
                        travelNodeForCFG(tryStmt.getTryBlock());
                        mPreTempNodes.addAll(mPreNodes);
                        tryStmt.getCatchClauses().forEach(catchClause -> {
                            addNextExecEdge(tryStmt, catchClause.getParameter());
                            resetPreNodes(catchClause.getParameter());
                            travelNodeForCFG(catchClause.getBody());
                            mPreTempNodes.addAll(mPreNodes);
                            resetPreNodes(node);
                        });
                        mPreNodes.addAll(mPreTempNodes);
                        tryStmt.getFinallyBlock().ifPresent(this::travelNodeForCFG);
                    }
                    break;
                    case "ExpressionStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                    }
                    break;
                    case "ReturnStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                    }
                    break;
                    case "IfStmt": {
                        processIfStmt(node);
                    }
                    break;
                    case "SwitchStmt": {
                        addNextExecEdgeForAllPres(node);
                        SwitchStmt switchStmt = (SwitchStmt) node;
                        Expression selector = switchStmt.getSelector();
                        addNextExecEdge(node, selector);
                        mPreNodes.clear();
                        mBreakNodes.clear();
                        for (SwitchEntryStmt entry : switchStmt.getEntries()) {
                            mPreNodes.add(selector);
                            addNextExecEdgeForAllPres(entry);
                            entry.getLabel().ifPresent(label -> {
                                addNextExecEdge(entry, label);
                            });
                            resetPreNodes(entry);
                            travelNodeForCFG(entry);
                        }
                        mPreNodes.addAll(mBreakNodes);
                    }
                    break;
                    case "WhileStmt": {
                        WhileStmt whileStmt = (WhileStmt) node;
                        addNextExecEdgeForAllPres(node);
                        addNextExecEdge(node, whileStmt.getCondition());
                        resetPreNodes(whileStmt.getCondition());
                        mBreakNodes.clear();
                        mContinueNodes.clear();
                        travelNodeForCFG(whileStmt.getBody());
                        mPreNodes.addAll(mContinueNodes);
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(whileStmt.getCondition());
                        mPreNodes.addAll(mBreakNodes);
                    }
                    break;
                    case "DoStmt": {
                        DoStmt doStmt = (DoStmt) node;
                        travelNodeForCFG(doStmt.getBody());
                        addNextExecEdgeForAllPres(doStmt.getCondition());
                        resetPreNodes(doStmt.getCondition());
                        travelNodeForCFG(doStmt.getBody());
                        resetPreNodes(doStmt.getCondition());
                    }
                    break;
                    case "ForEachStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                        ForEachStmt foreachStmt = (ForEachStmt) node;
                        addNextExecEdgeForAllPres(foreachStmt.getIterable());
                        resetPreNodes(foreachStmt.getIterable());
                        addNextExecEdgeForAllPres(foreachStmt.getVariable());
                        resetPreNodes(foreachStmt.getVariable());
                        travelNodeForCFG(foreachStmt.getBody());
                        addNextExecEdgeForAllPres(foreachStmt.getIterable());
                        resetPreNodes(foreachStmt.getIterable());
                    }
                    break;
                    case "ForStmt": {
                        addNextExecEdgeForAllPres(node);
                        ForStmt forStmt = (ForStmt) node;
                        resetPreNodes(forStmt);
                        forStmt.getInitialization().forEach(init -> {
                            addNextExecEdgeForAllPres(init);
                            resetPreNodes(init);
                        });
                        forStmt.getCompare().ifPresent(compare -> {
                            addNextExecEdgeForAllPres(compare);
                            resetPreNodes(compare);
                        });
                        mBreakNodes.clear();
                        mContinueNodes.clear();
                        travelNodeForCFG(forStmt.getBody());
                        mPreNodes.addAll(mContinueNodes);
                        forStmt.getUpdate().forEach(update -> {
                            addNextExecEdgeForAllPres(update);
                            resetPreNodes(update);
                        });
                        forStmt.getCompare().ifPresent(compare -> {
                            addNextExecEdgeForAllPres(compare);
                            resetPreNodes(compare);
                        });
                        mPreNodes.addAll(mBreakNodes);
                    }
                    break;
                    case "ThrowStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                    }
                    break;
                    case "AssertStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                    }
                    break;
                    case "LabeledStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                    }
                    break;
                    case "SynchronizedStmt": {
                        addNextExecEdgeForAllPres(node);
                        resetPreNodes(node);
                        SynchronizedStmt synchronizedStmt = (SynchronizedStmt) node;
                        addNextExecEdgeForAllPres(synchronizedStmt.getExpression());
                        resetPreNodes(synchronizedStmt.getExpression());
                        travelNodeForCFG(synchronizedStmt.getBody());
                    }
                    break;
                    case "BreakStmt": {
                        addNextExecEdgeForAllPres(node);
                        mPreNodes.clear();
                        mBreakNodes.push(node);
                    }
                    break;
                    case "ContinueStmt": {
                        addNextExecEdgeForAllPres(node);
                        mPreNodes.clear();
                        mContinueNodes.push(node);
                    }
                    break;
                    default: {
                        travelNodeForCFG(node);
                    }
                    break;
                }
            }
        }
    }

    public void addNextExecEdgeForAllPres(Node childNode) {
        for (Node node : mPreNodes) {
            addNextExecEdge(node, childNode);
        }
    }

    public void addNextExecEdge(Node pre, Node succ) {
        putEdge(RangeNode.newInstance(pre), RangeNode.newInstance(succ), EDGE_NEXT_EXEC);
    }

    public void resetPreNodes(Node newPre) {
        mPreNodes.clear();
        mPreNodes.add(newPre);
    }

    public void processIfStmt(Node childNode) {
        addNextExecEdgeForAllPres(childNode);
        IfStmt ifStmt = (IfStmt) childNode;
        addNextExecEdge(childNode, ifStmt.getCondition());
        resetPreNodes(ifStmt.getCondition());
        travelNodeForCFG(ifStmt.getThenStmt());
        // 把 true 的结果保存到 mPreTempNodes
        // mPreTempNodes = mPreNodes; 绝对不能这样写，这样写是引用赋值
        mPreTempNodes.addAll(mPreNodes);
        resetPreNodes(ifStmt.getCondition());
        ifStmt.getElseStmt().ifPresent(elseStmt -> {
            addNextExecEdgeForAllPres(elseStmt);
            if (elseStmt.isIfStmt()) {
                processIfStmt(elseStmt);
            } else {
                resetPreNodes(elseStmt);
                travelNodeForCFG(elseStmt);
            }
        });
        mPreNodes.addAll(mPreTempNodes);
        mPreTempNodes.clear();
    }

    public void addGuardedVariable(IfStmt ifStmt) {
        getVariableNames(ifStmt.getCondition())
                .forEach(nameExpr -> {
                    getVariableNames(ifStmt.getThenStmt()).stream()
                            .filter(nameExprThen -> nameExprThen.equals(nameExpr))
                            .forEach(nameExprThen -> addGuardedBy(nameExprThen, ifStmt.getCondition()));
                    ifStmt.getElseStmt().ifPresent(elseStmt -> getVariableNames(elseStmt).stream()
                            .filter(nameExprThen -> nameExprThen.equals(nameExpr))
                            .forEach(nameExprThen -> addGuardedByNegation(nameExprThen, ifStmt.getCondition())));
                });
    }

    public void addReturnsTo(MethodDeclaration methodDeclaration) {
        getSpecificChildNodes(methodDeclaration, ReturnStmt.class)
                .forEach(returnStmt -> addEdgeReturnsTo(returnStmt, methodDeclaration));
    }

    public void addDataFlowVarLinks(Node parent, VariableDeclarationExpr variableDeclarationExpr) {
        for (VariableDeclarator variableDeclarator : variableDeclarationExpr.getVariables()) {
            addDataFlowVarLinksForVarDector(parent, variableDeclarator);
        }
    }

    public void addDataFlowVarLinksForVarDector(Node parent, VariableDeclarator variableDeclarator) {
        SimpleName specificName = variableDeclarator.getName();
        variableDeclarator.getInitializer().ifPresent(init ->
                addComputedFromList(specificName, getVariableNames(init)));
        addDataFlowVarLinksForSimpleName(parent, specificName, variableDeclarator);
    }

    public void addDataFlowVarLinksForSimpleName(Node parent, SimpleName specificName, Node dataFlowBeforeNode) {
        mLastUse = specificName;
        mLastWrite = specificName;
        logInfo("==========");
        logInfo(specificName);
//        logInfo(getAssignsOrStmtsContains(parent, specificName).size());
        // Process the data flow between neighbor nodes of variableDeclarator and nodes in assign or Stmts.
        updateBlockDataFlow(parent, specificName, dataFlowBeforeNode);
    }

    public <T extends Node> void updateListBlockDataFlow(NodeList<T> nodeList, SimpleName specificName) {
        nodeList.forEach(node -> updateBlockDataFlow(node, specificName));
    }

    public <T extends Node> void updateOptionalBlockDataFlow(Optional<T> optional, SimpleName specificName) {
        optional.ifPresent(node -> updateBlockDataFlow(node, specificName));
    }

    public void updateBlockDataFlow(Node parent, SimpleName specificName, Node dataFlowBeforeNode) {
        for (Node exprOrStmt : getAssignsOrStmtsContains(parent, specificName)) {
            updateLastUseWriteOfVariables(
                    getSpecificVariableFlowsBetweenNodes(parent, specificName, dataFlowBeforeNode, exprOrStmt));
            addDataFlowEdge(exprOrStmt, specificName);
            dataFlowBeforeNode = exprOrStmt;
        }
        updateLastUseWriteOfVariables(
                getSpecificVariableFlowsLastNodes(parent, specificName, dataFlowBeforeNode));
    }

    public void updateBlockDataFlow(Node parent, SimpleName specificName) {
        List<Node> exprOrStmts = getAssignsOrStmtsContains(parent, specificName);
        if (exprOrStmts.isEmpty()) {
            updateLastUseWriteOfVariables(getSpecificVariableFlows(parent, specificName));
        } else {
            Node dataFlowBeforeNode = exprOrStmts.get(0);
            updateLastUseWriteOfVariables(getSpecificVariableFlowsStartNodes(parent, specificName, dataFlowBeforeNode));
            addDataFlowEdge(dataFlowBeforeNode, specificName);
            exprOrStmts.remove(dataFlowBeforeNode);
            for (Node exprOrStmt : exprOrStmts) {
                updateLastUseWriteOfVariables(
                        getSpecificVariableFlowsBetweenNodes(parent, specificName, dataFlowBeforeNode, exprOrStmt));
                addDataFlowEdge(exprOrStmt, specificName);
                dataFlowBeforeNode = exprOrStmt;
            }
            updateLastUseWriteOfVariables(
                    getSpecificVariableFlowsLastNodes(parent, specificName, dataFlowBeforeNode));
        }
    }

    public void updateLastUseWriteOfVariables(List<NameExpr> variableFlows) {
        for (NameExpr variableNameExpr : variableFlows) {
            updateLastUseWrite(variableNameExpr);
        }
    }

    public void updateLastUseWrite(NameExpr variableNameExpr) {
        addLastUse(variableNameExpr, mLastUse);
        mLastUse = variableNameExpr;
        addLastWrite(variableNameExpr, mLastWrite);
    }

    public void addDataFlowEdge(Node node, SimpleName variableName) {
        // TODO: Process every type node, such as If/For/While, add data flow edge in it.
        if (node instanceof AssignExpr) {
            addDataFlowEdgeAssignExpr((AssignExpr) node, variableName);
        } else if (node instanceof IfStmt) {
            addDataFlowEdgeIfStmt((IfStmt) node, variableName);
        } else if (node instanceof WhileStmt) {
            addDataFlowEdgeWhileStmt((WhileStmt) node, variableName);
        } else if (node instanceof DoStmt) {
            addDataFlowEdgeDoStmt((DoStmt) node, variableName);
        } else if (node instanceof ForStmt) {
            addDataFlowEdgeForStmt((ForStmt) node, variableName);
        } else if (node instanceof ForEachStmt) {
            addDataFlowEdgeForEachStmt((ForEachStmt) node, variableName);
        } else if (node instanceof SwitchStmt) {
            addDataFlowEdgeSwitchStmt((SwitchStmt) node, variableName);
        } else if (node instanceof TryStmt) {
            addDataFlowEdgeTryStmt((TryStmt) node, variableName);
        } else {
            // TODO: improve
//            updateBlockDataFlow(node, variableName);
        }
    }

    public void addDataFlowEdgeTryStmt(TryStmt tryStmt, SimpleName variableName) {
        // TODO: improve
        logInfo(tryStmt);
        updateBlockDataFlow(tryStmt.getTryBlock(), variableName);
        tryStmt.getCatchClauses().forEach(catchClause -> updateBlockDataFlow(catchClause, variableName));
        tryStmt.getFinallyBlock().ifPresent(f -> updateBlockDataFlow(f, variableName));
    }

    public void addDataFlowEdgeSwitchStmt(SwitchStmt switchStmt, SimpleName variableName) {
        // TODO: improve
        logInfo(switchStmt);
        updateBlockDataFlow(switchStmt.getSelector(), variableName);
        switchStmt.getEntries().forEach(entry -> updateBlockDataFlow(entry, variableName));
    }

    public void addDataFlowEdgeForEachStmt(ForEachStmt forEachStmt, SimpleName variableName) {
        logInfo(forEachStmt);
        updateBlockDataFlow(forEachStmt.getVariable(), variableName);
        updateBlockDataFlow(forEachStmt.getIterable(), variableName);
        updateBlockDataFlow(forEachStmt.getBody(), variableName);
    }

    public void addDataFlowEdgeForStmt(ForStmt forStmt, SimpleName variableName) {
        Node lastWrite = mLastWrite;
        updateListBlockDataFlow(forStmt.getInitialization(), variableName);
        updateOptionalBlockDataFlow(forStmt.getCompare(), variableName);
        updateListBlockDataFlow(forStmt.getUpdate(), variableName);
        updateBlockDataFlow(forStmt.getBody(), variableName);
        updateLastAgainWhenLoop(forStmt, variableName, lastWrite);
    }

    public void addDataFlowEdgeDoStmt(DoStmt doStmt, SimpleName variableName) {
        Node lastWrite = mLastWrite;
        updateBlockDataFlow(doStmt.getBody(), variableName);
        updateBlockDataFlow(doStmt.getCondition(), variableName);
        updateLastAgainWhenLoop(doStmt, variableName, lastWrite);
    }

    public void addDataFlowEdgeWhileStmt(WhileStmt whileStmt, SimpleName variableName) {
        Node lastWrite = mLastWrite;
        updateBlockDataFlow(whileStmt.getCondition(), variableName);
        updateBlockDataFlow(whileStmt.getBody(), variableName);
        // getSpecificVariableFlowsUntilFirstWrite: 没有什么是加一个中间层解决不了的。
        updateLastAgainWhenLoop(whileStmt, variableName, lastWrite);
    }

    public void updateLastAgainWhenLoop(Node loopNode, SimpleName variableName, Node lastWrite) {
        List<NameExpr> variableFlows = getSpecificVariableFlowsUntilFirstWrite(loopNode, variableName);
        if (!variableFlows.isEmpty()) {
            if (!RangeNode.newInstance(lastWrite).equals(RangeNode.newInstance(mLastWrite))) {
                addLastWriteList(variableFlows, mLastWrite);
            }
            addLastUse(variableFlows.get(0), mLastUse);
        }
    }

    public <T extends Node> void addLastWriteList(List<T> nodeUs, Object o) {
        nodeUs.forEach(nodeU -> addLastWrite(nodeU, o));
    }

    public void addDataFlowEdgeIfStmt(IfStmt ifStmt, SimpleName variableName) {
        updateBlockDataFlow(ifStmt.getCondition(), variableName);
        Node lastUse = mLastUse;
        Node lastWrite = mLastWrite;
        updateBlockDataFlow(ifStmt.getThenStmt(), variableName);
        if (ifStmt.getElseStmt().isPresent()) {
            Node lastLexicalUse = mLastUse;
            mLastUse = lastUse;
            mLastWrite = lastWrite;
            updateBlockDataFlow(ifStmt.getElseStmt().get(), variableName);
            addLastLexicalUse(mLastUse, lastLexicalUse);
        }
    }

    public void addDataFlowEdgeAssignExpr(AssignExpr assignExpr, SimpleName variableName) {
        for (NameExpr variableNameExpr : getVariableNames(assignExpr.getTarget())) {
            if (variableNameExpr.getName().equals(variableName)) {
                addComputedFromList(variableNameExpr, getVariableNames(assignExpr.getValue()));
                updateLastUseWriteOfVariables(getSpecificVariableFlows(assignExpr.getValue(), variableName));
                updateLastUseWrite(variableNameExpr);
                mLastWrite = variableNameExpr;
            }
        }
    }

    public <T extends Node> void addComputedFromList(Object o, List<T> nodes) {
        nodes.forEach(n -> addComputedFrom(o, n));
    }

    public void addChildNextTokenList(Object o, NodeList<? extends Node> nodeList) {
        if (!nodeList.isEmpty()) {
            Object before = null;
            for (Node node : nodeList) {
                addChildToken(o, node);
                if (before != null) {
                    addNextNodeToken(before, node);
                }
                before = node;
            }
        }
    }

    public void addChildNodeListThrows(Object o, NodeList<ReferenceType> thrownExceptions) {
        if (!thrownExceptions.isEmpty()) {
            addChildToken(o, "Throws");
            Object before = "Throws";
            for (ReferenceType r : thrownExceptions) {
                addChildToken(o, r);
                addNextToken(before, r);
                before = r;
            }
        }
    }

    public <T extends Node> void addChildOptionalNode(Object o, Optional<T> optional) {
        optional.ifPresent(T -> addChildNode(o, optional.get()));
    }

    public <T extends Node> void addChildOptionalNodeList(Object o, Optional<NodeList<T>> optionalList) {
        optionalList.ifPresent(nodeList -> addChildNodeList(o, nodeList));
    }

    public void addChildModifiers(Node node, EnumSet<Modifier> modifiers) {
        if (!modifiers.isEmpty()) {
            Modifier before = null;
            for (Modifier modifier : modifiers) {
                addChildTokenForObjectV(node, modifier);
                if (before != null) {
                    addNextTokenModifier(before, modifier);
                }
                before = modifier;
            }
        }
    }

    public void addNextTokenModifier(Modifier modifierU, Modifier modifierV) {
        // TODO: Modifier is not instanceof Node, so it can't be convert to GraphProcess.RangeNode.
        // So, Modifier will lost its Range information.
        putEdge(modifierU, modifierV, EDGE_NEXT_TOKEN);
    }

    public void addChildTokenForObjectV(Node nodeU, Object nodeV) {
        // TODO: nodeV maybe Modifier or String, need regularization
        putEdge(RangeNode.newInstance(nodeU), nodeV, EDGE_CHILD_TOKEN);
    }

    public <T extends Node> void addChildTokenList(Object o, NodeList<T> nodeList) {
        nodeList.forEach(node -> addChildToken(o, node));
    }

    public <T extends Node> void addChildNodeList(Object o, NodeList<T> nodeList) {
        nodeList.forEach(node -> addChildNode(o, node));
    }

    public <T extends Node> void addChildNodeList(Object o, List<T> nodeList) {
        nodeList.forEach(node -> addChildNode(o, node));
    }

    public void putEdge(Object nodeU, Object nodeV, String edgeType) {
        mNetwork.addEdge(nodeU, nodeV, edgeType + "_" + mEdgeNumber);
        mEdgeNumber = mEdgeNumber + 1;
        if (isContain(edgeType, new String[]{EDGE_COMPUTED_FROM, EDGE_LAST_USE,
                EDGE_LAST_WRITE, EDGE_LAST_LEXICAL_USE, EDGE_FORMAL_ARG_NAME})) {
            mDataFlowNodes.add((RangeNode) nodeU);
            mDataFlowNodes.add((RangeNode) nodeV);
        }
    }

    public void addChildNode(Object nodeU, Object nodeV) {
        putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_CHILD_NODE);
    }

    public void addChildToken(Object nodeU, Object nodeV) {
        if (nodeV instanceof String) {
            putEdge(RangeNode.newInstance((Node) nodeU), nodeV, EDGE_CHILD_TOKEN);
        } else if (nodeV instanceof Node) {
            putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_CHILD_TOKEN);
//            logInfo("NewNodeType: " + nodeV.getClass());
        } else {
            putEdge(RangeNode.newInstance((Node) nodeU), nodeV, EDGE_CHILD_TOKEN);
            logInfo("NewGraphNodeType: " + nodeV.getClass());
        }
    }

    public void addNextNodeToken(Object nodeU, Object nodeV) {
        putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_NEXT_TOKEN);
    }

    public void addNextToken(Object nodeU, Object nodeV) {
        putEdge(nodeU, nodeV, EDGE_NEXT_TOKEN);
    }

    public void addLastUse(Object nodeU, Object nodeV) {
        putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_LAST_USE);
        logInfo("A LAST USE EDGE");
        logInfo(nodeU.getClass().toString() + ": " + ((Node) nodeU).toString() + ": " + ((Node) nodeU).getBegin().get().line);
        logInfo(nodeV.getClass().toString() + ": " + ((Node) nodeV).toString() + ": " + ((Node) nodeV).getBegin().get().line);
    }

    public void addLastWrite(Object nodeU, Object nodeV) {
        putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_LAST_WRITE);
        logInfo("A LAST WRITE EDGE");
        logInfo(nodeU.getClass().toString() + ": " + ((Node) nodeU).toString() + ": " + ((Node) nodeU).getBegin().get().line);
        logInfo(nodeV.getClass().toString() + ": " + ((Node) nodeV).toString() + ": " + ((Node) nodeV).getBegin().get().line);
    }

    public void addComputedFrom(Object nodeU, Object nodeV) {
        putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_COMPUTED_FROM);
        logInfo("A COMPUTED FROM EDGE");
        logInfo(nodeU.getClass().toString() + ": " + ((Node) nodeU).toString() + ": " + ((Node) nodeU).getBegin().get().line);
        logInfo(nodeV.getClass().toString() + ": " + ((Node) nodeV).toString() + ": " + ((Node) nodeV).getBegin().get().line);
    }

    public void addLastLexicalUse(Object nodeU, Object nodeV) {
//        putEdge(nodeU, nodeV, EDGE_LAST_LEXICAL_USE);
        putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_LAST_LEXICAL_USE);
        putEdge(RangeNode.newInstance((Node) nodeU), RangeNode.newInstance((Node) nodeV), EDGE_COMPUTED_FROM);
        logInfo("A LAST LEXICAL USE EDGE");
        logInfo(nodeU.getClass().toString() + ": " + ((Node) nodeU).toString() + ": " + ((Node) nodeU).getBegin().get().line);
        logInfo(nodeV.getClass().toString() + ": " + ((Node) nodeV).toString() + ": " + ((Node) nodeV).getBegin().get().line);
    }

    public void addEdgeReturnsTo(ReturnStmt returnStmt, MethodDeclaration methodDeclaration) {
        putEdge(RangeNode.newInstance(returnStmt), RangeNode.newInstance(methodDeclaration), EDGE_RETURNS_TO);
    }

    public void addGuardedBy(NameExpr nameExpr, Node nodeV) {
        putEdge(RangeNode.newInstance(nameExpr), RangeNode.newInstance(nodeV), EDGE_GUARDED_BY);
    }

    public void addGuardedByNegation(NameExpr nameExpr, Node nodeV) {
        putEdge(RangeNode.newInstance(nameExpr), RangeNode.newInstance(nodeV), EDGE_GUARDED_BY_NEGATION);
    }

    public <T> void processOptionalPrint(Optional<T> optional) {
        stringPrint(optional.toString());
    }

    public <T extends Node> void processOptionalNodeListNodeTravel(Optional<NodeList<T>> optionalList) {
        optionalList.ifPresent(nodeList -> nodeList.forEach(this::travelNode));
    }

    public <T extends Node> void processOptionalNodeTravel(Optional<T> optional) {
        optional.ifPresent(this::travelNode);
    }

    public <T extends Node> void processNodeListNodeTravel(NodeList<T> nodeList) {
        nodeList.forEach(this::travelNode);
    }

    public <T extends Node> void processNodeListNodeTravel(List<T> nodeList) {
        nodeList.forEach(this::travelNode);
    }

    public void processThrows(NodeList<ReferenceType> thrownExceptions) {
        if (!thrownExceptions.isEmpty()) {
            stringPrint("Throws");
            thrownExceptions.forEach(this::travelNode);
        }
    }

    public void processModifiers(EnumSet<Modifier> modifierEnumSet) {
        for (Modifier m : modifierEnumSet) {
            stringPrint(m.toString());
        }
    }

    public void processLiteralExpr(Node node) {
        String nodeClassPackage = node.getClass().toString();
        String nodeClass = Util.getClassLastName(nodeClassPackage);
        if (isContain(nodeClassPackage, new String[]{"BooleanLiteralExpr", "CharLiteralExpr"})) {
            addChildTokenForObjectV(node, node.toString());
            nodePrint(node);
        } else if (isContain(nodeClassPackage, "StringLiteralExpr")) {
            StringLiteralExpr stringLiteralExpr = (StringLiteralExpr) node;
            if (stringLiteralExpr.asString().isEmpty()) {
                addChildTokenForObjectV(node, "Empty " + nodeClass);
                stringPrint("Empty " + nodeClass);
            } else {
                addChildTokenForObjectV(node, nodeClass);
                stringPrint(nodeClass);
            }
        } else if (isContain(nodeClassPackage, "DoubleLiteralExpr")) {
            DoubleLiteralExpr doubleLiteralExpr = (DoubleLiteralExpr) node;
            if (doubleLiteralExpr.asDouble() == 0.0) {
                stringPrint("Zero " + nodeClass);
            } else {
                stringPrint(nodeClass);
            }
        } else if (isContain(nodeClassPackage, "IntegerLiteralExpr")) {
            IntegerLiteralExpr integerLiteralExpr = (IntegerLiteralExpr) node;
            try {
                // 如果asInt时数据超过 范围，会抛出异常
                if(integerLiteralExpr.asInt() == 0){
                    addChildTokenForObjectV(node, "Zero " + nodeClass);
                    stringPrint("Zero " + nodeClass);
                } else{
                    addChildTokenForObjectV(node, nodeClass);
                    stringPrint(nodeClass);
                }
            }catch (Exception e){
                addChildTokenForObjectV(node, nodeClass);
                stringPrint(nodeClass);
            }
        } else if (isContain(nodeClassPackage, "LongLiteralExpr")) {
            LongLiteralExpr longLiteralExpr = (LongLiteralExpr) node;
            try{
                if (longLiteralExpr.asLong() == 0L) {
                    addChildTokenForObjectV(node, "Zero " + nodeClass);
                    stringPrint("Zero " + nodeClass);
                } else {
                    addChildTokenForObjectV(node, nodeClass);
                    stringPrint(nodeClass);
                }
            } catch (Exception e){
                addChildTokenForObjectV(node, nodeClass);
                stringPrint(nodeClass);
            }

        } else if (isContain(nodeClassPackage, "NullLiteralExpr")) {
            addChildTokenForObjectV(node, nodeClass);
            stringPrint(nodeClass);
        }
    }

    public void stringPrint(String string) {
//        logInfo(string);
    }

    public void nodePrint(Node node) {
//        logInfo(node.toString());
    }

    public void addChildNodesToVisited(Node node) {
        node.findAll(Node.class).forEach(childNode -> {
            childNode.getRange().ifPresent(range -> mVisitedNodes.add(range));
        });
    }

    public static boolean isContain(String master, String sub) {
        return master.contains(sub);
    }

    public static boolean isContain(String master, String[] sub) {
        for (String s : sub) {
            if (isContain(master, s)) {
                return true;
            }
        }
        return false;
    }

    public String getParseResult() {
        return mParseResult;
    }

    public void showGraph() {
        for (EndpointPair<Object> endpointPair : mGraph.edges()) {
            Optional<String> edgeValue = mGraph.edgeValue(endpointPair.nodeU(), endpointPair.nodeV());
            if (edgeValue.isPresent()) {
                String value = edgeValue.get();
                logInfo("A Edge");
                logInfo(value);
                if (EDGE_CHILD_NODE.equals(value)) {
                    logInfo(endpointPair.nodeU().getClass());
                    logInfo(endpointPair.nodeV().getClass());
                    logInfo(endpointPair.nodeV());
                } else if (EDGE_CHILD_TOKEN.equals(value)) {
                    logInfo(endpointPair.nodeU().getClass());
                    logInfo(endpointPair.nodeV().getClass());
                    logInfo(endpointPair.nodeV());
                } else if (EDGE_NEXT_TOKEN.equals(value)) {
                    logInfo(endpointPair.nodeU());
                    logInfo(endpointPair.nodeV().getClass());
                    logInfo(endpointPair.nodeV());
                }
            }
        }
    }

    public void showDataFlowGraph() {
        for (EndpointPair<Object> endpointPair : mGraph.edges()) {
            Optional<String> edgeValue = mGraph.edgeValue(endpointPair.nodeU(), endpointPair.nodeV());
            if (edgeValue.isPresent()) {
                String value = edgeValue.get();
//                logInfo(value);
                if (EDGE_LAST_USE.equals(value)) {
                    logInfo("A LAST USE EDGE");
                    printEndpointPair(endpointPair);
                } else if (EDGE_LAST_WRITE.equals(value)) {
                    logInfo("A LAST WRITE EDGE");
                    printEndpointPair(endpointPair);
                } else if (EDGE_COMPUTED_FROM.equals(value)) {
                    logInfo("A COMPUTED FROM EDGE");
                    printEndpointPair(endpointPair);
                } else if (EDGE_GUARDED_BY.equals(value)) {
                    logInfo("A GUARDED BY EDGE");
                    printEndpointPair(endpointPair);
                } else if (EDGE_GUARDED_BY_NEGATION.equals(value)) {
                    logInfo("A GUARDED BY NEGATION EDGE");
                    printEndpointPair(endpointPair);
                } else if (EDGE_RETURNS_TO.equals(value)) {
                    logInfo("A RETURNS TO EDGE");
                    printEndpointPair(endpointPair);
                }
            }
        }
    }

    public void showDataFlowNetwork() {
        for (String edge : mNetwork.edges()) {
            EndpointPair<Object> endpointPair = mNetwork.incidentNodes(edge);
            switch (getEdgeType(edge)) {
                case 0:
                    logInfo("A LAST USE EDGE");
                    printEndpointPair(endpointPair);
                    break;
                case 1:
                    logInfo("A LAST WRITE EDGE");
                    printEndpointPair(endpointPair);
                    break;
                case 2:
                    logInfo("A COMPUTED FROM EDGE");
                    printEndpointPair(endpointPair);
                    break;
                case 3:
                    logInfo("A GUARDED BY EDGE");
                    printEndpointPair(endpointPair);
                    break;
                case 4:
                    logInfo("A GUARDED BY NEGATION EDGE");
                    printEndpointPair(endpointPair);
                    break;
                case 5:
                    logInfo("A RETURNS TO EDGE");
                    printEndpointPair(endpointPair);
                    break;
                default:
                    break;

            }
        }
    }

    public int getEdgeType(String edge) {
        if (edge.contains(EDGE_LAST_USE)) {
            return 0;
        } else if (edge.contains(EDGE_LAST_WRITE)) {
            return 1;
        } else if (edge.contains(EDGE_COMPUTED_FROM)) {
            return 2;
        } else if (edge.contains(EDGE_GUARDED_BY_NEGATION)) {
            return 4;
        } else if (edge.contains(EDGE_GUARDED_BY)) {
            return 3;
        } else if (edge.contains(EDGE_RETURNS_TO)) {
            return 5;
        }
        return -1;
    }

    public void printEndpointPair(EndpointPair endpointPair) {
        logInfo(endpointPair.nodeU().getClass() + ": " + ((RangeNode) endpointPair.nodeU()).getNode().toString()
                + ": " + ((RangeNode) endpointPair.nodeU()).getNode().getBegin().get().line);
        logInfo(endpointPair.nodeV().getClass() + ": " + ((RangeNode) endpointPair.nodeV()).getNode().toString()
                + ": " + ((RangeNode) endpointPair.nodeV()).getNode().getBegin().get().line);
    }

    public MutableNetwork<Object, String> getNetwork() {
        return mNetwork;
    }

    public static void logInfo(Object object) {
//        System.out.println(object);
    }

    public Set<RangeNode> getDataFlowNodes() {
        return mDataFlowNodes;
    }

    public Set<RangeNode> getRelatedDataFlowNodes(RangeNode node, Set<RangeNode> done) {
        Set<RangeNode> results = new HashSet<>();
        results.add(node);
        for (RangeNode adjNode : getDataFlowNodes()) {
            if (results.contains(adjNode) || done.contains(adjNode)) {
                continue;
            }
            if ((mNetwork.hasEdgeConnecting(node, adjNode) &&
                    hasConnectingBelongs(node, adjNode, new String[]{EDGE_COMPUTED_FROM, EDGE_LAST_USE,
                    EDGE_LAST_WRITE, EDGE_LAST_LEXICAL_USE, EDGE_FORMAL_ARG_NAME})) ||
                    (mNetwork.hasEdgeConnecting(node, adjNode)
                            && hasConnectingBelongs(adjNode, node, new String[]{EDGE_LAST_USE, //  TODO: EDGE_FORMAL_ARG_NAME 可以看到var所在的方法体被谁调用
                            EDGE_LAST_WRITE, EDGE_LAST_LEXICAL_USE}))) {
                results.add(adjNode);
                results.addAll(getRelatedDataFlowNodes(adjNode, results));
            }
        }
        return results;
    }

    public Set<RangeNode> getRelatedDataFlowNodes(RangeNode node, Set<RangeNode> done, Set<RangeNode> excludes) {
        // TODO: Can't Find all Related Nodes
        Set<RangeNode> results = new HashSet<>(done);
        // TODO: maybe getDataFlowNodes is not contains all
        for (RangeNode adjNode : getDataFlowNodes()) {
            if (results.contains(adjNode) || excludes.contains(adjNode)) {
                continue;
            }
            if ((mNetwork.hasEdgeConnecting(node, adjNode) &&
                    hasConnectingBelongs(node, adjNode, new String[]{EDGE_COMPUTED_FROM, EDGE_LAST_USE,
                            EDGE_LAST_WRITE, EDGE_LAST_LEXICAL_USE, EDGE_FORMAL_ARG_NAME})) ||
                    (mNetwork.hasEdgeConnecting(node, adjNode)
                            && hasConnectingBelongs(adjNode, node, new String[]{EDGE_LAST_USE, //  TODO: EDGE_FORMAL_ARG_NAME 可以看到var所在的方法体被谁调用
                            EDGE_LAST_WRITE, EDGE_LAST_LEXICAL_USE}))) {
                results.add(adjNode);
                results.addAll(getRelatedDataFlowNodes(adjNode, results, excludes));
                // TODO: maybe add excludes should in here:
                // excludes.add(adjNode);
            } else {
                excludes.add(adjNode); // TODO: Maybe Something is wrong
            }
        }
        return results;
    }

    public boolean hasConnectingBelongs(Object nodeU, Object nodeV, String[] edgeTypes) {
        for (String edgeType : mNetwork.edgesConnecting(nodeU, nodeV)) {
            if (isContain(edgeType, edgeTypes)) {
                return true;
            }
        }
        return false;
    }

    public int getEdgeNumber() {
        return mEdgeNumber;
    }

    public void setNetwork(MutableNetwork<Object, String> network) {
        mNetwork = network;
    }

    public void setEdgeNumber(int edgeNumber) {
        mEdgeNumber = edgeNumber;
    }
}