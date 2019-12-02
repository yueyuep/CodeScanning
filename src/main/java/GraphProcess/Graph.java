package GraphProcess;

public interface Graph {
    String EDGE_CHILD_NODE = "child_node";
    String EDGE_CHILD_TOKEN = "child_token";
    String EDGE_NEXT_TOKEN = "next_token";
    String EDGE_LAST_USE = "last_use";
    String EDGE_LAST_WRITE = "last_write";
    String EDGE_COMPUTED_FROM = "computed_from";
    String EDGE_LAST_LEXICAL_USE = "last_lexical_use";
    String EDGE_RETURNS_TO = "reutrns_to";
    String EDGE_GUARDED_BY = "guarded_by";
    String EDGE_GUARDED_BY_NEGATION = "guarded_by_negation";
    String EDGE_NEXT_EXEC = "next_execution";
    String EDGE_METHOD_CALL = "method_call";
    String EDGE_FORMAL_ARG_NAME = "formal_arg_name";
}
