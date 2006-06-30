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

package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;

import org.openide.util.NbBundle;

public class SimpleExtBrowserBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor descr = new BeanDescriptor (SimpleExtBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "CTL_SimpleExtBrowser"));
        descr.setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_SimpleExtBrowser"));
        descr.setValue ("helpID", "org.netbeans.modules.extbrowser.SimpleExtBrowser");  // NOI18N
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
        PropertyDescriptor[] properties;
        try {
            properties = new PropertyDescriptor [] {
                new PropertyDescriptor (SimpleExtBrowser.PROP_BROWSER_EXECUTABLE, SimpleExtBrowser.class)    // NOI18N
            };
            properties[0].setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "PROP_browserExecutable"));
            properties[0].setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_browserExecutable"));
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
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.gif"); // NOI18N
    }
}

