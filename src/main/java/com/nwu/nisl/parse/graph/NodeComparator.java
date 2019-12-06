package com.nwu.nisl.parse.graph;

import com.github.javaparser.ast.Node;

import java.util.Comparator;

class NodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node faultResult1, Node faultResult2) {
        int cr = 0;
        //先按line排升序
        int a = faultResult2.getBegin().get().line - faultResult1.getBegin().get().line;
        if (a != 0) {
            cr = (a < 0) ? 3 : -1;     // "<"升序     ">"降序
        } else {
            //再按column排升序
            a =  faultResult2.getBegin().get().column -  faultResult1.getBegin().get().column;
            if (a != 0) {
                cr = (a < 0) ? 2 : -2; // "<"升序     ">"降序
            }
        }
        return cr;
    }
}