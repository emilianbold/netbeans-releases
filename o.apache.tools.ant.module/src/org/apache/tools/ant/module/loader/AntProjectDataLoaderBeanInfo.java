/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.loader;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.apache.tools.ant.module.AntModule;
import org.openide.loaders.DataLoader;
import org.openide.util.NbBundle;

public class AntProjectDataLoaderBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
        } catch (IntrospectionException ie) {
            AntModule.err.notify(ie);
            return null;
        }
    }
    
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        // Make extensions into a r/o property.
        // It will only contain the Ant MIME type.
        // Customizations should be done on the resolver object, not on the extension list.
        // Does not work to just use additional bean info from UniFileLoader and return one extensions
        // property with no setter--Introspector cleverly (!&#$@&) keeps your display name
        // and everything and adds back in the setter from the superclass.
        // So bypass UniFileLoader in the beaninfo search.
        try {
            PropertyDescriptor extensions = new PropertyDescriptor("extensions", AntProjectDataLoader.class, "getExtensions", null); // NOI18N
            extensions.setDisplayName(NbBundle.getMessage(AntProjectDataLoaderBeanInfo.class, "PROP_extensions"));
            extensions.setShortDescription(NbBundle.getMessage(AntProjectDataLoaderBeanInfo.class, "HINT_extensions"));
            extensions.setExpert(true);
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            AntModule.err.notify(ie);
            return null;
        }
    }

    @Override
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return org.openide.util.Utilities.loadImage ("org/apache/tools/ant/module/resources/AntIcon.gif");
        } else {
            return null;
        }
    }

}
