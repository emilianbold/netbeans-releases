package org.netbeans.modules.ruby.elements;

import org.jruby.ast.ConstDeclNode;
import org.netbeans.api.gsf.ElementKind;


public class AstConstantElement extends AstElement {
    ConstDeclNode constNode;

    public AstConstantElement(ConstDeclNode node) {
        super(node);
        this.constNode = node;
    }

    @Override
    public String getName() {
        return constNode.getName();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CONSTANT;
    }
}
