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
import javax.swing.border.EtchedBorder;


public class EtchedBorderBeanInfo extends BISupport {

    public EtchedBorderBeanInfo() {
        super("etchedBorder", javax.swing.border.EtchedBorder.class); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRO(EtchedBorder.class, "etchType"), // NOI18N
            createRO(EtchedBorder.class, "highlightColor"), // NOI18N
            createRO(EtchedBorder.class, "shadowColor"), // NOI18N
        };
        pds[0].setPropertyEditorClass(EtchTypePropertyEditor.class);
        return pds;
    }

    public static class EtchTypePropertyEditor extends BISupport.TaggedPropertyEditor {
        public EtchTypePropertyEditor() {
            super(
                new int[] {
                    EtchedBorder.LOWERED,
                    EtchedBorder.RAISED,
                },
                new String[] {
                    "javax.swing.border.EtchedBorder.LOWERED", // NOI18N
                    "javax.swing.border.EtchedBorder.RAISED" // NOI18N
                },
                new String[] {
                    "VALUE_EtchLowered", // NOI18N
                    "VALUE_EtchRaised", // NOI18N
                }
            );
        }
    }
    
}
