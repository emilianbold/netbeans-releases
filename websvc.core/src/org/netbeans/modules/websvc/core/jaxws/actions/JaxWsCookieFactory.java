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

package org.netbeans.modules.websvc.core.jaxws.actions;

import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Resource;
import  org.netbeans.modules.j2ee.dd.api.webservices.DDProvider;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.openide.loaders.*;

public class JaxWsCookieFactory {

    private JaxWsCookieFactory() {}
	
    public static JaxWsClassesCookie getJaxWsClassesCookie(Service service, JavaClass ce) {
        if (ce == null) return null;
        Resource r = ce.getResource();
        FileObject f = JavaModel.getFileObject(r);
        if (f != null) {
            return new JaxWsClassesCookieImpl(service,f);
        }
        return null;
    }


}
