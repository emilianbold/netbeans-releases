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

package org.netbeans.modules.form.beaninfo.swing;

import java.beans.*;
import javax.swing.border.SoftBevelBorder;

public class SoftBevelBorderBeanInfo extends BISupport {
    
    public SoftBevelBorderBeanInfo() {
        super("softBevelBorder", javax.swing.border.SoftBevelBorder.class); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRO(SoftBevelBorder.class, "bevelType"), // NOI18N
            createRO(SoftBevelBorder.class, "highlightOuterColor"), // NOI18N
            createRO(SoftBevelBorder.class, "highlightInnerColor"), // NOI18N
            createRO(SoftBevelBorder.class, "shadowOuterColor"), // NOI18N
            createRO(SoftBevelBorder.class, "shadowInnerColor"), // NOI18N
        };
        pds[0].setPropertyEditorClass(BevelBorderBeanInfo.BevelTypePropertyEditor.class);
        return pds;
    }
    
}
