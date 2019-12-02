package Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplitString {
    private SplitString() {
    }

    public static String splitUntilUpperCase(String s) {
        String[] noWhite = s.split("\\s+");
        List<String> splits = new ArrayList<>();
        Arrays.stream(noWhite).forEach(ns ->
                splits.addAll(Arrays.asList(ns.split("(?<!^)(?=[A-Z])"))));
        return String.join(" ", splits);
    }

    public static void main(String[] args) {
        String s = "MethodCallExpr  !=  FieldAccessExpr";
        System.out.println(splitUntilUpperCase(s));
    }
}
