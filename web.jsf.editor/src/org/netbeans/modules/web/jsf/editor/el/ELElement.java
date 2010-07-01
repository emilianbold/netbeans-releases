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
    private final ELException error;
    private final String expression;

    private ELElement(Node node, ELException error, String expression) {
        assert node == null || error == null;
        this.node = node;
        this.error = error;
        this.expression = expression;
    }

    static ELElement valid(Node node, String expression) {
        return new ELElement(node, null, expression);
    }

    static ELElement error(ELException error, String expression) {
        return new ELElement(null, error, expression);
    }

    public Node getNode() {
        return node;
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

    @Override
    public String toString() {
        return "ELElement{expression=" + expression + " node=" + node + " offset=" + node.startOffset() + "-" + node.endOffset() + " error=" + error + '}';
    }

}
