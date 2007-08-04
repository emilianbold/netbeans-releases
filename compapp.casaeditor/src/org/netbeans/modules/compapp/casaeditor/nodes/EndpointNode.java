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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddConnectionAction;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Base class for ConsumesNode and ProvidesNode.
 * 
 * @author jqian
 */
public class EndpointNode extends CasaNode {

    public EndpointNode(CasaEndpointRef component, CasaNodeFactory factory) {
        super(component, Children.LEAF, factory);
    }

    protected void setupPropertySheet(Sheet sheet) {
        final CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint == null) {
            return;
        }

        Sheet.Set mainPropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.MAIN_SET);

        PropertyUtils.installEndpointInterfaceQNameProperty(mainPropertySet, this, endpoint, JBIAttributes.INTERFACE_NAME.getName(), "interfaceQName", NbBundle.getMessage(getClass(), "PROP_InterfaceName"), NbBundle.getMessage(getClass(), "PROP_InterfaceName")); // NOI18N
        PropertyUtils.installEndpointServiceQNameProperty(mainPropertySet, this, endpoint, JBIAttributes.SERVICE_NAME.getName(), "serviceQName", NbBundle.getMessage(getClass(), "PROP_ServiceName"), NbBundle.getMessage(getClass(), "PROP_ServiceName")); // NOI18N
        PropertyUtils.installEndpointNameProperty(mainPropertySet, this, endpoint, JBIAttributes.ENDPOINT_NAME.getName(), "endpointName", NbBundle.getMessage(getClass(), "PROP_EndpointName"), NbBundle.getMessage(getClass(), "PROP_EndpointName")); // NOI18N
    }

    public String getName() {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            try {
                return endpoint.getEndpointName();
            } catch (Throwable t) {
                // getName MUST recover gracefully.
                return getBadName();
            }
        }
        return super.getName();
    }

    public boolean isEditable(String propertyType) {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            return getModel().isEditable(endpoint, propertyType);
        }
        return false;
    }

    public boolean isDeletable() {
        CasaEndpointRef endpoint = (CasaEndpointRef) getData();
        if (endpoint != null) {
            return getModel().isDeletable(endpoint);
        }
        return false;
    }

    @Override
    protected void addCustomActions(List<Action> actions) {
        actions.add(SystemAction.get(AddConnectionAction.class));
    }
}
