package owaspbench;

import GraphProcess.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.graph.MutableNetwork;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ Author     ：wxkong
 */
public class ExtractSubGraphs {
    String mSrcFileName;
    String mSaveFileName;

    private ExtractSubGraphs(String srcFileName, String saveFileName) {
        mSrcFileName = srcFileName;
        mSaveFileName = saveFileName;
    }

    public ExtractSubGraphs newInstance(String srcFileName, String saveFileName) {
        return new ExtractSubGraphs(srcFileName, saveFileName);//初始化
    }

    public static void ExtractSQLI(String filePath, String graphPath) {
        AST2Graph ast2Graph = AST2Graph.newInstance(filePath);
        if (ast2Graph == null) {
            System.out.println(filePath + ": null");
            return;
        }
        List<MethodCallExpr> relateMethodCalls = new ArrayList<>();//获取整个java文件的方法调用
        List<MethodDeclaration> methodDecls = new ArrayList<>();//获取整个java文件的函数声明
        for (MethodCallExpr mc : ast2Graph.getCompilationUnit().findAll(MethodCallExpr.class)) {//
            if (SQLI.ExecuteMethods.contains(mc.getNameAsString())) {//在所有的函数调用种找到相关的SQL调用的相关函数
                relateMethodCalls.add(mc);
                methodDecls.add(ast2Graph.findMethodDeclarationContains(mc));
            }
        }
        //遍历找到与漏洞相关的所有函数调用。
        for (int i = 0; i < relateMethodCalls.size(); i++) {
            MethodCallExpr callExpr = relateMethodCalls.get(i);
            MethodDeclaration decl = methodDecls.get(i);
            if (decl == null) {
                continue;
            }
            ast2Graph.initNetwork();//清理网络的配置。
            ast2Graph.constructNetwork(decl);
            MutableNetwork<Object, String> network = ast2Graph.getNetwork();
            List<Node> vars = new ArrayList<>();
            callExpr.getArguments().forEach(argument -> vars.addAll(new ArrayList<>(argument.findAll(NameExpr.class))));
            callExpr.getScope().ifPresent(scope -> vars.addAll(new ArrayList<>(scope.findAll(NameExpr.class))));
            List<RangeNode> rangeVars = new ArrayList<>();
            vars.forEach(var -> rangeVars.add(GenerateGraph.findNodeInNetwork(network, var)));
            Set<RangeNode> dataFlowNodes = new HashSet<>();
            for (RangeNode varNode : rangeVars) {
                if (varNode == null || dataFlowNodes.contains(varNode)) {
                    continue;
                }
                dataFlowNodes.addAll(ast2Graph.getRelatedDataFlowNodes(varNode, dataFlowNodes, new HashSet<RangeNode>()));
            }
            RemoveNode removeNode = RemoveNode.newInstance(filePath);
            if (removeNode == null) {
                System.out.println(filePath);
                return;
            }
//            ast2Graph.initNetworkWithoutRangeNode();
            removeNode.setRelatedNodes(dataFlowNodes);
            removeNode.setCallExpr(callExpr);
            removeNode.initNetwork();
            removeNode.constructNetwork(decl);
            MutableNetwork<Object, String> graph = removeNode.getNetwork();
            if (!graph.edges().isEmpty()) {
                Graph2Json graph2Json = Graph2Json.newInstance(graph);
                String save = graphPath + ".txt";
                if (i != 0) {
                    save = graphPath + "_" + i + ".txt";
                }
                graph2Json.saveToJson(save);
                System.out.println(save);
            }
        }
    }

    private static void removeNotContain(MutableNetwork<Object, String> network, Set<RangeNode> nodes) {
        Set<Object> toRemoved = new HashSet<>();
        for (Object node : network.nodes()) {
            if (node instanceof RangeNode) {
                Node parent = ((RangeNode) node).getNode();
                boolean remove = true;
                for (RangeNode rn : nodes) {
                    Node child = rn.getNode();
                    if (node.equals(rn) || parent.findAll(Node.class).contains(child)) {
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    toRemoved.add(node);
                }
            }
        }
        removeNodes(network, toRemoved);
    }

    private static void removeNodes(MutableNetwork<Object, String> network, Set<Object> toRemoved) {
        for (Object node : toRemoved) {
            network.removeNode(node);
        }
        Set<Object> remove = new HashSet<>();
        for (Object node : network.nodes()) {
            if (network.incidentEdges(node).isEmpty()) {
                remove.add(node);
            }
        }
        for (Object node : remove) {
            network.removeNode(node);
        }
    }

    public static void ExtractOwaspBackup(String filePath, String graphPath) {
        AST2Graph ast2Graph = AST2Graph.newInstance(filePath);
        if (ast2Graph == null) {
            System.out.println(filePath);
            return;
        }
        List<MethodDeclaration> methodPosts = ast2Graph.getCompilationUnit().findAll(MethodDeclaration.class).stream()
                .filter(methodDeclaration -> methodDeclaration.getNameAsString().equals("doPost"))
                .collect(Collectors.toList());
        if (!methodPosts.isEmpty()) {
            MethodDeclaration methodPost = methodPosts.get(0);
            ast2Graph.initNetwork();
            ast2Graph.constructNetwork(methodPost);
//            ast2Graph.renameNetworkVar();
            MutableNetwork<Object, String> network = ast2Graph.getNetwork();
            if (!network.edges().isEmpty()) {
                Graph2Json graph2Json = Graph2Json.newInstance(network);
                graph2Json.saveToJson(graphPath + ".txt");
                System.out.println(graphPath + ".txt");
            }
        }
    }

    public static int getVulDeeResult(String filePath,File javafile, String graphPath,String funcName,int flag) {
        flag++;
        CompilationUnit cuOrigin;
        //获取源码的编译单元
        try {
            cuOrigin = JavaParser.parse(new FileInputStream(filePath));
        } catch (Exception e) {
            System.out.println(filePath + " error\n" + e);
            return flag;
        }
        List<String> comments = new ArrayList<>();
        //获取函数声明的注释
        for (Comment comment : cuOrigin.getAllContainedComments()) {
            comments.add(comment.toString());
        }
        //处理源码中的字符串常量
        processStringCharLiteral(cuOrigin);
        CompilationUnit cu = JavaParser.parse(cuOrigin.toString());
        StringBuilder sb = new StringBuilder();
//        List<String> methodCalls = new ArrayList<>();
//        cu.findAll(MethodCallExpr.class).forEach((MethodCallExpr mc) ->
//                methodCalls.add(mc.getNameAsString()));
//        for (String methodName : methodCalls) {
//            getStringFromMethod(comments, cu, sb, methodName);
//        }
        getStringFromMethod(comments, cu, sb, funcName);
        String codeText = replaceBlank(sb.toString()).trim();
        graphPath=graphPath+javafile.getName()+"._"+flag+".txt";
        System.out.println(graphPath);
        Util.saveToFile(codeText, graphPath);
        return flag;
    }
    public  static void getVulDeeResultMutifile(String SourcePath,String SavePath){
        String funcName="";
        File cat=new File(SourcePath);
        for(File bug:cat.listFiles()){
            String url=bug.getPath()+"/sourceData/";
            File javaCat=new File(url);
            String desT=bug.getPath()+"/sentence/trueg/";
            String desF=bug.getPath()+"/sentence/falseg/";
            File dir1=new File(desT);
            File dir2=new File(desF);
            if(!dir1.exists()){dir1.mkdirs();}
            if(!dir2.exists()){dir2.mkdirs();}
            for(File javafile:javaCat.listFiles()){
                if (javafile.listFiles().length==1){
                    int falseg=0;
                    int trueg=0;
                    //单个文件的操作
                    AST2Graph ast2Graph= AST2Graph.newInstance(javafile.listFiles()[0].getPath());
                    List<MethodDeclaration> methodDeclaration=ast2Graph.getMethodDeclarations();
                    for(MethodDeclaration me:methodDeclaration){
                        if (me.getName().toString().endsWith("bad")){
                            //针对bad函数进行解析
                            funcName=me.getName().toString();
                            trueg=getVulDeeResult(javafile.listFiles()[0].getPath(),javafile.listFiles()[0],desT,funcName,trueg);
                        }
                        if(me.getName().toString().endsWith("good")){
                            //针对good函数进行解析
                            BlockStmt body=me.findAll(BlockStmt.class).get(0);
                            NodeList<Statement> statements=body.getStatements();
                            for(Statement st:statements){
                                MethodCallExpr methodCallExpr=st.findAll(MethodCallExpr.class).get(0);
                                String calledName=methodCallExpr.getName().toString();
                                MethodDeclaration fname=ast2Graph.getCompilationUnit().findAll(MethodDeclaration.class).stream().
                                        filter(m->m.getName().toString().equals(calledName)).collect(Collectors.toList()).get(0);
                                funcName=fname.getName().toString();
                                falseg=getVulDeeResult(javafile.listFiles()[0].getPath(),javafile.listFiles()[0],desF,funcName,falseg);
                            }
                        }
                    }
                }
                else {
                    int falseg=0;
                    int trueg=0;
                    //多个文件的操作
                   ArrayList<File> fileArrayList= new ArrayList<>();
                   HashMap<File, AST2Graph>fileAST2GraphHashMap=new HashMap<>();
                    for(File pfile:javafile.listFiles()){
                        if (pfile.getName().endsWith("base.java")){continue;}
                        else {
                            fileArrayList.add(pfile);
                            fileAST2GraphHashMap.put(pfile, AST2Graph.newInstance(pfile.getPath()));
                        }
                    }
                    //循环遍历文件，找到a结尾的文件
                    File Afile=fileAST2GraphHashMap.keySet().stream().filter(file -> file.getName().endsWith("a.java")).collect(Collectors.toList()).get(0);
                    AST2Graph Aast2Graph=fileAST2GraphHashMap.get(Afile);
                    for(MethodDeclaration me:Aast2Graph.getMethodDeclarations()){
                        if (me==null){
                            System.out.println("there is not method");
                            System.exit(0);
                        }
                        if (me.getName().toString().endsWith("bad")){
                            //bad函数
                            funcName=me.getName().toString();
                            trueg=getVulDeeResult(javafile.listFiles()[0].getPath(),javafile.listFiles()[0],desT,funcName,trueg);
                        }
                        if(me.getName().toString().endsWith("good")){

                            //=================《构建文件，函数申明》哈希表================
//                            HashMap<File,List<MethodDeclaration>>fileMethodList=new HashMap<>();
//                            for(File file:fileAST2GraphHashMap.keySet()){
//                                fileMethodList.put(file,fileAST2GraphHashMap.get(file).getMethodDeclarations());
//                            }
                            //=============================================================
                            ArrayList<File> visitedFile=new ArrayList<>();
                            BlockStmt body=me.findAll(BlockStmt.class).get(0);
                            NodeList<Statement> statements=body.getStatements();
                            for(Statement st:statements){//遍历所有的库函数和函数调用
                                MethodCallExpr methodCallExpr=st.findAll(MethodCallExpr.class).get(0);
                                String calledName=methodCallExpr.getName().toString();//获得被调用函数的名字
                                for(MethodDeclaration pmethodDeclaration:Aast2Graph.getMethodDeclarations()){//在a.java文件中寻找对应的函数调用（goodB2G类似的函数{
                                    if(pmethodDeclaration.getName().toString().equals(calledName)){//找到文件
                                      falseg=getSubCalledfun(visitedFile,fileArrayList,fileAST2GraphHashMap,Afile,pmethodDeclaration,desF,falseg);
                                    }
                                }

                            }
                        }

                    }



                }
            }
        }
    }
    public static int  getSubCalledfun(ArrayList<File>visitedFile,ArrayList<File> fileArrayList,
                                       HashMap<File, AST2Graph> fileAST2GraphHashMap,
                                       File Afile,MethodDeclaration pmethodDeclaration,String desF,int falseg){
        visitedFile.add(Afile);
        falseg=getVulDeeResult(Afile.getPath(),Afile,desF,pmethodDeclaration.getNameAsString(),falseg);//先构建本函数
        //=======================下面再寻找本函数中的调用的函数============================//
        Optional<BlockStmt> blockStmt=pmethodDeclaration.getBody();
        List<SimpleName> simpleNames=blockStmt.get().findAll(SimpleName.class);
        for(SimpleName sm:simpleNames){
            if(sm.getIdentifier().toString().endsWith("base")){continue;}//过滤掉base函数
            for(File otherfile:fileAST2GraphHashMap.keySet()){//其他文件进行查找
                if(sm.getIdentifier().contains("Sink")&&!Afile.getName().equals(otherfile.getName())&&!visitedFile.contains(otherfile)){
                    //otherfile为找到其他的文件，这个其他文件中包含对本函数的调用，递归过程
                    //更新所在文件、当前函数声明
                    for(MethodDeclaration me:fileAST2GraphHashMap.get(otherfile).getMethodDeclarations()){
                        if(me.getNameAsString().equals(sm.getIdentifier())){
                            pmethodDeclaration=me;
                            Afile=otherfile;
                            falseg=getSubCalledfun(visitedFile,fileArrayList,fileAST2GraphHashMap,Afile,pmethodDeclaration,desF,falseg);
                        }
                    }


                }
            }

        }
        return falseg;




    }
    public static StringBuilder getStringFromMethod(List<String> comments, CompilationUnit cu, StringBuilder sb, String methodName) {
        List<MethodDeclaration> methodPosts = cu.findAll(MethodDeclaration.class).stream()
                .filter(methodDeclaration -> methodDeclaration.getNameAsString().equals(methodName))
                .collect(Collectors.toList());
        if (!methodPosts.isEmpty()) {
            JavaToken javaToken;
            MethodDeclaration postDecl = methodPosts.get(0);
            //获取当前函数声明的所有的节点
            int index=0;
            List<JavaToken> tokens = tokensOfNode(postDecl);
            for(JavaToken javaToken1:tokens){
                if(javaToken1.getText().equals("{")){break;}
                index++;
            }
            for(int i=index;i<tokens.size()-1;i++){
                javaToken=tokens.get(i);
                if(javaToken.getText().length() != 0 && !comments.contains(javaToken.getText())){
                    sb.append(javaToken.getText().trim()).append(" ");
                }


            }
//            tokens.forEach(javaToken -> {
//                if (javaToken.getText().length() != 0 && !comments.contains(javaToken.getText())) {//去掉当前函数申明中的注释
//                    sb.append(javaToken.getText().trim()).append(" ");
//                }
//            });
        }

        return sb;
    }

    public static String replaceBlank(String str){
        String dest = null;
        if(str == null){
            return dest;
        }else{
            Pattern p = Pattern.compile("\\s+|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll(" ");
            return dest;
        }
    }

    public static void processStringCharLiteral(Node node) {
        for (StringLiteralExpr s : node.findAll(StringLiteralExpr.class)) {
            if (s.asString().isEmpty()) {
                s.setString("Empty String");
                s.setValue("Empty String");
            } else {
                s.setString("Not Empty String");
                s.setValue("Not Empty String");
            }
        }
        for (CharLiteralExpr c : node.findAll(CharLiteralExpr.class)) {
            c.setChar('c');
            c.setValue("C");
        }
    }

    public static ArrayList<JavaToken> tokensOfNode(Node node){
        ArrayList<JavaToken> tokens = new ArrayList<>();
        if (!node.getTokenRange().isPresent()) {
            return tokens;
        }
        JavaToken beginToken = node.getTokenRange().get().getBegin();
        JavaToken endToken = node.getTokenRange().get().getEnd();
        while(true){
            tokens.add(beginToken);
            //改正这处问题
            if (beginToken.getNextToken().isPresent()&&beginToken.getNextToken().get()!=endToken) {
                beginToken = beginToken.getNextToken().get();
            } else {
                break;
            }
        }
//        tokens.add(endToken);
        return tokens;
    }


    public static void main(String[] args) {

        String sourcePath="../CodeGraph/GNN-Data";
        String savePath="../CodeGraph/GNN-Data";
        getVulDeeResultMutifile(sourcePath,savePath);
      // getVulDeeResult("data/BenchmarkTest00159.java", "data/BenchmarkTest00159.java.txt");
        //ExtractSubGraphs.ExtractSQLI("data/CWE89_SQL_Injection__Property_executeQuery_07.java", "data/CWE89_SQL_Injection__Property_executeQuery_07.java.txt");
    }
}
