/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;

import org.openide.util.NbBundle;

public class SimpleExtBrowserBeanInfo extends SimpleBeanInfo {

    private static BeanDescriptor descr;

    static {
        descr = new BeanDescriptor (SimpleExtBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "CTL_SimpleExtBrowser"));
        descr.setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_SimpleExtBrowser"));
    }
    
    private static PropertyDescriptor[] properties = null; 

    public BeanDescriptor getBeanDescriptor () {
        return descr;
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
        if (properties == null) {
            try {
                properties = new PropertyDescriptor [] {
                    new PropertyDescriptor ("name", SimpleExtBrowser.class, "getName", "setName"),    // NOI18N
                    new PropertyDescriptor ("process", SimpleExtBrowser.class, "getProcess", "setProcess")    // NOI18N
                };
                
                properties[0].setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "PROP_browserName"));
                properties[0].setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_browserName"));
                properties[1].setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "PROP_process"));
                properties[1].setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_process"));

            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                    ie.printStackTrace ();
                return null;
            }
        }
        return properties;
    }

    /**
    * Returns the IceBrowserSettings' icon. 
    */
    public Image getIcon (int type) {
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.gif"); // NOI18N
    }
}

