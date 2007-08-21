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
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ResourceRefGroupNode extends NamedBeanGroupNode {

    public ResourceRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ResourceRef.RES_REF_NAME, 
                NbBundle.getMessage(ResourceRefGroupNode.class, "LBL_ResourceRefGroupHeader"), // NOI18N
                ICON_BASE_RESOURCE_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ResourceRefGroupNode.class, "LBL_AddResourceRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new ResourceRefNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        ResourceRef [] resourceRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            resourceRefs = ((SunWebApp) commonDD).getResourceRef();
        } else if(commonDD instanceof Ejb) {
            resourceRefs = ((Ejb) commonDD).getResourceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            resourceRefs = ((SunApplicationClient) commonDD).getResourceRef();
        }
        return resourceRefs;
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
//            stdBeans = webApp.getResourceRef();
//        } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.ejb.Ejb) {
//            org.netbeans.modules.j2ee.dd.api.ejb.Ejb ejb = (org.netbeans.modules.j2ee.dd.api.ejb.Ejb) stdParentDD;
//            stdBeans = ejb.getResourceRef();
//        } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.client.AppClient) {
//            org.netbeans.modules.j2ee.dd.api.client.AppClient appClient = (org.netbeans.modules.j2ee.dd.api.client.AppClient) stdParentDD;
//            stdBeans = appClient.getResourceRef();
//        }
//        
//        return stdBeans != null ? stdBeans : new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [0];
//    }

    protected CommonDDBean addNewBean() {
        ResourceRef newResourceRef = (ResourceRef) createBean();
        newResourceRef.setResRefName("resource_ref" + getNewBeanId()); // NOI18N
        return addBean(newResourceRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        ResourceRef newResourceRef = (ResourceRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addResourceRef(newResourceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addResourceRef(newResourceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addResourceRef(newResourceRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ResourceRef resourceRef = (ResourceRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeResourceRef(resourceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeResourceRef(resourceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeResourceRef(resourceRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new ResourceRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        ResourceRef newResourceRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newResourceRef = ((SunWebApp) commonDD).newResourceRef();
        } else if(commonDD instanceof Ejb) {
            newResourceRef = ((Ejb) commonDD).newResourceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newResourceRef = ((SunApplicationClient) commonDD).newResourceRef();
        }
        
        return newResourceRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((ResourceRef) sunBean).getResRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((ResourceRef) sunBean).setResRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return ResourceRef.RES_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.ResourceRef) standardBean).getResRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_RES_REF_NAME;
    }
}
