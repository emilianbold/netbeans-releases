package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;

import org.openide.util.NbBundle;

public class ExtWebBrowserBeanInfo extends SimpleBeanInfo {

    private static BeanDescriptor descr = null; 

    private static PropertyDescriptor[] properties = null; 

    public BeanDescriptor getBeanDescriptor() {
        if (descr == null) {
            descr = new BeanDescriptor (ExtWebBrowser.class);
            descr.setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "CTL_ExtBrowserName"));
            descr.setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_ExtBrowserName"));
        }
	return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        if (properties == null) {
            try {
                properties = new PropertyDescriptor [] {
                    new PropertyDescriptor ("name", ExtWebBrowser.class, "getName", null),    // NOI18N
                    new PropertyDescriptor ("executable", ExtWebBrowser.class, "getExecutable", "setExecutable")    // NOI18N
                };
                properties[0].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_browserName"));
                properties[0].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_browserName"));
                properties[1].setDisplayName (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "PROP_browserExecutable"));
                properties[1].setShortDescription (NbBundle.getMessage (ExtWebBrowserBeanInfo.class, "HINT_browserExecutable"));
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions"))   // NOI18N
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
