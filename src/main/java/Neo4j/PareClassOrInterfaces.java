package Neo4j;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static Neo4j.FunctionParse.getClassOfMethod;

public class PareClassOrInterfaces {
    private MethodDeclaration methodDeclaration;
    @Getter
    @Setter
    private List<String> classNameList = new ArrayList<>();

    public PareClassOrInterfaces(MethodDeclaration methodDeclaration) {//针对函数申明来得到当前函数申明中函数调用所使用的所有类
        this.methodDeclaration = methodDeclaration;

    }

    public List<String> findClassList() {
        //通过函数调用（MethodExpCall来找到函数中的所有的类型）
        String className = "";
        List<MethodCallExpr> methodCallExprs = this.methodDeclaration.findAll(MethodCallExpr.class);
        for (MethodCallExpr methodCallExpr : methodCallExprs) {
            int legth = methodCallExpr.findAll(ObjectCreationExpr.class).size();
            if (legth != 0) {
                //通过new来实现函数调用
                ObjectCreationExpr objectCreationExpr = methodCallExpr.findAll(ObjectCreationExpr.class).get(0);
                className = objectCreationExpr.getTypeAsString();
                classNameList.add(className);

            } else if (methodCallExpr.getScope().isPresent() && methodCallExpr.getScope().get().toString().charAt(0) - 0 < 'Z' - 0) {
                //通过静态类名来实现调用,并且是首字符是大写
                className = methodCallExpr.getScope().get().toString();
                classNameList.add(className);

            } else {
                //通过先前声明的对象来调用，小写字母，把收集当前函数声明当中的类或者接口
                //直接在最后全部加上即可,肯定不会漏掉
            }
        }
        methodDeclaration.findAll(ClassOrInterfaceType.class).forEach(classOrInterfaceType -> classNameList.add(classOrInterfaceType.getNameAsString()));
        //利用hashset去除重复元素
        Set hashset = new HashSet(classNameList);
        classNameList.clear();
        classNameList.addAll(hashset);
        return classNameList;
    }

    /** 
    * @Description: 返回当前类对象的函数声明中所有调用函数，所属的类名 
    * @Param:  
    * @return:  
    * @Author: Kangaroo
    * @Date: 2019/10/24
    */ 
    public List<String> findClassList1() {
        //得到当前函数申明中函数调用可能用到的所有类和接口
        List<MethodCallExpr> methodCallExprList = this.methodDeclaration.findAll(MethodCallExpr.class);

        for (MethodCallExpr methodCallExpr : methodCallExprList) {
            if (methodCallExpr.getScope().isPresent()) {
                //型如这种方式：类.函数调用
                //使用空格和.作为分隔符
                // TODO 可能存在异常，按照.来切分
                String name = methodCallExpr.getScope().get().toString();
                if (name.startsWith("new")) {
                    classNameList.add(
                            concatName(name.substring("new".length() + 1)));
                } else if (name.charAt(0) < 'Z') {
                    classNameList.add(concatName(name));
                } else {
                    // TODO 小写字母，需要追踪变量定义的位置，直接添加本函数中的所有的类和接口，这个变量的类型一定在里面，下面@1解决此处问题
                }

            } else {
                //函数调用前没有任何修饰符
                //被调用的函数一定是在本类当中，直接通过函数名引用即可，只需要添加本文件类即可。
                classNameList.add(getClassOfMethod(this.methodDeclaration));//添加本函数所在的类名
            }
        }
        // TODO @1
        // 如果参数是某个类的对象，则classNameList同样会将此类名添加进去

        // Data: 2019年10月31日20:39:07
        // 这里不处理变量的问题，在此函数被调用的后续进行处理
//        methodDeclaration.findAll(ClassOrInterfaceType.class).forEach(classOrInterfaceType -> classNameList.add(classOrInterfaceType.getNameAsString()));

        // 解决有些类带有括号
        classNameList = removeSpace(classNameList);

        //取出重复元素
        Set hashSet = new HashSet(classNameList);
        classNameList.clear();
        classNameList.addAll(hashSet);

        return classNameList;
    }

    public static String concatName(String classString) {
        String[] s = classString.split("\\.");
        String className = "";
        for (String name : s) {
            if (name.charAt(0) > 'Z') {
                //小写字母，是函数名
                continue;
            } else {
                //大写字母，类名
                className = className.concat(name + ".");

            }
        }
        try {
            className = className.substring(0, className.length() - 1);
        } catch (Exception e) {
//            System.out.println("concatName：函数异常");
            className = "";
            return className;
        }
        return className;

    }

    public List<String> removeSpace(List<String> className) {
        List<String> removedClassName = new ArrayList<>();
        className.stream().forEach(name -> removedClassName.add(name.replace("(", "").replace(")", "")));
        return removedClassName;
    }


}
