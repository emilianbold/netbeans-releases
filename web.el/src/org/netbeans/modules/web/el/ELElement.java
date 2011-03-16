package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import javax.el.ELException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Represents the parse result of a single EL expression.
 * 
 * @author Erno Mononen
 */
public final class ELElement {

    private final Node node;
    private final OffsetRange embeddedOffset;
    private final ELException error;
    private final String expression;
    private final Snapshot snapshot;
    private final OffsetRange originalOffset;

    private ELElement(Node node, ELException error, String expression, OffsetRange embeddedOffset, Snapshot snapshot) {
        assert node == null || error == null;
        this.node = node;
        this.embeddedOffset = embeddedOffset;
        this.snapshot = snapshot;
        this.error = error;
        this.expression = expression;

        int origStart = snapshot.getOriginalOffset(embeddedOffset.getStart());
        int origEnd = snapshot.getOriginalOffset(embeddedOffset.getEnd());
        this.originalOffset = new OffsetRange(origStart, origEnd);
    }

    static ELElement valid(Node node, String expression, OffsetRange embeddedOffset, Snapshot snapshot) {
        return new ELElement(node, null, expression, embeddedOffset, snapshot);
    }

    static ELElement error(ELException error, String expression, OffsetRange embeddedOffset, Snapshot snapshot) {
        return new ELElement(null, error, expression, embeddedOffset, snapshot);
    }

    /**
     * Makes a copy of this, but with the given {@code node} and {@code expression}.
     * Can't be invoked on valid elements, and the given {@code node} must represent
     * a valid expression.
     * 
     * @param node 
     * @param expression
     * @return a copy of this but with the given {@code node} and {@code expression}.
     */
    public ELElement makeValidCopy(Node node, String expression) {
        assert !isValid();
        return valid(node, expression, embeddedOffset, snapshot);
    }

    /**
     * Gets the root node of the expression.
     * 
     * @return
     */
    public Node getNode() {
        return node;
    }

    /**
     * Gets the offset in the embedded source.
     * @see #getOriginalOffset()
     * @return
     */
    public OffsetRange getEmbeddedOffset() {
        return embeddedOffset;
    }

    /**
     * Gets the offset in the original document.
     * @return
     */
    public OffsetRange getOriginalOffset() {
        return originalOffset;
    }

    /**
     * Gets the offset of the given {@code node} in the original document.
     * @param node a node contained by this element.
     * @return
     */
    public OffsetRange getOriginalOffset(Node node) {
        int start = originalOffset.getStart() + node.startOffset();
        int end = start + (node.endOffset() - node.startOffset());
        return new OffsetRange(start, end);
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

    /**
     * Gets the node at the given offset.
     * @param offset an offset in the original document.
     * @return the node at the given {@code offset} or {@code null}.
     */
    public Node findNodeAt(final int offset) {
        assert getOriginalOffset().containsInclusive(offset);
        if (getNode() == null) {
            return null;
        }
        final Node[] result = new Node[1];
        getNode().accept(new NodeVisitor() {
            @Override
            public void visit(Node node) throws ELException {
                int nodeLength = node.endOffset() - node.startOffset();
                if (originalOffset.getStart() + node.startOffset() <= offset
                        && originalOffset.getStart() + node.startOffset() + nodeLength > offset) {
                    result[0] = node;
                }

            }
        });
        return result[0];
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public String toString() {
        return "ELElement{expression=" + expression + " node=" + node + " offset=" + embeddedOffset + " error=" + error + '}';
    }

}
