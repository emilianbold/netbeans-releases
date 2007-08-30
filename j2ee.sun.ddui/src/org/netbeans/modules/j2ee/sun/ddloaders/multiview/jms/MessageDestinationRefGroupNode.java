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
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class MessageDestinationRefGroupNode extends NamedBeanGroupNode {

    public MessageDestinationRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME, MessageDestinationRef.class, 
                NbBundle.getMessage(MessageDestinationRefGroupNode.class, "LBL_MessageDestinationRefGroupHeader"), // NOI18N
                ICON_BASE_MESSAGE_DESTINATION_NODE, version);
        
        enableAddAction(NbBundle.getMessage(MessageDestinationRefGroupNode.class, "LBL_AddMessageDestinationRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new MessageDestinationRefNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        MessageDestinationRef [] destinationRefs = null;
        
        try {
            // TODO find a better way to do this for common beans.
            if(commonDD instanceof SunWebApp) {
                destinationRefs = ((SunWebApp) commonDD).getMessageDestinationRef();
            } else if(commonDD instanceof Ejb) {
                destinationRefs = ((Ejb) commonDD).getMessageDestinationRef();
            } else if(commonDD instanceof SunApplicationClient) {
                destinationRefs = ((SunApplicationClient) commonDD).getMessageDestinationRef();
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return destinationRefs;
    }

    protected CommonDDBean addNewBean() {
        MessageDestinationRef newMsgDestRef = (MessageDestinationRef) createBean();
        newMsgDestRef.setMessageDestinationRefName("destinationRef" + getNewBeanId()); // NOI18N
        return addBean(newMsgDestRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        MessageDestinationRef newMsgDestRef = (MessageDestinationRef) newBean;

        try {
            // TODO find a better way to do this for common beans.
            if(commonDD instanceof SunWebApp) {
                ((SunWebApp) commonDD).addMessageDestinationRef(newMsgDestRef);
            } else if(commonDD instanceof Ejb) {
                ((Ejb) commonDD).addMessageDestinationRef(newMsgDestRef);
            } else if(commonDD instanceof SunApplicationClient) {
                ((SunApplicationClient) commonDD).addMessageDestinationRef(newMsgDestRef);
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return newMsgDestRef;
    }
    
    protected void removeBean(CommonDDBean bean) {
        MessageDestinationRef msgDestRef = (MessageDestinationRef) bean;
        
        try {
            // TODO find a better way to do this for common beans.
            if(commonDD instanceof SunWebApp) {
                ((SunWebApp) commonDD).removeMessageDestinationRef(msgDestRef);
            } else if(commonDD instanceof Ejb) {
                ((Ejb) commonDD).removeMessageDestinationRef(msgDestRef);
            } else if(commonDD instanceof SunApplicationClient) {
                ((SunApplicationClient) commonDD).removeMessageDestinationRef(msgDestRef);
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new MessageDestinationRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        MessageDestinationRef newMsgDestRef = null;
        
        try {
            // TODO find a better way to do this for common beans.
            if(commonDD instanceof SunWebApp) {
                newMsgDestRef = ((SunWebApp) commonDD).newMessageDestinationRef();
            } else if(commonDD instanceof Ejb) {
                newMsgDestRef = ((Ejb) commonDD).newMessageDestinationRef();
            } else if(commonDD instanceof SunApplicationClient) {
                newMsgDestRef = ((SunApplicationClient) commonDD).newMessageDestinationRef();
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return newMsgDestRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((MessageDestinationRef) sunBean).getMessageDestinationRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((MessageDestinationRef) sunBean).setMessageDestinationRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) standardBean).getMessageDestinationRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_MSGDEST_REF_NAME;
    }
}
