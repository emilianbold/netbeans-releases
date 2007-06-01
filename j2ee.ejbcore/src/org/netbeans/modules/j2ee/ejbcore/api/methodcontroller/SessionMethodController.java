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
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.ErrorManager;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class SessionMethodController extends AbstractMethodController {

    private final MetadataModel<EjbJarMetadata> model;
    private final String sessionType;

    public SessionMethodController(final String ejbClass, MetadataModel<EjbJarMetadata> model) {
        super(ejbClass, model);
        this.model = model;
        String resultSessionType = null;
        try {
            resultSessionType = model.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws Exception {
                    Session session = (Session) metadata.findByEjbClass(ejbClass);
                    if (session != null) {
                        return session.getSessionType();
                    }
                    return null;
                }
            });
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        this.sessionType = resultSessionType;
    }

    @Override
    public boolean hasJavaImplementation(MethodModel intfView) {
        return true;
    }

    @Override
    public boolean hasJavaImplementation(MethodType methodType) {
        return true;
    }
    
    @Override
    public MethodType getMethodTypeFromImpl(MethodModel implView) {
        MethodType methodType = null;
        if (implView.getName().startsWith("ejbCreate")) {
            methodType = new MethodType.CreateMethodType(implView);
        } else if (!implView.getName().startsWith("ejb")) {
            methodType = new MethodType.BusinessMethodType(implView);
        }
        return methodType;
    }

    @Override
    public MethodType getMethodTypeFromInterface(MethodModel clientView) {
        // see if the interface is home or local home, otherwise assume business
        String localHome = getLocalHome();
        String home = getHome();
        if ((localHome != null && findInClass(localHome, clientView)) || (home != null && findInClass(home, clientView))) {
            return new MethodType.CreateMethodType(clientView);
        } else {
            return new MethodType.BusinessMethodType(clientView);
        }
    }

    public AbstractMethodController.GenerateFromImpl createGenerateFromImpl() {
        return new SessionGenerateFromImplVisitor();
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new SessionGenerateFromIntfVisitor();
    }

    @Override
    public boolean supportsMethodType(MethodType.Kind methodType) {
        boolean stateless = Session.SESSION_TYPE_STATELESS.equals(sessionType);
        return  methodType == MethodType.Kind.BUSINESS || (!isSimplified() && !stateless && (methodType == MethodType.Kind.CREATE));
    }
}