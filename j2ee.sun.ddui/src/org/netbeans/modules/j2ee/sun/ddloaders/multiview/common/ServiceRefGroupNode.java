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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ServiceRefGroupNode extends NamedBeanGroupNode {

    public ServiceRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ServiceRef.SERVICE_REF_NAME, 
                NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_ServiceRefGroupHeader"), // NOI18N
                ICON_BASE_SERVICE_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_AddServiceRef")); // NOI18N
    }

    protected SectionNode createNode(CommonDDBean bean) {
        return new ServiceRefNode(getSectionNodeView(), (ServiceRef) bean, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        ServiceRef [] serviceRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            serviceRefs = ((SunWebApp) commonDD).getServiceRef();
//        } else if(rootDD instanceof SunEjbJar) {
//            EnterpriseBeans eb = ((SunEjbJar) rootDD).getEnterpriseBeans();
//            if(eb != null) {
//                Ejb [] ejbs = eb.getEjb();
//                if(ejbs != null && ejbs.length > 0) {
//                    serviceRefs = getServiceRefs(ejbs);
//                }
//            }
        } else if(commonDD instanceof SunApplicationClient) {
            serviceRefs = ((SunApplicationClient) commonDD).getServiceRef();
        }
        return serviceRefs;
    }

//    private ServiceRef [] getServiceRefs(Ejb [] ejbs) {
//        List<ServiceRef> serviceRefs = new ArrayList<ServiceRef>(5);
//        for(int i = 0; i < ejbs.length; i++) {
//            ServiceRef [] refs = ejbs[i].getServiceRef();
//            if(refs != null && refs.length > 0) {
//                for(int j = 0; j < refs.length; j++) {
//                    serviceRefs.add(refs[j]);
//                }
//            }
//        }
//        return serviceRefs.toArray(new ServiceRef[serviceRefs.size()]);
//    }

    protected CommonDDBean addNewBean() {
        ServiceRef newServiceRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            newServiceRef = sunWebApp.newServiceRef();
            sunWebApp.addServiceRef(newServiceRef);
//        } else if(rootDD instanceof SunEjbJar) {
//            EnterpriseBeans eb = ((SunEjbJar) rootDD).getEnterpriseBeans();
//            if(eb != null) {
//                Ejb [] ejbs = eb.getEjb();
//                if(ejbs != null && ejbs.length > 0) {
//                    serviceRefs = getServiceRefs(ejbs);
//                }
//            }
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            newServiceRef = sunAppClient.newServiceRef();
            sunAppClient.addServiceRef(newServiceRef);
        }
        
        newServiceRef.setServiceRefName("service" + getNewBeanId()); // NOI18N
        
        return newServiceRef;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ServiceRef serviceRef = (ServiceRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            SunWebApp sunWebApp = (SunWebApp) commonDD;
            sunWebApp.removeServiceRef(serviceRef);
//        } else if(rootDD instanceof SunEjbJar) {
//            EnterpriseBeans eb = ((SunEjbJar) rootDD).getEnterpriseBeans();
//            if(eb != null) {
//                Ejb [] ejbs = eb.getEjb();
//                if(ejbs != null && ejbs.length > 0) {
//                    serviceRefs = getServiceRefs(ejbs);
//                }
//            }
        } else if(commonDD instanceof SunApplicationClient) {
            SunApplicationClient sunAppClient = (SunApplicationClient) commonDD;
            sunAppClient.removeServiceRef(serviceRef);
        }
    }
    
}
