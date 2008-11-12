package org.netbeans.modules.groovy.editor.api.elements;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.netbeans.modules.gsf.api.ElementKind;

public class AstFieldElement extends AstElement {
    public AstFieldElement(ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof FieldNode) {
                name = ((FieldNode)node).getName();
            }

            if (name == null) {
                name = node.toString();
            }
        }

        return name;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.FIELD;
    }
}
