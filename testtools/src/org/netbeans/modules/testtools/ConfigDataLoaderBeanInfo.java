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

package org.netbeans.modules.testtools;

/*
 * ConfigDataLoaderBeanInfo.java
 *
 * Created on November 26, 2002, 1:54 PM
 */


import java.beans.*;
import java.awt.Image;

import org.openide.loaders.DataLoader;
import org.openide.util.NbBundle;

/** Bean Info for ConfigDataLoader class
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ConfigDataLoaderBeanInfo extends SimpleBeanInfo {

    /** returns Bean Info of ancestor class
     * @return BeanInfo */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
        } catch (IntrospectionException ie) {
            return null;
        }
    }
    
    /** returns Property Descriptors of ConfigDataLoader properties
     * @return array of PropertyDescriptor */    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor extensions = new PropertyDescriptor("extensions", ConfigDataLoader.class, "getExtensions", null); // NOI18N
            extensions.setDisplayName(NbBundle.getMessage(ConfigDataLoaderBeanInfo.class, "ExtensionsName")); // NOI18N
            extensions.setShortDescription(NbBundle.getMessage(ConfigDataLoaderBeanInfo.class, "ExtensionsDescription")); // NOI18N
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            return null;
        }
    }

    /** returns icon of ConfigDataLoader
     * @param type int icon type
     * @return Image ConfigIcon */    
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return org.openide.util.Utilities.loadImage ("org/netbeans/modules/testtools/ConfigIcon.gif"); // NOI18N
        } else {
            return null;
        }
    }

}
