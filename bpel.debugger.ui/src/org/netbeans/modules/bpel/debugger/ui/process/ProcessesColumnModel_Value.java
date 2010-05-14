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

package org.netbeans.modules.bpel.debugger.ui.process;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.util.AbstractColumn;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * 
 * @author Kirill Sorokin
 */
public final class ProcessesColumnModel_Value extends AbstractColumn {
    private BpelDebugger myDebugger;
    
    public ProcessesColumnModel_Value(final ContextProvider context) {
        super();
        
        myDebugger = context.lookupFirst(null, BpelDebugger.class);
        
        myId = COLUMN_ID;
        myName = "CTL_Process_Column_Value"; // NOI18N
        myTooltip = "CTL_Process_Column_Value_Tooltip"; // NOI18N
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
            final Object value = getValue();
            
            if (value instanceof String) {
                return (String) value;
            } else {
                return myHelper.getValue(value);
            }
        }
        
        @Override
        public boolean supportsCustomEditor() {
            final Object value = getValue();
            
            return (value != null) && !value.toString().equals("");
        }
        
        @Override
        public Component getCustomEditor() {
            final Object value = getValue();
            
            if (value instanceof String) {
                return new ColumnCustomEditor((String) value);
            } else {
                return new ColumnCustomEditor(this, myHelper);
            }
        }
        
        public void attachEnv(
                final PropertyEnv propertyEnv) {
            // does nothing
        }
    }
    
    public static class ColumnCustomEditor extends JPanel {
        
        private JEditorPane myEditorPane;
        
        private ColumnPropertyEditor myEditor;
        private VariablesUtil myHelper;
        
        private String myValue;
        
        public ColumnCustomEditor(
                final ColumnPropertyEditor editor,
                final VariablesUtil helper) {
            myHelper = helper;
            myEditor = editor;
            
            init();
        }
        
        public ColumnCustomEditor(
                final String value) {
            myValue = value;
            
            init();
        }
        
        private void init() {
            final String text;
            final String mimeType;
            
            if (myValue == null) {
                text = 
                        myHelper.getCustomEditorValue(myEditor.getValue());
                mimeType = 
                        myHelper.getCustomEditorMimeType(myEditor.getValue());
            } else {
                text = myValue;
                mimeType = "text/plain"; // NOI18N
            }
            
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(12, 12, 0, 11));
            setPreferredSize (new java.awt.Dimension(640, 480));
            
            getAccessibleContext().setAccessibleDescription(
                    "ACSD_BpelVariableCustomEditor"); // NOI18N
            
            myEditorPane = new JEditorPane(mimeType, text); // NOI18N
            
            myEditorPane.setBorder(
                new CompoundBorder(myEditorPane.getBorder(),
                new EmptyBorder(2, 0, 2, 0))
            );
            myEditorPane.setEditable(false);
            myEditorPane.setText(text);
            myEditorPane.requestFocus();
            myEditorPane.setCaretPosition(0);
            
            myEditorPane.getAccessibleContext().
                    setAccessibleName("ACS_EditorPane"); // NOI18N
            myEditorPane.getAccessibleContext().
                    setAccessibleDescription("ACSD_EditorPane"); // NOI18N
            
            final JScrollPane scrollPane = new JScrollPane(myEditorPane);
            add(scrollPane, BorderLayout.CENTER);
        }
        
        private static final long serialVersionUID = 1L;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String COLUMN_ID
        = "ProcessValueColumn"; // NOI18N
}
