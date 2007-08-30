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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ServletGroupNode extends NamedBeanGroupNode {

    private SunWebApp sunWebApp;
    
    public ServletGroupNode(SectionNodeView sectionNodeView, SunWebApp sunWebApp, ASDDVersion version) {
        super(sectionNodeView, sunWebApp, Servlet.SERVLET_NAME, Servlet.class,
                NbBundle.getMessage(ServletGroupNode.class, "LBL_ServletGroupHeader"), // NOI18N
                ICON_BASE_SERVLET_NODE, version);
        
        this.sunWebApp = sunWebApp;
        enableAddAction(NbBundle.getMessage(ServletGroupNode.class, "LBL_AddServlet")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new ServletNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        return sunWebApp.getServlet();
    }
    
    protected CommonDDBean addNewBean() {
        Servlet newServlet = sunWebApp.newServlet();
        newServlet.setServletName("servlet" + getNewBeanId()); // NOI18N
        return addBean(newServlet);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        sunWebApp.addServlet((Servlet) newBean);
        return newBean;
    }

    protected void removeBean(CommonDDBean bean) {
        Servlet servlet = (Servlet) bean;
        sunWebApp.removeServlet(servlet);
    }
    
    // ------------------------------------------------------------------------
    // DescriptorReader implementation
    // ------------------------------------------------------------------------
    @Override
    public Map<String, Object> readDescriptor() {
        Map<String, Object> resultMap = null;
        
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = getStandardRootDD();
        if(stdRootDD instanceof org.netbeans.modules.j2ee.dd.api.web.WebApp) {
            org.netbeans.modules.j2ee.dd.api.web.WebApp webApp = (org.netbeans.modules.j2ee.dd.api.web.WebApp) stdRootDD;
            resultMap = ServletMetadataReader.readDescriptor(webApp);
        }
        
        return resultMap;
    }

    @Override
    public Map<String, Object> readAnnotations() {
        Map<String, Object> resultMap = null;
        
        try {
            MetadataModel<WebAppMetadata> webAppModel = getMetadataModel(WebAppMetadata.class);
            if(webAppModel != null) {
                resultMap = webAppModel.runReadAction(new ServletMetadataReader());
            }
        } catch (MetadataModelException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return resultMap;
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        return sunWebApp.newServlet();
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((Servlet) sunBean).getServletName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((Servlet) sunBean).setServletName(newName);
    }

    public String getSunBeanNameProperty() {
        return Servlet.SERVLET_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.web.Servlet) standardBean).getServletName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_SERVLET_NAME;
    }
    
}