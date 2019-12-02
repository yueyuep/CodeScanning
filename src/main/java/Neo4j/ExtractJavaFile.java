package Neo4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取当前目录下的所有java文件
 * @author Kangaroo
 */
class ExtractJavaFile {
    private List<File> fileList = new ArrayList<>();

    public ExtractJavaFile(){
    }

    public void getFileList(File dir){
        if (!dir.exists()||!dir.isDirectory()){
            //不是目录或者目录不存在
            return;
        } else {
            File[] files=dir.listFiles();
            for(File file:files){
                if(file.isFile() && file.getName().endsWith(".java")){
                    fileList.add(file);
                } else if (file.isFile() && !file.getName().endsWith(".java")) {
                    file.delete();
                }
                else {
                    getFileList(file);
                }
            }
        }
    }

    public File[] getFile() {
        int size = fileList.size();
        //返回文件数组
        return fileList.toArray(new File[size]);
    }

}
