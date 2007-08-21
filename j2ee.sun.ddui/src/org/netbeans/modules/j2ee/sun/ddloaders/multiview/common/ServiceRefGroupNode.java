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
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
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

    protected SectionNode createNode(DDBinding binding) {
        return new ServiceRefNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        ServiceRef [] serviceRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            serviceRefs = ((SunWebApp) commonDD).getServiceRef();
        } else if(commonDD instanceof Ejb) {
            serviceRefs = ((Ejb) commonDD).getServiceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            serviceRefs = ((SunApplicationClient) commonDD).getServiceRef();
        }
        return serviceRefs;
    }

//    protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] getStandardBeansFromModel() {
//        org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] stdBeans = null;
//        org.netbeans.modules.j2ee.dd.api.common.CommonDDBean stdParentDD = null;
//        
//        // get binding from parent node if this is ejb...
//        Node parentNode = getParentNode();
//        if(parentNode instanceof NamedBeanNode) {
//            NamedBeanNode namedNode = (NamedBeanNode) parentNode;
//            DDBinding parentBinding = namedNode.getBinding();
//            stdParentDD = parentBinding.getStandardBean();
//        } else {
//            stdParentDD = getStandardRootDD();
//        }
//        
//        try {
//            if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.web.WebApp) {
//                org.netbeans.modules.j2ee.dd.api.web.WebApp webApp = (org.netbeans.modules.j2ee.dd.api.web.WebApp) stdParentDD;
//                stdBeans = webApp.getServiceRef();
//            } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.ejb.Ejb) {
//                org.netbeans.modules.j2ee.dd.api.ejb.Ejb ejb = (org.netbeans.modules.j2ee.dd.api.ejb.Ejb) stdParentDD;
//                stdBeans = ejb.getServiceRef();
//            } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.client.AppClient) {
//                org.netbeans.modules.j2ee.dd.api.client.AppClient appClient = (org.netbeans.modules.j2ee.dd.api.client.AppClient) stdParentDD;
//                stdBeans = appClient.getServiceRef();
//            }
//        } catch(org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//        
//        return stdBeans != null ? stdBeans : new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [0];
//    }

    protected CommonDDBean addNewBean() {
        ServiceRef newServiceRef = (ServiceRef) createBean();
        newServiceRef.setServiceRefName("service" + getNewBeanId()); // NOI18N
        return addBean(newServiceRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        ServiceRef newServiceRef = (ServiceRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addServiceRef(newServiceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addServiceRef(newServiceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addServiceRef(newServiceRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ServiceRef serviceRef = (ServiceRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeServiceRef(serviceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeServiceRef(serviceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeServiceRef(serviceRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new ServiceRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        ServiceRef newServiceRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newServiceRef = ((SunWebApp) commonDD).newServiceRef();
        } else if(commonDD instanceof Ejb) {
            newServiceRef = ((Ejb) commonDD).newServiceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newServiceRef = ((SunApplicationClient) commonDD).newServiceRef();
        }
        
        return newServiceRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((ServiceRef) sunBean).getServiceRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((ServiceRef) sunBean).setServiceRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return ServiceRef.SERVICE_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.ServiceRef) standardBean).getServiceRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_SERVICE_REF_NAME;
    }
}
