package Neo4j.testcode;

public class Testcase1 {
    /*
    测试 FunctionClass中的findClassList，通过函数调用来获得当前函数申明中所调用的类
    如果存在内部类函数被调用格式如下：
    外部类.内部类
     */
Testcase1_1 testcase1_1=new Testcase1_1() {
        @Override
        public String getName() {
            System.out.println("重写方法1");
            return null;
        }

        @Override
        public String getSex() {
            System.out.println("重写方法2");
            return null;
        }
    };




}
