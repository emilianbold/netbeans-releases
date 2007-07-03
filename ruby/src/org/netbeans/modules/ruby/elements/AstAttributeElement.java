package org.netbeans.modules.ruby.elements;

import java.util.Collections;
import java.util.Set;

import org.jruby.ast.SymbolNode;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;


public class AstAttributeElement extends AstElement {
    SymbolNode symbolNode;

    public AstAttributeElement(SymbolNode node) {
        super(node);
        this.symbolNode = node;
    }

    @Override
    public String getName() {
        return symbolNode.getName();
    }

    public Set<Modifier> getModifiers() {
        // TODO compute!
        return Collections.emptySet();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }
}
