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

import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
class EntityGenerateFromIntfVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromIntf {

    private static final String TODO = "//TODO implement "; //NOI18N
    
    private final FileObject ejbClassFO;
    private final Entity entity;
    private MethodModel implMethod;
    private MethodModel secondaryMethod;
    
    public EntityGenerateFromIntfVisitor(FileObject ejbClassFO, Entity entity) {
        this.ejbClassFO = ejbClassFO;
        this.entity = entity;
    }
    
    public void getInterfaceMethodFromImpl(MethodType methodType) {
        methodType.accept(this);
    }
    
    public MethodModel getImplMethod() {
        return implMethod;
    }
    
    public MethodModel getSecondaryMethod() {
        return secondaryMethod;
    }
    
    public void visit(BusinessMethodType bmt) {
        implMethod = bmt.getMethodElement();
        String body = TODO + implMethod.getName();
        body += implMethod.getReturnType();
        implMethod = MethodModel.create(
                implMethod.getName(), 
                implMethod.getReturnType(),
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(CreateMethodType cmt) {
        implMethod = cmt.getMethodElement();
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        String type = entity.getPrimKeyClass();
        String body = TODO + newName + type;
        implMethod = MethodModel.create(
                newName, 
                type,
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
        secondaryMethod = cmt.getMethodElement();
        origName = secondaryMethod.getName();
        newName = prependAndUpper(origName,"ejbPost"); //NOI18N
        body = TODO + newName;
        secondaryMethod = MethodModel.create(
                newName, 
                "void",
                body,
                secondaryMethod.getParameters(),
                secondaryMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(HomeMethodType hmt) {
        implMethod = hmt.getMethodElement();
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejbHome"); //NOI18N
        String body = TODO + implMethod.getName() + implMethod.getReturnType();
        implMethod = MethodModel.create(
                newName, 
                implMethod.getReturnType(),
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(FinderMethodType fmt) {
        implMethod = fmt.getMethodElement();
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        String body = TODO + implMethod.getName() + implMethod.getReturnType();
        String collectionType = java.util.Collection.class.getName();
        String implMethodElement = implMethod.getReturnType();
        boolean isAssignable = false;
        try {
            isAssignable = isSubtype(implMethodElement, collectionType);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        implMethod = MethodModel.create(
                newName, 
                isAssignable ? "void" : entity.getPrimKeyClass(),
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    private boolean isSubtype(final String className1, final String className2) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final boolean[] result = new boolean[] {false};
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement1 = controller.getElements().getTypeElement(className1);
                TypeElement typeElement2 = controller.getElements().getTypeElement(className2);
                result[0] = controller.getTypes().isSubtype(typeElement1.asType(), typeElement2.asType());
            }
        }, true);
        return result[0];
    }
    
    private String prependAndUpper(String fullName, String prefix) {
        StringBuffer stringBuffer = new StringBuffer(fullName);
        stringBuffer.setCharAt(0, Character.toUpperCase(stringBuffer.charAt(0)));
        return prefix + stringBuffer.toString();
    }
    
    public static String getReturnStatement(String type) {
        String result = "";
        if ("void".equals(type)) {
            
        } else if ("boolean".equals(type)) {
            result = "\nreturn false;";
        } else if ("byte".equals(type)) {
            result = "\nreturn 0;";
        } else if ("char".equals(type)) {
            result ="\nreturn '0';";
        } else if ("double".equals(type)) {
            result ="\nreturn 0.0;";
        } else if ("float".equals(type)) {
            result ="\nreturn 0;";
        } else if ("int".equals(type)) {
            result ="\nreturn 0;";
        } else if ("long".equals(type)) {
            result ="\nreturn 0;";
        } else if ("short".equals(type)) {
            result ="\nreturn 0;";
        } else{
            result ="\nreturn null;";
        }
        return result;
    }

}
