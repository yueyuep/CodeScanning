package com.nwu.nisl.parse.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsePatch {

    private String mContent;
    private Matcher mMatcher;
    public static ParsePatch sParsePatch;
    private static final Pattern mDiffFilePattern = Pattern.compile("^diff --git (a.+java) (b.+java)");
    private static final Pattern mLineNumberPattern = Pattern.compile("^@@ -(\\d+).+ \\+(\\d+).+@@");
    private static final Pattern mDeletePattern = Pattern.compile("^-(?!-- a/.+java)");
    private static final Pattern mAddPattern = Pattern.compile("^\\+(?!\\+\\+ b/.+java)");
    private static final Pattern mDeleteFileInfoPattern = Pattern.compile("^--- a/.+java");
    private static final Pattern mAddFileInfoPattern = Pattern.compile("^\\+\\+\\+ b/.+java");

    private ParsePatch() {
    }

    public static ParsePatch getInstance() {
        sParsePatch = new ParsePatch();
        return sParsePatch;
    }

    public boolean isFileInfo() {
        return mDeleteFileInfoPattern.matcher(mContent).find() ||
                mAddFileInfoPattern.matcher(mContent).find();
    }

    public boolean isDiffFile() {
        mMatcher = mDiffFilePattern.matcher(mContent);
        return mMatcher.find();
    }

    public boolean isLineNumberShown() {
        mMatcher = mLineNumberPattern.matcher(mContent);
        return mMatcher.find();
    }

    public boolean isDeleteLine() {
        mMatcher = mDeletePattern.matcher(mContent);
        return mMatcher.find();
    }

    public boolean isAddLine() {
        mMatcher = mAddPattern.matcher(mContent);
        return mMatcher.find();
    }

    public Matcher getMatcher() {
        return mMatcher;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public static ArrayList<ChangedFilePair> getChangedFilePairsFromDiff(String patchPath) throws IOException {
        String patchContent = Util.readFileToString(patchPath);
        ArrayList<ChangedFilePair> changedFilePairs = new ArrayList<>();
        String[] lines = patchContent.split("\n");
        ParsePatch parsePatch = ParsePatch.getInstance();
        for (String line : lines) {
//            System.out.println(line);
            parsePatch.setContent(line);
            if (parsePatch.isDiffFile()) {
                changedFilePairs.add(new ChangedFilePair());
                changedFilePairs.get(changedFilePairs.size() - 1).setOldFileName(parsePatch.getMatcher().group(1));
                changedFilePairs.get(changedFilePairs.size() - 1).setNewFileName(parsePatch.getMatcher().group(2));
            } else if (parsePatch.isLineNumberShown()) {
                changedFilePairs.get(changedFilePairs.size() - 1).setOldStartLine(Integer.valueOf(parsePatch.getMatcher().group(1)));
                changedFilePairs.get(changedFilePairs.size() - 1).setNewStartLine(Integer.valueOf(parsePatch.getMatcher().group(2)));
            } else if (parsePatch.isDeleteLine()) {
                changedFilePairs.get(changedFilePairs.size() - 1).increaseOldCurrentLine();
                changedFilePairs.get(changedFilePairs.size() - 1).addOldLine();
            } else if (parsePatch.isAddLine()) {
                changedFilePairs.get(changedFilePairs.size() - 1).increaseNewCurrentLine();
                changedFilePairs.get(changedFilePairs.size() - 1).addNewLine();
            } else if (!parsePatch.isFileInfo()) {
                changedFilePairs.get(changedFilePairs.size() - 1).increaseOldCurrentLine();
                changedFilePairs.get(changedFilePairs.size() - 1).increaseNewCurrentLine();
            }
        }
        ArrayList<ChangedFilePair> changedFilePairWithNoOnlyAddOrDel = new ArrayList<>();
        for (ChangedFilePair changedFilePair : changedFilePairs) {
            if (!changedFilePair.getOldChangedLines().isEmpty() && !changedFilePair.getNewChangedLines().isEmpty()) {
                changedFilePairWithNoOnlyAddOrDel.add(changedFilePair);
            }
        }
        return changedFilePairWithNoOnlyAddOrDel;
    }
}
