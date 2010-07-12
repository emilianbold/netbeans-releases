package org.netbeans.modules.web.jsf.editor.el;

import com.sun.el.parser.Node;
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

    public Node getNode() {
        return node;
    }

    public OffsetRange getEmbeddedOffset() {
        return embeddedOffset;
    }

    public OffsetRange getOriginalOffset() {
        return originalOffset;
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
        if (!getEmbeddedOffset().containsInclusive(offset)) {
            return null;
        }
        if (isValid()) {
            // should fa
            return null;
        }
        return null;
    }

    public ELParserResult getParserResult() {
        return parserResult;
    }

    @Override
    public String toString() {
        return "ELElement{expression=" + expression + " node=" + node + " offset=" + embeddedOffset + " error=" + error + '}';
    }

}
