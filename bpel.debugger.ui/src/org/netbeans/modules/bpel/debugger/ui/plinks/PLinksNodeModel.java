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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.plinks;

import org.netbeans.modules.bpel.debugger.ui.plinks.models.EndpointWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.PartnerLinkWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.RoleRefWrapper;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.w3c.dom.Node;

/**
 *
 * @author Kirill Sorokin
 */
public class PLinksNodeModel implements NodeModel, Constants {
    
    public static final String ICONS_ROOT = 
            "org/netbeans/modules/bpel/debugger/ui/" + // NOI18N
            "resources/image/plinks/"; // NOI18N
    
    public static final String PARTNER_LINK_ICON =
            ICONS_ROOT + "PARTNER_LINK"; // NOI18N
    
    public static final String MY_ROLE_ICON =
            ICONS_ROOT + "MY_ROLE"; // NOI18N
    
    public static final String PARTNER_ROLE_ICON =
            ICONS_ROOT + "PARTNER_ROLE"; // NOI18N
    
    public static final String ENDPOINT_ICON =
            ICONS_ROOT + "ENDPOINT"; // NOI18N
    
    public static final String DEFAULT_NODE_ICON =
            ICONS_ROOT + "DEFAULT_NODE"; // NOI18N
    
    public static final String ELEMENT_NODE_ICON =
            ICONS_ROOT + "ELEMENT_NODE"; // NOI18N
            
    public static final String ATTRIBUTE_NODE_ICON =
            ICONS_ROOT + "ATTRIBUTE_NODE"; // NOI18N
    
    public static final String TEXT_NODE_ICON =
            ICONS_ROOT + "TEXT_NODE"; // NOI18N
    
    public static final String CDATA_NODE_ICON =
            ICONS_ROOT + "CDATA_NODE"; // NOI18N
    
    public PLinksNodeModel() {
        // does nothing
    }
    
    /**{@inheritDoc}*/
    public String getDisplayName(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                    PLinksNodeModel.class, 
                    "CTL_Column_Name"); // NOI18N
        }
        
        if (object instanceof PLinksTreeModel.Dummy) {
            return NbBundle.getMessage(
                    PLinksNodeModel.class, 
                    "CTL_Empty_Model"); // NOI18N
        }
        
        if (object instanceof PartnerLinkWrapper) {
            return ((PartnerLinkWrapper) object).getName();
        }
        
        if (object instanceof RoleRefWrapper) {
            final RoleRefWrapper rWrapper = (RoleRefWrapper) object;
            
            if (rWrapper.getType() == RoleRefWrapper.RoleType.MY) {
                return NbBundle.getMessage(
                        PLinksNodeModel.class, "CTL_MyRole"); // NOI18N
            }
            
            if (rWrapper.getType() == RoleRefWrapper.RoleType.PARTNER) {
                return NbBundle.getMessage(
                        PLinksNodeModel.class, "CTL_PartnerRole"); // NOI18N
            }
        }
        
        if (object instanceof EndpointWrapper) {
            return NbBundle.getMessage(
                        PLinksNodeModel.class, "CTL_Endpoint"); // NOI18N
        }
        
        if (object instanceof Node) {
            return ((Node) object).getNodeName();
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public String getShortDescription(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                PLinksNodeModel.class, 
                "CTL_Column_Name_Tooltip"); // NOI18N
        }
        
        return getDisplayName(object);
    }
    
    /**{@inheritDoc}*/
    public String getIconBase(
            final Object object) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return ""; // NOI18N
        }
        
        if (object instanceof PLinksTreeModel.Dummy) {
            return null;
        }
        
        if (object instanceof PartnerLinkWrapper) {
            return PARTNER_LINK_ICON;
        }
        
        if (object instanceof RoleRefWrapper) {
            final RoleRefWrapper rWrapper = (RoleRefWrapper) object;
            
            if (rWrapper.getType() == RoleRefWrapper.RoleType.MY) {
                return MY_ROLE_ICON;
            }
            
            if (rWrapper.getType() == RoleRefWrapper.RoleType.PARTNER) {
                return PARTNER_ROLE_ICON;
            }
        }
        
        if (object instanceof EndpointWrapper) {
            return ENDPOINT_ICON;
        }
        
        if (object instanceof Node) {
            switch (((Node) object).getNodeType()) {
                case Node.ELEMENT_NODE: 
                    return ELEMENT_NODE_ICON;
                    
                case Node.ATTRIBUTE_NODE: 
                    return ATTRIBUTE_NODE_ICON;
                    
                case Node.TEXT_NODE: 
                    return TEXT_NODE_ICON;
                    
                case Node.CDATA_SECTION_NODE: 
                    return CDATA_NODE_ICON;
                    
                default: 
                    return DEFAULT_NODE_ICON;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        // does nothing
    }

    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        // does nothing
    }
}
