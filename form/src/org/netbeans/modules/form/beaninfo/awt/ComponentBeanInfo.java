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

package org.netbeans.modules.form.beaninfo.awt;

import java.awt.Image;
import java.beans.*;
import org.openide.util.Utilities;

/** Base class for awt components - toolbars, checkboxes...
*
* @author Ales Novak
* @see sun.java.beans.infos.... or
*/
public class ComponentBeanInfo extends SimpleBeanInfo {

    /** no-arg */
    ComponentBeanInfo() {
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return new PropertyDescriptor[] {
                new PropertyDescriptor("background", java.awt.Component.class, "getBackground", "setBackground"), // 0 // NOI18N
                new PropertyDescriptor("foreground", java.awt.Component.class, "getForeground", "setForeground"), //1 // NOI18N
                new PropertyDescriptor("enabled", java.awt.Component.class, "isEnabled", "setEnabled"), //2 // NOI18N
                new PropertyDescriptor("name", java.awt.Component.class), //3 // NOI18N
                new PropertyDescriptor("visible", java.awt.Component.class), //4 // NOI18N
                new PropertyDescriptor("font", java.awt.Component.class) // NOI18N
            };
        } catch (IntrospectionException ex) {
            return super.getPropertyDescriptors();
        }
    }
    
    static class Support extends SimpleBeanInfo {

        String iconName;
        Class beanClass;
        
        Support(String icon, Class beanClass) {
            iconName = icon;
            this.beanClass = beanClass;
        }

        public BeanDescriptor getBeanDescriptor() {
            return new BeanDescriptor(beanClass);
        }

        /**
        * Return the icon
        */
        public Image getIcon(int type) {
            if (iconName == null) return null;
            return Utilities.loadImage("org/netbeans/modules/form/beaninfo/awt/" + iconName + ".gif"); // NOI18N
        }

        public BeanInfo[] getAdditionalBeanInfo() {
            return new BeanInfo[] { new ComponentBeanInfo() };
        }

        /** @return Propertydescriptors */
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                return createPDs();
            } catch (IntrospectionException ex) {
                return null;
            }
        }
            
        protected PropertyDescriptor[] createPDs() throws IntrospectionException {
            return new PropertyDescriptor[0];
        }
    }
}
