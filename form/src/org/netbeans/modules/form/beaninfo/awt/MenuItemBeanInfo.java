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

import java.beans.*;

/** A BeanInfo for java.awt.MenuItem.
*
* @author Ales Novak
*/
abstract class MenuItemBeanInfo extends MenuComponentBeanInfo {

    /** no-arg */
    MenuItemBeanInfo() {
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] inh = super.getPropertyDescriptors();
        PropertyDescriptor[] desc = new PropertyDescriptor[inh.length + 3];
        System.arraycopy(inh, 0, desc, 0, inh.length);
        try {
            desc[inh.length] = new PropertyDescriptor("actionCommand", java.awt.MenuItem.class); // NOI18N
            desc[inh.length + 1] = new PropertyDescriptor("label", java.awt.MenuItem.class); // NOI18N
            desc[inh.length + 2] = new PropertyDescriptor("enabled", java.awt.MenuItem.class); // NOI18N
            return desc;
        } catch (IntrospectionException ex) {
            return inh;
        }
    }
}
