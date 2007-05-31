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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.WebServiceNotifier;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 *
 * @author mkuchtiak
 */
public class ProjectWebServiceNotifier implements WebServiceNotifier {
    private static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    
    private Project proj;
    public ProjectWebServiceNotifier(Project proj) {
        this.proj=proj;
    }

    /** Notifies that web service was added */
    public void serviceAdded(String serviceName, String implementationClass) {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(proj.getProjectDirectory());
        if (jaxWsSupport!=null) jaxWsSupport.addService(serviceName, implementationClass, isJsr109Supported());
    }

    /** Notifies that web service was removed */
    public void serviceRemoved(String serviceName) {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(proj.getProjectDirectory());
        if (jaxWsSupport!=null) jaxWsSupport.serviceFromJavaRemoved(serviceName);
    }

    private boolean isJsr109Supported() {
        boolean jsr109Supported = true;
        EditableProperties projectProperties = null;
        try {
            projectProperties = WSUtils.getEditableProperties(proj, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        } catch (IOException ex) {
            
        }
        if (projectProperties!=null) {
            String serverInstance = projectProperties.getProperty(J2EE_SERVER_INSTANCE);
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    jsr109Supported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
                }
            }
        }
        return jsr109Supported;
    }

}
