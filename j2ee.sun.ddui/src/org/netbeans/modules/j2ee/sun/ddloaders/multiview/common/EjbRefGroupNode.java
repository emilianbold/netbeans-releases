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
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EjbRefGroupNode extends NamedBeanGroupNode {

    public EjbRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, EjbRef.EJB_REF_NAME, 
                NbBundle.getMessage(EjbRefGroupNode.class, "LBL_EjbRefGroupHeader"), // NOI18N
                ICON_BASE_EJB_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(EjbRefGroupNode.class, "LBL_AddEjbRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new EjbRefNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        EjbRef [] ejbRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ejbRefs = ((SunWebApp) commonDD).getEjbRef();
        } else if(commonDD instanceof Ejb) {
            ejbRefs = ((Ejb) commonDD).getEjbRef();
        } else if(commonDD instanceof SunApplicationClient) {
            ejbRefs = ((SunApplicationClient) commonDD).getEjbRef();
        }
        return ejbRefs;
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
//            stdBeans = webApp.getEjbRef();
//        } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.ejb.Ejb) {
//            org.netbeans.modules.j2ee.dd.api.ejb.Ejb ejb = (org.netbeans.modules.j2ee.dd.api.ejb.Ejb) stdParentDD;
//            stdBeans = ejb.getEjbRef();
//        } else if(stdParentDD instanceof org.netbeans.modules.j2ee.dd.api.client.AppClient) {
//            org.netbeans.modules.j2ee.dd.api.client.AppClient appClient = (org.netbeans.modules.j2ee.dd.api.client.AppClient) stdParentDD;
//            stdBeans = appClient.getEjbRef();
//        }
//        
//        return stdBeans != null ? stdBeans : new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [0];
//    }

    protected CommonDDBean addNewBean() {
        EjbRef newEjbRef = (EjbRef) createBean();
        newEjbRef.setEjbRefName("ejb_ref" + getNewBeanId()); // NOI18N
        return addBean(newEjbRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        EjbRef newEjbRef = (EjbRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addEjbRef(newEjbRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addEjbRef(newEjbRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addEjbRef(newEjbRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        EjbRef ejbRef = (EjbRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeEjbRef(ejbRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeEjbRef(ejbRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeEjbRef(ejbRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new EjbRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        EjbRef newEjbRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newEjbRef = ((SunWebApp) commonDD).newEjbRef();
        } else if(commonDD instanceof Ejb) {
            newEjbRef = ((Ejb) commonDD).newEjbRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newEjbRef = ((SunApplicationClient) commonDD).newEjbRef();
        }
        
        return newEjbRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((EjbRef) sunBean).getEjbRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((EjbRef) sunBean).setEjbRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return EjbRef.EJB_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.EjbRef) standardBean).getEjbRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_EJB_REF_NAME;
    }
}
