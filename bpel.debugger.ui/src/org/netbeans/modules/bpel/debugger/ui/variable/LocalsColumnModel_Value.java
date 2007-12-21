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
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.util.AbstractColumn;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * 
 * @author Kirill Sorokin
 */
public final class LocalsColumnModel_Value extends AbstractColumn {
    private BpelDebugger myDebugger;
    
    public LocalsColumnModel_Value(final ContextProvider context) {
        super();
        
        myDebugger = (BpelDebugger) context.lookupFirst(
                null, BpelDebugger.class);
        
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
        
        public ColumnPropertyEditor(
                final BpelDebugger debugger) {
            
            myHelper = new VariablesUtil(debugger);
        }
        
        @Override
        public String getAsText() {
            return myHelper.getValue(getValue());
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return myHelper.supportsCustomEditor(getValue());
        }
        
        @Override
        public Component getCustomEditor() {
            final String text = 
                    myHelper.getCustomEditorValue(getValue());
            final String mimeType = 
                    myHelper.getCustomEditorMimeType(getValue());
            final boolean editable = 
                    !myHelper.isValueReadOnly(getValue());
            
            return new ColumnCustomEditor(text, mimeType, editable, this);
        }
        
        public void attachEnv(
                final PropertyEnv propertyEnv) {
            // does nothing
        }
    }
    
    public static class ColumnCustomEditor extends JPanel {
        
        private JEditorPane myEditorPane;
        
        private String myText;
        private String myMimeType;
        private boolean myEditable;
        private ColumnPropertyEditor myEditor;
        
        public ColumnCustomEditor(
                final String text, 
                final String mimeType,
                final boolean editable, 
                final ColumnPropertyEditor editor) {
            myText = text;
            myMimeType = mimeType;
            myEditable = editable;
            myEditor = editor;
            
            init();
        }
        
        public void init() {
            final ResourceBundle bundle = 
                    NbBundle.getBundle(LocalsColumnModel_Value.class);
            
            
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(12, 12, 0, 11));
            setPreferredSize (new java.awt.Dimension(640, 480));
            
            getAccessibleContext().setAccessibleDescription(
                    "ACSD_BpelVariableCustomEditor"); // NOI18N
            
            myEditorPane = new JEditorPane(myMimeType, myText); // NOI18N
            
            myEditorPane.setBorder(
                new CompoundBorder(myEditorPane.getBorder(),
                new EmptyBorder(2, 0, 2, 0))
            );
            myEditorPane.setEditable(myEditable);
            myEditorPane.setText(myText);
            myEditorPane.requestFocus();
            myEditorPane.setCaretPosition(0);
            
            myEditorPane.getDocument().addDocumentListener(
                    new DocumentListener() {
                public void insertUpdate(DocumentEvent event) {
                    myEditor.setValue(myEditorPane.getText());
                }
                
                public void removeUpdate(DocumentEvent event) {
                    myEditor.setValue(myEditorPane.getText());
                }
                
                public void changedUpdate(DocumentEvent event) {
                    myEditor.setValue(myEditorPane.getText());
                }
            });
            
            myEditorPane.getAccessibleContext().
                    setAccessibleName("ACS_EditorPane"); // NOI18N
            myEditorPane.getAccessibleContext().
                    setAccessibleDescription("ACSD_EditorPane"); // NOI18N
            
            final JScrollPane scrollPane = new JScrollPane(myEditorPane);
            add(scrollPane, BorderLayout.CENTER);
        }
    }
}
