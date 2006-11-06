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
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
class EntityGenerateFromImplVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromImpl {
    
    private WorkingCopy workingCopy;
    private ExecutableElement intfMethod;
    private TypeElement destination;
    private TypeElement home;
    private TypeElement component;
    private Entity e;
    
    public EntityGenerateFromImplVisitor(WorkingCopy workingCopy, Entity e) {
        this.workingCopy = workingCopy;
        this.e = e;
    }
    
    public void getInterfaceMethodFromImpl(MethodType m, TypeElement home, TypeElement component) {
        this.home = home;
        this.component = component;
        m.accept(this);
    }
    
    public ExecutableElement getInterfaceMethod() {
        return intfMethod;
    }
    
    public TypeElement getDestinationInterface() {
        return destination;
    }
    
    public void visit(BusinessMethodType bmt) {
        intfMethod = bmt.getMethodElement();
        destination = component;
    }
    
    public void visit(CreateMethodType cmt) {
        intfMethod = cmt.getMethodElement();
        String origName = intfMethod.getSimpleName().toString();
        String newName = null;
        if (origName.startsWith("ejbPostCreate")) {
            newName = chopAndUpper(origName,"ejbPost"); //NOI18N
        } else {
            newName = chopAndUpper(origName,"ejb"); //NOI18N
        }
        MethodTree resultTree = AbstractMethodController.modifyMethod(workingCopy, intfMethod, null, newName, null, null, null, null);
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        intfMethod =  (ExecutableElement) trees.getElement(treePath);
        destination = home;
    }
    
    public void visit(HomeMethodType hmt) {
        intfMethod = hmt.getMethodElement();
        String origName = intfMethod.getSimpleName().toString();
        String newName = chopAndUpper(origName,"ejbHome"); //NOI18N
        MethodTree resultTree = AbstractMethodController.modifyMethod(workingCopy, intfMethod, null, newName, null, null, null, null);
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        intfMethod =  (ExecutableElement) trees.getElement(treePath);
        destination = home;
    }
    
    public void visit(FinderMethodType fmt) {
        intfMethod = fmt.getMethodElement();
        String origName = intfMethod.getSimpleName().toString();
        String newName = chopAndUpper(origName,"ejb"); //NOI18N
        MethodTree resultTree = AbstractMethodController.modifyMethod(workingCopy, intfMethod, null, newName, null, null, null, null);
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        intfMethod =  (ExecutableElement) trees.getElement(treePath);
        TypeMirror returnType = intfMethod.getReturnType();
        if (TypeKind.DECLARED == returnType.getKind()) {
            DeclaredType declaredType = (DeclaredType) returnType;
            String rv = ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
            if (!rv.equals(Collection.class.getName()) || !rv.equals(Set.class.getName())) {
                resultTree = AbstractMethodController.modifyMethod(workingCopy, intfMethod, null, null, trees.getTree(component), null, null, null);
                treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
                intfMethod =  (ExecutableElement) trees.getElement(treePath);
            }
        }
        //TODO: RETOUCHE need to empty the body?
//        intfMethod.setBody(null);
        destination = home;
    }
    
    private String chopAndUpper(String fullName, String chop) {
        StringBuffer sb = new StringBuffer(fullName);
        sb.delete(0, chop.length());
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }
}
