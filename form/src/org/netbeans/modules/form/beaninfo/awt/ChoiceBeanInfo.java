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
import java.awt.Choice;

/** A BeanInfo for java.awt.Choice.
*
* @author Ales Novak
*/
public class ChoiceBeanInfo extends ComponentBeanInfo.Support {

    public ChoiceBeanInfo() {
        super("choice", java.awt.Choice.class); // NOI18N
    }

    /** @return Propertydescriptors */
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        return new PropertyDescriptor[] {
            new PropertyDescriptor("selectedObjects", Choice.class, "getSelectedObjects", null), // NOI18N
            new PropertyDescriptor("selectedIndex", Choice.class, "getSelectedIndex", null), // NOI18N
            new PropertyDescriptor("itemCount", Choice.class, "getItemCount", null), // NOI18N
            new PropertyDescriptor("item", Choice.class, "getItem", null), // NOI18N
            new PropertyDescriptor("selectedItem", Choice.class, "getSelectedItem", null), // NOI18N
        };
    }

}
