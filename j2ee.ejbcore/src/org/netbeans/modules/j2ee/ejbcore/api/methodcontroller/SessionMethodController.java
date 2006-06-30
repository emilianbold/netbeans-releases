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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;


/**
 *
 * @author Chris Webster
 */
public class SessionMethodController extends AbstractMethodController {
    private Session model;
    private ClassPath cp;

    public SessionMethodController(Session model, ClassPath cp) {
        super(model,cp);
        this.model = model;
        this.cp = cp;
    }

    public boolean hasJavaImplementation(Method intfView) {
        return true;
    }

    public boolean hasJavaImplementation(MethodType mt) {
        return true;
    }
    
    public MethodType getMethodTypeFromImpl(Method implView) {
        MethodType mt = null;
        if (implView.getName().startsWith("ejbCreate")) {
            mt = new MethodType.CreateMethodType(implView);
        } else if (!implView.getName().startsWith("ejb")) {
            mt = new MethodType.BusinessMethodType(implView);
        }
        return mt;
    }

    public MethodType getMethodTypeFromInterface(Method clientView) {
        assert clientView.getDeclaringClass() != null: "declaring class cannot be null";
        // see if the interface is home or local home, otherwise assume business
        String cName = clientView.getDeclaringClass().getName();
        MethodType mt = null;
        if (cName.equals(model.getLocalHome()) || 
            cName.equals(model.getHome())) {
            mt = new MethodType.CreateMethodType(clientView);
        } else {
            mt = new MethodType.BusinessMethodType(clientView);
        }
        return mt;
    }

    public AbstractMethodController.GenerateFromImpl createGenerateFromImpl() {
        return new SessionGenerateFromImplVisitor();
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new SessionGenerateFromIntfVisitor();
    }

    public boolean supportsMethodType(int mt) {
        boolean stateless = 
                Session.SESSION_TYPE_STATELESS.equals(model.getSessionType());
        return  mt == MethodType.METHOD_TYPE_BUSINESS ||
                (!stateless && (mt == MethodType.METHOD_TYPE_CREATE));
    }
}