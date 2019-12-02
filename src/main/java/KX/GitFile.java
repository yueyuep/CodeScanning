package KX;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GitFile {

    public String username;
    public String hashjava;
    public ArrayList<Integer> addlist=new ArrayList<Integer>();
    public ArrayList<Integer> dellist=new ArrayList<Integer>();
//    public String storedir="C:\\Users\\Troye Sivan\\Desktop\\javaf_byuser\\javaf_byuser\\java_raw_files\\new_javafile\\";
    public String storedir="../npe/new_files/";
//    public String storedir="../java_raw_files/new_javafile/";
//    public String storedir="java_raw_files\\new_javafile\\";
    public String storeSliceDir="slices\\raw\\";
    public String path_newfile;
    public String path_oldfile;
    public ArrayList<MethodDeclaration> old_funcs=new ArrayList<>();
    public ArrayList<Node> old_declarition=new ArrayList<>();
    public ArrayList<MethodDeclaration> new_funcs=new ArrayList<>();
    public ArrayList<Node> new_declarition=new ArrayList<>();
    public List<IfStmt> old_ifStmts=new ArrayList<>();
    public  List<AssignExpr> old_assignExprs=new ArrayList<>();
    public List<IfStmt> new_ifStmts=new ArrayList<>();
    public  List<AssignExpr> new_assignExprs=new ArrayList<>();

    public List<List<String>> slices_new=new ArrayList<>();
    public List<List<String>> slices_rawnew=new ArrayList<>();
    public List<List<String>> slices_rawold=new ArrayList<>();
    public List<List<String>> slices_old=new ArrayList<>();

    public void setStoredir(String path){
        this.storedir=path;
    }

    public void setOld_funcs(ArrayList<MethodDeclaration> old_funcs) {
        this.old_funcs = old_funcs;
    }

    public void setNew_funcs(ArrayList<MethodDeclaration> new_funcs) {
        this.new_funcs = new_funcs;
    }

    public void setOld_declarition(ArrayList<Node> old_declarition) {
        this.old_declarition = old_declarition;
    }

    public void setNew_declarition(ArrayList<Node> new_declarition) {
        this.new_declarition = new_declarition;
    }

    public GitFile(String username, String hashjava, ArrayList<Integer> addlist, ArrayList<Integer> dellist) {
        this.username=username;
        this.hashjava=hashjava;
        this.addlist=addlist;
        this.dellist=dellist;
        //    \\ vs /
        this.path_newfile=this.storedir+username+"/"+hashjava;
        this.path_oldfile=this.storedir.replace("new","old")+username+"/"+hashjava;
    }

    public void findIfandAssign() throws IOException {
        for (MethodDeclaration node:this.new_funcs){
            if (node.findAll(IfStmt.class).stream().filter(c->this.addlist.contains(c.getBegin().get().line))!=null)
                node.findAll(IfStmt.class).stream().filter(c->this.addlist.contains(c.getBegin().get().line)).forEach(f->new_ifStmts.add(f));
            if (node.findAll(AssignExpr.class).stream().filter(c->c.findAll(ConditionalExpr.class).size()>0)!=null)
                node.findAll(AssignExpr.class).stream().filter(c->c.findAll(ConditionalExpr.class).size()>0&&this.addlist.contains(c.getBegin().get().line)).forEach(f->new_assignExprs.add(f));
        }
        for (MethodDeclaration node:this.old_funcs){
            if(this.dellist!=null) {
                if (node.findAll(IfStmt.class).stream().filter(c -> this.dellist.contains(c.getBegin().get().line)) != null)
                    node.findAll(IfStmt.class).stream().filter(c -> this.dellist.contains(c.getBegin().get().line)).forEach(f -> old_ifStmts.add(f));
                if (node.findAll(AssignExpr.class).stream().filter(c -> c.findAll(ConditionalExpr.class).size() > 0) != null)
                    node.findAll(AssignExpr.class).stream()
                            .filter(c -> c.findAll(ConditionalExpr.class).size() > 0 && this.dellist.contains(c.getBegin().get().line))
                            .forEach(f -> old_assignExprs.add(f));
            }
        }
        System.out.println("-------------------"+this.hashjava+"\n");
        findBugVar();
    }

    public void findBugVar() throws IOException {
        if(new_ifStmts.size()>0){
            for(MethodDeclaration node:this.new_funcs){
                List<IfStmt> ifStmt_tmp=new ArrayList<>();
                node.findAll(IfStmt.class).stream().filter(c->this.addlist.contains(c.getBegin().get().line)).forEach(f->ifStmt_tmp.add(f));
                for(IfStmt ifStmt:ifStmt_tmp){
                    MethodDeclaration ifParentMethod=new MethodDeclaration();
                    List<NameExpr> bugvar=new ArrayList<>();
                    if(ifStmt.getCondition().findAll(BinaryExpr.class).size()>0){
                        for (BinaryExpr a:ifStmt.getCondition().findAll(BinaryExpr.class)) {
//                            BinaryExpr.Operator operator=a.getOperator();
                            for(NullLiteralExpr null_:a.findAll(NullLiteralExpr.class)){
                                null_.getParentNode().get().findAll(NameExpr.class).stream().forEach(c->{
                                    if(!bugvar.contains(c)) {
                                        bugvar.add(c);
                                    }
                                });
                               ifParentMethod=ifStmt.findParent(MethodDeclaration.class).get();
                            }
                        }
                    }

                    GenSlice gen_new=new GenSlice(bugvar,this.hashjava,"new");

                    if(bugvar.size()>0) {
//                        bugvar.forEach(op -> System.out.println(op.getNameAsString()));
                       gen_new= gen_new.visit(ifStmt.findParent(MethodDeclaration.class).get());
                       if (gen_new.slice.size()>0&&!slices_new.contains(gen_new.slice)&&!slices_rawnew.contains(gen_new.slice_raw)) {
                           slices_new.add(gen_new.slice);
                           slices_rawnew.add(gen_new.slice_raw);
                       }
                    }
                    for(MethodDeclaration node_old:this.old_funcs){
                        if(node_old.getNameAsString().equals(ifParentMethod.getNameAsString())
                                && node_old.getParameters().equals(ifParentMethod.getParameters())) {
                            int line_old=node_old.getBegin().get().line;
                            int line_new=ifParentMethod.getBegin().get().line;
                            int cha=line_new-line_old;
                            if(cha<=10&&cha>=-10)  {
                                if (gen_new.bugvar_tot.size() > 0) {
    //                              bugvar.forEach(op -> System.out.println(op.getNameAsString()));
                                    GenSlice gen_old = new GenSlice(gen_new.bugvar_tot, this.hashjava, "old");
                                    gen_old = gen_old.visit(node_old);
                                    if (gen_old.slice.size() > 0 && !slices_old.contains(gen_old.slice)&&!slices_rawold.contains(gen_old.slice_raw)) {
                                        slices_old.add(gen_old.slice);
                                        slices_rawold.add(gen_old.slice_raw);
                                    }
                                }
                            }
                            else {
                                System.out.println(hashjava+" cha "+cha +"old "+node_old.getNameAsString()+"new "+ ifParentMethod.getNameAsString());
                           }
                        }
                    }
//                    ifStmt.getCondition().stream().forEach(c-> System.out.println(c.getClass());))
                }
            }
        }
        String slicepath_new=storeSliceDir.replace("raw","mature")+username+"_new";
        String slicepath_new_view=storeSliceDir+username+"_new";
        String slicepath_old=storeSliceDir.replace("raw","mature")+username+"_old";
        String slicepath_old_view = storeSliceDir + username+"_old";
        if(slices_new.size()>0&&slices_old.size()>0) {
            this.writeToFile(slicepath_new,this.slices_new);
            this.writeToFileView(slicepath_new_view,this.slices_rawnew,"new");
            this.writeToFile(slicepath_old,this.slices_old);
            this.writeToFileView(slicepath_old_view,this.slices_rawold,"old");
        }
    }

    public void writeToFile(String userroot,List<List<String>> slices) throws IOException {
        String path=userroot;
        File file=new File(path);
        if(! file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        FileWriter fw= new FileWriter(file,true);
//            fw.write("------------------"+hashjava+" "+newOrold+"\n");
        for(List<String> slice:slices) {
            for (String line : slice) {
                if (line != null && line.trim() != null && line.trim() != " ") {
                    line = fenciline(line);
                    line=line.trim();
                    line= line.replace("\r","");
                    line=line.replace("\n","");
                    fw.write(line);
                    fw.write(" NEXTLINE ");
                }
            }
            fw.write("\n");
        }
        fw.close();
    }

    public void writeToFileView(String userroot,List<List<String>> slices,String newOrold) throws IOException {
        String path=userroot;
        File file=new File(path);
        if(! file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        FileWriter fw= new FileWriter(file,true);
        fw.write("------------------"+hashjava+" "+newOrold+"\n");
        for(List<String>slice :slices) {
            for (String line : slice) {
                line = fenciline(line);
                fw.write(line);
                fw.write("\n");
            }
            fw.write("\n");
        }
        fw.close();
    }

    public static String fenciline(String s) {
        String final_s = "";
        for (char item : s.toCharArray()) {
            Boolean flag = false;
            if (item != 32 && (item < 65 || item > 90 && item < 97 || item > 122))
                flag = true;
            if (flag)
                final_s = final_s + " " + item + " ";
            else
                final_s = final_s + item;
        }
        final_s= final_s.replace("  "," ");
//            System.out.println(final_s);
        return final_s;
    }

    private static class GenSlice {
        List<NameExpr> bugvar=new ArrayList<>();
        List<NameExpr> bugvar_tot=new ArrayList<>();
        List<Node> nodeList=new ArrayList<>();
        List<String> slice_raw=new ArrayList<>();
        List<String> slice=new ArrayList<>();
        String hashjava="";
        String newOrold="";

        Node node_;
        public GenSlice(List<NameExpr> bugvar,String hashjava,String newOrold){
            bugvar.forEach(c->{
                this.bugvar_tot.add(c);
                this.bugvar.add(c);
            });
            this.hashjava=hashjava;
            this.newOrold=newOrold;
        }

        public List<Node> changename(List<Node> nodeList){
            for(Node n:nodeList){
                n.removeComment();
                n.stream().forEach(c->{
                    if (c instanceof LiteralStringValueExpr&&(!((LiteralStringValueExpr) c).getValue().toLowerCase().equals("null"))){
                        ((LiteralStringValueExpr) c).setValue("Literal");
                    }
                });
            }
            return nodeList;
        }

        public void getnode(Node node){
            if (node instanceof Expression||node instanceof VariableDeclarator||node instanceof IfStmt||node instanceof ReturnStmt){
                if(node instanceof  IfStmt) {
                    node_=((IfStmt) node).getCondition();
                } else {
                    node_=node;
                }
//                System.out.println(node.toString()+" "+node.getBegin().get().line);
                getnode(node.getParentNode().get());
            }
        }

        public String fencimethod(String s){
            String final_s="";
            for(char item:s.toCharArray() ){
                if(item>64 && item<91){
                    final_s=final_s+" ";
                }
                final_s=final_s+item;
            }
//            final_s=final_s.trim();
            return final_s;
        }

        public GenSlice visit(MethodDeclaration n) {
//            System.out.println("***********************"+this.newOrold);
            n.stream().forEach(c->{
                for(NameExpr name:bugvar) {
                    if ((c instanceof NameExpr || c instanceof SimpleName)&& c.toString().equals(name.toString())) {
                        if(c.findParent(AssignExpr.class).isPresent()){
                            c.findParent(AssignExpr.class).get().findAll(NameExpr.class).forEach(m->
                                    {
                                        if(!bugvar_tot.contains(m)){
                                            bugvar_tot.add(m);
//                                            System.out.println("add a bugvar: "+m.getNameAsString());
                                        }
                                    }
                            );
                        }

                        if(c.findParent(VariableDeclarationExpr.class).isPresent()){
                            c.findParent(VariableDeclarationExpr.class).get().findAll(NameExpr.class).forEach(m-> {
                                        if(!bugvar_tot.contains(m)){
                                            bugvar_tot.add(m);
//                                            System.out.println("add a bugvar: "+m.getNameAsString());
                                        }
                                    }
                            );
                        }
                    }
                }
            });
            n.stream().forEach(c->{
                for(NameExpr name:bugvar_tot) {
                    if ((c instanceof NameExpr || c instanceof SimpleName)&& c.toString().equals(name.toString())) {
                        getnode(c.getParentNode().get());
                        boolean flag=false;
                        if(!nodeList.contains(node_)&&node_!=null) {
                            for(Node node_t:nodeList){
                                if(node_t.containsWithin(node_))
                                    flag=true;
                            }
                            if (flag==false)
                                     this.nodeList.add(node_);
                        }
                    }
                }
            });

            nodeList=changename(nodeList);
            for (Node c : nodeList) {
                String c_string = c.toString();
                slice_raw.add(c_string);
            }

            if(nodeList!=null&&nodeList.size()>0) {
//                System.out.println(nodeList.size());
                for (Node c : nodeList) {
//                    System.out.println(c.getBegin().get().line);
                    String c_string = c.toString();
                    System.out.println(c_string);
                    HashMap<String,String> var2classvar=new HashMap<>();
                    c.stream().forEach(m -> {
                                if (m.getChildNodes().isEmpty() && !(m instanceof NameExpr)){
                                    String mclass="";
                                    String mstring="";
                                   if(m.getParentNode().isPresent()&&!(m.getParentNode().get() instanceof  NameExpr)) {
                                       String[] tmp = m.getParentNode().get().getClass().getName().split("\\.");
                                       mclass=tmp[tmp.length-1];

                                       if( m.getParentNode().get() instanceof  MethodCallExpr
                                               ||m instanceof SimpleName ||m instanceof NameExpr) {
                                           mstring=fencimethod(m.toString());
                                       } else {
                                           mstring=m.toString();
                                       }
                                       var2classvar.put(m.toString(),mclass + " " + mstring);
                                   }
                                }
                            }
                    );
                   /* for (String var:
                        var2classvar.keySet() ) {
                        c_string=c_string.replace(var,var2classvar.get(var));

                    }*/
                    slice.add(c_string.trim());
//                    System.out.println(c_string);
                }
            }
        return this;
        }
    }
}


