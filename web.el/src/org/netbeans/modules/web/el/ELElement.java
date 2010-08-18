package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import javax.el.ELException;
import org.netbeans.modules.csl.api.OffsetRange;

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
    private final ELParserResult parserResult;
    private final OffsetRange originalOffset;

    private ELElement(Node node, ELException error, String expression, OffsetRange embeddedOffset, ELParserResult parserResult) {
        assert node == null || error == null;
        if (node != null) {
            assert embeddedOffset.getLength() == node.endOffset() - node.startOffset()
                    : "Offsets don't match: " + embeddedOffset + ", node: " + node.startOffset() + "-" + node.endOffset();
        }
        this.node = node;
        this.embeddedOffset = embeddedOffset;
        this.parserResult = parserResult;
        this.error = error;
        this.expression = expression;

        int origStart = parserResult.getSnapshot().getOriginalOffset(embeddedOffset.getStart());
        int origEnd = parserResult.getSnapshot().getOriginalOffset(embeddedOffset.getEnd());
        this.originalOffset = new OffsetRange(origStart, origEnd);
    }

    static ELElement valid(Node node, String expression, OffsetRange embeddedOffset, ELParserResult parserResult) {
        return new ELElement(node, null, expression, embeddedOffset, parserResult);
    }

    static ELElement error(ELException error, String expression, OffsetRange embeddedOffset, ELParserResult parserResult) {
        return new ELElement(null, error, expression, embeddedOffset, parserResult);
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
     * @return the node at the given {@code offset} or {@code null}.
     */
    public Node findNodeAt(final int offset) {
        assert getOriginalOffset().containsInclusive(offset);
        final Node[] result = new Node[1];
        getNode().accept(new NodeVisitor() {
            @Override
            public void visit(Node node) throws ELException {
                int nodeLength = node.endOffset() - node.startOffset();
                if (originalOffset.getStart() + node.startOffset() <= offset
                        && originalOffset.getStart() + node.startOffset() + nodeLength >= offset) {
                    result[0] = node;
                }

            }
        });
        return result[0];
    }

    public ELParserResult getParserResult() {
        return parserResult;
    }

    @Override
    public String toString() {
        return "ELElement{expression=" + expression + " node=" + node + " offset=" + embeddedOffset + " error=" + error + '}';
    }

}
