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

import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class SessionMethodController extends AbstractMethodController {

    private final FileObject ejbClassFO;
    private final Session model;

    public SessionMethodController(FileObject ejbClassFO, Session model) {
        super(ejbClassFO, model);
        this.ejbClassFO = ejbClassFO;
        this.model = model;
    }

    public boolean hasJavaImplementation(MethodModel intfView) {
        return true;
    }

    public boolean hasJavaImplementation(MethodType methodType) {
        return true;
    }
    
    public MethodType getMethodTypeFromImpl(MethodModel implView) {
        MethodType methodType = null;
        if (implView.getName().startsWith("ejbCreate")) {
            methodType = new MethodType.CreateMethodType(implView);
        } else if (!implView.getName().startsWith("ejb")) {
            methodType = new MethodType.BusinessMethodType(implView);
        }
        return methodType;
    }

    public MethodType getMethodTypeFromInterface(MethodModel clientView) {
        assert clientView.getClassName() != null: "declaring class cannot be null";
        // see if the interface is home or local home, otherwise assume business
        String cName = clientView.getClassName();
        MethodType methodType = null;
        if (cName.equals(model.getLocalHome()) || 
            cName.equals(model.getHome())) {
            methodType = new MethodType.CreateMethodType(clientView);
        } else {
            methodType = new MethodType.BusinessMethodType(clientView);
        }
        return methodType;
    }

    public AbstractMethodController.GenerateFromImpl createGenerateFromImpl() {
        return new SessionGenerateFromImplVisitor();
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new SessionGenerateFromIntfVisitor();
    }

    public boolean supportsMethodType(MethodType.Kind methodType) {
        boolean stateless = Session.SESSION_TYPE_STATELESS.equals(model.getSessionType());
        boolean simplified = model.getRoot().getVersion().doubleValue() > 2.1;
        return  methodType == MethodType.Kind.BUSINESS || (!simplified && !stateless && (methodType == MethodType.Kind.CREATE));
    }
}