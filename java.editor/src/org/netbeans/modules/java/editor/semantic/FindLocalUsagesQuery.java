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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;

/**
 *
 * @author Jan Lahoda
 */
public class FindLocalUsagesQuery extends CancellableTreePathScanner<Void, Stack<Tree>> {
    
    private CompilationInfo info;
    private Set<Token<JavaTokenId>> usages;
    private Element toFind;
    private Document doc;
    
    /** Creates a new instance of FindLocalUsagesQuery */
    public FindLocalUsagesQuery() {
    }
    
    public Set<Token<JavaTokenId>> findUsages(Element element, CompilationInfo info, Document doc) {
        this.info = info;
        this.usages = new HashSet<Token<JavaTokenId>>();
        this.toFind = element;
        this.doc = doc;
        
        scan(info.getCompilationUnit(), null);
        return usages;
    }

    private void handlePotentialVariable(TreePath tree) {
        Element el = info.getTrees().getElement(tree);
        
        if (toFind.equals(el)) {
            Token<JavaTokenId> t = Utilities.getToken(info, doc, tree);
            
            if (t != null)
                usages.add(t);
        }
    }
    
    @Override
    public Void visitIdentifier(IdentifierTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        super.visitIdentifier(tree, d);
        return null;
    }
    
    @Override
    public Void visitMethod(MethodTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        super.visitMethod(tree, d);
        return null;
    }
    
    @Override
    public Void visitMemberSelect(MemberSelectTree node, Stack<Tree> p) {
        handlePotentialVariable(getCurrentPath());
        super.visitMemberSelect(node, p);
        return null;
    }
    
    @Override
    public Void visitVariable(VariableTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        super.visitVariable(tree, d);
        return null;
    }
    
    @Override
    public Void visitClass(ClassTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        super.visitClass(tree, d);
        return null;
    }
}
