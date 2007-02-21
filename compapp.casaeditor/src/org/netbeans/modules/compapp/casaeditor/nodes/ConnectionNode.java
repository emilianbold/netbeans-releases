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

/*
 * ConnectionNode.java
 *
 * Created on November 2, 2006, 8:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Josh Sandusky
 */
public class ConnectionNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/ConnectionNode.png");
    
    public ConnectionNode(CasaComponent component, Children children, Lookup lookup) {
        super(component, children, lookup);
    }
    
    public ConnectionNode(CasaComponent component, Lookup lookup) {
        super(component, Children.LEAF, lookup);
    }
    
    
    protected void setupPropertySheet(Sheet sheet) {
        final CasaConnection casaConnection = (CasaConnection) getData();
        if (casaConnection == null) {
            return;
        }
        final CasaEndpoint casaConsumes = casaConnection.getConsumer().get();
        final CasaEndpoint casaProvides = casaConnection.getProvider().get();
        
        Sheet.Set consumerProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.CONSUMER_SET);
        Node.Property consumerServiceNameSupport = new PropertySupport.ReadOnly(
                "serviceName", // NO18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_ServiceName"),
                "") {
            public QName getValue() {
                return casaConsumes.getServiceQName();
            }
        };
        Node.Property consumerEndpointNameSupport = new PropertySupport.ReadOnly(
                "endpointName", // NO18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_EndpointName"),
                "") {
            public String getValue() {
                return casaConsumes.getEndpointName();
            }
        };
        consumerProperties.put(consumerServiceNameSupport);
        consumerProperties.put(consumerEndpointNameSupport);
        
        Sheet.Set providerProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.PROVIDER_SET);
        Node.Property providerServiceNameSupport = new PropertySupport.ReadOnly(
                "serviceName", // NO18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_ServiceName"),
                "") {
            public QName getValue() {
                return casaProvides.getServiceQName();
            }
        };
        Node.Property providerEndpointNameSupport = new PropertySupport.ReadOnly(
                "endpointName", // NO18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_EndpointName"),
                "") {
            public String getValue() {
                return casaProvides.getEndpointName();
            }
        };
        providerProperties.put(providerServiceNameSupport);
        providerProperties.put(providerEndpointNameSupport);
    }
    
    //The navigator title is unable to decode HTML text and showing the encoded chars (&#60;-&#62;) as is...
    @Override
    public String getName() {
        CasaConnection casaConnection = (CasaConnection) getData();
        if (casaConnection != null) {
            try {
                return casaConnection.getConsumer().get().getEndpointName() + 
                        "<->" + casaConnection.getProvider().get().getEndpointName();
            } catch (Throwable t) {
                // getName MUST recover gracefully.
                return getBadName();
            }
        }
        return super.getName();
    }
    
    private String getEncodingName() {
        CasaConnection casaConnection = (CasaConnection) getData();
        if (casaConnection != null) {
            return casaConnection.getConsumer().get().getEndpointName() + 
                    "&#60;-&#62;" + casaConnection.getProvider().get().getEndpointName();
        }
        return super.getName();
    }
    
    @Override
    public String getHtmlDisplayName() {
        try {
            String htmlDisplayName = getEncodingName();
            CasaConnection casaConnection = (CasaConnection) getData();
            String decoration = null;
            if (casaConnection != null) {
                String attr = casaConnection.getState();
                if ((attr != null) && (attr.equalsIgnoreCase("deleted"))) {
                    decoration = "<font color='#999999'><DEL>"+htmlDisplayName+"</DEL></font>";
                }
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return decoration;
        } catch (Throwable t) {
            // getHtmlDisplayName MUST recover gracefully.
            return getBadName();
        }
    }
    
    public Image getIcon(int type) {
        return ICON;
    }
    
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    public boolean isDeletable() {
        CasaConnection connection = (CasaConnection) getData();
        if (connection != null) {
            return getModel().isDeletable(connection);
        }
        return false;
    }
    
}
