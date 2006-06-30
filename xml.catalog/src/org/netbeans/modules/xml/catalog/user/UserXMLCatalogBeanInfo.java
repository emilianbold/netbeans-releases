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
