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

    public MessageDestinationGroupNode(SectionNodeView sectionNodeView, RootInterface rootDD, ASDDVersion version) {
        super(sectionNodeView, rootDD, MessageDestination.MESSAGE_DESTINATION_NAME, 
                NbBundle.getMessage(MessageDestinationGroupNode.class, "LBL_MessageDestinationGroupHeader"), // NOI18N
                ICON_BASE_MESSAGE_DESTINATION_NODE, version);
        
        enableAddAction(NbBundle.getMessage(MessageDestinationGroupNode.class, "LBL_AddMessageDestination")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new MessageDestinationNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        MessageDestination [] destinations = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            destinations = ((SunWebApp) commonDD).getMessageDestination();
        } else if(commonDD instanceof SunEjbJar) {
            EnterpriseBeans eb = ((SunEjbJar) commonDD).getEnterpriseBeans();
            destinations = (eb != null) ? eb.getMessageDestination() : null;
        } else if(commonDD instanceof SunApplicationClient) {
            destinations = ((SunApplicationClient) commonDD).getMessageDestination();
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
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addMessageDestination(newMsgDest);
        } else if(commonDD instanceof SunEjbJar) {
            SunEjbJar sunEjbJar = ((SunEjbJar) commonDD);
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb == null) {
                eb = sunEjbJar.newEnterpriseBeans();
                sunEjbJar.setEnterpriseBeans(eb);
            }
            eb.addMessageDestination(newMsgDest);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addMessageDestination(newMsgDest);
        }
        
        return newMsgDest;
    }
    
    protected void removeBean(CommonDDBean bean) {
        MessageDestination msgDest = (MessageDestination) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeMessageDestination(msgDest);
        } else if(commonDD instanceof SunEjbJar) {
            SunEjbJar sunEjbJar = ((SunEjbJar) commonDD);
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb != null) {
                eb.removeMessageDestination(msgDest);
                // TODO if eb is empty of all data now, we could remove it too.
            }
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeMessageDestination(msgDest);
        }
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
        if(commonDD instanceof SunWebApp) {
            newMsgDest = ((SunWebApp) commonDD).newMessageDestination();
        } else if(commonDD instanceof SunEjbJar) {
            if(ejbJarMesgDestFactory == null) {
                ejbJarMesgDestFactory = ((SunEjbJar) commonDD).newEnterpriseBeans();
            }
            newMsgDest = ejbJarMesgDestFactory.newMessageDestination();
        } else if(commonDD instanceof SunApplicationClient) {
            newMsgDest = ((SunApplicationClient) commonDD).newMessageDestination();
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
