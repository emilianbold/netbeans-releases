package org.netbeans.modules.ruby.elements;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.modules.ruby.AstUtilities;


public class AstMethodElement extends AstElement implements MethodElement {
    private List<String> parameters;
    private Modifier access = Modifier.PUBLIC;

    public AstMethodElement(Node node) {
        super(node);
    }

    @SuppressWarnings("unchecked")
    public List<String> getParameters() {
        if (parameters == null) {
            parameters = AstUtilities.getDefArgs((MethodDefNode)node, false);

            if (parameters == null) {
                parameters = Collections.emptyList();
            }
        }

        return parameters;
    }

    public boolean isDeprecated() {
        // XXX TODO: When wrapping java objects I guess these functions -could- be deprecated, right?
        return false;
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof DefnNode) {
                name = ((DefnNode)node).getName();
            } else if (node instanceof DefsNode) {
                name = ((DefsNode)node).getName();
            }

            if (name == null) {
                name = node.toString();
            }
        }

        return name;
    }

    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            if (node instanceof DefsNode) {
                modifiers = EnumSet.of(Modifier.STATIC, access);
            } else {
                modifiers = EnumSet.of(access);
            }
        }

        return modifiers;
    }

    public void setAccess(Modifier access) {
        this.access = access;
    }

    @Override
    public ElementKind getKind() {
        if ("initialize".equals(getName()) || "new".equals(getName())) {
            return ElementKind.CONSTRUCTOR;
        } else {
            return ElementKind.METHOD;
        }
    }

    /**
     * @todo Compute answer
     */
    public boolean isTopLevel() {
        return false;
    }
}
