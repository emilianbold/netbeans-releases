/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.transform;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.tree.TreeInfo;
import java.util.List;

/**
 * Fake tree. Represents fields separated by comma.
 *
 * @author pflaska
 */
public class FieldGroupTree extends JCTree implements Tree {

    private List<JCVariableDecl> vars;
    private boolean enumeration;

    public FieldGroupTree(List<JCVariableDecl> vars, boolean enumeration) {
        this.vars = vars;
        pos = TreeInfo.getStartPos(vars.get(0));
        this.enumeration = enumeration;
    }

    public Kind getKind() {
        return Kind.OTHER;
    }

    public List<JCVariableDecl> getVariables() {
        return vars;
    }

    public boolean isEnum() {
        return enumeration;
    }

    public int endPos() {
        return TreeInfo.endPos(vars.get(vars.size()-1));
    }

    public <R, D> R accept(TreeVisitor<R, D> arg0, D arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void accept(Visitor arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof FieldGroupTree) {
            return vars.equals(((FieldGroupTree) arg0).getVariables());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return vars.hashCode();
    }

    public int getTag() {
        return 0;
    }
}
