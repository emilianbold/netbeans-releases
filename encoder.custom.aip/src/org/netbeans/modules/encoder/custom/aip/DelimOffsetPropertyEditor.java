/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.custom.aip;

import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * Property editor of the embedded delimiter offset property.
 *
 * @author Jun Xu
 */
public class DelimOffsetPropertyEditor extends PropertyEditorSupport
    implements ExPropertyEditor {
    
    private static final IntegerInplaceEditor mIntegerInplaceEditor =
            new IntegerInplaceEditor(0, Integer.MAX_VALUE);

    public void attachEnv(PropertyEnv propertyEnv) {
        FeatureDescriptor desc = propertyEnv.getFeatureDescriptor();
        // Provide own InplaceEditor for editing the string value
        desc.setValue("inplaceEditor", mIntegerInplaceEditor); // NOI18N
    }
}
