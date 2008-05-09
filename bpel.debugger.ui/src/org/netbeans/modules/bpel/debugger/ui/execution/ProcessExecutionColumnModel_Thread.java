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

package org.netbeans.modules.bpel.debugger.ui.execution;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.bpel.debugger.ui.util.AbstractColumn;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * 
 * @author Kirill Sorokin
 */
public final class ProcessExecutionColumnModel_Thread extends AbstractColumn {
    public ProcessExecutionColumnModel_Thread() {
        super();
        
        myId = COLUMN_ID;
        myName = "CTL_PEV_Column_Thread"; // NOI18N
        myTooltip = "CTL_PEV_Column_Thread_Tooltip"; // NOI18N
        myType = String.class;
        mySortable = false;
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        return new ColumnPropertyEditor();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String COLUMN_ID = 
            "ThreadColumn"; // NOI18N
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ColumnPropertyEditor extends PropertyEditorSupport
            implements ExPropertyEditor {
            
        public ColumnPropertyEditor() {
            // does nothing
        }
        
        public void attachEnv(
                final PropertyEnv propertyEnv) {
            // does nothing
        }
    }
}
