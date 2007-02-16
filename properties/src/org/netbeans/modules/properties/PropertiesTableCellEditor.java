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


package org.netbeans.modules.properties;


import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * @author Petr Jiricka
 */
public class PropertiesTableCellEditor extends DefaultCellEditor {

    /** Value holding info if the editing cell is a key or value. */
    private boolean isKeyCell;

    /** Generated serial version UID. */
    static final long serialVersionUID =-5292598860635851664L;
    
    /** Document listener. */
    private DocumentListener listener;
    
    /** Value component */
    private JTextComponent valueComponent;
    
    /** Comment component */
    private JTextComponent commentComponent;

    /** Class representing settings used in table view. */
    private final TableViewSettings settings;
    
    
    /** Constructs a PropertiesTableCellEditor that uses a text field.
    * @param x  a JTextField object ...
    */
    public PropertiesTableCellEditor(JTextField tf, final JTextComponent commentComponent,
        final JTextComponent valueComponent, final JLabel valueLabel, DocumentListener listener) {
        super(tf);
        // Number of clicks needed to edit an editable cell.
        this.clickCountToStart = 1;
        this.listener = listener;
        this.valueComponent = valueComponent;
        this.commentComponent = commentComponent;
        valueComponent.setDocument(tf.getDocument());
        this.delegate = new PropertiesEditorDelegate(commentComponent, valueComponent, valueLabel);
        ((JTextField)editorComponent).addActionListener(delegate);
        
        settings = TableViewSettings.getDefault();
    }

    
    /** Overrides superclass method. 
    * It sets the cursot at the beginnig of edited cell, in case of searching it highlights the found text.
    * At the end it request for focus so the editor component (JTextField) has it, not the table.
    * This is also a hack with reason to figure out which cell is going to be edited, if a key or a value.
    */
    public Component getTableCellEditorComponent(JTable table,
        Object value, boolean isSelected, int row, int column) {
            
        // Key or value? Only in the first column are keys.
        isKeyCell = (column == 0) ? true : false;
        
        valueComponent.getDocument().removeDocumentListener(listener);
        commentComponent.getDocument().removeDocumentListener(listener);
        final JTextField textField = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        valueComponent.getDocument().addDocumentListener(listener);
        commentComponent.getDocument().addDocumentListener(listener);
        Caret caret = textField.getCaret();
        caret.setVisible(true);
        caret.setDot(0);
        
        textField.setFont(settings.getFont());
        
        // Check for search results.
        // If search was performed, highlight the found string.
        int[] result = (int[])table.getClientProperty(FindPerformer.TABLE_SEARCH_RESULT);
        if(result != null && row == result[0] && column == result[1]) {
            table.putClientProperty(FindPerformer.TABLE_SEARCH_RESULT, null); // removes property
            caret.setDot(result[2]);
            caret.moveDot(result[3]);
        }

        return textField;
    }


    /** Inner class which is cell editor delegate. */
    private class PropertiesEditorDelegate extends DefaultCellEditor.EditorDelegate {

        /** Reference to text component showing comments on bundle edit table. */
        JTextComponent commentComponent;
        /** Reference to text component showing key or value respectively on bundle edit table. */
        JTextComponent valueComponent;
        /** Reference to the value label. */
        JLabel valueLabel;

        /** Generated serial version UID. */
        static final long serialVersionUID =9082979978712223677L;
        
        
        /** Constructor. */        
        public PropertiesEditorDelegate(JTextComponent commentComponent, JTextComponent valueComponent, JLabel valueLabel) {
            this.commentComponent = commentComponent;
            this.valueComponent = valueComponent;
            this.valueLabel = valueLabel;
        }
        

        /** Overrides superclass method. */
        public void setValue(Object x) {
            // PENDING - due to a compiler error explicitly do "super" code instead of calling super
            this.value = x;
            //super.setValue(x);
            PropertiesTableModel.StringPair sp = (PropertiesTableModel.StringPair)x;

            // set values as they deserve
            if (sp != null) {
                //!!! text area is required due to multiline values
                // otherwise the textfield removes new lines
                ((JTextField)getComponent()).setText(sp.getValue());
                commentComponent.setText(sp.getComment());
            } else {
                ((JTextField)getComponent()).setText(""); // NOI18N
                commentComponent.setText(""); // NOI18N
            }
        }

        /** Overrides superclass method. */
        public Object getCellEditorValue() {
            String value = ((JTextField)getComponent()).getText();
            
            // Cell is a properties key.
            if(isKeyCell) {
                Mnemonics.setLocalizedText(valueLabel, NbBundle.getBundle(PropertyPanel.class).getString("LBL_KeyLabel"));
            }
            // Cell is a properties value.
            else {
                Mnemonics.setLocalizedText(valueLabel, NbBundle.getBundle(BundleEditPanel.class).getString("LBL_ValueLabel"));
            }
            
            // the cell is a properties key 
            return new PropertiesTableModel.StringPair(commentComponent.getText(),value,isKeyCell);
        }

    } // End of inner PropertiesEditorDelegate class.
}
