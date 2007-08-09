package org.netbeans.modules.ruby.elements;

import java.util.Collections;
import java.util.Set;
import org.jruby.ast.Node;

import org.jruby.ast.SymbolNode;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;


public class AstAttributeElement extends AstElement {
    SymbolNode symbolNode;
    Node creationNode;

    public AstAttributeElement(SymbolNode node, Node creationNode) {
        super(node);
        this.symbolNode = node;
        this.creationNode = creationNode;
    }
    
    public boolean isReadOnly() {
        if (creationNode == null || !(creationNode instanceof INameNode)) {
            return false;
        } else {
            String name = ((INameNode)creationNode).getName();
            return name.indexOf("writer") == -1 && name.indexOf("accessor") == -1; // NOI18N
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
