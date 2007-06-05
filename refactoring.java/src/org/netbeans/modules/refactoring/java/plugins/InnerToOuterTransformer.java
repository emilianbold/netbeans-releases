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

import org.netbeans.modules.refactoring.java.spi.SearchVisitor;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;

/**
 *
 * @author Jan Becicka
 */
public class InnerToOuterTransformer extends SearchVisitor {

    private Element inner;
    private Element outer;
    private InnerToOuterRefactoring refactoring;
    
    private Element getCurrentElement() {
        return workingCopy.getTrees().getElement(getCurrentPath());
    }
    
    public InnerToOuterTransformer(InnerToOuterRefactoring re) {
        this.refactoring = re;
    }
    
    public void setWorkingCopy(WorkingCopy wc) {
        super.setWorkingCopy(wc);
        this.inner = refactoring.getSourceType().resolveElement(wc);
        outer = SourceUtils.getEnclosingTypeElement(inner);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (inner.equals(getCurrentElement())) {
            Tree newTree = make.setLabel(node, refactoring.getClassName());        
            workingCopy.rewrite(node, newTree);
        } else if (isThisReferenceToOuter()) {
            MemberSelectTree m = make.MemberSelect(node, refactoring.getReferenceName());
            workingCopy.rewrite(node, m);
        }
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMethod(MethodTree constructor, Element element) {
        if (constructor.getReturnType()==null) {
            //constructor
            if (!inner.equals(getCurrentClass()) && workingCopy.getTypes().isSubtype(getCurrentElement().getEnclosingElement().asType(), inner.asType())) {
                MemberSelectTree arg = make.MemberSelect(make.Identifier(getCurrentClass().getEnclosingElement().getSimpleName()), "this");
                MethodInvocationTree superCall = (MethodInvocationTree) ((ExpressionStatementTree)constructor.getBody().getStatements().get(0)).getExpression();
                MethodInvocationTree newSuperCall = make.insertMethodInvocationArgument(superCall, 0, arg, null);
                workingCopy.rewrite(superCall, newSuperCall);
            }
            
        }
        return super.visitMethod(constructor, element);
    }

    @Override
    public Tree visitClass(ClassTree classTree, Element element) {
        return super.visitClass(classTree, element);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree memberSelect, Element element) {
        Element current = getCurrentElement();
        if (inner.equals(current)) {
            ExpressionTree ex = memberSelect.getExpression();
            Tree newTree;
            if (ex.getKind() == Tree.Kind.IDENTIFIER) {
                newTree = make.Identifier(refactoring.getClassName());
                workingCopy.rewrite(memberSelect, newTree);
            } else if (ex.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree m = make.MemberSelect(((MemberSelectTree) ex).getExpression(),refactoring.getClassName());
                workingCopy.rewrite(memberSelect,m);
            }
        } else if (isThisReferenceToOuter()) {
            MemberSelectTree m = make.MemberSelect(memberSelect, refactoring.getReferenceName());
            workingCopy.rewrite(memberSelect, m);
        }
        
        return super.visitMemberSelect(memberSelect, element);
    }
    
    private boolean isThisReferenceToOuter() {
        Element cur = getCurrentElement();
        if (cur==null || cur.getKind() == ElementKind.PACKAGE)
                return false;
        TypeElement encl = SourceUtils.getEnclosingTypeElement(cur);
        if (outer.equals(encl) && workingCopy.getTypes().isSubtype(getCurrentClass().asType(), inner.asType())) {
            return true;
        }
        return false;
    }
    
    private TypeElement getCurrentClass() {
        TreePath tp = getCurrentPath().getParentPath();
        while (tp!=null) {
            if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                return (TypeElement) workingCopy.getTrees().getElement(tp);
            }
            tp = tp.getParentPath();
        }
        throw new IllegalStateException();
    }

    
    private boolean isIn(Element el) {
        if (el==null)
            return false;
        Element current = el;
        while (current.getKind() != ElementKind.PACKAGE) {
            if (current.equals(inner)) {
                return true;
            }
            current = current.getEnclosingElement();
        }
        return false;
    }

//    @Override
//    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
//        updateUsageIfMatch(getCurrentPath(), node,p);
//        return super.visitMemberSelect(node, p);
//    }
}
