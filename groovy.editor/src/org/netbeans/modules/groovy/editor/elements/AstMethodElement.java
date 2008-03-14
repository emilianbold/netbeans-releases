package org.netbeans.modules.groovy.editor.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;

public class AstMethodElement extends AstElement implements MethodElement {
    private List<String> parameters;
    private Modifier access = Modifier.PUBLIC;

    public AstMethodElement(ASTNode node) {
        super(node);
    }

    @SuppressWarnings("unchecked")
    public List<String> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<String>();
            for (Parameter parameter : ((MethodNode)node).getParameters()) {
                parameters.add(parameter.getName());
            }
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
            if (node instanceof ConstructorNode) {
                name = ((ConstructorNode)node).getDeclaringClass().getNameWithoutPackage();
            } else if (node instanceof MethodNode) {
                name = ((MethodNode)node).getName();
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

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            if (node instanceof MethodNode) {
                modifiers = EnumSet.of(Modifier.STATIC, access);
            } else {
                modifiers = EnumSet.of(access);
            }
        }

        return modifiers;
    }

    public void setAccess(Modifier access) {
        this.access = access;
        if (modifiers != null && modifiers.contains(Modifier.STATIC)) {
            modifiers = EnumSet.of(Modifier.STATIC, access);
        } else {
            modifiers = null;
        }
    }

    @Override
    public ElementKind getKind() {
        if (node instanceof ConstructorNode) {
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

    /**
     * @todo Compute answer
     */
    public boolean isInherited() {
        return false;
    }
}
