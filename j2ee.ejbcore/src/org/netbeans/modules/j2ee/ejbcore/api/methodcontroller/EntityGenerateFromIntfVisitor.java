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
import com.sun.source.tree.StatementTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
class EntityGenerateFromIntfVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromIntf {

    private final WorkingCopy workingCopy;
    private ExecutableElement implMethod;
    private ExecutableElement secondaryMethod;
    private final Entity entity;
    private static final String TODO = "//TODO implement "; //NOI18N
    
    public EntityGenerateFromIntfVisitor(WorkingCopy workingCopy, Entity entity) {
        this.workingCopy = workingCopy;
        this.entity = entity;
    }
    
    public void getInterfaceMethodFromImpl(MethodType methodType) {
        methodType.accept(this);
    }
    
    public ExecutableElement getImplMethod() {
        return implMethod;
    }
    
    public ExecutableElement getSecondaryMethod() {
        return secondaryMethod;
    }
    
    public void visit(BusinessMethodType bmt) {
        implMethod = bmt.getMethodElement().resolve(workingCopy);
        
        SourcePositions[] positions = new SourcePositions[1];
        TreeUtilities treeUtilities = workingCopy.getTreeUtilities();
        String body = TODO + implMethod.getSimpleName();
        TypeMirror type = implMethod.getReturnType();
        body += getReturnStatement(type);
        StatementTree bodyTree = treeUtilities.parseStatement(body, positions);
        
        MethodTree resultTree = AbstractMethodController.modifyMethod(
                workingCopy, 
                implMethod, 
                Collections.singleton(Modifier.PUBLIC), 
                null, null, null, null,
                workingCopy.getTreeMaker().Block(Collections.singletonList(bodyTree), false)
                );
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        implMethod = (ExecutableElement) trees.getElement(treePath);
    }
    
    public void visit(CreateMethodType cmt) {
        implMethod = cmt.getMethodElement().resolve(workingCopy);
        String origName = implMethod.getSimpleName().toString();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        TypeElement type = workingCopy.getElements().getTypeElement(entity.getPrimKeyClass());
        
        SourcePositions[] positions = new SourcePositions[1];
        TreeUtilities treeUtilities = workingCopy.getTreeUtilities();
        TypeMirror typeMirror = type.asType();
        String body = TODO + newName + getReturnStatement(typeMirror);
        StatementTree bodyTree = treeUtilities.parseStatement(body, positions);
        
        MethodTree resultTree = AbstractMethodController.modifyMethod(
                workingCopy, 
                implMethod, 
                Collections.singleton(Modifier.PUBLIC), 
                newName, 
                workingCopy.getTrees().getTree(type), 
                null, null,
                workingCopy.getTreeMaker().Block(Collections.singletonList(bodyTree), false)
                );
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        implMethod = (ExecutableElement) trees.getElement(treePath);
        
        secondaryMethod = cmt.getMethodElement().resolve(workingCopy);
        origName = secondaryMethod.getSimpleName().toString();
        newName = prependAndUpper(origName,"ejbPost"); //NOI18N
        
        positions = new SourcePositions[1];
        body = TODO + newName;
        bodyTree = treeUtilities.parseStatement(body, positions);
        
        resultTree = AbstractMethodController.modifyMethod(
                workingCopy, 
                secondaryMethod, 
                Collections.singleton(Modifier.PUBLIC), 
                newName, 
                workingCopy.getTreeMaker().PrimitiveType(TypeKind.VOID),
                null, null,
                workingCopy.getTreeMaker().Block(Collections.singletonList(bodyTree), false)
                );
        treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        secondaryMethod = (ExecutableElement) trees.getElement(treePath);
    }
    
    public void visit(HomeMethodType hmt) {
        implMethod = hmt.getMethodElement().resolve(workingCopy);
        String origName = implMethod.getSimpleName().toString();
        String newName = prependAndUpper(origName,"ejbHome"); //NOI18N
        
        SourcePositions[] positions = new SourcePositions[1];
        TreeUtilities treeUtilities = workingCopy.getTreeUtilities();
        String body = TODO + implMethod.getSimpleName() + getReturnStatement(implMethod.asType());
        StatementTree bodyTree = treeUtilities.parseStatement(body, positions);
        
        MethodTree resultTree = AbstractMethodController.modifyMethod(
                workingCopy, 
                implMethod, 
                Collections.singleton(Modifier.PUBLIC), 
                newName, 
                null, null, null,
                workingCopy.getTreeMaker().Block(Collections.singletonList(bodyTree), false)
                );
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        implMethod = (ExecutableElement) trees.getElement(treePath);
    }
    
    public void visit(FinderMethodType fmt) {
        implMethod = fmt.getMethodElement().resolve(workingCopy);
        String origName = implMethod.getSimpleName().toString();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        
        SourcePositions[] positions = new SourcePositions[1];
        TreeUtilities treeUtilities = workingCopy.getTreeUtilities();
        String body = TODO + implMethod.getSimpleName() + getReturnStatement(implMethod.asType());
        StatementTree bodyTree = treeUtilities.parseStatement(body, positions);
        
        TypeElement collectionType = workingCopy.getElements().getTypeElement(java.util.Collection.class.getName());
        boolean isAssignable = workingCopy.getTypes().isSubtype(implMethod.asType(), collectionType.asType());
        TypeElement pkType = workingCopy.getElements().getTypeElement(entity.getPrimKeyClass());
        
        MethodTree resultTree = AbstractMethodController.modifyMethod(
                workingCopy, 
                implMethod, 
                Collections.singleton(Modifier.PUBLIC), 
                newName, 
                isAssignable ? null : workingCopy.getTrees().getTree(pkType),
                null, null,
                workingCopy.getTreeMaker().Block(Collections.singletonList(bodyTree), false)
                );
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        implMethod = (ExecutableElement) trees.getElement(treePath);
    }
    
    private String prependAndUpper(String fullName, String prefix) {
        StringBuffer stringBuffer = new StringBuffer(fullName);
        stringBuffer.setCharAt(0, Character.toUpperCase(stringBuffer.charAt(0)));
        return prefix+stringBuffer.toString();
    }
    
    public static String getReturnStatement(TypeMirror type) {
        String result = "";
        if (TypeKind.VOID == type.getKind()) {
            
        } else if (TypeKind.BOOLEAN == type.getKind()){
            result = "\nreturn false;";
        } else if (TypeKind.BYTE == type.getKind()){
            result = "\nreturn 0;";
        } else if (TypeKind.CHAR == type.getKind()){
            result ="\nreturn '0';";
        } else if (TypeKind.DOUBLE == type.getKind()){
            result ="\nreturn 0.0;";
        } else if (TypeKind.FLOAT == type.getKind()){
            result ="\nreturn 0;";
        } else if (TypeKind.INT == type.getKind()){
            result ="\nreturn 0;";
        } else if (TypeKind.LONG == type.getKind()){
            result ="\nreturn 0;";
        } else if (TypeKind.SHORT == type.getKind()){
            result ="\nreturn 0;";
        } else{
            result ="\nreturn null;";
        }
        return result;
    }
}
