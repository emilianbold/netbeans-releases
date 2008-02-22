package org.netbeans.modules.ruby.elements;

import org.jruby.ast.Node;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.fpi.gsf.ElementKind;


public class AstVariableElement extends AstElement {
    public AstVariableElement(CompilationInfo info, Node node, String name) {
        super(info, node);
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
