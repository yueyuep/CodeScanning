package com.nwu.nisl.parse.graph;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RangeNode {
    private Node mNode;
    private Optional<Range> mOptionalRange;
    private static Map<String, RangeNode> nodes = new HashMap<>();
    private static Map<Range, RangeNode> rangeNodes = new HashMap<>();

    private RangeNode(Node node) {
        mNode = node;
        mOptionalRange = node.getRange();
    }

    public Node getmNode() {
        return mNode;
    }

    public static void nodeCacheClear() {
        nodes.clear();
    }

    public static RangeNode newInstance(Node node) {
        String key = String.valueOf(node.hashCode());
        if (node.getRange().isPresent()) {
            if (rangeNodes.keySet().contains(node.getRange().get())) {
                return rangeNodes.get(node.getRange().get());
            } else {
                RangeNode rangeNode = new RangeNode(node);
                rangeNodes.put(node.getRange().get(), rangeNode);
                return rangeNode;
            }
        } else if (nodes.keySet().contains(key)) {
            return nodes.get(key);
        } else {
            RangeNode rangeNode = new RangeNode(node);
            nodes.put(key, rangeNode);
            return rangeNode;
        }
    }

    public Node getNode() {
        return mNode;
    }

//    public Range getOptionalRange() {
    public Optional<Range> getOptionalRange() {
        return mOptionalRange;
    }



    @Override
    public String toString() {
        return mNode.toString() + ", " + mOptionalRange.toString();
    }
}
