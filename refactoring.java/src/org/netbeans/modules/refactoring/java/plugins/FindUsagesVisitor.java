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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class FindUsagesVisitor extends FindVisitor {

    public FindUsagesVisitor(WorkingCopy workingCopy) {
        super(workingCopy);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        addIfMatch(getCurrentPath(), node, p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        addIfMatch(getCurrentPath(), node,p);
        return super.visitMemberSelect(node, p);
    }
    
    @Override
    public Tree visitNewClass(NewClassTree node, Element p) {
        Trees trees = workingCopy.getTrees();
        ClassTree classTree = ((NewClassTree) node).getClassBody();
        if (classTree != null && p.getKind()==ElementKind.CONSTRUCTOR) {
            Element anonClass = workingCopy.getTrees().getElement(TreePath.getPath(workingCopy.getCompilationUnit(), classTree));
            for (ExecutableElement c: ElementFilter.constructorsIn(anonClass.getEnclosedElements())) {
                MethodTree t = workingCopy.getTrees().getTree(c);
                TreePath superCall = trees.getPath(workingCopy.getCompilationUnit(), ((ExpressionStatementTree) t.getBody().getStatements().get(0)).getExpression());
                Element superCallElement = trees.getElement(superCall);
                if (superCallElement != null && superCallElement.equals(p)) {
                    addUsage(superCall);
                }
            }
        } else {
            addIfMatch(getCurrentPath(), node, p);
        }
        return super.visitNewClass(node, p);
    }
    
    private void addIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path)) {
            return;
        }
        Trees trees = workingCopy.getTrees();
        Element el = trees.getElement(path);
        if (el == null) {
            return;
        }
        if (elementToFind.getKind() == ElementKind.METHOD && el.getKind() == ElementKind.METHOD) {
            if (el.equals(elementToFind) || workingCopy.getElements().overrides((ExecutableElement) el, (ExecutableElement) elementToFind, (TypeElement) elementToFind.getEnclosingElement())) {
                addUsage(getCurrentPath());
            }
        } else if (el.equals(elementToFind)) {
            addUsage(getCurrentPath());
            return;
        }
    }
}

