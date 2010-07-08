package org.netbeans.modules.web.jsf.editor.el;

import com.sun.el.parser.Node;
import javax.el.ELException;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Represents the parse result of a single EL expression.
 * 
 * @author Erno Mononen
 */
public final class ELElement {

    private final Node node;
    private final OffsetRange offset;
    private final ELException error;
    private final String expression;

    private ELElement(Node node, OffsetRange offset, ELException error, String expression) {
        assert node == null || error == null;
        if (node != null) {
            assert offset.getLength() == node.endOffset() - node.startOffset()
                    : "Offsets don't match: " + offset + ", node: " + node.startOffset() + "-" + node.endOffset();
        }
        this.node = node;
        this.offset = offset;
        this.error = error;
        this.expression = expression;
    }

    static ELElement valid(Node node, OffsetRange offset, String expression) {
        return new ELElement(node, offset, null, expression);
    }

    static ELElement error(ELException error, OffsetRange offset, String expression) {
        return new ELElement(null, offset, error, expression);
    }

    public Node getNode() {
        return node;
    }

    public OffsetRange getOffset() {
        return offset;
    }

    public boolean isValid() {
        return error == null;
    }

    public ELException getError() {
        return error;
    }

    public String getExpression() {
        return expression;
    }

    public Node findNodeAt(int offset) {
        if (!getOffset().containsInclusive(offset)) {
            return null;
        }
        if (isValid()) {
            // should fa
            return null;
        }
        return null;
    }

    @Override
    public String toString() {
        return "ELElement{expression=" + expression + " node=" + node + " offset=" + offset + " error=" + error + '}';
    }

}
