package com.nwu.nisl.parse.graph;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


public class CFG {

    class EmpComparator implements Comparator<Node> {
        @Override
        public int compare(Node faultResult1, Node faultResult2) {
            int cr = 0;
            //先按line排升序
            int a = faultResult2.getBegin().get().line -faultResult1.getBegin().get().line;
            if (a != 0) {
                cr = (a < 0) ? 3 : -1;     // "<"升序     ">"降序
            } else {
                //再按column排升序
                a =  faultResult2.getBegin().get().column -  faultResult1.getBegin().get().column;
                if (a != 0) {
                    cr = (a < 0) ? 2 : -2; // "<"升序     ">"降序
                }
            }
            return cr;
        }
    }

    private String srcfilename;
    private CompilationUnit cu;
    private List<SimpleName> SimpleNamesOfClassOrInterfaceType;
    private ArrayList<VariableDeclarator> field_variableDeclarators = new ArrayList<VariableDeclarator>();
    private List<MethodCallExpr> methodCallExprs = new ArrayList<MethodCallExpr>();
    private List<MethodDeclaration> methodDeclarations = new ArrayList<MethodDeclaration>();

    private CFG(String srcFile) throws FileNotFoundException {
        this.srcfilename = srcFile;
        FileInputStream in = new FileInputStream(this.srcfilename);
        this.cu = JavaParser.parse(in);

        List<ClassOrInterfaceType> classOrInterfaceTypes_all = this.cu.findAll(ClassOrInterfaceType.class);
        this.SimpleNamesOfClassOrInterfaceType = classOrInterfaceTypes_all.stream().map(ClassOrInterfaceType::getName).collect(Collectors.toList());
        this.methodCallExprs = this.cu.findAll(MethodCallExpr.class);
        this.methodDeclarations = this.cu.findAll(MethodDeclaration.class);
        this.cu.findAll(FieldDeclaration.class).forEach((FieldDeclaration fieldDeclaration) -> {
            this.field_variableDeclarators.addAll(new ArrayList<>(fieldDeclaration.findAll(VariableDeclarator.class)));
        });
    }

    private List<Node> getSliceOfMethod(MethodCallExpr methodCallExpr, MethodDeclaration methodDeclaration) {
        // 找到所需函数所在的方法声明
        int methodCall_line = methodCallExpr.getBegin().get().line;
        NodeList<Expression> arguments = methodCallExpr.getArguments();
        ArrayList<Parameter> parameters_of_methodDeclaration = new ArrayList<>(methodDeclaration.getParameters());

        List<VariableDeclarator> variableDeclarators_of_methodCall = methodDeclaration.findAll(VariableDeclarator.class);
        List<AssignExpr> assignExprs_of_methodCall = methodDeclaration.findAll(AssignExpr.class);
        // 将arguments中的 NameExpr 与 FieldAccessExpr 放入 _want ArrayList 中
        ArrayList<NameExpr> nameExprs_want = new ArrayList<NameExpr>();
        ArrayList<FieldAccessExpr> fieldAccessExpr_want = new ArrayList<FieldAccessExpr>();
        ArrayList<VariableDeclarator> variableDeclarator_want = new ArrayList<VariableDeclarator>();
        ArrayList<AssignExpr> assignExpr_want = new ArrayList<AssignExpr>();
        ArrayList<NameExpr> args_finalNameExprs_want = nameExprs_want;
        ArrayList<FieldAccessExpr> args_finalFieldAccessExpr_want = fieldAccessExpr_want;
        arguments.forEach(call_args -> {
            args_finalNameExprs_want.addAll(call_args.findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
            args_finalFieldAccessExpr_want.addAll(call_args.findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
        });
        methodCallExpr.getScope().ifPresent(expression -> {
            args_finalNameExprs_want.addAll(expression.findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
            args_finalFieldAccessExpr_want.addAll(expression.findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
        });

        ArrayList<NameExpr> nameExprs_want_temp = new ArrayList<NameExpr>();
        ArrayList<FieldAccessExpr> fieldAccessExpr_want_temp = new ArrayList<FieldAccessExpr>();
        ArrayList<NameExpr> nameExprs_want_done = new ArrayList<NameExpr>();
        ArrayList<FieldAccessExpr> fieldAccessExpr_want_done = new ArrayList<FieldAccessExpr>();

        Boolean ifMap = Boolean.TRUE;
        while (ifMap) {
            for (NameExpr name : nameExprs_want) {
                if (nameExprs_want_done.contains(name)) {
                    continue;
                }
                for (VariableDeclarator variableDeclarator : variableDeclarators_of_methodCall) {
                    if (Objects.equals(variableDeclarator.getNameAsString(), name.toString())) {
                        variableDeclarator_want.add(variableDeclarator);
                        if (variableDeclarator.getInitializer().isPresent()) {
                            nameExprs_want_temp.addAll(variableDeclarator.getInitializer().get().findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
                            fieldAccessExpr_want_temp.addAll(variableDeclarator.getInitializer().get().findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
                        }
                    }
                }
                for (AssignExpr assignExpr : assignExprs_of_methodCall) {
                    if (Objects.equals(assignExpr.getTarget().toString(), name.toString())) {
                        assignExpr_want.add(assignExpr);
                        nameExprs_want_temp.addAll(assignExpr.getValue().findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
                        fieldAccessExpr_want_temp.addAll(assignExpr.getValue().findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
                    }
                }
                nameExprs_want_done.add(name);
            }
            for (FieldAccessExpr fieldAccessExpr_of_want : fieldAccessExpr_want) {
                if (fieldAccessExpr_want_done.contains(fieldAccessExpr_of_want)) {
                    continue;
                }
                for (VariableDeclarator variableDeclarator : this.field_variableDeclarators) {
                    if (Objects.equals(variableDeclarator.getNameAsString(), fieldAccessExpr_of_want.getNameAsString())) {
                        variableDeclarator_want.add(variableDeclarator);
                        if (variableDeclarator.getInitializer().isPresent()) {
                            nameExprs_want_temp.addAll(variableDeclarator.getInitializer().get().findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
                            fieldAccessExpr_want_temp.addAll(variableDeclarator.getInitializer().get().findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
                        }
                    }
                }

                for (AssignExpr assignExpr : assignExprs_of_methodCall) {
                    if (Objects.equals(assignExpr.getTarget().toString(), fieldAccessExpr_of_want.toString())) {
                        assignExpr_want.add(assignExpr);
                        nameExprs_want_temp.addAll(assignExpr.getValue().findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
                        fieldAccessExpr_want_temp.addAll(assignExpr.getValue().findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
                    }
                }
                fieldAccessExpr_want_done.add(fieldAccessExpr_of_want);
            }
            nameExprs_want = nameExprs_want_temp;
            nameExprs_want_temp = new ArrayList<NameExpr>();
            fieldAccessExpr_want = fieldAccessExpr_want_temp;
            fieldAccessExpr_want_temp = new ArrayList<FieldAccessExpr>();
            if (nameExprs_want.isEmpty() && fieldAccessExpr_want.isEmpty()) {
                ifMap = Boolean.FALSE;
            }
        }
        ArrayList<VariableDeclarator> final_variableDeclarator_want = new ArrayList<>();
        ArrayList<Node> final_variableDeclaratorParentNode_want = new ArrayList<>();
        ArrayList<AssignExpr> final_assignExpr_want = new ArrayList<>();
        ArrayList<Parameter> final_parameter_want = new ArrayList<>();
        for (Parameter parameter : parameters_of_methodDeclaration) {
            for (NameExpr nameExpr : nameExprs_want_done) {
                if (Objects.equals(parameter.getNameAsString(), nameExpr.toString())) {
                    final_parameter_want.add(parameter);
                    break;
                }
            }
        }
        for (VariableDeclarator variableDeclarator : variableDeclarator_want) {
            if (final_variableDeclarator_want.contains(variableDeclarator)) {
                continue;
            }
            final_variableDeclarator_want.add(variableDeclarator);
            final_variableDeclaratorParentNode_want.add(variableDeclarator.getParentNode().get().removeComment());
        }
        for (AssignExpr assignExpr : assignExpr_want) {
            if (final_assignExpr_want.contains(assignExpr)) {
                continue;
            }
            final_assignExpr_want.add(assignExpr);
        }
        List<Node> final_nodes_want = new ArrayList<>();
//            final_nodes_want.addAll(final_variableDeclarator_want);
        final_nodes_want.addAll(final_variableDeclaratorParentNode_want);
        final_nodes_want.addAll(final_assignExpr_want);
        final_nodes_want.addAll(final_parameter_want);
        final_nodes_want = final_nodes_want.stream().filter(fi -> fi.getBegin().get().line < methodCall_line).collect(Collectors.toList());
//            final_nodes_want = final_nodes_want.stream().filter(fi -> fi.getBegin().get().line < methodCall_line).map(fi->fi.getParentNode().get()).collect(Collectors.toList());
//            final_nodes_want.sort(new Comparator<Node>() {
//                @Override
//                public int compare(Node o1, Node o2) {
//                    return o1.getBegin().get().line - o2.getBegin().get().line;
//                }
//            });
        final_nodes_want.sort(new EmpComparator());
        final_nodes_want.add(methodCallExpr);
        return final_nodes_want;
    }

    private void renameVariableName(List<Node> nodeList) {
        int i = 1;
        int j = 1;
        ArrayList<NameExpr> nameExprs = new ArrayList<NameExpr>();
        ArrayList<FieldAccessExpr> fieldAccessExprs = new ArrayList<FieldAccessExpr>();
        ArrayList<VariableDeclarator> variableDeclarators = new ArrayList<VariableDeclarator>();
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        nodeList.forEach(node -> {
            nameExprs.addAll(node.findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
            fieldAccessExprs.addAll(node.findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
            variableDeclarators.addAll(node.findAll(VariableDeclarator.class).stream().filter(variableDeclarator -> !this.field_variableDeclarators.contains(variableDeclarator)).collect(Collectors.toList()));
            parameters.addAll(new ArrayList<>(node.findAll(Parameter.class)));
        });
        ArrayList<VariableDeclarator> variableDeclarators_done = new ArrayList<>();
        HashMap<String,String> name_done = new HashMap<String,String>();
        for (NameExpr nameExpr: nameExprs) {
            if (name_done.containsKey(nameExpr.getNameAsString())) {
                nameExpr.setName((String) name_done.get(nameExpr.getNameAsString()));
                continue;
            }
            for (VariableDeclarator variableDeclarator: variableDeclarators) {
                if (Objects.equals(variableDeclarator.getNameAsString(), nameExpr.getNameAsString())){
                    variableDeclarator.setName("var"+i);
                    variableDeclarators_done.add(variableDeclarator);
                }
            }
            for (Parameter parameter: parameters) {
                if (Objects.equals(parameter.getNameAsString(), nameExpr.getNameAsString())){
                    parameter.setName("var"+i);
                }
            }
            name_done.put(nameExpr.getNameAsString(), "var" +i);
            nameExpr.setName("var"+i);
            i++;
        }
        HashMap<String,String> field_done = new HashMap<String,String>();
        for (FieldAccessExpr fieldAccessExpr: fieldAccessExprs) {
            if (field_done.containsKey(fieldAccessExpr.getNameAsString())) {
                fieldAccessExpr.setName((String) field_done.get(fieldAccessExpr.getNameAsString()));
                continue;
            }
            for (VariableDeclarator variableDeclarator: this.field_variableDeclarators) {
                if (Objects.equals(variableDeclarator.getNameAsString(), fieldAccessExpr.getNameAsString())){
                    variableDeclarator.setName("field"+j);
                    variableDeclarators_done.add(variableDeclarator);
                }
            }
            field_done.put(fieldAccessExpr.getNameAsString(), "field" +j);
            fieldAccessExpr.setName("field"+j);
            j++;
        }
        for (VariableDeclarator variableDeclarator: variableDeclarators) {
            if (variableDeclarators_done.contains(variableDeclarator)){continue;}
            variableDeclarator.setName("unused");
        }
    }


    private void renameVariableFieldName(List<Node> nodeList) {
        int i = 1;
        int j = 1;
        ArrayList<NameExpr> nameExprs = new ArrayList<NameExpr>();
        ArrayList<FieldAccessExpr> fieldAccessExprs = new ArrayList<FieldAccessExpr>();
        ArrayList<VariableDeclarator> variableDeclarators = new ArrayList<VariableDeclarator>();
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        nodeList.forEach(node -> {
            nameExprs.addAll(node.findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
            fieldAccessExprs.addAll(node.findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
            variableDeclarators.addAll(node.findAll(VariableDeclarator.class).stream().filter(variableDeclarator -> !this.field_variableDeclarators.contains(variableDeclarator)).collect(Collectors.toList()));
            parameters.addAll(new ArrayList<>(node.findAll(Parameter.class)));
        });
        ArrayList<VariableDeclarator> variableDeclarators_done = new ArrayList<>();
        HashMap<String,String> name_done = new HashMap<String,String>();
        ArrayList<Node> nodes = new ArrayList<>();
        parameters.sort(new EmpComparator());
        variableDeclarators.sort(new EmpComparator());
        this.field_variableDeclarators.sort(new EmpComparator());
        HashMap<String,String> field_done = new HashMap<String,String>();
        HashMap<String,String> variableDeclarator_done = new HashMap<String,String>();
        System.out.println(fieldAccessExprs);
        for (VariableDeclarator variableDeclarator: this.field_variableDeclarators) {
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
//
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


    private void renameVariableFieldNames(List<Node> nodeList) {
        int i = 1;
        int j = 1;
        ArrayList<NameExpr> nameExprs = new ArrayList<NameExpr>();
        ArrayList<FieldAccessExpr> fieldAccessExprs = new ArrayList<FieldAccessExpr>();
        ArrayList<VariableDeclarator> variableDeclarators = new ArrayList<VariableDeclarator>();
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        nodeList.forEach(node -> {
            nameExprs.addAll(node.findAll(NameExpr.class).stream().filter(nameExpr -> !this.SimpleNamesOfClassOrInterfaceType.contains(nameExpr.getName())).collect(Collectors.toList()));
            fieldAccessExprs.addAll(node.findAll(FieldAccessExpr.class).stream().filter(fieldAccessExpr -> fieldAccessExpr.getScope().isThisExpr()).collect(Collectors.toList()));
            variableDeclarators.addAll(node.findAll(VariableDeclarator.class).stream().filter(variableDeclarator -> !this.field_variableDeclarators.contains(variableDeclarator)).collect(Collectors.toList()));
            parameters.addAll(new ArrayList<>(node.findAll(Parameter.class)));
        });
        ArrayList<VariableDeclarator> variableDeclarators_done = new ArrayList<>();
        HashMap<String,String> name_done = new HashMap<String,String>();
        ArrayList<Node> nodes = new ArrayList<>();
        parameters.sort(new EmpComparator());
        variableDeclarators.sort(new EmpComparator());
        this.field_variableDeclarators.sort(new EmpComparator());
        HashMap<String,String> field_done = new HashMap<String,String>();
        HashMap<String,String> variableDeclarator_done = new HashMap<String,String>();
        System.out.println(fieldAccessExprs);
        for (VariableDeclarator variableDeclarator: this.field_variableDeclarators) {
            Boolean isGet = Boolean.FALSE;
            for (FieldAccessExpr fieldAccessExpr: fieldAccessExprs) {
                if (Objects.equals(variableDeclarator.getNameAsString(), fieldAccessExpr.getNameAsString())) {
                    fieldAccessExpr.setName("var" + i);
                    isGet = Boolean.TRUE;
                }
            }
            if (isGet) {
                variableDeclarator.setName("var" + i);
                i++;
            }
        }
        for (Parameter parameter: parameters) {
            for (NameExpr nameExpr: nameExprs) {
                if (Objects.equals(parameter.getNameAsString(), nameExpr.getNameAsString())) {
                    nameExpr.setName("var" + i);
                }
            }
            parameter.setName("var" + i);
            i++;
        }
        for (VariableDeclarator variableDeclarator: variableDeclarators) {
            for (NameExpr nameExpr: nameExprs) {
                if (Objects.equals(variableDeclarator.getNameAsString(), nameExpr.getNameAsString())) {
                    nameExpr.setName("var" + i);
                }
            }
            variableDeclarator_done.put(variableDeclarator.getNameAsString(), "var" +i);
            variableDeclarators_done.add(variableDeclarator);
            variableDeclarator.setName("var" + i);
            i++;
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

    private ArrayList<MethodCallExpr> getMethodCallOfline(int line){
        ArrayList<MethodCallExpr> methodCallExprsOfline = new ArrayList<MethodCallExpr>();
        for (MethodCallExpr methodCallExpr: this.methodCallExprs) {
            if (methodCallExpr.getBegin().get().line == line){methodCallExprsOfline.add(methodCallExpr);}
        }
        return methodCallExprsOfline;
    }

    private List<Node> getSliceOfMethodCallOfLine(int methodCall_line, String methodCall_name) {
        for (MethodDeclaration methodDeclaration :
                this.methodDeclarations) {
            List<MethodCallExpr> methodCallExprs = methodDeclaration.findAll(MethodCallExpr.class);
            Boolean isThisMethodDecl = Boolean.FALSE;
            for (MethodCallExpr methodCallExpr : methodCallExprs) {
                // 方法调用的 名字、行号
                if (Objects.equals(methodCallExpr.getNameAsString(), methodCall_name) && methodCallExpr.getBegin().get().line == methodCall_line) {
                    // 获取方法调用的参数的代码段，作为返回值
                    return this.getSliceOfMethod(methodCallExpr, methodDeclaration);
                }
            }
        }
        return null;
    }


    public static void main(String[] args) throws FileNotFoundException {
        String srcfilename = "data/ClusterUnitDatabase2.java";
        CFG p = new CFG(srcfilename);
        System.out.println("GraphProcess.Parse: " + p.srcfilename);

    }
}
