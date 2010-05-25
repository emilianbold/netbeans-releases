/*
 * DelimLengthPropertyEditor.java
 *
 * Created on January 6, 2007, 10:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.encoder.custom.aip;

import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * Property editor of the embedded delimiter length property.
 *
 * @author Jun Xu
 */
public class DelimLengthPropertyEditor extends PropertyEditorSupport
    implements ExPropertyEditor {
    
    private static final ShortIntegerInplaceEditor mInplaceEditor =
            new ShortIntegerInplaceEditor((short) 0, (short) 100);

    public void attachEnv(PropertyEnv propertyEnv) {
        FeatureDescriptor desc = propertyEnv.getFeatureDescriptor();
        // Provide own InplaceEditor for editing the string value
        desc.setValue("inplaceEditor", mInplaceEditor); // NOI18N
    }
}
