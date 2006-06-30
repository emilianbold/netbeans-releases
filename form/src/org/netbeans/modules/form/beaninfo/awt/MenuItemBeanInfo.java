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
