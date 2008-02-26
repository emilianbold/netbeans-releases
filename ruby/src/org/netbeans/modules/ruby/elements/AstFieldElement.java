package org.netbeans.modules.ruby.elements;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;


public class AstFieldElement extends AstElement {
    public AstFieldElement(CompilationInfo info, Node node) {
        super(info, node);
    }

    @Override
    public String getName() {
        if (name == null) {
            if (node instanceof INameNode) { // InstVarNode, ClassDeclVarNode, ConstNode, etc.
                name = ((INameNode)node).getName();
            } else if (node instanceof ClassVarNode) { // should be INameNode)
                name = ((ClassVarNode)node).getName();
            }

            if (name == null) {
                name = node.toString();
            }

            // Chop off "@" and "@@"
            if (name.startsWith("@@")) {
                name = name.substring(2);
            } else if (name.startsWith("@")) {
                name = name.substring(1);
            }
        }

        return name;
    }

    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            // TODO - find access level!
            if (node instanceof ClassVarNode || node instanceof ClassVarDeclNode) {
                modifiers = EnumSet.of(Modifier.STATIC);
            } else {
                modifiers = Collections.emptySet();
            }
        }

        return modifiers;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.FIELD;
    }
}
