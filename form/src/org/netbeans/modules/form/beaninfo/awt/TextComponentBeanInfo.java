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

/** A BeanInfo for java.awt.TextComponent.
*
* @author Ales Novak
*/
class TextComponentBeanInfo extends ComponentBeanInfo.Support {

    /** no-arg */
    TextComponentBeanInfo() {
        super(null, java.awt.TextComponent.class);
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] createPDs() throws IntrospectionException {
        return new PropertyDescriptor[] {
            new PropertyDescriptor("selectionStart", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("text", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("caretPosition", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("selectionEnd", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("editable", java.awt.TextComponent.class), // NOI18N
        };
    }
}
