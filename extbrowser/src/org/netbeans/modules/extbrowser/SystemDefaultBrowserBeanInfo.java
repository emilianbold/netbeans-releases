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

public class SystemDefaultBrowserBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor (SystemDefaultBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "CTL_SystemDefaultBrowserName"));
        descr.setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_SystemDefaultBrowserName")); //TODO

        descr.setValue ("helpID", "org.netbeans.modules.extbrowser.ExtWebBrowser");  // NOI18N //TODO
	return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        
        try {
            properties = new PropertyDescriptor [] {
                                new PropertyDescriptor(ExtWebBrowser.PROP_BROWSER_EXECUTABLE, SystemDefaultBrowser.class, "getBrowserExecutable", null), // NOI18N
//                                new PropertyDescriptor(ExtWebBrowser.PROP_BROWSER_START_TIMEOUT, SystemDefaultBrowser.class),
                                new PropertyDescriptor(ExtWebBrowser.PROP_DDE_ACTIVATE_TIMEOUT, SystemDefaultBrowser.class),
                                new PropertyDescriptor(ExtWebBrowser.PROP_DDE_OPENURL_TIMEOUT, SystemDefaultBrowser.class)
                             };

            properties[0].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_browserExecutable"));
            properties[0].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_browserExecutable"));

//            properties[1].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_BROWSER_START_TIMEOUT"));
//            properties[1].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_BROWSER_START_TIMEOUT"));
//            properties[1].setExpert(Boolean.TRUE.booleanValue());

            properties[1].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_DDE_ACTIVATE_TIMEOUT"));
            properties[1].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_DDE_ACTIVATE_TIMEOUT"));
            properties[1].setExpert(Boolean.TRUE.booleanValue());
            properties[1].setHidden(true);

            properties[2].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_DDE_OPENURL_TIMEOUT"));
            properties[2].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_DDE_OPENURL_TIMEOUT"));
            properties[2].setExpert(Boolean.TRUE.booleanValue());
            properties[2].setHidden(true);

        } catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault().notify(ie);
            return null;
        }
        
        return properties;
    }

    /**
    * Returns the icon. 
    */
    public Image getIcon (int type) {
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.png"); // NOI18N
    }
    
}
