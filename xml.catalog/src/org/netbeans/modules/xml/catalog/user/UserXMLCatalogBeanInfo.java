/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.catalog.user;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.NbBundle;

/**
 * description of {@link UserXMLCatalog}.
 * @author Milan Kuchtiak
 */
public class UserXMLCatalogBeanInfo extends SimpleBeanInfo {
    
    public UserXMLCatalogBeanInfo() {}
    
    private static final int PROPERTY_displayName = 0;
    private static final int PROPERTY_shortDescription = 1;
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties = new PropertyDescriptor[2];
    
        try {
            properties[PROPERTY_displayName] = new PropertyDescriptor ("displayName", UserXMLCatalog.class, "getDisplayName", null);
            properties[PROPERTY_displayName].setDisplayName (NbBundle.getMessage(UserXMLCatalog.class,"PROP_catalog_name"));
            properties[PROPERTY_displayName].setShortDescription (NbBundle.getMessage(UserXMLCatalog.class,"HINT_catalog_name"));
            properties[PROPERTY_shortDescription] = new PropertyDescriptor ( "shortDescription", UserXMLCatalog.class, "getShortDescription", null );
            properties[PROPERTY_shortDescription].setDisplayName (NbBundle.getMessage(UserXMLCatalog.class,"PROP_catalog_desc"));
            properties[PROPERTY_shortDescription].setShortDescription (NbBundle.getMessage(UserXMLCatalog.class,"HINT_catalog_desc"));
        } catch( java.beans.IntrospectionException e) {
        }
        return properties;
    }
    
}
