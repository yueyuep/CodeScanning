package com.nwu.nisl.parse.neo4j;
import com.nwu.nisl.parse.graph.Util;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.Type;
import com.nwu.nisl.parse.graph.AST2Graph;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GraphParse {

    private String fileName;
    private String version;
    /**
     * 函数名-类名.类名-参数类型1-参数类型2
     **/
    private List<String> callMethodName = new ArrayList<>();
    private String methodName;

    private static Logger logger = LoggerFactory.getLogger(GraphParse.class);

    public GraphParse() {
        /* 无参构造函数； */
    }


    public static void main(String[] args) {
        System.out.println("Please enter the version number: ");
        Scanner scanner = new Scanner(System.in);
        String version = scanner.next();

        System.out.println("Parsing:");
        /* 运行之前，手动删除测试项目中所有测试代码 (test文件夹) */
        String sourcePath = "dataset/source/" + version;
        String targetPath = "dataset/target/" + version;
        File dir = new File(sourcePath);
        ExtractJavaFile javaFile = new ExtractJavaFile(dir);
        javaFile.getFileList(dir);
        File[] fileList = javaFile.getFile();
        ProcessMultiFile(fileList, targetPath);

        System.out.println("End Parsing");
    }

    public static void ProcessMultiFile(File[] fileList, String targetPath) {
        //写入当前文件的头文件信息
        /**
         0、methodDeclation包含当前文件中所有的函数包括如下：解决函数重名问题
         1、常规文件类中的函数
         2、内部类中的函数
         3、构造函数
         4、重载函数处理
         */
        //头文件处理
        HashMap<String, HashMap<MethodDeclaration, String>> callMethod;

        // 获得所有文件的内部类函数和外部类函数
        List<HashMap<File, HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>>>> fileMethodDeclarationMap =
                Utils.getFileMethodDeclarationMap(fileList);

        // 删除之前生成保存 json 文件的文件夹
        FileSystemUtils.deleteRecursively(new File(targetPath));

        //循环遍历文件处理
        for (File file : fileList) {
            //log
            logger.info("Parsing File:" + file.getName());

            AST2Graph ast2Graph = AST2Graph.newInstance(file.getPath());
            // 不包含 new 类{ 函数 }的情况
            List<MethodDeclaration> methodDeclarations = ast2Graph.getmethodDeclarations();

            // 写入当前文件的头文件信息
            new GraphParse().headOfJson(file, methodDeclarations, targetPath + File.separator + Utils.getFileNameWithPath(file) + ".txt");
            //获得当前文件的外部类、内部类函数
            HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> outclassMethods = fileMethodDeclarationMap.get(1).get(file);
            HashMap<ClassOrInterfaceDeclaration, List<MethodDeclaration>> innerclassMethods = fileMethodDeclarationMap.get(0).get(file);

            //循环遍历函数声明处理
            for (MethodDeclaration methodDeclaration : methodDeclarations) {
                //  函数申明在外部类或者内部类中
                if (Utils.containMethod(methodDeclaration, outclassMethods, innerclassMethods)) {
                    //目前只处理外部类和内部类中的函数
                    callMethod = Utils.getcallMethods(methodDeclaration, fileMethodDeclarationMap);
                    try {
                        new GraphParse().methodOfJson(file, methodDeclaration, callMethod, targetPath + File.separator + Utils.getFileNameWithPath(file) + ".txt");
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        System.out.println(methodDeclaration.getNameAsString() + "\t:内外部类函数构造异常");
                        continue;
                    }

                }

            }
        }
    }

    /**
     * @Description: 将文件的基本信息写入Json文件中（第一行）
     * @Param:
     * @return:
     * @Author: Kangaroo
     * @Date: 2019/10/22
     */
    public void headOfJson(File file, List<MethodDeclaration> methodDeclarations, String saveFilePath) {
        this.fileName = Utils.getFileNameWithPath(file);
        this.version = Utils.getVersion(file);
        // 函数名-类名.类名-参数类型-参数类型
        // 无参函数： 函数名-类名.类名-
        methodDeclarations.forEach(methodDeclaration -> this.callMethodName.add(
                methodDeclaration.getNameAsString() + "-" +
                        getClassNameOfMethod(methodDeclaration) + "-" +
                        getMethodParameter(methodDeclaration)
        ));

        Util.saveToJsonFile(new DataToJson.Head(this.fileName, this.version, this.callMethodName), saveFilePath);
    }

    /**
     * @Description: 保存文件中函数的基本信息到Json中
     * @Param:
     * @return:
     * @Author: Kangaroo
     * @Date: 2019/10/18
     */
    public void methodOfJson(File file, MethodDeclaration methodDeclaration, HashMap<String, HashMap<MethodDeclaration, String>> CalledMethod, String saveFilePath) {
        this.fileName = Utils.getFileNameWithPath(file);
        this.version = Utils.getVersion(file);
        this.methodName = methodDeclaration.getNameAsString() + "-" + getClassNameOfMethod(methodDeclaration) + "-" + getMethodParameter(methodDeclaration);

        DataToJson.Body body = new DataToJson.Body(file, this.fileName, this.version, this.methodName, methodDeclaration, CalledMethod);
        body.addFeatureMethodOfJson();

        Util.saveToJsonFile(body, saveFilePath);
    }

    /**
     * @Description: 返回函数的类名，多层嵌套
     * @Param:
     * @return:
     * @Author: Kangaroo
     * @Date: 2019/10/22
     */
    public String getClassNameOfMethod(Node methodDeclaration) {
        List<String> allClassName = new ArrayList<>();

        while (methodDeclaration.getParentNode().isPresent() && !(methodDeclaration.getParentNode().get() instanceof CompilationUnit)) {

            if (methodDeclaration.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
                allClassName.add(((ClassOrInterfaceDeclaration) methodDeclaration.getParentNode().get()).getName().toString());
            } else if (methodDeclaration.getParentNode().get() instanceof ObjectCreationExpr) {
                // TODO
                // 函数定义在 new 类名(){}中的情况暂不完善
                //
//                allClassName.add(((ObjectCreationExpr)methodDeclaration.getParentNode().get()).getTypeAsString());

            } else {
                // TODO
                // 第二种情况再往上遍历时，会找到其他类型的节点

//                System.out.println("此情况未考虑");
//                System.exit(0);
            }
            methodDeclaration = methodDeclaration.getParentNode().get();
        }

        Collections.reverse(allClassName);
        return StringUtils.join(allClassName.toArray(), ".");
    }

    /**
     * @Description: 获取带参数类型的函数名
     * @Param:
     * @return: String
     * @Author: Kangaroo
     * @Date: 2019/10/22
     */
    public String getMethodParameter(MethodDeclaration methodDeclaration) {
        List<String> res = new ArrayList<>();

        for (Parameter parameter : methodDeclaration.getParameters()) {
            Type type = parameter.getType();
            String string = new String();

            if (type.isArrayType()) {
                string = parameter.getType().asArrayType().asString();

            } else if (type.isClassOrInterfaceType()) {
                string = parameter.getType().asClassOrInterfaceType().asString();

            } else if (type.isIntersectionType()) {
                string = parameter.getType().asIntersectionType().asString();

            } else if (type.isPrimitiveType()) {
                string = parameter.getType().asPrimitiveType().asString();

            } else if (type.isReferenceType()) {
                System.out.println("ReferenceType");
                // pass

            } else if (type.isTypeParameter()) {
                string = parameter.getType().asTypeParameter().asString();

            } else if (type.isUnionType()) {
                string = parameter.getType().asUnionType().asString();

            } else if (type.isUnknownType()) {
                string = parameter.getType().asUnknownType().asString();

            } else if (type.isVarType()) {
                string = parameter.getType().asVarType().asString();

            } else if (type.isVoidType()) {
                string = parameter.getType().asVoidType().asString();

            } else if (type.isWildcardType()) {
                string = parameter.getType().asWildcardType().asString();

            } else {
                System.out.println("Wrong!");
                System.exit(0);
            }
            res.add(string);
        }

        return StringUtils.join(res.toArray(), "-");
    }

}



