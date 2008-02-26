package org.netbeans.modules.ruby.elements;

import java.util.Set;

import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon3Node;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.ast.types.INameNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;


public class AstClassElement extends AstElement implements ClassElement {
    private String fqn;
    private Set<String> includes;

    public AstClassElement(CompilationInfo info, Node node) {
        super(info, node);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof ClassNode) {
                Colon3Node n = ((ClassNode)node).getCPath();
                
                name = n.getName();
            } else if (node instanceof SClassNode) {
                Node n = ((SClassNode)node).getReceiverNode();

                // What if it's a selfnode?
                if (n instanceof Colon3Node) {
                    Colon3Node c3n = (Colon3Node)n;
                    name = c3n.getName();
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
