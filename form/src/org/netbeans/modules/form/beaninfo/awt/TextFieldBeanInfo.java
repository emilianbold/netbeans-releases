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
import java.awt.TextField;

/** A BeanInfo for java.awt.TextField.
*
* @author Ales Novak
*/
public class TextFieldBeanInfo extends ComponentBeanInfo.Support {
    
    public TextFieldBeanInfo() {
        super("textfield", java.awt.TextField.class); // NOI18N
    }
    
    public BeanInfo[] getAdditionalBeanInfo() {
        return new BeanInfo[] { new TextComponentBeanInfo(), new ComponentBeanInfo() };
    }

    /** @return Propertydescriptors */
    public PropertyDescriptor[] createPDs() throws IntrospectionException {
        return new PropertyDescriptor[] {
            new PropertyDescriptor("columns", TextField.class), // NOI18N
            new PropertyDescriptor("echoChar", TextField.class), // NOI18N
        };
    }
}
