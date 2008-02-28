package org.netbeans.modules.ruby.elements;

import org.jruby.ast.ConstDeclNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;


public class AstConstantElement extends AstElement {
    private ConstDeclNode constNode;

    public AstConstantElement(CompilationInfo info, ConstDeclNode node) {
        super(info, node);
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
