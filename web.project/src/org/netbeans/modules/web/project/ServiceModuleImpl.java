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
package org.netbeans.modules.web.project;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import org.netbeans.modules.serviceapi.ServiceComponent;
import org.netbeans.modules.serviceapi.ServiceModule;

/**
 *
 * @author Nam Nguyen
 */
public class ServiceModuleImpl extends ServiceModule {
    private WebServicesSupportImpl wsSupport;
    private WebProject project;
    
    /** Creates a new instance of ServiceModuleImpl */
    public ServiceModuleImpl(WebProject webProject) {
        this.project = webProject;
        this.wsSupport = project.getLookup().lookup(WebServicesSupportImpl.class);
    }
    
    /**
     * @return name of the service module.
     */
    public String getName() {
        return project.getName();
    }

    /**
     * Returns service components contained in this module.
     */
    public Collection<ServiceComponent> getServiceComponents() {
        ArrayList<ServiceComponent> ret = new ArrayList<ServiceComponent>();
        Webservices wss = getWebservices();
        if (wss != null) {
            for (WebserviceDescription wsd : wss.getWebserviceDescription()) {
                String path = wsd.getWsdlFile();
                //the webservices.xml gives u the servlet-link or the ejb-link 
                //then you have to go to the EE DD and resolve the links
            }
        }
        return ret;
    }

    private Webservices getWebservices() {
        BaseBean bb = project.getWebModule().getDeploymentDescriptor(J2eeModule.WEBSERVICES_XML);
        if (bb instanceof Webservices) {
            return (Webservices) bb;
        }
        return null;
    }
    
    /**
     * Add service component.
     */
    
    public void addServiceComponent(ServiceComponent component) {
    }
    
    /**
     * Remove service component.
     */
    public void removeServiceComponent(ServiceComponent component) {
    }

    /**
     * @return the project if applicable (not applicable for service modules from appserver).
     */
    public Project getProject() {
	return project;
    }
}
