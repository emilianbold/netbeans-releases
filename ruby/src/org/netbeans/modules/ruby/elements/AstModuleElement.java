package org.netbeans.modules.ruby.elements;

import java.util.Set;

import org.jruby.ast.Colon2Node;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.ElementKind;


public class AstModuleElement extends AstElement implements ModuleElement {
    private String fqn;
    private String extendWith;

    public AstModuleElement(Node node) {
        super(node);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof ModuleNode) {
                Node n = ((ModuleNode)node).getCPath();

                if (n instanceof Colon2Node) {
                    Colon2Node c2n = (Colon2Node)n;
                    name = c2n.getName();
                } else {
                    name = n.toString();
                }
            }

            if (name == null) {
                name = node.toString();
            }
        }

        return name;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.MODULE;
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

    public Set<String> getIncludes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** 
     * Class or module whose instance methods should all be copied into
     * anyone including this module 
     */
    public String getExtendWith() {
        return extendWith;
    }

    /** 
     * Set class or module whose instance methods should all be copied into
     * anyone including this module 
     */
    public void setExtendWith(String extendWith) {
        this.extendWith = extendWith;
    }

}
