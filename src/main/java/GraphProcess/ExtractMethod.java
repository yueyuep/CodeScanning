package GraphProcess;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtractMethod {

    public String getParseResult(String srcFilePath) throws FileNotFoundException {
        ParseMethodUtil parseMethodUtil = new ParseMethodUtil(srcFilePath);
        List<MethodDeclaration> methodDeclarations = parseMethodUtil.getMethodDeclarations();
        if (methodDeclarations.size() <= 0) {
            System.out.println("There is no method declaration.");
            return null;
        }
        MethodDeclaration methodDeclarationTest = methodDeclarations.get(0);
        methodDeclarationTest.removeComment();
        parseMethodUtil.travelNode(methodDeclarationTest);
        String parseResult = parseMethodUtil.getParseResult();
        return parseResult;
    }

    public String getParseFunctionResult(String srcFilePath, String functionName, String functionLineNumber) {
        ParseMethodUtil parseMethodUtil = null;
        try {
            parseMethodUtil = new ParseMethodUtil(srcFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(srcFilePath);
            return "";
        }
        List<MethodDeclaration> methodDeclarations = parseMethodUtil.getMethodDeclarations();
        if (methodDeclarations.size() <= 0) {
            System.out.println("There is no method declaration.");
            return null;
        }
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            if (Objects.equals(methodDeclaration.getNameAsString(), functionName)
                    && methodDeclaration.getBegin().isPresent()
                    && methodDeclaration.getBegin().get().line == Integer.valueOf(functionLineNumber)) {
//                System.out.println(parseMethodUtil.travelNodeGetParseResult(methodDeclaration));

                return parseMethodUtil.travelNodeGetParseResult(methodDeclaration);
            }
        }
        return "";
    }

    public List<ChangedFunction> findMethodDeclaration(String srcFilePath, List<Integer> lines) {
        List<ChangedFunction> changedFunctions = new ArrayList<>();
        ParseMethodUtil parseMethodUtil = null;
        try {
            parseMethodUtil = new ParseMethodUtil(srcFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(srcFilePath);
            return changedFunctions;
        }
        List<MethodDeclaration> methodDeclarations = parseMethodUtil.getMethodDeclarations();
        if (methodDeclarations.size() <= 0) {
            System.out.println("There is no method declaration.");
            return new ArrayList<ChangedFunction>();
        }
        ArrayList<MethodDeclaration> changedMethods = new ArrayList<>();
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            methodDeclaration.getRange().ifPresent(position -> {
                int begin = position.begin.line;
                int end = position.end.line;
                for (Integer line : lines) {
                    if (isMiddle(begin, line, end)) {
                        changedMethods.add(methodDeclaration);
                        break;
                    }
                }
            });
        }
        for (MethodDeclaration changedMethod : changedMethods) {
            changedFunctions.add(ChangedFunction.newInstance(
                    changedMethod.getNameAsString(), String.valueOf(changedMethod.getBegin().get().line)));
        }
        return changedFunctions;
    }

    private boolean isMiddle(int begin, int middle, int end) {
        return middle >= begin && middle <= end;
    }

    public String getParseResult(ChangedFilePair changedFilePair) {
        return null;
    }

    public ArrayList<ChangedFilePair> getChangedFilePairsFromDiff(String patchPath) throws IOException {
        ArrayList<ChangedFilePair> changedFilePairs = ParsePatch.getChangedFilePairsFromDiff(patchPath);
        return changedFilePairs;
    }

    public static void main(String[] args) {
        String bugPath = "C:\\Users\\star\\Documents\\bugsDotJar\\bug\\";
        File bugDir = new File(bugPath);
        File[] bugProjects = bugDir.listFiles();

        ExtractMethod extractMethod = new ExtractMethod();
        if (bugProjects != null) {
            for (File bugProject : bugProjects) {
                System.out.println(bugProject);
                if (bugProject.isDirectory()) {
                    String projectDir = bugProject.getPath();
                    String[] pathSplit = projectDir.split("\\\\");
                    String parseResultFile = "data/"
                            + pathSplit[pathSplit.length - 1]
                            + "_parseMethodResult.json";
                    extractMethod.saveParseResult(projectDir, parseResultFile);
                    try {
                        ExtractParse.newInstance(parseResultFile).saveCorpus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void saveParseResult(String projectDir, String parseResultFile) {
        ExtractMethod extractMethod = new ExtractMethod();
        File project = new File(projectDir);
//        File project = new File("./files/maven-old");
        File[] listFiles = project.listFiles();
        List<String> versionsDir = new ArrayList<String>();
        if (listFiles != null) {
            for (File listFile : listFiles) {
                if (listFile.isDirectory()) {
                    versionsDir.add(listFile.getPath());
                }
            }
        }
        ArrayList<ChangedFilePair> allChangedFilePairs = new ArrayList<>();
        // 修改文件名
        for (String versionDir : versionsDir) {
            ArrayList<ChangedFilePair> changedFilePairs = null;
            try {
                changedFilePairs = extractMethod.getChangedFilePairsFromDiff(
                        new File(versionDir, ".bugs-dot-jar/developer-patch.diff").toString());
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            for (ChangedFilePair filePair: changedFilePairs) {
                filePair.setOldFileName(new File(versionDir, filePair.getOldFileName().substring(2)).getPath()
                        + ".old");
                filePair.setNewFileName(new File(versionDir, filePair.getNewFileName().substring(2)).getPath());
//                        .replaceFirst("bug", "clean"));
            }
            allChangedFilePairs.addAll(changedFilePairs);
        }
        // 添加修改的方法声明
        for (ChangedFilePair filePair: allChangedFilePairs) {
            filePair.setOldChangedFunctions(extractMethod.findMethodDeclaration(
                    filePair.getOldFileName(), filePair.getOldChangedLines()));
            filePair.setNewChangedFunctions(extractMethod.findMethodDeclaration(
                    filePair.getNewFileName(), filePair.getNewChangedLines()));
            for (ChangedFunction changedFunction : filePair.getOldChangedFunctions()) {
                changedFunction.setFunctionParse(
                        extractMethod.getParseFunctionResult(
                                filePair.getOldFileName(),
                                changedFunction.getFunctionName(),
                                changedFunction.getFunctionLineNumber()));
            }
            for (ChangedFunction changedFunction : filePair.getNewChangedFunctions()) {
                changedFunction.setFunctionParse(
                        extractMethod.getParseFunctionResult(
                                filePair.getNewFileName(),
                                changedFunction.getFunctionName(),
                                changedFunction.getFunctionLineNumber()));
            }
        }
        // 打印输出结果
        // 保存parse result到文件
//        for (GraphProcess.ChangedFilePair filePair: allChangedFilePairs) {
//            System.out.println(filePair.getOldFileName());
//            System.out.println(filePair.getNewFileName());
//            System.out.println(filePair.getOldChangedLines());
//            System.out.println(filePair.getNewChangedLines());
//            for (GraphProcess.ChangedFunction changedFunction : filePair.getOldChangedFunctions()) {
//                System.out.println(changedFunction.getFunctionName());
//                System.out.println(changedFunction.getFunctionLineNumber());
//                System.out.println(changedFunction.getFunctionParse());
//
//            }
//            for (GraphProcess.ChangedFunction changedFunction : filePair.getNewChangedFunctions()) {
//                System.out.println(changedFunction.getFunctionName());
//                System.out.println(changedFunction.getFunctionLineNumber());
//                System.out.println(changedFunction.getFunctionParse());
//
//            }
//        }

        Gson gson = new Gson();
        String allChangedFilesJson = gson.toJson(allChangedFilePairs);
//        System.out.println(allChangedFilesJson);
//        String toJsonFilePath = "data/parseMethodResult.json";
        File toJsonFile = new File(parseResultFile);
        FileWriter toFileWriter = null;
        try {
            toFileWriter = new FileWriter(toJsonFile);
            toFileWriter.write(allChangedFilesJson);
            toFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
