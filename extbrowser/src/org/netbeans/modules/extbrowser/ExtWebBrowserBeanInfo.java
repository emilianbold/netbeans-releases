/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class ExtWebBrowserBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(ExtWebBrowser.class);
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        if (Utilities.isWindows()) {
            try {
                properties = new PropertyDescriptor [] {
                                    new PropertyDescriptor(ExtWebBrowser.PROP_BROWSER_EXECUTABLE, ExtWebBrowser.class),
//                                    new PropertyDescriptor(ExtWebBrowser.PROP_BROWSER_START_TIMEOUT, ExtWebBrowser.class),
                                    new PropertyDescriptor(ExtWebBrowser.PROP_DDE_ACTIVATE_TIMEOUT, ExtWebBrowser.class),
                                    new PropertyDescriptor(ExtWebBrowser.PROP_DDE_OPENURL_TIMEOUT, ExtWebBrowser.class)
                                 };

                properties[0].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_browserExecutable"));
                properties[0].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_browserExecutable"));

//                properties[1].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_BROWSER_START_TIMEOUT"));
//                properties[1].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_BROWSER_START_TIMEOUT"));
//                properties[1].setExpert(true);

                properties[1].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_DDE_ACTIVATE_TIMEOUT"));
                properties[1].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_DDE_ACTIVATE_TIMEOUT"));
                properties[1].setExpert(true);
                properties[1].setHidden(true);

                properties[2].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_DDE_OPENURL_TIMEOUT"));
                properties[2].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_DDE_OPENURL_TIMEOUT"));
                properties[2].setExpert(true);
                properties[2].setHidden(true);

            } catch (IntrospectionException ie) {
                org.openide.ErrorManager.getDefault().notify(ie);
                return null;
            }
        } else {
            try {
                properties = new PropertyDescriptor [] {
                                    new PropertyDescriptor (ExtWebBrowser.PROP_BROWSER_EXECUTABLE, ExtWebBrowser.class),
                                 };

                properties[0].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_browserExecutable"));
                properties[0].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_browserExecutable"));

            } catch (IntrospectionException ie) {
                org.openide.ErrorManager.getDefault().notify(ie);
                return null;
            }
        }
        return properties;
    }

    /**
    * Returns the icon. 
    */
    public Image getIcon (int type) {
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.gif"); // NOI18N
    }
    
}
