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

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.netbeans.spi.debugger.ui.Constants;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * 
 * @author Kirill Sorokin
 */
public final class LocalsColumnModel_Value extends AbstractColumn {
    private BpelDebugger myDebugger;
    
    public LocalsColumnModel_Value(final ContextProvider context) {
        super();
        
        myDebugger = context.lookupFirst(null, BpelDebugger.class);
        
        myId = Constants.LOCALS_VALUE_COLUMN_ID;
        myName = "CTL_Variable_Column_Value";
        myTooltip = "CTL_Variable_Column_Value_Tooltip";
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
        private PropertyEnv myPropertyEnv;
        
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
            
            // Sometimes it's "Evaluating..." by the good will of the 
            // debuggercore.
            if (getValue() instanceof String) {
                return (String) getValue();
            }
            
            return ((LocalsTableModel.Pair) getValue()).getValue();
        }
        
        @Override
        public void setAsText(
                final String value) {
            
            setValue(new LocalsTableModel.Pair(((LocalsTableModel.Pair) getValue()).getKey(), value));
        }
        
        @Override
        public boolean supportsCustomEditor() {
            if (getValue() instanceof LocalsTreeModel.Dummy) {
                return false;
            }
            
            if (getValue() == null) {
                return false;
            }
            
            // Sometimes it's "Evaluating..." by the good will of the 
            // debuggercore.
            if (getValue() instanceof String) {
                return false;
            }
            
            return myHelper.supportsCustomEditor(((LocalsTableModel.Pair) getValue()).getKey());
        }
        
        @Override
        public Component getCustomEditor() {
            return new ColumnCustomEditor(
                    this, myHelper, myPropertyEnv);
        }
        
        public void attachEnv(
                final PropertyEnv propertyEnv) {
            myPropertyEnv = propertyEnv;
        }
    }
    
    public static class ColumnCustomEditor extends JPanel 
            implements PropertyChangeListener {
        
        private JEditorPane myEditorPane;
        
        private ColumnPropertyEditor myEditor;
        private VariablesUtil myHelper;
        private PropertyEnv myPropertyEnv;
        
        public ColumnCustomEditor(
                final ColumnPropertyEditor editor,
                final VariablesUtil helper,
                final PropertyEnv propertyEnv) {
            myHelper = helper;
            myEditor = editor;
            myPropertyEnv = propertyEnv;
            
            init();
            
            myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            myPropertyEnv.addPropertyChangeListener(this);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && 
                    (evt.getNewValue() == PropertyEnv.STATE_VALID)) {
                myEditor.setAsText(myEditorPane.getText());
            }
        }
        
        private void init() {
            final String text = 
                    myHelper.getCustomEditorValue(((LocalsTableModel.Pair) myEditor.getValue()).getKey());
            final String mimeType = 
                    myHelper.getCustomEditorMimeType(((LocalsTableModel.Pair) myEditor.getValue()).getKey());
            final boolean editable = 
                    !myHelper.isValueReadOnly(((LocalsTableModel.Pair) myEditor.getValue()).getKey());
            
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
            myEditorPane.setEditable(editable);
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
}
