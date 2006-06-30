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
package org.netbeans.modules.xml.catalog.impl.sun;

import java.beans.*;
import java.awt.Image;

import org.openide.util.Utilities;

import org.netbeans.modules.xml.catalog.spi.*;

public class CatalogBeanInfo extends SimpleBeanInfo {

    private static final String ICON_DIR_BASE = "org/netbeans/modules/xml/catalog/impl/sun/"; // NOI18N


    // Bean descriptor //GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/;
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( Catalog.class , CatalogCustomizer.class );
        beanDescriptor.setDisplayName ( Util.THIS.getString("PROP_Catalog") );
        beanDescriptor.setShortDescription ( Util.THIS.getString("PROP_Catalog_desc") );//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;         }//GEN-LAST:BeanDescriptor


    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_displayName = 0;
    private static final int PROPERTY_shortDescription = 1;
    private static final int PROPERTY_icon = 2;

    // Property array 
    /*lazy PropertyDescriptor*/;
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[3];
    
        try {
            properties[PROPERTY_displayName] = new PropertyDescriptor ( "displayName", Catalog.class, "getDisplayName", null );
            properties[PROPERTY_displayName].setDisplayName ( Util.THIS.getString("PROP_catalog_name") );
            properties[PROPERTY_displayName].setShortDescription ( Util.THIS.getString("PROP_catalog_name_desc") );
            properties[PROPERTY_shortDescription] = new PropertyDescriptor ( "shortDescription", Catalog.class, "getShortDescription", null );
            properties[PROPERTY_shortDescription].setDisplayName ( Util.THIS.getString("PROP_catalog_info") );
            properties[PROPERTY_shortDescription].setShortDescription ( Util.THIS.getString("PROP_catalog_info_desc") );
            properties[PROPERTY_icon] = new IndexedPropertyDescriptor ( "icon", Catalog.class, null, null, "getIcon", null );
            properties[PROPERTY_icon].setHidden ( true );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

        properties[PROPERTY_shortDescription].setName(CatalogDescriptor.PROP_CATALOG_DESC);
        properties[PROPERTY_displayName].setName(CatalogDescriptor.PROP_CATALOG_NAME);
        properties[PROPERTY_icon].setName(CatalogDescriptor.PROP_CATALOG_ICON);
        
        return properties;         }//GEN-LAST:Properties

    // EventSet identifiers//GEN-FIRST:Events

    // EventSet array
    /*lazy EventSetDescriptor*/;
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[0];//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

        return eventSets;         }//GEN-LAST:Events

    // Method identifiers //GEN-FIRST:Methods

    // Method array 
    /*lazy MethodDescriptor*/;
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[0];//GEN-HEADEREND:Methods

        // Here you can add code for customizing the methods array.
    
        return methods;         }//GEN-LAST:Methods


    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx

    
    //GEN-FIRST:Superclass

    // Here you can add code for customizing the Superclass BeanInfo.

    //GEN-LAST:Superclass
	
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
	return getBdescriptor();
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
	return getPdescriptor();
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return  An array of EventSetDescriptors describing the kinds of 
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
	return getEdescriptor();
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     * 
     * @return  An array of MethodDescriptors describing the methods 
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
	return getMdescriptor();
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are 
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean. 
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }

    /**
     * This method returns an image object that can be used to
     * represent the bean in toolboxes, toolbars, etc.   Icon images
     * will typically be GIFs, but may in future include other formats.
     * <p>
     * Beans aren't required to provide icons and may return null from
     * this method.
     * <p>
     * There are four possible flavors of icons (16x16 color,
     * 32x32 color, 16x16 mono, 32x32 mono).  If a bean choses to only
     * support a single icon we recommend supporting 16x16 color.
     * <p>
     * We recommend that icons have a "transparent" background
     * so they can be rendered onto an existing background.
     *
     * @param  iconKind  The kind of icon requested.  This should be
     *    one of the constant values ICON_COLOR_16x16, ICON_COLOR_32x32, 
     *    ICON_MONO_16x16, or ICON_MONO_32x32.
     * @return  An image object representing the requested icon.  May
     *    return null if no suitable icon is available.
     */
    public Image getIcon (int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {

            return Utilities.loadImage (ICON_DIR_BASE + "sunCatalog.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
