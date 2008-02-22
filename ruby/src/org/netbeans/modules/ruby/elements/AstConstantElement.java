package org.netbeans.modules.ruby.elements;

import org.jruby.ast.ConstDeclNode;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.fpi.gsf.ElementKind;


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
