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
import java.awt.Checkbox;

/** A BeanInfo for java.awt.Checkbox.
*
* @author Ales Novak
*/
public class CheckboxBeanInfo extends ComponentBeanInfo.Support {

    public CheckboxBeanInfo() {
        super("checkbox", java.awt.Checkbox.class); // NOI18N
    }

    /** @return Propertydescriptors */
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        return new PropertyDescriptor[] {
            new PropertyDescriptor("state", Checkbox.class), // NOI18N
            new PropertyDescriptor("label", Checkbox.class), // NOI18N
            new PropertyDescriptor("checkboxGroup", Checkbox.class), // NOI18N
            new PropertyDescriptor("selectedObjects", Checkbox.class, "getSelectedObjects", null), // NOI18N
        };
    }
}
