package Neo4j;

import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

public class ThreeTuple {
    @Setter
    @Getter
    private File file;
    @Setter
    @Getter
    private String className;
    @Setter
    @Getter
    private MethodDeclaration methodDeclaration;
    public ThreeTuple(){
        //无参构造
    }
    public ThreeTuple(File file,String className,MethodDeclaration methodDeclaration){
        this.file=file;
        this.className=className;
        this.methodDeclaration=methodDeclaration;
    }


}
