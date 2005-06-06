/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog;

import java.beans.*;
import java.awt.Image;

import org.openide.util.Utilities;

public class CatalogEntryBeanInfo extends SimpleBeanInfo {

    private static final String ICON_DIR_BASE = "org/netbeans/modules/xml/catalog/resources/"; // NOI18N

    private static final String PUBLICID_N = Util.THIS.getString("PROP_public_id");
    private static final String PUBLICID_D = Util.THIS.getString("PROP_public_id_desc");
    private static final String SYSTEMID_D = Util.THIS.getString("PROP_system_id_desc");
    private static final String SYSTEMID_N = Util.THIS.getString("PROP_system_id");
    private static final String URI_D = Util.THIS.getString("PROP_uri_desc");
    private static final String URI_N = Util.THIS.getString("PROP_uri");
    
    // Property identifiers
    private static final int PROPERTY_publicID = 0;
    private static final int PROPERTY_systemID = 1;
    private static final int PROPERTY_URI = 2;

    // Property array 
    private static PropertyDescriptor[] properties = new PropertyDescriptor[3];

    static {
        try {
            properties[PROPERTY_publicID] = new PropertyDescriptor ( "publicID", CatalogEntry.class, "getPublicIDValue", null ); // NOI18N
            properties[PROPERTY_publicID].setDisplayName ( PUBLICID_N );
            properties[PROPERTY_publicID].setShortDescription ( PUBLICID_D );
            properties[PROPERTY_systemID] = new PropertyDescriptor ( "systemID", CatalogEntry.class, "getSystemIDValue", null ); // NOI18N
            properties[PROPERTY_systemID].setDisplayName ( SYSTEMID_N );
            properties[PROPERTY_systemID].setShortDescription ( SYSTEMID_D );
            properties[PROPERTY_URI] = new PropertyDescriptor ( "uri", CatalogEntry.class, "getUriValue", null ); // NOI18N
            properties[PROPERTY_URI].setDisplayName ( URI_N );
            properties[PROPERTY_URI].setShortDescription ( URI_D );
        }
        catch( IntrospectionException e) {}                          

        // Here you can add code for customizing the properties array.

    }

    // EventSet identifiers//GEN-FIRST:Events

    // EventSet array
    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[0];
//GEN-HEADEREND:Events

    // Here you can add code for customizing the event sets array.

//GEN-LAST:Events

    // Method identifiers//GEN-FIRST:Methods

    // Method array 
    private static MethodDescriptor[] methods = new MethodDescriptor[0];
//GEN-HEADEREND:Methods

    // Here you can add code for customizing the methods array.
    
//GEN-LAST:Methods


    private static int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static int defaultEventIndex = -1;//GEN-END:Idx


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
        return properties;
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return  An array of EventSetDescriptors describing the kinds of 
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return eventSets;
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     * 
     * @return  An array of MethodDescriptors describing the methods 
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return methods;
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
        return defaultPropertyIndex;
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

            return Utilities.loadImage (ICON_DIR_BASE + "catalog-entry.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
