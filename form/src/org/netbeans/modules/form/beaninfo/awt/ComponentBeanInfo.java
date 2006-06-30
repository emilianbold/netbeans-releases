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
