package GraphProcess;

import KX.GitFile;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import owaspbench.RemoveNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateGraph {
    private Set<Object> mAllSubGraphNodes = new HashSet<>();
    private Set<Object> mSubGraphNodes = new HashSet<>();
    private List<MutableNetwork> mNewSubNetworks = new ArrayList<>();
    private List<MutableNetwork> mOldSubNetworks = new ArrayList<>();
    private int mJsonIndex = 0;

    public GenerateGraph() {
    }

    public List<MutableNetwork> getNewSubNetworks() {
        return mNewSubNetworks;
    }

    public List<MutableNetwork> getOldSubNetworks() {
        return mOldSubNetworks;
    }

    public void generateGraphs(GitFile gitFile) {
        // new-old两种文件，首先处理new文件，生成Graph，然后 比较 oldGraphs
        AST2Graph newAst2Graph = AST2Graph.newInstance(gitFile.path_newfile);
        if (newAst2Graph == null) {
            return;
        }
        logTem(gitFile.path_newfile);
        mNewSubNetworks.clear();
        generateSubGraphsInRelate(newAst2Graph, gitFile.new_funcs, gitFile.addlist, mNewSubNetworks);
//        generateSubGraphs(newAst2Graph, gitFile.new_funcs, gitFile.addlist, mNewSubNetworks);
        // 再处理old文件，生成Graph，然后 比较 newGraphs
        AST2Graph oldAst2Graph = AST2Graph.newInstance(gitFile.path_oldfile);
        if (oldAst2Graph == null) {
            return;
        }
        logTem(gitFile.path_oldfile);
        mOldSubNetworks.clear();
        generateSubGraphsInRelate(oldAst2Graph, gitFile.old_funcs, gitFile.dellist, mOldSubNetworks);
//        generateSubGraphs(oldAst2Graph, gitFile.old_funcs, gitFile.dellist, mOldSubNetworks);
//        // 比较 old-new graph
//        List<MutableNetwork> sameN = new ArrayList<>();
//        List<MutableNetwork> sameO = new ArrayList<>();
//        for (MutableNetwork newN : mNewSubNetworks) {
//            for (MutableNetwork oldN : mOldSubNetworks) {
//                if (newN.equals(oldN)) {
//                    sameN.add(newN);
//                    sameO.add(oldN);
//                    System.out.println("+++++++++++++++++++++++++++++++");
//                    System.out.println("+++++++++++++++++++++++++++++++");
//                    System.out.println("+++++++++++++++++++++++++++++++");
//                }
//            }
//        }
//        mNewSubNetworks.removeAll(sameN);
//        mNewSubNetworks.removeAll(sameO);
        writeGraphs(gitFile, "../npe_graph/false/", mNewSubNetworks);
//        writeGraphs(gitFile, "../subGraphs/subGraphsGood/", mNewSubNetworks);
        mJsonIndex = 0;
        writeGraphs(gitFile, "../npe_graph/true/", mOldSubNetworks);
//        writeGraphs(gitFile, "../subGraphs/subGraphsBug/", mOldSubNetworks);
    }

    public void writeGraphs(GitFile gitFile, String dirName, List<MutableNetwork> networks) {
        for (MutableNetwork mutableNetwork : networks) {
            String jsonFileName = dirName + gitFile.username + "_" + gitFile.hashjava + "_" + mJsonIndex + ".txt";
            mJsonIndex++;
            // 修改Graph保存的样式，原来是FeatureString，现在是Attributes
            Graph2Json graph2Json = Graph2Json.newInstance(mutableNetwork);
            graph2Json.saveToJson(jsonFileName);
        }
    }

    public void generateSubGraphs(AST2Graph ast2Graph, List<MethodDeclaration> methodDeclarations,
                                  List<Integer> modifyLines, List<MutableNetwork> mSubNetworks) {
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            logTem(methodDeclaration.getNameAsString());
            ast2Graph.initNetwork();
            ast2Graph.travelNodeForCFG(methodDeclaration);
            ast2Graph.travelNode(methodDeclaration);
            MutableNetwork mutableNetwork = ast2Graph.getNetwork();
            Set<Node> modifyVars = getModifyVars(modifyLines, ast2Graph, methodDeclaration);
            mAllSubGraphNodes.clear();
            for (Node var : modifyVars) {
                RangeNode varRangeNode = findNodeInNetwork(mutableNetwork, var);
                if (varRangeNode == null || mAllSubGraphNodes.contains(varRangeNode)) {
                    continue;
                }
                logTem(varRangeNode);
                mSubGraphNodes.clear();
                mSubGraphNodes.add(varRangeNode);
                updateSubGraphNodes(ast2Graph, varRangeNode);
                List<Object> nodesNotInNetwork = new ArrayList<>();
                for (Object node : mSubGraphNodes) {
                    if (!ast2Graph.getNetwork().nodes().contains(node)) {
                        System.exit(-1);
                        nodesNotInNetwork.add(node);
                    }
                }
                mSubGraphNodes.removeAll(nodesNotInNetwork);
                MutableNetwork mutableNetworkTemp = Graphs.inducedSubgraph(mutableNetwork, mSubGraphNodes);
                mSubNetworks.add(mutableNetworkTemp);
                mAllSubGraphNodes.addAll(mSubGraphNodes);
            }
        }
    }

    public void generateSubGraphsInRelate(AST2Graph ast2Graph, List<MethodDeclaration> methodDeclarations,
                                  List<Integer> modifyLines, List<MutableNetwork> mSubNetworks) {
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            logTem(methodDeclaration.getNameAsString());
            ast2Graph.initNetwork();
            ast2Graph.travelNodeForCFG(methodDeclaration);
            try {
                ast2Graph.travelNode(methodDeclaration);
            } catch (Exception e) {
                System.out.println(ast2Graph.getSrcFilePath() + "\n" + e);
                continue;
            }
            MutableNetwork mutableNetwork = ast2Graph.getNetwork();
            Set<Node> modifyVars = getModifyVars(modifyLines, ast2Graph, methodDeclaration);
            mAllSubGraphNodes.clear();
            List<RangeNode> rangeVars = new ArrayList<>();
            modifyVars.forEach(var -> rangeVars.add(GenerateGraph.findNodeInNetwork(mutableNetwork, var)));
            Set<RangeNode> dataFlowNodes = new HashSet<>();
            for (RangeNode varNode : rangeVars) {
                if (varNode == null || dataFlowNodes.contains(varNode)) {
                    continue;
                }
                dataFlowNodes.addAll(ast2Graph.getRelatedDataFlowNodes(varNode, dataFlowNodes, new HashSet<RangeNode>()));
            }
            RemoveNode removeNode = RemoveNode.newInstance(ast2Graph.getSrcFilePath());
            if (removeNode == null) {
                System.out.println(ast2Graph.getSrcFilePath() + ": RemoveNode == null");
                return;
            }
            removeNode.setRelatedNodes(dataFlowNodes);
            int lastLine = 0;
            for (Integer line : modifyLines) {
                if (line <= methodDeclaration.getEnd().get().line && line >= methodDeclaration.getBegin().get().line && line > lastLine) {
                    lastLine = line;
                }
            }
            removeNode.initNetwork();
            removeNode.constructNetworkWithLastLine(methodDeclaration, lastLine);
            removeNode.renameNetworkVar();
            MutableNetwork<Object, String> graph = removeNode.getNetwork();
            mSubNetworks.add(graph);
        }
    }

    private static RangeNode findNodeInDataFlow(Set<RangeNode> dataFlowNodes, Node node) {
        for (RangeNode rangeNode : dataFlowNodes) {
            if (rangeNode.getNode().equals(node) && rangeNode.getOptionalRange().equals(node.getRange())) {
                return rangeNode;
            }
        }
        return null;
    }

    private static RangeNode findNodeInDataFlow(Set<RangeNode> dataFlowNodes, RangeNode node) {
        for (RangeNode rangeNode : dataFlowNodes) {
            if (rangeNode.getNode().equals(node.getNode()) && rangeNode.getOptionalRange().equals(node.getOptionalRange())) {
                return rangeNode;
            }
        }
        return null;
    }

    public static RangeNode findNodeInNetwork(MutableNetwork network, Node node) {
        for (Object object : network.nodes()) {
            if (object instanceof RangeNode && ((RangeNode) object).getNode().equals(node)
                    && ((RangeNode) object).getOptionalRange().equals(node.getRange())) {
                return (RangeNode) object;
            }
        }
        return null;
    }

    private void updateSubGraphNodes(AST2Graph ast2Graph, Object var) {
        if (!ast2Graph.getNetwork().nodes().contains(var)) {
            System.exit(-2);
            return;
        }
        for (Object adjNode : ast2Graph.getNetwork().adjacentNodes(var)) {
            if (mSubGraphNodes.contains(adjNode)) {
                continue;
            }
            mSubGraphNodes.add(adjNode);
            // 将什么样的邻接节点继续遍历，目前是数据流的 RangeNode
            if (adjNode instanceof RangeNode && connectWithDataFlow(ast2Graph, var, adjNode)) { // 增加 边 的类型判断，如果有数据流边才继续索引
                updateSubGraphNodes(ast2Graph, adjNode);
            }
        }
    }

    private boolean connectWithDataFlow(AST2Graph ast2Graph, Object nodeU, Object nodeV) {
        for (String edge : ast2Graph.getNetwork().edgesConnecting(nodeU, nodeV)) {
            if (edge.contains(Graph.EDGE_LAST_USE)
                    || edge.contains(Graph.EDGE_LAST_WRITE)
                    || edge.contains(Graph.EDGE_COMPUTED_FROM)
                    || edge.contains(Graph.EDGE_LAST_LEXICAL_USE)
                    || edge.contains(Graph.EDGE_GUARDED_BY)
                    || edge.contains(Graph.EDGE_GUARDED_BY_NEGATION)) {
                return true;
            }
        }
        return false;
    }

    private static Set<Node> getModifyVars(List<Integer> modifyLines, AST2Graph ast2Graph, MethodDeclaration methodDeclaration) {
        Set<Node> modifyVars = new HashSet<>();
        for (int addLine : modifyLines){
            if (addLine < methodDeclaration.getBegin().get().line || addLine > methodDeclaration.getEnd().get().line) {
                continue;
            }
            modifyVars.addAll(methodDeclaration.findAll(NameExpr.class).stream()
                    .filter(nameExpr -> ast2Graph.notBelongToClassNames(nameExpr)
                            && ast2Graph.isNodeInThisLine(nameExpr, addLine))
                    .collect(Collectors.toSet()));
            modifyVars.addAll(methodDeclaration.findAll(VariableDeclarator.class).stream()
                    .filter(variableDeclarator -> ast2Graph.isNodeInThisLine(variableDeclarator, addLine))
                    .map(VariableDeclarator::getName)
                    .collect(Collectors.toSet()));
        }
        return modifyVars;
    }

    public void logTem(Object object) {
//        System.out.println(object);
    }
}
