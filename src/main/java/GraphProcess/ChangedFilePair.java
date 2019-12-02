package GraphProcess;

import java.util.ArrayList;
import java.util.List;

public class ChangedFilePair {
    private String mOldFileName;
    private String mNewFileName;
    private int mOldStartLine;
    private int mNewStartLine;
    private int mOldCurrentLine;
    private int mNewCurrentLine;
    private List<Integer> mOldChangedLines = new ArrayList<>();
    private List<Integer> mNewChangedLines = new ArrayList<>();
    private List<ChangedFunction> mOldChangedFunctions = new ArrayList<>();
    private List<ChangedFunction> mNewChangedFunctions = new ArrayList<>();

    public ChangedFilePair() {
    }

    public void addOldLine() {
        mOldChangedLines.add(mOldCurrentLine);
    }

    public void addNewLine() {
        mNewChangedLines.add(mNewCurrentLine);
    }

    public void setOldFileName(String oldFileName) {
        mOldFileName = oldFileName;
    }

    public void setNewFileName(String newFileName) {
        mNewFileName = newFileName;
    }

    public int getOldStartLine() {
        return mOldStartLine;
    }

    public void setOldStartLine(int oldStartLine) {
        mOldStartLine = oldStartLine;
        mOldCurrentLine = mOldStartLine - 1;
    }

    public int getNewStartLine() {
        return mNewStartLine;
    }

    public void setNewStartLine(int newStartLine) {
        mNewStartLine = newStartLine;
        mNewCurrentLine = mNewStartLine - 1;
    }

    public int getOldCurrentLine() {
        return mOldCurrentLine;
    }

    public void increaseOldCurrentLine() {
        mOldCurrentLine = mOldCurrentLine + 1;
    }

    public int getNewCurrentLine() {
        return mNewCurrentLine;
    }

    public void increaseNewCurrentLine() {
        mNewCurrentLine = mNewCurrentLine + 1;
    }

    public void setOldChangedLines(List<Integer> oldChangedLines) {
        mOldChangedLines = oldChangedLines;
    }

    public void setNewChangedLines(List<Integer> newChangedLines) {
        mNewChangedLines = newChangedLines;
    }

    public String getOldFileName() {
        return mOldFileName;
    }

    public String getNewFileName() {
        return mNewFileName;
    }

    public List<Integer> getOldChangedLines() {
        return mOldChangedLines;
    }

    public List<Integer> getNewChangedLines() {
        return mNewChangedLines;
    }

    public List<ChangedFunction> getOldChangedFunctions() {
        return mOldChangedFunctions;
    }

    public void setOldChangedFunctions(List<ChangedFunction> oldChangedFunctions) {
        mOldChangedFunctions = oldChangedFunctions;
    }

    public List<ChangedFunction> getNewChangedFunctions() {
        return mNewChangedFunctions;
    }

    public void setNewChangedFunctions(List<ChangedFunction> newChangedFunctions) {
        mNewChangedFunctions = newChangedFunctions;
    }
}
