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
