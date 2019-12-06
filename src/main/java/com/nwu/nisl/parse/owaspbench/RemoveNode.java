package com.nwu.nisl.parse.owaspbench;

import com.nwu.nisl.parse.graph.AST2Graph;
import com.nwu.nisl.parse.graph.RangeNode;
import com.nwu.nisl.parse.graph.Util;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @ Author     ：wxkong
 */
public class RemoveNode extends AST2Graph {
    public Set<RangeNode> mRelatedNodes = new HashSet<>();
    public boolean mAll = false; // 表示是否此节点的所有子节点都应添加到图中
    private boolean mAddDataLink = false;
    public List<MethodDeclaration> mAllMethodDeclarations;
    public MethodCallExpr mCallExpr;

    public RemoveNode(String srcFilePath) throws FileNotFoundException {
        super(srcFilePath);
        mAllMethodDeclarations = getMethodDeclarations();
    }

    public void setAllMethodDeclarations(List<MethodDeclaration> allMethodDeclarations) {
        mAllMethodDeclarations = allMethodDeclarations;
    }

    public void setCallExpr(MethodCallExpr callExpr) {
        mCallExpr = callExpr;
    }

    public static RemoveNode newInstance(String srcFilePath) {
        try {
            return new RemoveNode(srcFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logInfo("Not Found File " + srcFilePath);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo(e + "\n" + srcFilePath);
            return null;
        }
    }

    @Override
    public void initNetwork() {
        super.initNetworkWithoutRangeNode();
        mAll = false;
        mAddDataLink = false;
    }

    public void constructNetwork(Node node) {
        travelNodeForTaint(node);
        travelNodeForCFG(node);
        Set<Object> nodes = new HashSet<>();
        for (Object n : mNetwork.nodes()) {
            if (n instanceof RangeNode) {
                if (((RangeNode) n).getOptionalRange().isPresent()) {
                    if (((RangeNode) n).getOptionalRange().get().begin.line > mCallExpr.getEnd().get().line) {
                        nodes.add(n);
                    }
                }
            }
        }
        nodes.forEach(mNetwork::removeNode);
        travelCalled();
    }

    public void constructNetworkWithLastLine(Node node, int lastLine) {
        travelNodeForTaint(node);
        travelNodeForCFG(node);
        Set<Object> nodes = new HashSet<>();
        for (Object n : mNetwork.nodes()) {
            if (n instanceof RangeNode) {
                if (((RangeNode) n).getOptionalRange().isPresent()) {
                    if (((RangeNode) n).getOptionalRange().get().begin.line > lastLine) {
                        nodes.add(n);
                    }
                }
            }
        }
        nodes.forEach(mNetwork::removeNode);
        travelCalled();
    }

    public void travelCalled() {
        if (mCalledMethodDecls.isEmpty()) {
            return;
        }
        MethodDeclaration called = mCalledMethodDecls.remove(0);
        if (called.getRange().isPresent() || !mVisitedNodes.contains(called.getRange().get())) {
            initCFG();
            travelNodeForCFG(called);
            mAll = true;
            mAddDataLink = true;
            travelNodeForTaint(called);
            mAll = false;
            mAddDataLink = false;
        }
        travelCalled();
    }


    public <T extends Node> boolean travelNodeForTaint(T node) {
        if (!node.getRange().isPresent() || mVisitedNodes.contains(node.getRange().get())) {
            return false;
        }
        mVisitedNodes.add(node.getRange().get());
        String nodeClassPackage = node.getClass().toString();
        String nodeClass = Util.getClassLastName(nodeClassPackage);
        node.removeComment();
        {
            if (isContain(nodeClassPackage, "BreakStmt")) {
                return false;
            }
            if (isContain(nodeClassPackage, "ContinueStmt")) {
                return false;
            }
            if (isContain(nodeClassPackage, "WildcardType")) {
                if (mAll) {
                    WildcardType wildcardType = (WildcardType) node;
                    wildcardType.getSuperType().ifPresent(c -> {
                        addNextToken(nodeClass, "SuperType");
                        addNextToken("SuperType", wildcardType.getSuperType().get());
                    });
                    wildcardType.getExtendedType().ifPresent(c -> {
                        addNextToken(nodeClass, "ExtendedType");
                        addNextToken("ExtendedType", wildcardType.getExtendedType().get());
                    });
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "UnionType")) {
                if (mAll) {
                    UnionType unionType = (UnionType) node;
                    addChildNextTokenList(unionType, unionType.getElements());
                    processNodeListNodeTravel(unionType.getElements());
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "IntersectionType")) {
                if (mAll) {
                    IntersectionType intersectionType = (IntersectionType) node;
                    addChildNextTokenList(intersectionType, intersectionType.getElements());
                    processNodeListNodeTravel(intersectionType.getElements());
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "ArrayType")) {
                if (mAll) {
                    ArrayType arrayType = (ArrayType) node;
                    addChildToken(arrayType, arrayType.getComponentType());
                    addNextNodeToken(arrayType.getComponentType(), arrayType.getElementType());
                    addChildToken(arrayType, arrayType.getElementType());
                    addChildNodesToVisited(arrayType.getComponentType());
                    addChildNodesToVisited(arrayType.getElementType());
                    travelNodeForTaint(arrayType.getComponentType());
                    travelNodeForTaint(arrayType.getElementType());
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "MethodDeclaration")) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) node;
                addChildModifiers(methodDeclaration, methodDeclaration.getModifiers());
                addChildToken(methodDeclaration, methodDeclaration.getType());
                addChildToken(methodDeclaration, methodDeclaration.getName());
                addNextNodeToken(methodDeclaration.getType(), methodDeclaration.getName());
                boolean r = false;
                for (Parameter parameter : methodDeclaration.getParameters()) {
                    if (travelNodeForTaint(parameter)) {
                        addChildNode(methodDeclaration, parameter);
                        addDataFlowVarLinksForSimpleName(methodDeclaration, parameter.getName(), parameter);
                        r = true;
                    }
//                    addChildNodesToVisited(parameter);
                }
                addChildNodeListThrows(methodDeclaration, methodDeclaration.getThrownExceptions());
                addChildOptionalNode(methodDeclaration, methodDeclaration.getBody());
                addReturnsTo(methodDeclaration);
                processModifiers(methodDeclaration.getModifiers());
                boolean all = mAll;
                mAll = true;
                travelNodeForTaint(methodDeclaration.getType());
                travelNodeForTaint(methodDeclaration.getName());
                processThrows(methodDeclaration.getThrownExceptions());
                mAll = all;
                processOptionalNodeTravel(methodDeclaration.getBody());
                return r;
            }
            if (isContain(nodeClassPackage, "AssertStmt")) {
                AssertStmt assertStmt = (AssertStmt) node;
                return addChildNodeIfTravel(node, assertStmt.getCheck());
            }
            if (isContain(nodeClassPackage, "ExpressionStmt")) {
                ExpressionStmt expressionStmt = (ExpressionStmt) node;
                Expression expression = expressionStmt.getExpression();
                boolean add = false;
                if (expression.isVariableDeclarationExpr()) {
                    for (VariableDeclarator variableDeclarator : ((VariableDeclarationExpr) expression).getVariables()) {
                        if (travelNodeForTaint(variableDeclarator)) {
                            if (expression.getParentNode().isPresent() && mAddDataLink) {
                                addDataFlowVarLinksForVarDector(expression.getParentNode().get(), variableDeclarator);
                                mAddDataLink = false;
                            }
                            addChildNode(expression, variableDeclarator);
                            add = true;
                        }
                    }
                    if (add) {
                        addChildModifiers(expression, ((VariableDeclarationExpr) expression).getModifiers());
                        processModifiers(((VariableDeclarationExpr) expression).getModifiers());
                    }
                    addChildNodesToVisited(expression);
                } else {
                    add = travelNodeForTaint(expression);
                }
                if (add) {
                    addChildNode(expressionStmt, expressionStmt.getExpression());
                }
                return add;
            }
            if (isContain(nodeClassPackage, "WhileStmt")) {
                WhileStmt whileStmt = (WhileStmt) node;
                boolean r1 = addChildNodeIfTravel(whileStmt, whileStmt.getCondition());
                boolean r2 = addChildNodeIfTravel(whileStmt, whileStmt.getBody());
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "TryStmt")) {
                TryStmt tryStmt = (TryStmt) node;
                boolean r1 = addChildNodeIfTravel(tryStmt, tryStmt.getTryBlock());
                boolean r2 = addChildNodeListIfTravel(tryStmt, tryStmt.getCatchClauses());
                boolean r3 = addChildOptionalNodeIfTravel(tryStmt, tryStmt.getFinallyBlock());
                return r1 || r2 || r3;
            }
            if (isContain(nodeClassPackage, "ThrowStmt")) {
                ThrowStmt throwStmt = (ThrowStmt) node;
                return addChildNodeIfTravel(throwStmt, throwStmt.getExpression());
            }
            if (isContain(nodeClassPackage, "SynchronizedStmt")) {
                SynchronizedStmt synchronizedStmt = (SynchronizedStmt) node;
                boolean r1 = addChildNodeIfTravel(synchronizedStmt, synchronizedStmt.getExpression());
                boolean r2 = addChildNodeIfTravel(synchronizedStmt, synchronizedStmt.getBody());
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "SwitchStmt")) {
                SwitchStmt switchStmt = (SwitchStmt) node;
                boolean r1 = addChildNodeIfTravel(switchStmt, switchStmt.getSelector());
                boolean r2 = addChildNodeListIfTravel(switchStmt, switchStmt.getEntries());
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "SwitchEntryStmt")) {
                SwitchEntryStmt switchEntryStmt = (SwitchEntryStmt) node;
                boolean r1 = addChildOptionalNodeIfTravel(switchEntryStmt, switchEntryStmt.getLabel());
                boolean r2 = addChildNodeListIfTravel(switchEntryStmt, switchEntryStmt.getStatements());
                if (r1 || r2) {
                    addChildToken(switchEntryStmt, nodeClass);
                }
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "ReturnStmt")) {
                ReturnStmt returnStmt = (ReturnStmt) node;
                addChildToken(returnStmt, nodeClass);
                return addChildOptionalNodeIfTravel(returnStmt, returnStmt.getExpression());
            }
            if (isContain(nodeClassPackage, "LabeledStmt")) {
                LabeledStmt labeledStmt = (LabeledStmt) node;
                boolean r = travelNodeForTaint(labeledStmt.getStatement());
                if (r) {
                    addChildToken(labeledStmt, labeledStmt.getLabel());
                    addChildNode(labeledStmt, labeledStmt.getStatement());
                }
                return r;
            }
            if (isContain(nodeClassPackage, "IfStmt")) {
                IfStmt ifStmt = (IfStmt) node;
                boolean r1 = addChildNodeIfTravel(ifStmt, ifStmt.getCondition());
                if (r1) {
                    addGuardedVariable(ifStmt);
                }
                boolean r2 = addChildNodeIfTravel(ifStmt, ifStmt.getThenStmt());
                boolean r3 = addChildOptionalNodeIfTravel(ifStmt, ifStmt.getElseStmt());
                return r1 || r2 || r3;
            }
            if (isContain(nodeClassPackage, "ForStmt")) {
                ForStmt forStmt = (ForStmt) node;
                boolean r1 = false;
                for (Expression expression : forStmt.getInitialization()) {
                    if (r1) {
                        boolean r11 = addChildNodeIfTravel(forStmt, expression);
                        if (r11 && expression.isVariableDeclarationExpr() && mAddDataLink) {
                            addDataFlowVarLinks(forStmt, (VariableDeclarationExpr) expression);
                            mAddDataLink = false;
                        }
                    } else {
                        r1 = addChildNodeIfTravel(forStmt, expression);
                        if (r1 && expression.isVariableDeclarationExpr() && mAddDataLink) {
                            addDataFlowVarLinks(forStmt, (VariableDeclarationExpr) expression);
                            mAddDataLink = false;
                        }
                    }
                }
                boolean r2 = addChildOptionalNodeIfTravel(forStmt, forStmt.getCompare());
                boolean r3 = addChildNodeListIfTravel(forStmt, forStmt.getUpdate());
                boolean r4 = addChildNodeIfTravel(forStmt, forStmt.getBody());
                return r1 || r2 || r3 || r4;
            }
            if (isContain(nodeClassPackage, "ForEachStmt")) {
                ForEachStmt foreachStmt = (ForEachStmt) node;
                boolean r1 = addChildNodeIfTravel(foreachStmt, foreachStmt.getVariable());
                boolean r2 = addChildNodeIfTravel(foreachStmt, foreachStmt.getIterable());
                boolean r3 = addChildNodeIfTravel(foreachStmt, foreachStmt.getBody());
                if (r1 && mAddDataLink) {
                    addDataFlowVarLinks(foreachStmt, foreachStmt.getVariable());
                    mAddDataLink = false;
                }
                return r1 || r2 || r3;
            }
            if (isContain(nodeClassPackage, "DoStmt")) {
                DoStmt doStmt = (DoStmt) node;
                boolean r1 = addChildNodeIfTravel(doStmt, doStmt.getBody());
                boolean r2 = addChildNodeIfTravel(doStmt, doStmt.getCondition());
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "BlockStmt")) {
                BlockStmt blockStmt = (BlockStmt) node;
                return addChildNodeListIfTravel(blockStmt, blockStmt.getStatements());
            }
            if (isContain(nodeClassPackage, "CatchClause")) {
                CatchClause catchClause = (CatchClause) node;
                boolean r1 = addChildNodeIfTravel(catchClause, catchClause.getParameter());
                boolean r2 = addChildNodeIfTravel(catchClause, catchClause.getBody());
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "stmt")) {
                Statement statement = (Statement) node;
                return addChildNodeListIfTravel(statement, statement.getChildNodes());
            }
            if (isContain(nodeClassPackage, "TypeParameter")) {
                TypeParameter parameter = (TypeParameter) node;
                if (mAll) {
                    addChildToken(parameter, parameter.getElementType());
                    if (!parameter.getElementType().isUnknownType()) {
                        travelNodeForTaint(parameter.getElementType());
                        travelNodeForTaint(parameter.getName());
                    }
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "ParameterCompare")) {
                Parameter parameter = (Parameter) node;
                if (travelNodeForTaint(parameter.getName())) {
                    boolean all = mAll;
                    mAll = true;
                    addChildToken(parameter, parameter.getName());
                    addChildModifiers(parameter, parameter.getModifiers());
                    if (parameter.getType().isUnknownType()) {
                    addChildToken(parameter, parameter.getType());
                    } else {
                        addChildNode(parameter, parameter.getType());
                        travelNodeForTaint(parameter.getType());
                    }
                    mAll = all;
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "ClassOrInterfaceType")) {
                if (mAll) {
                    ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) node;
                    addChildOptionalNode(classOrInterfaceType, classOrInterfaceType.getScope());
                    addChildToken(classOrInterfaceType, classOrInterfaceType.getName());
                    addChildOptionalNodeList(classOrInterfaceType, classOrInterfaceType.getTypeArguments());
                    processOptionalNodeTravel(classOrInterfaceType.getScope());
                    processOptionalNodeListNodeTravel(classOrInterfaceType.getTypeArguments());
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "VariableDeclarationExpr")) {
                VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) node;
                boolean r1 = addChildNodeListIfTravel(variableDeclarationExpr, variableDeclarationExpr.getVariables());
                if (r1) {
                    addChildModifiers(variableDeclarationExpr, variableDeclarationExpr.getModifiers());
                    processModifiers(variableDeclarationExpr.getModifiers());
                }
                return r1;
            }
            if (isContain(nodeClassPackage, "VariableDeclarator")) {
                VariableDeclarator variableDeclarator = (VariableDeclarator) node;
                boolean r = false;
                if (travelNodeForTaint(variableDeclarator.getName())) {
                    r = true;
                    boolean all = mAll;
                    mAll = true;
                    if (variableDeclarator.getInitializer().isPresent()) {
                        addChildNode(variableDeclarator, variableDeclarator.getInitializer().get());
                        travelNodeForTaint(variableDeclarator.getInitializer().get());
                    }
                    mAll = all;
                    mAddDataLink = true;
                    addChildToken(variableDeclarator, variableDeclarator.getType());
                    addChildToken(variableDeclarator, variableDeclarator.getName());
                    addNextNodeToken(variableDeclarator.getType(), variableDeclarator.getName());
                } else if (variableDeclarator.getInitializer().isPresent()) {
                    r = addChildNodeIfTravel(variableDeclarator, variableDeclarator.getInitializer().get());
                }
                return r;
            }
            if (isContain(nodeClassPackage, "MethodCallExpr")) {
                MethodCallExpr methodCallExpr = (MethodCallExpr) node;
                boolean all = mAll;
                if (mRelatedNodes.contains(RangeNode.newInstance(methodCallExpr))) {
                    mAll = true;
                }
                for (NameExpr n : methodCallExpr.findAll(NameExpr.class)) {
                    if (mRelatedNodes.contains(RangeNode.newInstance(n))) {
                        mAll = true;
                        break;
                    }
                }
                boolean r1 = addChildOptionalNodeIfTravel(methodCallExpr, methodCallExpr.getScope());
                boolean r2 = addChildNodeListIfTravel(methodCallExpr, methodCallExpr.getArguments());
                if (mAll || r1 || r2) {
                    mAll = true;
                    addChildToken(methodCallExpr, methodCallExpr.getName());
                    addChildOptionalNodeList(methodCallExpr, methodCallExpr.getTypeArguments());
                    addMethodCallFor(methodCallExpr);
                    travelNodeForTaint(methodCallExpr.getName());
                    mAll = all;
                }
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "MethodReferenceExpr")) {
                if (mAll) {
                    MethodReferenceExpr methodReferenceExpr = (MethodReferenceExpr) node;
                    addChildNode(methodReferenceExpr, methodReferenceExpr.getScope());
                    addChildToken(methodReferenceExpr, methodReferenceExpr.getIdentifier());
                    addChildOptionalNodeList(methodReferenceExpr, methodReferenceExpr.getTypeArguments());
                    travelNodeForTaint(methodReferenceExpr.getScope());
                    for (MethodDeclaration called : getMethodDeclarations()) {
                        if (methodReferenceExpr.getIdentifier().equals(called.getNameAsString())) {
                            addMethodCall(methodReferenceExpr, called);
                            break;
                        }
                    }
                    addChildNodesToVisited(methodReferenceExpr);
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, new String[]{"SimpleName", "NameExpr", "Name"})) {
                return mAll || mRelatedNodes.contains(RangeNode.newInstance(node));
            }
            if (isContain(nodeClassPackage, "Binary")) {
                BinaryExpr binaryExpr = (BinaryExpr) node;
                boolean r1 = addChildNodeIfTravel(binaryExpr, binaryExpr.getLeft());
                boolean r2 = addChildNodeIfTravel(binaryExpr, binaryExpr.getRight());
                if (r1 && r2) {
                    addChildTokenForObjectV(binaryExpr, binaryExpr.getOperator());
                }
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "Unary")) {
                UnaryExpr unaryExpr = (UnaryExpr) node;
                boolean r1 = addChildNodeIfTravel(unaryExpr, unaryExpr.getExpression());
                if (r1) {
                    addChildTokenForObjectV(unaryExpr, unaryExpr.getOperator());
                }
                return r1;
            }
            if (isContain(nodeClassPackage, "CastExpr")) {
                CastExpr castExpr = (CastExpr) node;
                boolean r1 = addChildNodeIfTravel(castExpr, castExpr.getExpression());
                if (r1) {
                    addChildToken(castExpr, castExpr.getType());
                    travelNodeForTaint(castExpr.getType());
                }
                return r1;
            }
            if (isContain(nodeClassPackage, "ClassExpr")) {
                if (mAll) {
                    ClassExpr classExpr = (ClassExpr) node;
                    addChildToken(classExpr, classExpr.getType());
                    addChildNodesToVisited(node);
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "TypeExpr")) {
                if (mAll) {
                    TypeExpr typeExpr = (TypeExpr) node;
                    addChildToken(typeExpr, typeExpr.getType());
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "LiteralExpr")) {
                if (mAll) {
                    processLiteralExpr(node);
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "AssignExpr")) {
                AssignExpr assignExpr = (AssignExpr) node;
                boolean r1 = addChildNodeIfTravel(assignExpr, assignExpr.getTarget());
                boolean r2 = addChildNodeIfTravel(assignExpr, assignExpr.getValue());
                if (r1 || r2) {
                    addChildTokenForObjectV(assignExpr, assignExpr.getOperator());
                }
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "FieldAccessExpr")) {
                FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
                boolean r1 = addChildNodeIfTravel(fieldAccessExpr, fieldAccessExpr.getScope());
                if (r1) {
                    addChildToken(fieldAccessExpr, fieldAccessExpr.getName());
                    addChildOptionalNodeList(fieldAccessExpr, fieldAccessExpr.getTypeArguments());
                    travelNodeForTaint(fieldAccessExpr.getName());
                }
                return r1;
            }
            if (isContain(nodeClassPackage, "ObjectCreationExpr")) {
                if (mAll) {
                    ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) node;
                    addChildOptionalNodeList(objectCreationExpr, objectCreationExpr.getTypeArguments());
                    addChildOptionalNode(objectCreationExpr, objectCreationExpr.getScope());
                    addNextNodeToken(objectCreationExpr, objectCreationExpr.getType());
                    addChildNodeList(objectCreationExpr, objectCreationExpr.getArguments());
                    addChildNodesToVisited(objectCreationExpr.getType());
                    travelNodeForTaint(objectCreationExpr);
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "ArrayCreationExpr")) {
                if (mAll) {
                    ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) node;
                    addChildNode(arrayCreationExpr, arrayCreationExpr.getElementType());
                    addChildNextTokenList(arrayCreationExpr, arrayCreationExpr.getLevels());
                    addChildOptionalNode(arrayCreationExpr, arrayCreationExpr.getInitializer());
                    addChildNodesToVisited(arrayCreationExpr.getElementType());
                    if (!arrayCreationExpr.getLevels().isEmpty()) {
                        arrayCreationExpr.getLevels().forEach(this::travelNodeForTaint);
                    }
                    if (arrayCreationExpr.getInitializer().isPresent()) {
                        processOptionalNodeTravel(arrayCreationExpr.getInitializer());
                        addChildNodesToVisited(arrayCreationExpr.getInitializer().get());
                    }
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "ArrayAccessExpr")) {
                ArrayAccessExpr arrayAccessExpr = (ArrayAccessExpr) node;
                boolean r1 = addChildNodeIfTravel(arrayAccessExpr, arrayAccessExpr.getName());
                if (r1) {
                    addChildNode(arrayAccessExpr, arrayAccessExpr.getIndex());
                    travelNodeForTaint(arrayAccessExpr.getName());
                }
                return r1;
            }
            if (isContain(nodeClassPackage, "ArrayInitializerExpr")) {
                ArrayInitializerExpr arrayInitializerExpr = (ArrayInitializerExpr) node;
                return addChildNodeListIfTravel(arrayInitializerExpr, arrayInitializerExpr.getValues());
            }
            if (isContain(nodeClassPackage, "ArrayCreationLevel")) {
                if (mAll) {
                    ArrayCreationLevel arrayCreationLevel = (ArrayCreationLevel) node;
                    if (arrayCreationLevel.getDimension().isPresent()) {
                        addChildNodeIfTravel(arrayCreationLevel, arrayCreationLevel.getDimension().get());
                    }
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "LambdaExpr")) {
                LambdaExpr lambdaExpr = (LambdaExpr) node;
                boolean r1 = addChildNodeListIfTravel(lambdaExpr, lambdaExpr.getParameters());
                boolean r2 = addChildNodeIfTravel(lambdaExpr, lambdaExpr.getBody());
                boolean r3 = addChildOptionalNodeIfTravel(lambdaExpr, lambdaExpr.getExpressionBody());
                return r1 || r2 || r3;
            }
            if (isContain(nodeClassPackage, "EnclosedExpr")) {
                EnclosedExpr enclosedExpr = (EnclosedExpr) node;
                return addChildNodeIfTravel(enclosedExpr, enclosedExpr.getInner());
            }
            if (isContain(nodeClassPackage, "InstanceOfExpr")) {
                InstanceOfExpr instanceOfExpr = (InstanceOfExpr) node;
                boolean r1 = addChildNodeIfTravel(instanceOfExpr, instanceOfExpr.getExpression());
                if (r1) {
                    addChildToken(instanceOfExpr, nodeClass);
                    addChildToken(instanceOfExpr, instanceOfExpr.getType());
                    addNextToken(nodeClass, instanceOfExpr.getType());
                }
                return r1;
            }
            if (isContain(nodeClassPackage, "MemberValuePair")) {
                MemberValuePair memberValuePair = (MemberValuePair) node;
                boolean r1 = addChildNodeIfTravel(memberValuePair, memberValuePair.getName());
                boolean r2 = addChildNodeIfTravel(memberValuePair, memberValuePair.getValue());
                return r1 || r2;
            }
            if (isContain(nodeClassPackage, "SuperExpr")) {
                if (mAll) {
                    SuperExpr superExpr = (SuperExpr) node;
                    addChildToken(superExpr, nodeClass);
                    addChildOptionalNode(superExpr, superExpr.getClassExpr());
                    return true;
                }
                return false;
            }
            if (isContain(nodeClassPackage, "ThisExpr")) {
                if (mAll) {
                    ThisExpr thisExpr = (ThisExpr) node;
                    addChildToken(thisExpr, nodeClass);
                    addChildOptionalNode(thisExpr, thisExpr.getClassExpr());
                    return true;
                }
                return false;
            }
            {
                //TODO: 其他情况
//                addChildNodeList(node, node.getChildNodes());
//                stringPrint(nodeClass);
//                nodePrint(node);
//                travelNode(node);
            }
        }
        return false;
    }

    private void addMethodCallFor(MethodCallExpr mdthodCaller) {
        for (MethodDeclaration called : mAllMethodDeclarations) {
            if (mdthodCaller.getNameAsString().equals(called.getNameAsString())
                    && mdthodCaller.getArguments().size() == called.getParameters().size()) {
                addFormalArgs(mdthodCaller, called);
                addMethodCall(mdthodCaller, called);
                break;
            }
        }
    }

    private <T extends Node> boolean addChildNodeListIfTravel(Object nodeU, List<T> nodeVs) {
        boolean r = false;
        for (T nodeV : nodeVs) {
            if (travelNodeForTaint(nodeV)) {
                addChildNode(nodeU, nodeV);
                r = true;
            }
        }
        return r;
    }

    private <T extends Node> boolean addChildOptionalNodeIfTravel(Object nodeU, Optional<T> nodeV) {
        if (nodeV.isPresent() && travelNodeForTaint(nodeV.get())) {
            addChildNode(nodeU, nodeV.get());
            return true;
        }
        return false;
    }

    private <T extends Node> boolean addChildNodeIfTravel(Object nodeU, T nodeV) {
        if (travelNodeForTaint(nodeV)) {
            addChildNode(nodeU, nodeV);
            return true;
        }
        return false;
    }

    public <T extends Node> void processNodeListNodeTravel(NodeList<T> nodeList) {
        nodeList.forEach(this::travelNodeForTaint);
    }

    public void processThrows(NodeList<ReferenceType> thrownExceptions) {
        if (!thrownExceptions.isEmpty()) {
            stringPrint("Throws");
            thrownExceptions.forEach(this::travelNodeForTaint);
        }
    }

    public <T extends Node> void processOptionalNodeTravel(Optional<T> optional) {
        optional.ifPresent(this::travelNodeForTaint);
    }

    public <T extends Node> void processOptionalNodeListNodeTravel(Optional<NodeList<T>> optionalList) {
        optionalList.ifPresent(nodeList -> nodeList.forEach(this::travelNodeForTaint));
    }

    public Set<RangeNode> getRelatedNodes() {
        return mRelatedNodes;
    }

    public void setRelatedNodes(Set<RangeNode> relatedNodes) {
        mRelatedNodes = relatedNodes;
    }
}
