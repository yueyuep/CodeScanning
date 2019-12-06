package com.nwu.nisl.parse.neo4j;

import com.nwu.nisl.parse.graph.RangeNode;
import com.nwu.nisl.parse.neo4j.attribute.ParseExpression;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Utils {
    @Setter
    @Getter
    private static List<MethodCallExpr> methodCallExprsNeed = new ArrayList<>();


    public static String addAttributes(Object object) {//为CFG每个节点添加属性
        if (object instanceof RangeNode) {
            Node node = ((RangeNode) object).getmNode();
            return new ParseExpression(node).set2List();
        } else {
            System.out.println("不存在RangeNode节点");
            return null;
        }

    }

    public static Node Object2Node(Object object) {
        if (object instanceof RangeNode) {
            Node node = ((RangeNode) object).getNode();
            return node;

        } else {
            System.out.println("节点类型错误(!RangeNode)");
            return null;
        }
    }

    /**
    * @Description: 获取当前函数声明中实例化的类对象(变量)名与类名的对应关系
    * @Param:
    * @return:
    * @Author: Kangaroo
    * @Date: 2019/10/23
    */
    public static HashMap<String, String> variableDeclaratorOfClass(MethodDeclaration methodDeclaration){
        HashMap<String, String> variableDeclaratorList = new HashMap<>();

        List<VariableDeclarator> variableDeclarators = methodDeclaration.findAll(VariableDeclarator.class);

        for (VariableDeclarator variableDeclarator: variableDeclarators){
            if (variableDeclarator.getType().isClassOrInterfaceType()){
                String name = variableDeclarator.getNameAsString();
                // TODO
                //  type可能不是仅一个单独的字符串， 推测可能有 类名.类名 的情况
                String type = variableDeclarator.getTypeAsString();
                variableDeclaratorList.put(name, type);
            }
        }
        return variableDeclaratorList;
    }

    /**
    * @Description: 获取当前函数所属的类中声明的 类对象名与类名的对应关系
    * @Param:
    * @return:
    * @Author: Kangaroo
    * @Date: 2019/10/24
    */
    public static HashMap<String, String> variableDeclaratorOfClassInClass(MethodDeclaration methodDeclaration){
        HashMap<String, String> variableDeclaratorList = new HashMap<>();

        // 父节点应该肯定存在，以防万一加上判断
        if (methodDeclaration.getParentNode().isPresent() && methodDeclaration.getParentNode().get() instanceof ClassOrInterfaceDeclaration){
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration =
                    ((ClassOrInterfaceDeclaration) methodDeclaration.getParentNode().get()).asClassOrInterfaceDeclaration();

            for (FieldDeclaration fieldDeclaration: classOrInterfaceDeclaration.findAll(FieldDeclaration.class)){
                for (VariableDeclarator variableDeclarator: fieldDeclaration.getVariables()){
                    if (variableDeclarator.getType().isClassOrInterfaceType()){
                        String name = variableDeclarator.getNameAsString();
                        String type = variableDeclarator.getTypeAsString();
                        variableDeclaratorList.put(name, type);
                    }
                }
            }
        }

        return variableDeclaratorList;
    }



    /** 
    * @Description: 判断函数声明属于外部类或内部类中， 不属于的情况， new 类{ 函数()} 
    * @Param:  
    * @return:  
    * @Author: Kangaroo
    * @Date: 2019/10/22 
    */ 
    public static boolean containMethod(MethodDeclaration methodDeclaration, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> outclassMethods, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> innertclassMethods) {
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : outclassMethods.keySet()) {
            if (outclassMethods.get(classOrInterfaceDeclaration).contains(methodDeclaration)) {
                return true;
            }
        }
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : innertclassMethods.keySet()) {
            if (innertclassMethods.get(classOrInterfaceDeclaration).contains(methodDeclaration)) {
                return true;
            }
        }
        return false;
    }

    public static boolean compareMethod(MethodCallExpr methodCallExpr, MethodDeclaration methodDeclaration) {
        //解决函数调用和函数名相等、参数数量相等情况下，找到不止一个函数位置
        if (methodCallExpr.getNameAsString().equals(methodDeclaration.getNameAsString()) && methodCallExpr.getArguments().size() == methodDeclaration.getParameters().size()) {
            if (true) {
                return true;
                //参数类型一样
            }
        }
        return false;
    }

    /** 
    * @Description: 返回所有文件中包含的 内部函数声明 和 外部函数声明; 内部函数的类只保存第一层
    * @Param:  
    * @return:  
    * @Author: Kangaroo
    * @Date: 2019/10/22 
    */ 
    public static List<HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>>> getFileMethodDeclarationMap(File[] files) {
        // 获得所有文件的内部函数声明和外部函数声明
        List<HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>>> fileMethodDeclarationMap = new ArrayList<>();
        //<外部类，外部类中所有的方法声明>
        HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>> allOutclassMethods = new HashMap<>();
        //<内部类，内部类中所有的方法声明>
        HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>> allInnerclassMethods = new HashMap<>();

        for (File file : files) {
            FunctionParse functionParse = new FunctionParse(file);
            functionParse.PareMethod();
            allOutclassMethods.put(file, functionParse.getOutclassMethods());
            allInnerclassMethods.put(file, functionParse.getInnerclassMethods());
        }
        fileMethodDeclarationMap.add(allInnerclassMethods);
        fileMethodDeclarationMap.add(allOutclassMethods);
        
        return fileMethodDeclarationMap;
    }

    /** 
    * @Description: 保存调用函数的基本信息（如果存在），所属文件，类名，函数声明 
    * @Param:  
    * @return:  
    * @Author: Kangaroo
    * @Date: 2019/10/23 
    */ 
    public static ThreeTuple getCalledExprLocation(HashMap<ClassOrInterfaceDeclaration, String> candidateFile,
                                                   List<HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>>> fileMethodDeclarationMap,
                                                   MethodCallExpr methodCallExpr,
                                                   HashMap<String, String> variableDeclaratorOfClass,
                                                   HashMap<String, String> variableDeclaratorOfClassInClass) {
        // 返回<文件名，类名,methodDeclaration>，现在只是在内部类、外部类函数中找，(接口实现重写未处理)
        String methodCallExprName = methodCallExpr.getNameAsString();
        ThreeTuple calledExprLocation = new ThreeTuple();

        HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>> allInnerclassMethods = fileMethodDeclarationMap.get(0);
        HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>> allOutclassMethods = fileMethodDeclarationMap.get(1);

        if (candidateFile.keySet().isEmpty()){
            //候选集为空，不存在函数调用，返回空值。
            return calledExprLocation;
        }
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : candidateFile.keySet()) {
            //首先
            // TODO
            //  存在逻辑问题，如果文件的外部类和内部类都在候选集中，有同名且参数个数相同的函数，判断会出错（若正确匹配在内部类中，但当前逻辑为先判断外部类中是否存在，导致匹配错误）
            if (classOrInterfaceDeclaration.getNameAsString().concat(".java").equals(candidateFile.get(classOrInterfaceDeclaration))) {
                //文件名和类名一样，在外部类中调用
                //得到函数调用所在的函数名字
                String filename = getMethodExprFileName(methodCallExpr, variableDeclaratorOfClass, variableDeclaratorOfClassInClass);
                for (File file : allOutclassMethods.keySet()) {
                    if (file.getName().equals(filename)) {//找到文件
                        HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> classOrInterfaceDeclarationListHashMap = allOutclassMethods.get(file);

                        List<ClassOrInterfaceDeclaration> targetClassList;
                        List<MethodDeclaration> targetMethodList;
                        try {
                            targetClassList = classOrInterfaceDeclarationListHashMap.keySet().stream().
                                    filter(classOrInterfaceDeclaration1 -> classOrInterfaceDeclaration1
                                    .getNameAsString().concat(".java").equals(filename))
                                    .collect(Collectors.toList()); //外部类中，类名与文件全部一样
                            if (targetClassList != null && targetClassList.size() > 0){
                                targetMethodList = classOrInterfaceDeclarationListHashMap.get(targetClassList.get(0)).stream().
                                        filter(methodDeclaration ->
                                                methodDeclaration.getNameAsString().equals(methodCallExprName)
                                                        && methodDeclaration.getParameters().size() == methodCallExpr.getArguments().size())
                                        .collect(Collectors.toList());  //函数名一致，并且参数列表的长度一致
                                if (targetMethodList != null && targetMethodList.size() > 0){
                                    // 外部类
                                    calledExprLocation.setFile(file);
                                    calledExprLocation.setClassName(targetClassList.get(0).getNameAsString());
                                    calledExprLocation.setMethodDeclaration(targetMethodList.get(0));
                                    return calledExprLocation;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            System.out.println("MethodCallexpr：" + filename + "\n" + "classOrInterfaceDeclaration:" + classOrInterfaceDeclaration.getNameAsString());
                            continue;

                        }
                    }
                }
            } else {
                //在内部类中调用
                //得到函数调用所在的文件名
                String filename = getMethodExprFileName(methodCallExpr, variableDeclaratorOfClass, variableDeclaratorOfClassInClass);
                for (File file : allInnerclassMethods.keySet()) {
                    if (file.getName().equals(filename)) {//找到文件
                        HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> classOrInterfaceDeclarationListHashMap = allInnerclassMethods.get(file);

                        List<ClassOrInterfaceDeclaration> targetClassList;
                        List<MethodDeclaration> targetMethodList;
                        try {
                            targetClassList = classOrInterfaceDeclarationListHashMap.keySet().stream()
                                    .filter(classOrInterfaceDeclaration1 -> classOrInterfaceDeclaration1
                                    .getNameAsString().equals(classOrInterfaceDeclaration.getNameAsString()))
                                    .collect(Collectors.toList());

                            if (targetClassList != null && targetClassList.size() > 0){
                                targetMethodList = classOrInterfaceDeclarationListHashMap.get(targetClassList.get(0)).stream()
                                        .filter(methodDeclaration ->
                                                methodDeclaration.getNameAsString().equals(methodCallExprName)
                                                        && methodDeclaration.getParameters().size() == methodCallExpr.getArguments().size())
                                        .collect(Collectors.toList());
                                if (targetMethodList != null && targetMethodList.size() > 0){
                                    // 内部类
                                    calledExprLocation.setFile(file);
//                                    calledExprLocation.setClassName(targetClassList.get(0).getNameAsString());
                                    calledExprLocation.setClassName(new GraphParse().getClassNameOfMethod(targetMethodList.get(0)));
                                    calledExprLocation.setMethodDeclaration(targetMethodList.get(0));
                                    return calledExprLocation;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            System.out.println("MethodCallexpr：" + methodCallExpr.getNameAsString() + "\n" + "classOrInterfaceDeclaration:" + classOrInterfaceDeclaration.getNameAsString());
                            continue;

                        }
                    }
                }
            }
            return calledExprLocation;//这种情况没有找到，系统库函数、或者第三方的函数。
        }

        return calledExprLocation;

    }

    /**
    * @Description: 返回函数声明中调用函数，所属的类声明，文件名。 用于缩小下一步的范围
    * @Param:
    * @return:
    * @Author: Kangaroo
    * @Date: 2019/10/23
    */
    public static HashMap<ClassOrInterfaceDeclaration, String> getCandidateFileByClass(MethodDeclaration methodDeclaration,
                                                                                       List<HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>>> fileMethodDeclarationMap,
                                                                                       ArrayList<String> classOrInterfaceNameInClass,
                                                                                       ArrayList<String> classOrInterfaceNameInMethod) {
        HashMap<ClassOrInterfaceDeclaration, String> candidateFile = new HashMap<>();
        List<File> pFile;
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList;

        // 我们的函数调用通过类名来引用：new对象，或者直接使用静态类；
        List<String> classOrInterfaceNameList = new PareClassOrInterfaces(methodDeclaration).findClassList1();//获得当前函数声明中所有类名字;
        // 添加变量属于的类名
        classOrInterfaceNameList.addAll(classOrInterfaceNameInClass);
        classOrInterfaceNameList.addAll(classOrInterfaceNameInMethod);


        for (String className : classOrInterfaceNameList) {
            // TODO
            //  考虑不完整，多层类嵌套
            if (className.contains(".")) {
                //处理GsonCompatibilityMode.Builder这种类型的数据，前一个是外部类，后一个是内部类，scope作为修饰符
                String fileName = className.split("\\.")[0];
                String classOrInterfaceName = className.split("\\.")[1];
                pFile = fileMethodDeclarationMap.get(0).keySet().stream().filter(file -> file.getName().equals(fileName.concat(".java"))).collect(Collectors.toList());
                if (pFile.isEmpty()) {
//                    System.out.println(className + "是系统类，跳过");
                    continue;
                }
                try {
                    classOrInterfaceDeclarationList = fileMethodDeclarationMap.get(0).get(pFile.get(0)).keySet().stream()
                            .filter(classOrInterfaceDeclaration1 -> classOrInterfaceDeclaration1.getNameAsString()
                            .equals(classOrInterfaceName)).collect(Collectors.toList());
                    if (classOrInterfaceDeclarationList != null && classOrInterfaceDeclarationList.size() > 0){
                        candidateFile.put(classOrInterfaceDeclarationList.get(0), fileName.concat(".java"));
                    }

                } catch (Exception e) {
                    System.out.println("跳过越界");
                    continue;
                }

            } else {
                // JsoniterSpi这种形式属于外部类函数
                String fileName = className;
                String classOrInterfaceName = className;
                //optional包装下，允许空值
                pFile = fileMethodDeclarationMap.get(1).keySet().stream().filter(file -> file.getName().equals(fileName.concat(".java"))).collect(Collectors.toList());
                if (pFile.isEmpty()) {
//                    System.out.println(className + "是系统类，跳过");
                    continue;
                }
                try {
                    // 存在找到与className同名的文件，但是此文件不包含类
                    classOrInterfaceDeclarationList = fileMethodDeclarationMap.get(1).get(pFile.get(0)).keySet().stream().filter(classOrInterfaceDeclaration1
                            -> classOrInterfaceDeclaration1.getNameAsString().equals(classOrInterfaceName))
                            .collect(Collectors.toList());

                    if (classOrInterfaceDeclarationList != null && classOrInterfaceDeclarationList.size() > 0){
                        candidateFile.put(classOrInterfaceDeclarationList.get(0), fileName.concat(".java"));
                    }

                } catch (Exception e) {
                    System.out.println("跳过越界异常");
                    continue;
                }
            }
        }
        return candidateFile;
    }

    /**
    * @Description: 返回此函数声明中函数调用的信息 {文件名：{函数声明：类名}}
    * @Param:
    * @return:
    * @Author: Kangaroo
    * @Date: 2019/10/23
    */
    public static HashMap<String, HashMap<MethodDeclaration, String>> getcallMethods(MethodDeclaration methodDeclaration, List<HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>>> fileMethodDeclarationMap) {
        /**
         找到所有函数调用所在的文件名，类名、函数申明
         *
         * 通过类找到函数调用的位置，类中的函数必须通过类名或者接口来调用（提升查找效率）
         */

        HashMap<String, HashMap<MethodDeclaration, String>> CalledMethod = new HashMap<>();
        List<MethodCallExpr> methodCallExprList = methodDeclaration.findAll(MethodCallExpr.class);

        HashMap<String, String> variableDeclaratorOfClassInClass = variableDeclaratorOfClassInClass(methodDeclaration);
        HashMap<String, String> variableDeclaratorOfClass= variableDeclaratorOfClass(methodDeclaration);
        HashMap<ClassOrInterfaceDeclaration, String> candidateFile = getCandidateFileByClass(
                methodDeclaration,
                fileMethodDeclarationMap,
                (ArrayList<String>) variableDeclaratorOfClassInClass.values().stream().collect(Collectors.toList()),
                (ArrayList<String>) variableDeclaratorOfClass.values().stream().collect(Collectors.toList()));

        for (MethodCallExpr methodCallExpr : methodCallExprList) {
            //new GsonBuilder().setDataFormat("bdg").create(),最后一个.后面的才是我们的方法调用。
            //通过《文件名,类名》候选集来实现简化查找
            ThreeTuple calledExprLocation = getCalledExprLocation(candidateFile, fileMethodDeclarationMap, methodCallExpr, variableDeclaratorOfClass, variableDeclaratorOfClassInClass);//查找到某个函数调用的位置，用三元组表示

            if (calledExprLocation.getClassName() == null || calledExprLocation.getFile() == null || calledExprLocation.getMethodDeclaration() == null) {
//                System.out.println(methodCallExpr.getNameAsString() + "：是系统、库函数");
                //需要添加跳过操作continue
            } else {
                //找到函数的位置
                //[文件名，函数，类名],只存在一个3键对
                if (CalledMethod.keySet().contains(Utils.getFileNameWithPath(calledExprLocation.getFile()))){
                    CalledMethod.get(Utils.getFileNameWithPath(calledExprLocation.getFile())).put(calledExprLocation.getMethodDeclaration(), calledExprLocation.getClassName());
                }else{
                    CalledMethod.put(Utils.getFileNameWithPath(calledExprLocation.getFile()), new HashMap<MethodDeclaration, String>() {{
                        put(calledExprLocation.getMethodDeclaration(), calledExprLocation.getClassName());
                    }});
                }
            }

        }

        return CalledMethod;

    }

    /**
    * @Description: 返回调用函数所属的文件名
    * @Param:
    * @return:
    * @Author: Kangaroo
    * @Date: 2019/10/23
    */
    public static String getMethodExprFileName(MethodCallExpr methodCallExpr,
                                               HashMap<String, String> variableDeclaratorOfClass,
                                               HashMap<String, String> variableDeclaratorOfClassInClass) {
        String methodName = methodCallExpr.toString();
        String[] split = methodName.split("\\.");
        List<ObjectCreationExpr> objectCreationExpr = methodCallExpr.findAll(ObjectCreationExpr.class);

        if (!objectCreationExpr.isEmpty()) {
            //通过new创建的对象，匿名类调用
            return objectCreationExpr.get(0).getTypeAsString().split("\\.")[0].concat(".java");
        } else if (split[0].charAt(0) < 'Z') {
            //大写字母，静态类
            return split[0].concat(".java");
        } else {
            //TODO 通过对像来调用，提前定义好对象，然后通过对象来调用方法
            // 变量对应的类，如果是内部类，形如 A.B

            // 变量名.函数
            // Case 1:  a.test()
            // Case 2:  this.a.test()
            int index = 0;
            if (split[0].equals("this")){
                index = 1;
            }

            // 变量在当前函数中定义
            for (String name: variableDeclaratorOfClass.keySet()){
                if (name.equals(split[index])){
                    return variableDeclaratorOfClass.get(name).split("\\.")[index].concat(".java");
                }
            }

            // 变量在当前所属的类中定义，而不是当前函数中
            for (String name: variableDeclaratorOfClassInClass.keySet()){
                if (name.equals(split[index])){
                    return variableDeclaratorOfClassInClass.get(name).split("\\.")[0].concat(".java");
                }
            }
        }

        return null;
    }


    /**
    * @Description: 获取文件的路径（从所在项目根目录开始）
    * @Param:
    * @return:
    * @Author: Kangaroo
    * @Date: 2019/10/29
    */
    public static String getFileNameWithPath(File file){
        String[] array = file.getPath().split(Matcher.quoteReplacement(File.separator));
        String version = array[2];
        String res = file.getPath();

        return res.substring(res.indexOf(version) + version.length() + 1).replace("\\","/");
    }

    /**
     * @Author Kangaroo
     * @Description 获取文件的版本号，
     * @Date 2019/12/2 22:11
     * @Param [file]
     * @return java.lang.String
     **/
    public static String getVersion(File file) {
        int index = 2;
        return file.getPath().split(Matcher.quoteReplacement(File.separator))[index];
    }

}
