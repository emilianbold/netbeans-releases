package org.netbeans.modules.groovy.editor.elements;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;

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

    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            // TODO - find access level!
            if (node instanceof FieldNode) {
                modifiers = EnumSet.of(Modifier.STATIC);
            } else {
                modifiers = Collections.emptySet();
            }
        }

        return modifiers;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.FIELD;
    }
}
