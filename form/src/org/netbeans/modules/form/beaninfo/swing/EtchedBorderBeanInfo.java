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
