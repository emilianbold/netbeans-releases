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

package org.netbeans.beaninfo;

import java.awt.Image;
import java.beans.*;

import org.openide.loaders.DataLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

public class DataLoaderPool {

    /** Create read-only 'extensions' property. Method used in [Folder|Instance]LoaderBeanInfo.
     */
    private static PropertyDescriptor[] createExtensionsPropertyDescriptor () {
        try {
            PropertyDescriptor extensions = new PropertyDescriptor ("extensions", UniFileLoader.class); // NOI18N
            extensions.setDisplayName (NbBundle.getBundle (DataLoaderPool.class).getString ("PROP_UniFileLoader_extensions"));
            extensions.setShortDescription (NbBundle.getBundle (DataLoaderPool.class).getString ("HINT_UniFileLoader_extensions"));
            extensions.setWriteMethod(null);
            return new PropertyDescriptor[] { extensions };
        } 
        catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }
    
    public static class FolderLoaderBeanInfo extends SimpleBeanInfo {
        
        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                // FolderLoader bean info uses MultiFileLoader's bean info instead of
                // UniFileLoader's one. That is why it is necessary to remove 'extensions'
                // property (declared in UniFileLoaderBeanInfo).
                // Currently this property is only addition to MultiFileLoader bean info
                // provided by UniFileLoaderBeanInfo.
                return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        }

        public PropertyDescriptor[] getPropertyDescriptors () {
             return createExtensionsPropertyDescriptor();
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("org/openide/loaders/defaultFolder.gif"); // NOI18N
            } else {
                return Utilities.loadImage("org/openide/loaders/defaultFolder32.gif"); // NOI18N
            }
        }
    }

    public static class InstanceLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                // InstanceLoader bean info uses MultiFileLoader's bean info instead of
                // UniFileLoader's one. That is why it is necessary to change 'extensions'
                // property from r/w (declared in UniFileLoaderBeanInfo) to r/o property.
                // Currently this property is only addition to MultiFileLoader bean info
                // provided by UniFileLoaderBeanInfo.
                return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        }

        public PropertyDescriptor[] getPropertyDescriptors () {
             return createExtensionsPropertyDescriptor();
        }
        
        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("org/netbeans/core/resources/action.gif"); // NOI18N
            } else {
                return Utilities.loadImage ("org/netbeans/core/resources/action32.gif"); // NOI18N
            }
        }

    }

    public static class DefaultLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage ("org/openide/resources/pending.gif"); // NOI18N
            } else {
                return Utilities.loadImage ("org/openide/resources/pending32.gif"); // NOI18N
            }
        }

    }

    public static class ShadowLoaderBeanInfo extends SimpleBeanInfo {
        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        }

        public PropertyDescriptor[] getPropertyDescriptors () {
            try {
                // Hide the actions property from users, since shadows inherit actions anyway:
                Class c = Class.forName ("org.openide.loaders.DataLoaderPool$ShadowLoader"); // NOI18N
                PropertyDescriptor actions = new PropertyDescriptor ("actions", c); // NOI18N
                actions.setHidden (true);
                return new PropertyDescriptor[] { actions };
            } catch (ClassNotFoundException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                return Utilities.loadImage("org/openide/resources/actions/copy.gif"); // NOI18N
            } else {
                // [PENDING]
                //return Utilities.loadImage ("org/openide/resources/actions/copy32.gif"); // NOI18N
                return null;
            }
        }

    }

}
