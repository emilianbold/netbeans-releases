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

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class XTestDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
        } catch (IntrospectionException ie) {
            return null;
        }
    }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor extensions = new PropertyDescriptor("extensions", XTestDataLoader.class, "getExtensions", null);
            extensions.setDisplayName("Extensions and MIME Types");
            extensions.setShortDescription("Extensions and MIME Types");
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            return null;
        }
    }

    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return org.openide.util.Utilities.loadImage ("org/netbeans/modules/testtools/XTestIcon.gif");
        } else {
            return null;
        }
    }

}
