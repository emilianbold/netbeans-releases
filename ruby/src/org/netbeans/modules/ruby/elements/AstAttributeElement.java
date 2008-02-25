package org.netbeans.modules.ruby.elements;

import java.util.Collections;
import java.util.Set;
import org.jruby.ast.Node;

import org.jruby.ast.SymbolNode;
import org.jruby.ast.types.INameNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;


public class AstAttributeElement extends AstElement {
    SymbolNode symbolNode;
    Node creationNode;

    public AstAttributeElement(CompilationInfo info, SymbolNode node, Node creationNode) {
        super(info, node);
        this.symbolNode = node;
        this.creationNode = creationNode;
    }
    
    public boolean isReadOnly() {
        if (creationNode == null || !(creationNode instanceof INameNode)) {
            return false;
        } else {
            String n = ((INameNode)creationNode).getName();
            return n.indexOf("writer") == -1 && n.indexOf("accessor") == -1; // NOI18N
        }
    }
    
    public Node getCreationNode() {
        return creationNode;
    }

    @Override
    public String getName() {
        return symbolNode.getName();
    }

    public Set<Modifier> getModifiers() {
        // TODO compute!
        return Collections.emptySet();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }
}
