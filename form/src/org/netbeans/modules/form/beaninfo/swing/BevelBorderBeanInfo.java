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
import javax.swing.border.BevelBorder;

public class BevelBorderBeanInfo extends BISupport {

    public BevelBorderBeanInfo() {
        super("bevelBorder", javax.swing.border.BevelBorder.class); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRO(BevelBorder.class, "bevelType"), // NOI18N
            createRO(BevelBorder.class, "highlightOuterColor"), // NOI18N
            createRO(BevelBorder.class, "highlightInnerColor"), // NOI18N
            createRO(BevelBorder.class, "shadowOuterColor"), // NOI18N
            createRO(BevelBorder.class, "shadowInnerColor"), // NOI18N
        };
        pds[0].setPropertyEditorClass(BevelTypePropertyEditor.class);
        return pds;
    }    

    public static class BevelTypePropertyEditor extends BISupport.TaggedPropertyEditor {
        public BevelTypePropertyEditor() {
            super(
                new int[] {
                    BevelBorder.RAISED,
                    BevelBorder.LOWERED,
                },
                new String[] {
                    "javax.swing.border.BevelBorder.RAISED", // NOI18N
                    "javax.swing.border.BevelBorder.LOWERED", // NOI18N
                },
                new String[] {
                    "VALUE_BevelRaised",  // NOI18N
                    "VALUE_BevelLowered", // NOI18N
                }
            );
        }
    }

}
