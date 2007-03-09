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

package org.netbeans.modules.sun.manager.jbi.nodes;

import java.awt.Image;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;

import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceUnitStatus;
import org.netbeans.modules.sun.manager.jbi.util.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.NodeTypes;
import org.netbeans.modules.sun.manager.jbi.util.Utils;

/**
 * Node for one JBI Service Unit.
 *
 * @author jqian
 */
public class JBIServiceUnitNode extends AppserverJBIMgmtLeafNode {
    
    private static final String NODE_TYPE = NodeTypes.SERVICE_UNIT;
    
    public JBIServiceUnitNode(final AppserverJBIMgmtController controller,
            final String name,
            final String displayName,
            final String description) {
        super(controller, NODE_TYPE);
        setName(name);
        setDisplayName(displayName);
        setShortDescription(description);
    }
    
    /**
     * Return the SheetProperties to be displayed for this JVM.
     *
     * @return A java.util.Map containing all JVM properties.
     */
    protected Map<Attribute, MBeanAttributeInfo> getSheetProperties() {
        JBIServiceUnitStatus serviceUnit = getServiceUnitStatus();
        return Utils.getIntrospectedPropertyMap(serviceUnit, true);
    }
    
    /**
     *
     */
    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }
    
    /**
     *
     * @return
     */
    private JBIServiceUnitStatus getServiceUnitStatus() {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        String assemblyName = getParentNode().getName();
        return controller.getJBIAdministrationService().
                getServiceUnitStatus(assemblyName, getName());
    }
    
    /**
     *
     */
    public Image getIcon(int type) {
        
        String baseIconName = IconConstants.SERVICE_UNIT_ICON;
        
        JBIServiceUnitStatus unitStatus = getServiceUnitStatus();
        String status = (unitStatus == null) ? null : unitStatus.getStatus();
        
        String externalBadgeIconName = null;
        if (JBIComponentStatus.INSTALLED_STATE.equals(status)) {
            externalBadgeIconName = IconConstants.INSTALLED_ICON;
        } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
            externalBadgeIconName = IconConstants.STOPPED_ICON;
        } else if (!JBIComponentStatus.STARTED_STATE.equals(status)) {
            externalBadgeIconName = IconConstants.UNKNOWN_ICON;
        }
       
        return Utils.getBadgedIcon(getClass(), baseIconName, null, externalBadgeIconName);
    }
}
