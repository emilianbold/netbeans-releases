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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
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
public class MessageDestinationGroupNode extends NamedBeanGroupNode {

    private final SunWebApp sunWebApp;
    private final SunEjbJar sunEjbJar;
    private final SunApplicationClient sunAppClient;
    
    public MessageDestinationGroupNode(SectionNodeView sectionNodeView, RootInterface rootDD, ASDDVersion version) {
        super(sectionNodeView, rootDD, MessageDestination.MESSAGE_DESTINATION_NAME, MessageDestination.class,
                NbBundle.getMessage(MessageDestinationGroupNode.class, "LBL_MessageDestinationGroupHeader"), // NOI18N
                ICON_BASE_MESSAGE_DESTINATION_NODE, version);
        
        sunWebApp = (commonDD instanceof SunWebApp) ? (SunWebApp) commonDD : null;
        sunEjbJar = (commonDD instanceof SunEjbJar) ? (SunEjbJar) commonDD : null;
        sunAppClient = (commonDD instanceof SunApplicationClient) ? (SunApplicationClient) commonDD : null;
        
        enableAddAction(NbBundle.getMessage(MessageDestinationGroupNode.class, "LBL_AddMessageDestination")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new MessageDestinationNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        MessageDestination [] destinations = null;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            destinations = sunWebApp.getMessageDestination();
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            destinations = (eb != null) ? eb.getMessageDestination() : null;
        } else if(sunWebApp != null) {
            destinations = sunAppClient.getMessageDestination();
        }
        return destinations;
    }

    protected CommonDDBean addNewBean() {
        MessageDestination newMsgDest = (MessageDestination) createBean();
        newMsgDest.setMessageDestinationName("destination" + getNewBeanId()); // NOI18N
        return addBean(newMsgDest);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        MessageDestination newMsgDest = (MessageDestination) newBean;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            sunWebApp.addMessageDestination(newMsgDest);
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb == null) {
                eb = sunEjbJar.newEnterpriseBeans();
                sunEjbJar.setEnterpriseBeans(eb);
            }
            eb.addMessageDestination(newMsgDest);
        } else if(sunAppClient != null) {
            sunAppClient.addMessageDestination(newMsgDest);
        }
        
        return newMsgDest;
    }
    
    protected void removeBean(CommonDDBean bean) {
        MessageDestination msgDest = (MessageDestination) bean;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            sunWebApp.removeMessageDestination(msgDest);
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb != null) {
                eb.removeMessageDestination(msgDest);
                if(eb.isTrivial(null)) {
                    sunEjbJar.setEnterpriseBeans(null);
                }
            }
        } else if(sunAppClient != null) {
            sunAppClient.removeMessageDestination(msgDest);
        }
    }
    
    /** MessageDestinationGroupNode gets events from <EnterpriseBeans> when in 
     *  sun-ejb-jar so we need custom event source matching.
     */
    @Override
    protected boolean isEventSource(Object source) {
        if(source != null && (
                sunEjbJar != null && source == sunEjbJar.getEnterpriseBeans() ||
                super.isEventSource(source))
                ) {
            return true;
        }
        return false;
        
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new MessageDestinationMetadataReader();
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    private volatile EnterpriseBeans ejbJarMesgDestFactory = null;
    
    public CommonDDBean createBean() {
        MessageDestination newMsgDest = null;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            newMsgDest = sunWebApp.newMessageDestination();
        } else if(sunEjbJar != null) {
            if(ejbJarMesgDestFactory == null) {
                ejbJarMesgDestFactory = sunEjbJar.newEnterpriseBeans();
            }
            newMsgDest = ejbJarMesgDestFactory.newMessageDestination();
        } else if(sunAppClient != null) {
            newMsgDest = sunAppClient.newMessageDestination();
        }
        
        return newMsgDest;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((MessageDestination) sunBean).getMessageDestinationName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((MessageDestination) sunBean).setMessageDestinationName(newName);
    }

    public String getSunBeanNameProperty() {
        return MessageDestination.MESSAGE_DESTINATION_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.MessageDestination) standardBean).getMessageDestinationName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_MSGDEST_NAME;
    }
}
