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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.plinks;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.bpel.debugger.ui.util.AbstractColumn;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * 
 * @author Kirill Sorokin
 */
public final class PLinksColumnModel_Value extends AbstractColumn {
    public PLinksColumnModel_Value() {
        super();
        
        myId = PLinksTableModel.VALUE_COLUMN_ID;
        myName = "CTL_Column_Value"; // NOI18N
        myTooltip = "CTL_Column_Value_Tooltip"; // NOI18N
        myType = String.class;
    }

//    @Override
//    public PropertyEditor getPropertyEditor() {
//        return new ColumnPropertyEditor();
//    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ColumnPropertyEditor extends PropertyEditorSupport
            implements ExPropertyEditor {
            
        public ColumnPropertyEditor() {
            // does nothing
        }

        @Override
        public boolean supportsCustomEditor() {
            if ("".equals(getValue())) {
                return false;
            }
            
            return super.supportsCustomEditor();
        }
        
        public void attachEnv(
                final PropertyEnv propertyEnv) {
            // does nothing
        }
    }
}
