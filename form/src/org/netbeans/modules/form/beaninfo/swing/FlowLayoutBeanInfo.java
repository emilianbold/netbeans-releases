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
import java.awt.FlowLayout;

public class FlowLayoutBeanInfo extends BISupport {

    public FlowLayoutBeanInfo() {
        super("flowLayout", java.awt.FlowLayout.class); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRW(FlowLayout.class, "alignment"), // NOI18N
            createRW(FlowLayout.class, "hgap"), // NOI18N
            createRW(FlowLayout.class, "vgap"), // NOI18N
        };
        pds[0].setPropertyEditorClass(AlignmentPropertyEditor.class);
        return pds;
    }

    
    
    public static class AlignmentPropertyEditor extends BISupport.TaggedPropertyEditor {
        public AlignmentPropertyEditor() {
            super(
                new int[] {
                    FlowLayout.CENTER,
                    FlowLayout.LEFT,
                    FlowLayout.RIGHT
                },
                new String[] {
                    "java.awt.FlowLayout.CENTER", // NOI18N
                    "java.awt.FlowLayout.LEFT", // NOI18N
                    "java.awt.FlowLayout.RIGHT" // NOI18N
                },
                new String[] {
                    "VALUE_AlignmentCenter", // NOI18N
                    "VALUE_AlignmentLeft", // NOI18N
                    "VALUE_AlignmentRight", // NOI18N
                }
            );
        }
    }

}
