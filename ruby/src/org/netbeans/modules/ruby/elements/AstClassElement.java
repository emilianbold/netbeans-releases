package org.netbeans.modules.ruby.elements;

import java.util.Set;

import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.ElementKind;


public class AstClassElement extends AstElement implements ClassElement {
    private String fqn;
    private Set<String> includes;

    public AstClassElement(Node node) {
        super(node);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof ClassNode) {
                Node n = ((ClassNode)node).getCPath();

                if (n instanceof Colon2Node) {
                    Colon2Node c2n = (Colon2Node)n;
                    name = c2n.getName();
                } else if (n instanceof INameNode) {
                    name = ((INameNode)n).getName();
                } else {
                    name = n.toString();
                }
            } else if (node instanceof SClassNode) {
                Node n = ((SClassNode)node).getReceiverNode();

                // What if it's a selfnode?
                if (n instanceof Colon2Node) {
                    Colon2Node c2n = (Colon2Node)n;
                    name = c2n.getName();
                } else if (n instanceof INameNode) {
                    name = ((INameNode)n).getName();
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
