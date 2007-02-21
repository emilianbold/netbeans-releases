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
package org.netbeans.modules.compapp.casaeditor.properties;


import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceUnit;
//import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;



/**
 * @author nk160297
 */
public abstract class PropertyUtils {
    
    
    public static enum PropertiesGroups {
        MAIN_SET,
        IDENTIFICATION_SET,
        TARGET_SET,
        CONSUMER_SET,
        PROVIDER_SET,
        EXPERT_SET,
        COLOR_SET,
        FONT_SET,
        GENERIC_SET;
        
        private String myDisplayName;
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(PropertyType.class, this.toString());
            }
            return myDisplayName;
        }
    }
    
    
    public static Node.Property createErrorProperty(String displayName) {
        return new PropertySupport.ReadOnly(
            "error", // NO18N
            String.class,
            displayName,
            "") {
            public String getValue() {
                return NbBundle.getMessage(PropertyUtils.class, "PROP_ERROR_VALUE");
            }
        };
    }
    
    public static void installEndpointInterfaceQNameProperty(
            Sheet.Set propertySet, 
            CasaNode node,
            CasaEndpointRef component,
            String propertyType, 
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyEndpointInterfaceQName(
                node, 
                component, 
                propertyType, 
                attributeName,
                displayName,
                displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public static void installEndpointServiceQNameProperty(
            Sheet.Set propertySet, 
            CasaNode node,
            CasaEndpointRef component,
            String propertyType, 
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyEndpointServiceQName(
                node, 
                component, 
                propertyType, 
                attributeName,
                displayName,
                displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public static void installEndpointEndpointNameProperty(
            Sheet.Set propertySet, 
            CasaNode node,
            CasaEndpointRef component,
            String propertyType, 
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyEndpointEndpointName(
                node, 
                component, 
                propertyType, 
                attributeName,
                displayName,
                displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public static void installServiceUnitNameProperty(
            Sheet.Set propertySet, 
            CasaNode node,
            CasaServiceUnit component,
            String propertyType, 
            String attributeName,
            String displayName,
            String displayDescription) {
        try {
            Node.Property property = new PropertyServiceUnitName(
                node, 
                component, 
                propertyType, 
                attributeName,
                displayName,
                displayDescription);
            propertySet.put(property);
        } catch (Exception e) {
            propertySet.put(createErrorProperty(displayName));
            ErrorManager.getDefault().notify(e);
        }
    }
}
