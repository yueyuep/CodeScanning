package GraphProcess;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExtractParse {

//    private String mParseResultFile;
//    private String mToOldJsonFile;
//    private String mToOldCorpusFile;
//    private String mToNewJsonFile;
//    private String mToNewCorpusFile;
    private String mParseResultFile;        //"data/parseMethodResult.json";
    private String mToOldJsonFile;          //"data/parseMethodResultOld.txt";
    private String mToOldCorpusFile;        //"data/parseMethodResultOldCorpus.txt";
    private String mToNewJsonFile;          //"data/parseMethodResultNew.txt";
    private String mToNewCorpusFile;        //"data/parseMethodResultNewCorpus.txt";
//    private String mParseResultFile = "data/parseMethodResult.json";
//    private String mToOldJsonFile = "data/parseMethodResult.json.Old.txt";
//    private String mToOldCorpusFile = "data/parseMethodResult.json.OldCorpus.txt";
//    private String mToNewJsonFile = "data/parseMethodResult.json.New.txt";
//    private String mToNewCorpusFile = "data/parseMethodResult.json.NewCorpus.txt";

    private static String sSymbolRegex = "([\\p{Pd}-\\p{Po}]|\\p{S})";

    private ExtractParse() {
    }

    public static ExtractParse newInstance(String parseResultFile) {
        ExtractParse extractParse = new ExtractParse();

        extractParse.setParseResultFile(parseResultFile);
        extractParse.setToOldJsonFile(parseResultFile + ".Old.txt");
        extractParse.setToNewJsonFile(parseResultFile + ".New.txt");
        extractParse.setToOldCorpusFile(parseResultFile + ".OldCorpus.txt");
        extractParse.setToNewCorpusFile(parseResultFile + ".NewCorpus.txt");

        return extractParse;
    }

    public void saveCorpus() throws IOException {
        splitOldNew(mParseResultFile);
        saveCorpusFromTo(mToOldJsonFile, mToOldCorpusFile);
        saveCorpusFromTo(mToNewJsonFile, mToNewCorpusFile);
    }

    public static void saveCorpusFromTo(String origin, String another) throws IOException {
        String json = Util.readFileToString(origin);
        json = json.replaceAll(sSymbolRegex, " $1 ");

        FileWriter toOldFileWriter = new FileWriter(new File(another));
        toOldFileWriter.write(json);
        toOldFileWriter.close();
    }

    public void splitOldNew(String jsonPath) throws IOException {
        Gson gson = new Gson();
        String json = Util.readFileToString(jsonPath);
        ArrayList<ChangedFilePair> allChangedFilePairs = gson.fromJson(json, new TypeToken<ArrayList<ChangedFilePair>>(){}.getType());
        StringBuilder oldResult = new StringBuilder();
        StringBuilder newResult = new StringBuilder();
        for (ChangedFilePair filePair: allChangedFilePairs) {
            for (ChangedFunction changedFunction : filePair.getOldChangedFunctions()) {
                oldResult.append(changedFunction.getFunctionParse()).append("\n");
            }
            for (ChangedFunction changedFunction : filePair.getNewChangedFunctions()) {
                newResult.append(changedFunction.getFunctionParse()).append("\n");
            }
        }

        FileWriter toOldFileWriter = new FileWriter(new File(mToOldJsonFile));
        toOldFileWriter.write(oldResult.toString().trim());
        toOldFileWriter.close();

        FileWriter toNewFileWriter = new FileWriter(new File(mToNewJsonFile));
        toNewFileWriter.write(newResult.toString().trim());
        toNewFileWriter.close();

    }

    public String getToOldJsonFile() {
        return mToOldJsonFile;
    }

    private void setToOldJsonFile(String toOldJsonFile) {
        mToOldJsonFile = toOldJsonFile;
    }

    public String getToNewJsonFile() {
        return mToNewJsonFile;
    }

    private void setToNewJsonFile(String toNewJsonFile) {
        mToNewJsonFile = toNewJsonFile;
    }

    public String getToOldCorpusFile() {
        return mToOldCorpusFile;
    }

    private void setToOldCorpusFile(String toOldCorpusFile) {
        mToOldCorpusFile = toOldCorpusFile;
    }

    public String getToNewCorpusFile() {
        return mToNewCorpusFile;
    }

    private void setToNewCorpusFile(String toNewCorpusFile) {
        mToNewCorpusFile = toNewCorpusFile;
    }

    public String getParseResultFile() {
        return mParseResultFile;
    }

    private void setParseResultFile(String parseResultFile) {
        mParseResultFile = parseResultFile;
    }
}
