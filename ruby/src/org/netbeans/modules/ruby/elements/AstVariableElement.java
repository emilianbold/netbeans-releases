package org.netbeans.modules.ruby.elements;

import org.jruby.ast.Node;
import org.netbeans.api.gsf.ElementKind;


public class AstVariableElement extends AstElement {
    public AstVariableElement(Node node, String name) {
        super(node);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.VARIABLE;
    }
}
