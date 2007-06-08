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
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class PortInfoGroupNode extends NamedBeanGroupNode {

    private ServiceRef serviceRef;
    
    public PortInfoGroupNode(SectionNodeView sectionNodeView, ServiceRef serviceRef, ASDDVersion version) {
        super(sectionNodeView, serviceRef, ServiceRef.SERVICE_REF_NAME, 
                NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_PortInfoGroupHeader"), // NOI18N
                ICON_BASE_PORT_INFO_NODE, version);
        
        this.serviceRef = serviceRef;
        enableAddAction(NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_AddPortInfo")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new PortInfoNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        return serviceRef.getPortInfo();
    }

    protected CommonDDBean addNewBean() {
        PortInfo portInfo = serviceRef.newPortInfo();
        serviceRef.addPortInfo(portInfo);
        return portInfo;
    }    
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        serviceRef.addPortInfo((PortInfo) newBean);
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        PortInfo portInfo = (PortInfo) bean;
        serviceRef.removePortInfo(portInfo);
    }
    
    protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] getStandardBeansFromModel() {
        org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] stdBeans = null;
//        !PW FIXME are we going to bind port-info to anything?  wsdl-port from standard descriptor?
//        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = getStandardRootDD();
//        if(stdRootDD instanceof org.netbeans.modules.j2ee.dd.api.web.WebApp) {
//            org.netbeans.modules.j2ee.dd.api.web.WebApp webApp = (org.netbeans.modules.j2ee.dd.api.web.WebApp) stdRootDD;
//            stdBeans = webApp.getServlet();
//        }
        return stdBeans != null ? stdBeans : new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [0];
    }

//    protected CommonDDBean addNewBean() {
//        PortInfo newPortInfo = (PortInfo) createBean();
//        newPortInfo.setPortInfoName("service" + getNewBeanId()); // NOI18N
//        return addBean(newPortInfo);
//    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        return serviceRef.newPortInfo();
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return PortInfoNode.generateTitle((PortInfo) sunBean);
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        throw new UnsupportedOperationException("Can't set name of port-info this way");
    }

    public String getSunBeanNameProperty() {
        throw new UnsupportedOperationException("No name property for port-info");
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        throw new UnsupportedOperationException("No binding for port-info (yet)");
    }

    public String getStandardBeanNameProperty() {
        throw new UnsupportedOperationException("No name property for bound port-info (yet?)");
    }
}
