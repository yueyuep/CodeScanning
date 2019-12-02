package GraphProcess;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;

/**
 * @ Author     ：wxkong
 */
public class NodePositionComparator {

    public static boolean isBeforePosition(Node a, Node b) {
        return compare(a.getEnd().get(), b.getBegin().get()) > 0;
    }

    public static boolean isAfterPosition(Node a, Node b) {
        return compare(a.getBegin().get(), b.getEnd().get()) < 0;
    }

    public static int compare(Position a, Position b) {
        if (a.line < b.line) {
            // 升序
            return 3;
        } else if (a.line > b.line) {
            // 升序
            return -1;
        } else {
            return b.column - a.column;
        }

    }

    public static boolean ifSmaller(Position positionSmall, Position positionBig) {
        return compare(positionSmall, positionBig) >= 0;
    }
}
