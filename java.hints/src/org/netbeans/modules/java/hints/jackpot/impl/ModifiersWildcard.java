package org.netbeans.modules.java.hints.jackpot.impl;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.util.List;
import javax.lang.model.element.Name;

/**
 *
 * @author lahvac
 */
public class ModifiersWildcard extends JCModifiers implements IdentifierTree {

    private final Name ident;
    private final JCIdent jcIdent;

    public ModifiersWildcard(Name ident, JCIdent jcIdent) {
        super(0, List.<JCAnnotation>nil());
        this.ident = ident;
        this.jcIdent = jcIdent;
    }

    public Name getName() {
        return ident;
    }

    @Override
    public Kind getKind() {
        return Kind.IDENTIFIER;
    }

    @Override
    public void accept(Visitor v) {
        v.visitIdent(jcIdent);
    }

    @Override
    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
        return v.visitIdentifier(this, d);
    }

    @Override
    public String toString() {
        return ident.toString();
    }

}
