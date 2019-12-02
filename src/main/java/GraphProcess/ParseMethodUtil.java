package GraphProcess;

import com.github.javaparser.Range;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.type.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ParseMethodUtil extends ParseUtil {

    private List<Range> visitedNodes = new ArrayList<>();
    private String mParseResult = "";

    ParseMethodUtil(String srcFilePath) throws FileNotFoundException {
        super(srcFilePath);
    }

    public static void main(String[] args) throws FileNotFoundException {
        String srcFilePath = "files/ClusterUnitDatabase.java";
        ParseMethodUtil parseMethodUtil = null;
        try {
            parseMethodUtil = new ParseMethodUtil(srcFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Not Found File " + srcFilePath);
            System.exit(0);
        }

        List<MethodDeclaration> methodDeclarations = parseMethodUtil.getMethodDeclarations();
        if (methodDeclarations.size() <= 0) {
            System.out.println("There is no method declaration.");
            System.exit(0);
        }
        MethodDeclaration methodDeclarationTest = methodDeclarations.get(0);
        methodDeclarationTest.removeComment();
        parseMethodUtil.travelNode(methodDeclarationTest);
        System.out.println(parseMethodUtil.getParseResult());
     }

    public <T extends Node> void travelNode(T nodeRoot) {
        List<Node> nodes = ((Node) nodeRoot).findAll(Node.class);

        for (Node node : nodes) {
            if (!node.getRange().isPresent() || visitedNodes.contains(node.getRange().get())) {
                continue;
            }
            node.getRange().ifPresent(range -> visitedNodes.add(range));

            String nodeClassPackage = node.getClass().toString();
            String[] nodeClassPackageSplit = node.getClass().toString().split("\\.");
            String nodeClass = nodeClassPackageSplit[nodeClassPackageSplit.length - 1];

            node.removeComment();

            if (isContain(nodeClassPackage, "Comment")) {
                addChildNodesToVisited(node);
            } else if (isContain(nodeClassPackage, new String[] {"VoidType","UnknownType"})) {
                stringPrint(nodeClass);
            } else if (isContain(nodeClassPackage, "WildcardType")) {
                WildcardType wildcardType = (WildcardType) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(wildcardType.getAnnotations());
//                travelNode(wildcardType.getElementType());
                wildcardType.getSuperType().ifPresent(c -> {
                    stringPrint("SuperType");
                    travelNode(c);
                });
                wildcardType.getExtendedType().ifPresent(c -> {
                    stringPrint("ExtendedType");
                    travelNode(c);
                });
            } else if (isContain(nodeClassPackage, "UnionType")) {
                UnionType unionType = (UnionType) node;
                processNodeListNodeTravel(unionType.getAnnotations());
                processNodeListNodeTravel(unionType.getElements());
            } else if (isContain(nodeClassPackage, "IntersectionType")) {
                IntersectionType intersectionType = (IntersectionType) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(intersectionType.getAnnotations());
                processNodeListNodeTravel(intersectionType.getElements());
            } else if (isContain(nodeClassPackage, "ArrayType")) {
                ArrayType arrayType = (ArrayType) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(arrayType.getAnnotations());
                travelNode(arrayType.getComponentType());
                travelNode(arrayType.getElementType());
                stringPrint(String.valueOf(arrayType.getArrayLevel()));
            } else if (isContain(nodeClassPackage, "Annotation")) {
                stringPrint(nodeClass);
                nodePrint(node);
                addChildNodesToVisited(node);
            } else if (isContain(nodeClassPackage, "InitializerDeclaration")) {
                InitializerDeclaration initializerDeclaration = (InitializerDeclaration) node;
                stringPrint("InitializerDeclaration");
                processNodeListNodeTravel(initializerDeclaration.getAnnotations());
                travelNode(initializerDeclaration.getBody());
                stringPrint("InitializerDeclaration END");
            } else if (isContain(nodeClassPackage, "AnnotationMemberDeclaration")) {
                AnnotationMemberDeclaration annotationMemberDeclaration = (AnnotationMemberDeclaration) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(annotationMemberDeclaration.getAnnotations());
                processModifiers(annotationMemberDeclaration.getModifiers());
                travelNode(annotationMemberDeclaration.getType());
                travelNode(annotationMemberDeclaration.getName());
                annotationMemberDeclaration.getDefaultValue().ifPresent((c) -> stringPrint("default"));
                processOptionalNodeTravel(annotationMemberDeclaration.getDefaultValue());
            } else if (isContain(nodeClassPackage, "AnnotationDeclaration")) {
                AnnotationDeclaration annotationDeclaration = (AnnotationDeclaration) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(annotationDeclaration.getAnnotations());
                processModifiers(annotationDeclaration.getModifiers());
                travelNode(annotationDeclaration.getName());
                processNodeListNodeTravel(annotationDeclaration.getMembers());
                processNodeListNodeTravel(annotationDeclaration.getFields());
                processNodeListNodeTravel(annotationDeclaration.getMethods());
            } else if (isContain(nodeClassPackage, "FieldDeclaration")) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(fieldDeclaration.getAnnotations());
                processModifiers(fieldDeclaration.getModifiers());
                processNodeListNodeTravel(fieldDeclaration.getVariables());
            } else if (isContain(nodeClassPackage, "ClassOrInterfaceDeclaration")) {
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) node;
                stringPrint(nodeClass);
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
                addChildNodesToVisited(enumConstantDeclaration.getName());
                processNodeListNodeTravel(enumConstantDeclaration.getAnnotations());
                processNodeListNodeTravel(enumConstantDeclaration.getArguments());
                enumConstantDeclaration.getClassBody().forEach(this::travelNode);
            } else if (isContain(nodeClassPackage, "MethodDeclaration")) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) node;
                processNodeListNodeTravel(methodDeclaration.getAnnotations());
                stringPrint(nodeClass);
                processModifiers(methodDeclaration.getModifiers());
                travelNode(methodDeclaration.getType());
                travelNode(methodDeclaration.getName());
                processNodeListNodeTravel(methodDeclaration.getParameters());
                processThrows(methodDeclaration.getThrownExceptions());
                processOptionalNodeTravel(methodDeclaration.getBody());
            } else if (isContain(nodeClassPackage, "ConstructorDeclaration")) {
                ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(constructorDeclaration.getAnnotations());
                processModifiers(constructorDeclaration.getModifiers());
                travelNode(constructorDeclaration.getName());
                processNodeListNodeTravel(constructorDeclaration.getParameters());
                processThrows(constructorDeclaration.getThrownExceptions());
                travelNode(constructorDeclaration.getBody());
//            } else if (isContain(nodeClassPackage, "LocalClassDeclarationStmt")) {
//                LocalClassDeclarationStmt localClassDeclarationStmt = (LocalClassDeclarationStmt) node;

            } else if (isContain(nodeClassPackage, "ExplicitConstructorInvocationStmt")) {
                ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt = (ExplicitConstructorInvocationStmt) node;
                stringPrint("ExplicitConstructorInvocationStmt");
                processOptionalNodeListNodeTravel(explicitConstructorInvocationStmt.getTypeArguments());
                processNodeListNodeTravel(explicitConstructorInvocationStmt.getArguments());
                processOptionalNodeTravel(explicitConstructorInvocationStmt.getExpression());
                stringPrint("ExplicitConstructorInvocationStmt END");
            } else if (isContain(nodeClassPackage, new String[]{"BreakStmt", "ContinueStmt"})) {
                stringPrint(nodeClass);
            } else if (isContain(nodeClassPackage, "ExpressionStmt")) {
                travelNode(node);
            } else if (isContain(nodeClassPackage, "stmt")) {
                stringPrint(nodeClass);
                travelNode(node);
                stringPrint(nodeClass+ " END");
            } else if (isContain(nodeClassPackage, "TypeParameter")) {
                TypeParameter parameter = (TypeParameter) node;
                parameter.getAnnotations().forEach(this::travelNode);
                if (parameter.getElementType().isUnknownType()){
                    stringPrint("UnknownType");
                    stringPrint(parameter.getNameAsString());
                } else {
                    travelNode(parameter.getElementType());
                    travelNode(parameter.getName());
                }
            } else if (isContain(nodeClassPackage, "ParameterCompare")) {
                Parameter parameter = (Parameter) node;
                parameter.getAnnotations().forEach(this::travelNode);
                if (parameter.getType().isUnknownType()){
                    stringPrint("UnknownType");
                    stringPrint(parameter.getNameAsString());
                } else {
                    travelNode(parameter.getType());
                    processModifiers(parameter.getModifiers());
                    processNodeListNodeTravel(parameter.getVarArgsAnnotations());
                    travelNode(parameter.getName());
                }
            } else if (isContain(nodeClassPackage, "ClassOrInterfaceType")) {
                ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) node;
                processOptionalNodeTravel(classOrInterfaceType.getScope());
                nodePrint(classOrInterfaceType.getName());
                processOptionalNodeListNodeTravel(classOrInterfaceType.getTypeArguments());
            } else if (isContain(nodeClassPackage, "CatchClause")) {
                CatchClause catchClause = (CatchClause) node;
                stringPrint(nodeClass);
                travelNode(catchClause.getParameter());
                travelNode(catchClause.getBody());
            } else if (isContain(nodeClassPackage, "VariableDeclarationExpr")) {
                stringPrint(nodeClass);
                VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) node;
                if (!variableDeclarationExpr.getModifiers().isEmpty()) {
                    stringPrint(variableDeclarationExpr.getModifiers().toString());
                }
                processModifiers(variableDeclarationExpr.getModifiers());
                travelNode(variableDeclarationExpr);
            }else if (isContain(nodeClassPackage, "VariableDeclarator")) {
                stringPrint(nodeClass);
                VariableDeclarator variableDeclarator = (VariableDeclarator) node;
                addChildNodesToVisited(variableDeclarator.getType());
                addChildNodesToVisited(variableDeclarator.getNameAsExpression());
                nodePrint(variableDeclarator.getType());
                stringPrint(variableDeclarator.getNameAsString());
                variableDeclarator.getInitializer().ifPresent((consumer) -> {
                    stringPrint("=");
                    travelNode(consumer);
                });
            } else if (isContain(nodeClassPackage, new String[]{"SimpleName","NameExpr","Name"})) {
                nodePrint(node);
            } else if (isContain(nodeClassPackage, "Binary")) {
                BinaryExpr binaryExpr = (BinaryExpr) node;
                travelNode(binaryExpr.getLeft());
                stringPrint(binaryExpr.getOperator().toString());
                travelNode(binaryExpr.getRight());
            } else if (isContain(nodeClassPackage, "Unary")) {
                UnaryExpr unaryExpr = (UnaryExpr) node;
                travelNode(unaryExpr.getExpression());
                stringPrint(unaryExpr.getOperator().asString());
            }else if (isContain(nodeClassPackage, "CastExpr")) {
                CastExpr castExpr = (CastExpr) node;
                stringPrint(nodeClass);
                travelNode(castExpr.getType());
                travelNode(castExpr.getExpression());
            }else if (isContain(nodeClassPackage, "ClassExpr")) {
                nodePrint(node);
                addChildNodesToVisited(node);
            }else if (isContain(nodeClassPackage, "MethodCallExpr")) {
                MethodCallExpr methodCallExpr = (MethodCallExpr) node;
                processOptionalNodeTravel(methodCallExpr.getScope());
                stringPrint(nodeClass);
                travelNode(methodCallExpr.getName());
                methodCallExpr.getArguments().forEach(this::travelNode);
            }else if (isContain(nodeClassPackage, "MethodReferenceExpr")) {
                MethodReferenceExpr methodReferenceExpr = (MethodReferenceExpr) node;
                stringPrint(nodeClass);
                processOptionalPrint(methodReferenceExpr.getTypeArguments());
                travelNode(methodReferenceExpr.getScope());
                stringPrint(methodReferenceExpr.getIdentifier());
                addChildNodesToVisited(node);
            }else if (isContain(nodeClassPackage, "TypeExpr")) {
                stringPrint(nodeClass);
                nodePrint(node);
            }else if (isContain(nodeClassPackage, "LiteralExpr")) {
                processLiteralExpr(node);
            }else if (isContain(nodeClassPackage, "AssignExpr")) {
                AssignExpr assignExpr = (AssignExpr) node;
                stringPrint(nodeClass);
                travelNode(assignExpr.getTarget());
                stringPrint("=");
                travelNode(assignExpr.getValue());
            }else if (isContain(nodeClassPackage,"FieldAccessExpr")) {
                FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
                travelNode(fieldAccessExpr.getScope());
                stringPrint("FieldAccess");
                travelNode(fieldAccessExpr.getNameAsExpression());
            }else if (isContain(nodeClassPackage, "ArrayAccessExpr")) {
                ArrayAccessExpr arrayAccessExpr = (ArrayAccessExpr) node;
                stringPrint(nodeClass);
                travelNode(arrayAccessExpr.getName());
                nodePrint(arrayAccessExpr.getIndex());
            }else if (isContain(nodeClassPackage, "ArrayCreationExpr")) {
                ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) node;
                stringPrint(nodeClass);
                nodePrint(arrayCreationExpr.getElementType());
                addChildNodesToVisited(arrayCreationExpr.getElementType());
                if (arrayCreationExpr.getLevels().isEmpty()){
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
            }else if (isContain(nodeClassPackage, "ArrayInitializerExpr")) {
                ArrayInitializerExpr arrayInitializerExpr = (ArrayInitializerExpr) node;
                processNodeListNodeTravel(arrayInitializerExpr.getValues());
            }else if (isContain(nodeClassPackage, "ArrayCreationLevel")) {
                ArrayCreationLevel arrayCreationLevel = (ArrayCreationLevel) node;
                if (arrayCreationLevel.getDimension().isPresent()){
                    stringPrint("Dimension NotEmpty");
                    travelNode(arrayCreationLevel.getDimension().get());
                } else {
                    stringPrint("Dimension Empty");
                }
            }else if (isContain(nodeClassPackage, "ObjectCreationExpr")) {
                ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) node;
                stringPrint(nodeClass);
                nodePrint(objectCreationExpr.getType());
                addChildNodesToVisited(objectCreationExpr.getType());
//                stringPrint(objectCreationExpr.getArguments().toString());
                stringPrint(objectCreationExpr.getScope().toString());
                stringPrint(objectCreationExpr.getTypeArguments().toString());
                travelNode(node);
//                objectCreationExpr.
            }else if (isContain(nodeClassPackage, "LambdaExpr")) {
                LambdaExpr lambdaExpr = (LambdaExpr) node;
                stringPrint(nodeClass);
                processNodeListNodeTravel(lambdaExpr.getParameters());
                travelNode(lambdaExpr.getBody());
                processOptionalNodeTravel(lambdaExpr.getExpressionBody());
            }else if (isContain(nodeClassPackage, "EnclosedExpr")) {
                EnclosedExpr enclosedExpr = (EnclosedExpr) node;
                stringPrint(nodeClass);
                travelNode(enclosedExpr.getInner());
            }else if (isContain(nodeClassPackage, "InstanceOfExpr")) {
                InstanceOfExpr instanceOfExpr = (InstanceOfExpr) node;
                travelNode(instanceOfExpr.getExpression());
                stringPrint(nodeClass);
                travelNode(instanceOfExpr.getType());
            }else if (isContain(nodeClassPackage, "MemberValuePair")) {
                MemberValuePair memberValuePair = (MemberValuePair) node;
                stringPrint(nodeClass);
                travelNode(memberValuePair.getName());
                travelNode(memberValuePair.getValue());
            }else if (isContain(nodeClassPackage, new String[]{"ThisExpr", "SuperExpr"})) {
                stringPrint(nodeClass);
            }else {
                stringPrint(nodeClass);
                nodePrint(node);
                travelNode(node);
            }
        }
    }

    private <T> void processOptionalPrint(Optional<T> optional) {
        stringPrint(optional.toString());
    }

    private <T extends Node> void processOptionalNodeListNodeTravel(Optional<NodeList<T>> optionalList) {
        optionalList.ifPresent(nodeList -> nodeList.forEach(this::travelNode));
    }

    private <T extends Node> void processOptionalNodeTravel(Optional<T> optional) {
        optional.ifPresent(this::travelNode);
    }

    private <T extends Node> void processNodeListNodeTravel(NodeList<T> nodeList) {
        nodeList.forEach(this::travelNode);
    }

    private <T extends Node> void processNodeListNodeTravel(List<T> nodeList) {
        nodeList.forEach(this::travelNode);
    }

    private void processThrows(NodeList<ReferenceType> thrownExceptions) {
        if (!thrownExceptions.isEmpty()) {
            stringPrint("Throws");
            thrownExceptions.forEach(this::travelNode);
        }
    }

    public void processModifiers(EnumSet<Modifier> modifierEnumSet) {
        for (Modifier m : modifierEnumSet){
            stringPrint(m.toString());
        }
    }

    private void processLiteralExpr(Node node) {
        String nodeClassPackage = node.getClass().toString();
        String[] nodeClassPackageSplit = node.getClass().toString().split("\\.");
        String nodeClass = nodeClassPackageSplit[nodeClassPackageSplit.length - 1];
        if (isContain(nodeClassPackage, new String[]{"BooleanLiteralExpr","CharLiteralExpr"})) {
            nodePrint(node);
        }else if (isContain(nodeClassPackage, "StringLiteralExpr")) {
            StringLiteralExpr stringLiteralExpr = (StringLiteralExpr) node;
            if (stringLiteralExpr.asString().isEmpty()){
                stringPrint("Empty " + nodeClass);
            }else {
                stringPrint(nodeClass);
            }
        }else if (isContain(nodeClassPackage, "DoubleLiteralExpr")) {
            DoubleLiteralExpr doubleLiteralExpr = (DoubleLiteralExpr) node;
            if (doubleLiteralExpr.asDouble() == 0.0) {
                stringPrint("Zero " + nodeClass);
            } else {
                stringPrint(nodeClass);
            }
        }else if (isContain(nodeClassPackage, "IntegerLiteralExpr")) {
            IntegerLiteralExpr integerLiteralExpr = (IntegerLiteralExpr) node;
            if (integerLiteralExpr.asInt() == 0) {
                stringPrint("Zero " + nodeClass);
            } else {
                stringPrint(nodeClass);
            }
        }else if (isContain(nodeClassPackage, "LongLiteralExpr")) {
            LongLiteralExpr longLiteralExpr = (LongLiteralExpr) node;
            if (longLiteralExpr.asLong() == 0l) {
                stringPrint("Zero " + nodeClass);
            } else {
                stringPrint(nodeClass);
            }
        }else if (isContain(nodeClassPackage, "NullLiteralExpr")) {
            stringPrint(nodeClass);
        }
    }

    private void stringPrint(String string) {
        mParseResult = mParseResult + string + " ";
    }

    private void nodePrint(Node node) {
        mParseResult = mParseResult + node.toString() + " ";
    }

    public void addChildNodesToVisited(Node node) {
        node.findAll(Node.class).forEach(childNode -> {
            childNode.getRange().ifPresent(range -> visitedNodes.add(range));
        });
    }

    public boolean isContain(String master, String sub) {
        return master.contains(sub);
    }

    public boolean isContain(String master, String[] sub) {
        for (String s : sub) {
            if (isContain(master, s)){
                return true;
            }
        }
        return false;
    }

    public String getParseResult() {
        return mParseResult;
    }

    public <T extends Node> String travelNodeGetParseResult(T nodeRoot){
        List<Node> nodes = new ArrayList<>();
        nodes.add(nodeRoot);
        renameVariableFieldName(nodes);
        travelNode(nodeRoot);
        return getParseResult();
    }
}