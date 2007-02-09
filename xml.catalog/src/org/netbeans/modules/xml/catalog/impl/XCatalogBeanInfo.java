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
package org.netbeans.modules.xml.catalog.impl;

import java.beans.*;
import java.awt.Image;

import org.openide.util.Utilities;

import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;

public class XCatalogBeanInfo extends SimpleBeanInfo {

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( XCatalog.class , XCatalogCustomizer.class );
        beanDescriptor.setDisplayName ( Util.THIS.getString("NAME_x_catalog") );
        beanDescriptor.setShortDescription ( Util.THIS.getString("TEXT_x_catalog_desc") );
        
	return beanDescriptor;
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties = new PropertyDescriptor[4];
        int PROPERTY_source = 0;
        int PROPERTY_displayName = 1;
        int PROPERTY_shortDescription = 2;
        int PROPERTY_icon = 3;
        try {
            properties[PROPERTY_source] = new PropertyDescriptor ( "source", XCatalog.class, "getSource", "setSource" );
            properties[PROPERTY_source].setExpert ( true );
            properties[PROPERTY_source].setDisplayName ( Util.THIS.getString("PROP_xcatalog_location") );
            properties[PROPERTY_source].setShortDescription ( Util.THIS.getString("PROP_xcatalog_location_desc") );
            properties[PROPERTY_displayName] = new PropertyDescriptor ( "displayName", XCatalog.class, "getDisplayName", null );
            properties[PROPERTY_displayName].setDisplayName ( Util.THIS.getString("PROP_xcatalog_name") );
            properties[PROPERTY_displayName].setShortDescription ( Util.THIS.getString("PROP_xcatalog_name_desc") );
            properties[PROPERTY_shortDescription] = new PropertyDescriptor ( "shortDescription", XCatalog.class, "getShortDescription", null );
            properties[PROPERTY_shortDescription].setDisplayName ( Util.THIS.getString("PROP_xcatalog_info") );
            properties[PROPERTY_shortDescription].setShortDescription ( Util.THIS.getString("PROP_xcatalog_info_desc") );
            properties[PROPERTY_icon] = new IndexedPropertyDescriptor ( "icon", XCatalog.class, null, null, "getIcon", null );
            properties[PROPERTY_icon].setHidden ( true );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
        
        // Here you can add code for customizing the properties array.
        
        properties[PROPERTY_shortDescription].setName(CatalogDescriptor.PROP_CATALOG_DESC);
        properties[PROPERTY_displayName].setName(CatalogDescriptor.PROP_CATALOG_NAME);
        properties[PROPERTY_icon].setName(CatalogDescriptor.PROP_CATALOG_ICON);
	return properties;
    }

    public Image getIcon (int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {

            return Utilities.loadImage ("org/netbeans/modules/xml/catalog/impl/xmlCatalog.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
