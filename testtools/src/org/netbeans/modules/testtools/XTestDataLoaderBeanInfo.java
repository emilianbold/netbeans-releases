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

package org.netbeans.modules.testtools;

/*
 * XTestDataLoaderBeanInfo.java
 *
 * Created on May 3, 2002, 1:54 PM
 */


import java.beans.*;
import java.awt.Image;

import org.openide.loaders.DataLoader;
import org.openide.util.NbBundle;

/** Bean Info for XTestDataLoader class
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestDataLoaderBeanInfo extends SimpleBeanInfo {

    /** returns Bean Info of ancestor class
     * @return BeanInfo */    
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
        } catch (IntrospectionException ie) {
            return null;
        }
    }
    
    /** returns Property Descriptors of XTestDataLoader properties
     * @return array of PropertyDescriptor */    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor extensions = new PropertyDescriptor("extensions", XTestDataLoader.class, "getExtensions", null); // NOI18N
            extensions.setDisplayName(NbBundle.getMessage(XTestDataLoaderBeanInfo.class, "ExtensionsName")); // NOI18N
            extensions.setShortDescription(NbBundle.getMessage(XTestDataLoaderBeanInfo.class, "ExtensionsDescription")); // NOI18N
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            return null;
        }
    }

    /** returns icon of XTestDataLoader
     * @param type int icon type
     * @return Image XTestIcon */    
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return org.openide.util.Utilities.loadImage ("org/netbeans/modules/testtools/XTestIcon.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
