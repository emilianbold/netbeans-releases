/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

public class FirefoxBrowserBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor (FirefoxBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (FirefoxBrowserBeanInfo.class, "CTL_FirefoxBrowserName"));
        descr.setShortDescription (NbBundle.getMessage (FirefoxBrowserBeanInfo.class, "HINT_FirefoxBrowserName"));

        descr.setValue ("helpID", "org.netbeans.modules.extbrowser.ExtWebBrowser");  // NOI18N //TODO
	return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor [0];
    }

    /**
    * Returns the IceBrowserSettings' icon. 
    */
    public Image getIcon (int type) {
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.gif"); // NOI18N
    }
    
    /**
     * Claim there are no other relevant BeanInfo objects.  You
     * may override this if you want to (for example) return a
     * BeanInfo for a base class.
     */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (ExtWebBrowser.class) };
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
}
