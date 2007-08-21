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
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ResourceEnvRefGroupNode extends NamedBeanGroupNode {

    public ResourceEnvRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ResourceEnvRef.RESOURCE_ENV_REF_NAME, 
                NbBundle.getMessage(ResourceEnvRefGroupNode.class, "LBL_ResourceEnvRefGroupHeader"), // NOI18N
                ICON_BASE_RESOURCE_ENV_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ResourceEnvRefGroupNode.class, "LBL_AddResourceEnvRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new ResourceEnvRefNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        ResourceEnvRef [] resourceEnvRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            resourceEnvRefs = ((SunWebApp) commonDD).getResourceEnvRef();
        } else if(commonDD instanceof Ejb) {
            resourceEnvRefs = ((Ejb) commonDD).getResourceEnvRef();
        } else if(commonDD instanceof SunApplicationClient) {
            resourceEnvRefs = ((SunApplicationClient) commonDD).getResourceEnvRef();
        }
        return resourceEnvRefs;
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
//        if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.web.WebApp) {
//            org.netbeans.modules.j2ee.dd.api.web.WebApp webApp = (org.netbeans.modules.j2ee.dd.api.web.WebApp) stdParentDD;
//            stdBeans = webApp.getResourceEnvRef();
//        } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.ejb.Ejb) {
//            org.netbeans.modules.j2ee.dd.api.ejb.Ejb ejb = (org.netbeans.modules.j2ee.dd.api.ejb.Ejb) stdParentDD;
//            stdBeans = ejb.getResourceEnvRef();
//        } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.client.AppClient) {
//            org.netbeans.modules.j2ee.dd.api.client.AppClient appClient = (org.netbeans.modules.j2ee.dd.api.client.AppClient) stdParentDD;
//            stdBeans = appClient.getResourceEnvRef();
//        }
//        
//        return stdBeans != null ? stdBeans : new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [0];
//    }

    protected CommonDDBean addNewBean() {
        ResourceEnvRef newResourceEnvRef = (ResourceEnvRef) createBean();
        newResourceEnvRef.setResourceEnvRefName("resource_env_ref" + getNewBeanId()); // NOI18N
        return addBean(newResourceEnvRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        ResourceEnvRef newResourceEnvRef = (ResourceEnvRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addResourceEnvRef(newResourceEnvRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addResourceEnvRef(newResourceEnvRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addResourceEnvRef(newResourceEnvRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ResourceEnvRef resourceEnvRef = (ResourceEnvRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeResourceEnvRef(resourceEnvRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeResourceEnvRef(resourceEnvRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeResourceEnvRef(resourceEnvRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new ResourceEnvRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        ResourceEnvRef newResourceEnvRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newResourceEnvRef = ((SunWebApp) commonDD).newResourceEnvRef();
        } else if(commonDD instanceof Ejb) {
            newResourceEnvRef = ((Ejb) commonDD).newResourceEnvRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newResourceEnvRef = ((SunApplicationClient) commonDD).newResourceEnvRef();
        }
        
        return newResourceEnvRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((ResourceEnvRef) sunBean).getResourceEnvRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((ResourceEnvRef) sunBean).setResourceEnvRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return ResourceEnvRef.RESOURCE_ENV_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef) standardBean).getResourceEnvRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_RESOURCE_ENV_REF_NAME;
    }
}
