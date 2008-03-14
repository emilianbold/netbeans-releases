package org.netbeans.modules.groovy.editor.elements;

import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.modules.gsf.api.ElementKind;

public class AstClassElement extends AstElement implements ClassElement {
    private String fqn;
    private Set<String> includes;

    public AstClassElement(ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof ClassNode) {
                name = ((ClassNode)node).getNameWithoutPackage();
            }

            if (name == null) {
                name = node.toString();
            }
        }

        return name;
    }

    public String getFqn() {
        if (fqn == null) {
            return getName();
        }

        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CLASS;
    }
}
