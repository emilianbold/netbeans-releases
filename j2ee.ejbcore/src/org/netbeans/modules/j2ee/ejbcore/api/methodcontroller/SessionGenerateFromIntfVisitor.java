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

import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
final class SessionGenerateFromIntfVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromIntf {

    private MethodModel implMethod;
    private static final String TODO = "//TODO implement "; //NOI18N
    
    public void getInterfaceMethodFromImpl(MethodType methodType) {
        methodType.accept(this);
    }
    
    public MethodModel getImplMethod() {
        return implMethod;
    }
    
    public MethodModel getSecondaryMethod() {
        return null;
    }
    
    public void visit(BusinessMethodType bmt) {
        implMethod = bmt.getMethodElement();
        String body = TODO + implMethod.getName() + EntityGenerateFromIntfVisitor.getReturnStatement(implMethod.getReturnType());
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
        String newName = prependAndUpper(origName, "ejb"); //NOI18N
        String body = TODO + newName;
        implMethod = MethodModel.create(
                newName, 
                "void",
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(HomeMethodType hmt) {
        assert false: "session beans do not have home methods";
    }
    
    public void visit(FinderMethodType fmt) {
        assert false: "session beans do not have finder methods";
    }
    
    private String prependAndUpper(String fullName, String prefix) {
         StringBuffer buffer = new StringBuffer(fullName);
         buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
         return prefix+buffer.toString();
    }
}
