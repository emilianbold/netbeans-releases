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

package org.netbeans.modules.bpel.debugger.ui.variable;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.util.AbstractColumn;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * 
 * @author Kirill Sorokin
 */
public final class LocalsColumnModel_Type extends AbstractColumn {
    private BpelDebugger myDebugger;
    
    public LocalsColumnModel_Type(final ContextProvider context) {
        super();
        
        myDebugger = context.lookupFirst(null, BpelDebugger.class);
        
        myId = Constants.LOCALS_TYPE_COLUMN_ID;
        myName = "CTL_Variable_Column_Type";
        myTooltip = "CTL_Variable_Column_Type_Tooltip";
        myType = String.class;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new ColumnPropertyEditor(myDebugger);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ColumnPropertyEditor extends PropertyEditorSupport
            implements ExPropertyEditor {
            
        private VariablesUtil myHelper;
        
        public ColumnPropertyEditor(
                final BpelDebugger debugger) {
            myHelper = new VariablesUtil(debugger);
        }
        
        @Override
        public String getAsText() {
            if (getValue() instanceof LocalsTreeModel.Dummy) {
                return "";
            }
            
            if (getValue() == null) {
                return "";
            }
            
            return myHelper.getType(getValue());
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void attachEnv(
                final PropertyEnv propertyEnv) {
            // does nothing
        }
    }
}
