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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class SessionMethodController extends AbstractMethodController {

    private WorkingCopy workingCopy;
    private Session model;

    public SessionMethodController(WorkingCopy workingCopy, Session model) {
        super(workingCopy, model);
        this.workingCopy = workingCopy;
        this.model = model;
    }

    public boolean hasJavaImplementation(ExecutableElement intfView) {
        return true;
    }

    public boolean hasJavaImplementation(MethodType mt) {
        return true;
    }
    
    public MethodType getMethodTypeFromImpl(ExecutableElement implView) {
        MethodType mt = null;
        if (implView.getSimpleName().toString().startsWith("ejbCreate")) {
            mt = new MethodType.CreateMethodType(implView);
        } else if (!implView.getSimpleName().toString().startsWith("ejb")) {
            mt = new MethodType.BusinessMethodType(implView);
        }
        return mt;
    }

    public MethodType getMethodTypeFromInterface(ExecutableElement clientView) {
        assert clientView.getEnclosingElement() != null: "declaring class cannot be null";
        // see if the interface is home or local home, otherwise assume business
        String cName = ((TypeElement) clientView.getEnclosingElement()).getQualifiedName().toString();
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
        return new SessionGenerateFromImplVisitor(workingCopy);
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new SessionGenerateFromIntfVisitor(workingCopy);
    }

    public boolean supportsMethodType(int mt) {
        boolean stateless = Session.SESSION_TYPE_STATELESS.equals(model.getSessionType());
        boolean simplified = model.getRoot().getVersion().doubleValue() > 2.1;
        return  mt == MethodType.METHOD_TYPE_BUSINESS || (!simplified && !stateless && (mt == MethodType.METHOD_TYPE_CREATE));
    }
}