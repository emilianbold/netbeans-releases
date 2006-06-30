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
