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

package org.netbeans.beaninfo;

import java.awt.Image;
import java.beans.*;

import org.openide.loaders.DataLoader;
import org.openide.loaders.UniFileLoader;

public class DataLoaderPool {

    private static Image folderIcon;
    private static Image folderIcon32;
    private static Image shadowIcon;
    private static Image shadowIcon32;
    private static Image instanceIcon;
    private static Image instanceIcon32;
    private static Image defaultIcon;
    private static Image defaultIcon32;

    public static class FolderLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                if (folderIcon == null) folderIcon = loadImage ("/org/openide/resources/defaultFolder.gif"); // NOI18N
                return folderIcon;
            } else {
                if (folderIcon32 == null) folderIcon32 = loadImage ("/org/openide/resources/defaultFolder32.gif"); // NOI18N
                return folderIcon32;
            }
        }

    }

    public static class InstanceLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                if (instanceIcon == null) instanceIcon = loadImage ("/org/netbeans/core/resources/action.gif"); // NOI18N
                return instanceIcon;
            } else {
                if (instanceIcon32 == null) instanceIcon32 = loadImage ("/org/netbeans/core/resources/action32.gif"); // NOI18N
                return instanceIcon32;
            }
        }

    }

    public static class DefaultLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                if (defaultIcon == null) defaultIcon = loadImage ("/org/openide/resources/pending.gif"); // NOI18N
                return defaultIcon;
            } else {
                if (defaultIcon32 == null) defaultIcon32 = loadImage ("/org/openide/resources/pending32.gif"); // NOI18N
                return defaultIcon32;
            }
        }

    }

    public static class ShadowLoaderBeanInfo extends SimpleBeanInfo {

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public PropertyDescriptor[] getPropertyDescriptors () {
            try {
                // Hide the actions property from users, since shadows inherit actions anyway:
                PropertyDescriptor actions = new PropertyDescriptor ("actions", org.openide.loaders.DataLoaderPool.ShadowLoader.class); // NOI18N
                actions.setHidden (true);
                return new PropertyDescriptor[] { actions };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                if (shadowIcon == null) shadowIcon = loadImage ("/org/openide/resources/actions/copy.gif"); // NOI18N
                return shadowIcon;
            } else {
                // [PENDING]
                //if (shadowIcon32 == null) shadowIcon32 = loadImage ("/org/openide/resources/actions/copy32.gif"); // NOI18N
                return shadowIcon32;
            }
        }

    }

}

/*
 * Log
 *  2    Gandalf   1.1         1/13/00  Jaroslav Tulach I18N
 *  1    Gandalf   1.0         1/13/00  Jesse Glick     
 * $
 */
