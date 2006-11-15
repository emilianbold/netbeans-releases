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

package org.netbeans.modules.j2ee.ejbcore;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.openide.ErrorManager;

/**
 *
 * @author Martin Adamek
 */
public class EjbInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public EjbInjectionTargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar apiEjbJar = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(controller.getFileObject());
        String fqn = typeElement.getQualifiedName().toString();
        if (apiEjbJar != null && 
                !apiEjbJar.getJ2eePlatformVersion().equals("1.3") && 
                !apiEjbJar.getJ2eePlatformVersion().equals("1.4")) {
            try {
                EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(apiEjbJar.getMetadataUnit());
                if (ejbJar != null && ejbJar.getEnterpriseBeans() != null) {
                    if (ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.SESSION, Session.EJB_CLASS, fqn) != null) {
                        return true;
                    }
                    if (ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.MESSAGE_DRIVEN, MessageDriven.EJB_CLASS, fqn) != null) {
                        return true;
                    }
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return false;
    }

    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
    
}
