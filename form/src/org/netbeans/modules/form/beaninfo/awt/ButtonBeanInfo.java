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
import java.awt.Button;

/** A BeanInfo for java.awt.Button.
*
* @author Ales Novak
*/
public class ButtonBeanInfo extends ComponentBeanInfo.Support {

    public ButtonBeanInfo() {
        super("button", java.awt.Button.class); // NOI18N
    }

    /** @return Propertydescriptors */
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        return new PropertyDescriptor[] {
            new PropertyDescriptor("actionCommand", Button.class), // NOI18N
            new PropertyDescriptor("label", Button.class) // NOI18N
        };
    }
}
