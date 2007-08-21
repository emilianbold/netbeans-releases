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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice;

import java.util.Map;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class WebServiceGroupNode extends NamedBeanGroupNode {

    public WebServiceGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME, 
                NbBundle.getMessage(WebServiceGroupNode.class, "LBL_WebServiceGroupHeader"), // NOI18N
                ICON_BASE_SERVICE_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(WebServiceGroupNode.class, "LBL_AddWebService")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new WebServiceNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        WebserviceDescription [] webServiceDesc = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            webServiceDesc = ((SunWebApp) commonDD).getWebserviceDescription();
        } else if(commonDD instanceof SunEjbJar) {
            EnterpriseBeans eb = ((SunEjbJar) commonDD).getEnterpriseBeans();
            if(eb != null) {
                webServiceDesc = eb.getWebserviceDescription();
            }
        }
        
        return webServiceDesc;
    }

    protected CommonDDBean addNewBean() {
        WebserviceDescription newWebServiceDesc = (WebserviceDescription) createBean();
        newWebServiceDesc.setWebserviceDescriptionName("service" + getNewBeanId()); // NOI18N
        return addBean(newWebServiceDesc);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        WebserviceDescription newWebServiceDesc = (WebserviceDescription) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addWebserviceDescription(newWebServiceDesc);
        } else if(commonDD instanceof SunEjbJar) {
            SunEjbJar sunEjbJar = (SunEjbJar) commonDD;
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb == null) {
                eb = sunEjbJar.newEnterpriseBeans();
                sunEjbJar.setEnterpriseBeans(eb);
            }
            eb.addWebserviceDescription(newWebServiceDesc);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        WebserviceDescription webServiceDesc = (WebserviceDescription) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeWebserviceDescription(webServiceDesc);
        } else if(commonDD instanceof SunEjbJar) {
            EnterpriseBeans eb = ((SunEjbJar) commonDD).getEnterpriseBeans();
            if(eb != null) {
                eb.removeWebserviceDescription(webServiceDesc);
            }
            // TODO if eb is empty of all data now, we could remove it too.
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override
    public Map<String, Object> readDescriptor() {
        CommonBeanReader reader = getModelReader();
        return reader != null ? reader.readDescriptor(getWebServicesRootDD()) : null;
    }
    
    @Override 
    protected CommonBeanReader getModelReader() {
        return new WebServiceMetadataReader();
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    private volatile EnterpriseBeans ejbWebserviceDescFactory = null;
    
    public CommonDDBean createBean() {
        WebserviceDescription newWebServiceDesc = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newWebServiceDesc = ((SunWebApp) commonDD).newWebserviceDescription();
        } else if(commonDD instanceof SunEjbJar) {
            SunEjbJar sunEjbJar = (SunEjbJar) commonDD;
            if(ejbWebserviceDescFactory == null) {
                ejbWebserviceDescFactory = sunEjbJar.newEnterpriseBeans();
            }
            newWebServiceDesc = ejbWebserviceDescFactory.newWebserviceDescription();
        }
        
        return newWebServiceDesc;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((WebserviceDescription) sunBean).getWebserviceDescriptionName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((WebserviceDescription) sunBean).setWebserviceDescriptionName(newName);
    }

    public String getSunBeanNameProperty() {
        return WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription)
                standardBean).getWebserviceDescriptionName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_WEBSERVICE_DESC_NAME;
    }
}
