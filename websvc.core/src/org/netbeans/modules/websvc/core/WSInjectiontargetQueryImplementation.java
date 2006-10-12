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

package org.netbeans.modules.websvc.core;

import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class WSInjectiontargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    /** Creates a new instance of WSInjectiontargetQueryImplementation */
    public WSInjectiontargetQueryImplementation() {
    }

    public boolean isInjectionTarget(JavaClass jc) {
        if (jc == null) {
            throw new NullPointerException("Passed null to WSInjectiontargetQueryImplementation.isInjectionTarget(JavaClass)"); // NOI18N
        }
        FileObject fo = JavaModel.getFileObject(jc.getResource());
        Project project = FileOwnerQuery.getOwner(fo);
        if (Util.isJavaEE5orHigher(project)) {
            JAXWSSupport jaxwss = JAXWSSupport.getJAXWSSupport(fo);
            if (jaxwss != null) {
            List services = jaxwss.getServices();
                for (Iterator it = services.iterator(); it.hasNext();) {
                    Service service = (Service) it.next();
                    if (jc.getName().equals(service.getImplementationClass())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isStaticReferenceRequired(JavaClass jc) {
        return false;
    }
    
}
