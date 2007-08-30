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
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EndpointGroupNode extends NamedBeanGroupNode {

    public EndpointGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, WebserviceEndpoint.PORT_COMPONENT_NAME, WebserviceEndpoint.class,
                NbBundle.getMessage(EndpointGroupNode.class, "LBL_EndpointGroupHeader"), // NOI18N
                ICON_BASE_ENDPOINT_NODE, version);
        
        setExpanded(false);
        enableAddAction(NbBundle.getMessage(EndpointGroupNode.class, "LBL_AddEndpoint")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new EndpointNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        WebserviceEndpoint [] endpoints = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            endpoints = ((Servlet) commonDD).getWebserviceEndpoint();
        } else if(commonDD instanceof Ejb) {
            endpoints = ((Ejb) commonDD).getWebserviceEndpoint();
        }
        return endpoints;
    }
    
    protected CommonDDBean addNewBean() {
        WebserviceEndpoint newWebserviceEndpoint = (WebserviceEndpoint) createBean();
        newWebserviceEndpoint.setPortComponentName("endpoint" + getNewBeanId()); // NOI18N
        return addBean(newWebserviceEndpoint);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        WebserviceEndpoint newWebserviceEndpoint = (WebserviceEndpoint) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            ((Servlet) commonDD).addWebserviceEndpoint(newWebserviceEndpoint);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addWebserviceEndpoint(newWebserviceEndpoint);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        WebserviceEndpoint ejbRef = (WebserviceEndpoint) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            ((Servlet) commonDD).removeWebserviceEndpoint(ejbRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeWebserviceEndpoint(ejbRef);
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
        return new PortComponentMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        WebserviceEndpoint newWebserviceEndpoint = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            newWebserviceEndpoint = ((Servlet) commonDD).newWebserviceEndpoint();
        } else if(commonDD instanceof Ejb) {
            newWebserviceEndpoint = ((Ejb) commonDD).newWebserviceEndpoint();
        }
        
        return newWebserviceEndpoint;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((WebserviceEndpoint) sunBean).getPortComponentName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((WebserviceEndpoint) sunBean).setPortComponentName(newName);
    }

    public String getSunBeanNameProperty() {
        return WebserviceEndpoint.PORT_COMPONENT_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.webservices.PortComponent) standardBean).getPortComponentName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_PORTCOMPONENT_NAME;
    }
}
