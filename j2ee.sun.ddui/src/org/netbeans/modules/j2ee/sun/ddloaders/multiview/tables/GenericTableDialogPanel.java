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
/*
 * GenericTableDialogPanel.java
 *
 * Created on October 8, 2003, 1:47 PM
 */

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.util.List;
import java.util.ResourceBundle;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;


/**
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class GenericTableDialogPanel extends JPanel implements GenericTableDialogPanelAccessor {

    private static final ResourceBundle bundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N		

    private List<TableEntry> fieldEntries;
    private String [] values;
    private JTextField [] textFields;
    private int preferredWidth;

    /** Creates generic field entry panel using unvalidated JTextFields for all 
     *  inputs.
     */
    public GenericTableDialogPanel() {
    }

    public void init(ASDDVersion asVersion, int width, List<TableEntry> entries, Object data) {
        // data field not used in generic dialog.	
        fieldEntries = entries;
        preferredWidth = width;
        values = new String [entries.size()];
        textFields = new JTextField [entries.size()];

        initUserComponents();
    }

    public void setValues(Object[] v) {
        if(v != null && v.length == values.length) {
            for(int i = 0; i < values.length && i < v.length; i++) {
                values[i] = (v[i] != null) ? v[i].toString() : "";	// NOI18N
            }
        } else {
            if(v != null) {
                assert (v.length == values.length);	// Should fail
            }

            // default values
            for(int i = 0; i < values.length; i++) {
                values[i] = "";	// NOI18N
            }
        }

        setComponentValues();
    }

    public Object [] getValues() {
        return values;
    }

    private void setComponentValues() {
        for(int i = 0; i < values.length; i++) {
            textFields[i].setText(values[i]);
        }
    }

    private void initUserComponents() {
        GridBagConstraints gridBagConstraints;

        // !PW TODO migrate this to GroupLayout (what a pain -- see NewJPanel.java
        // in this folder for an example.)

        // panel parameters
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
        setPreferredSize(new Dimension(preferredWidth, 22*fieldEntries.size()+8));

        for(int i = 0; i < fieldEntries.size(); i++) {
            TableEntry entry = fieldEntries.get(i);
            JLabel requiredMark = new JLabel();
            JLabel label = new JLabel();
            textFields[i] = new JTextField();

            // First control is either empty label or '*' label to mark required
            // field.
            if(entry.isRequiredField()) {
                requiredMark.setText(bundle.getString("LBL_RequiredMark"));	// NOI18N
                requiredMark.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_RequiredMark"));	// NOI18N
                requiredMark.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_RequiredMark"));	// NOI18N
            }
            requiredMark.setLabelFor(textFields[i]);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 0, 4, 4);
            add(requiredMark, gridBagConstraints);

            // Initialize and add label
            label.setLabelFor(textFields[i]);
            label.setText(entry.getLabelName());	// NOI18N
            label.setDisplayedMnemonic(entry.getLabelMnemonic());
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 0, 4, 4);
            add(label, gridBagConstraints);

            // Initialize and add text field
            textFields[i].addKeyListener(new TextFieldHandler(textFields[i], i));
            textFields[i].getAccessibleContext().setAccessibleName(entry.getAccessibleName());
            textFields[i].getAccessibleContext().setAccessibleDescription(entry.getAccessibleDescription());

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, 0, 4, 0);
            add(textFields[i], gridBagConstraints);
        }	
    }
	
//    public Collection getErrors(ValidationSupport validationSupport) {
//        ArrayList errorList = new ArrayList();
//
//        // Only record one error per field.
//        for(int i = 0; i < fieldEntries.size(); i++) {
//            TableEntry entry = fieldEntries.get(i);
//            if(entry.isRequiredField()) {
//                if(!Utils.notEmpty(values[i])) {
//                    Object [] args = new Object [1];
//                    args[0] = entry.getColumnName();
//                    errorList.add(MessageFormat.format(bundle.getString("ERR_SpecifiedFieldIsEmpty"), args));
//                    continue;
//                }
//            }
//
//            if(entry.isNameField()) {
//                if(Utils.containsWhitespace(values[i])) {
//                    Object [] args = new Object [1];
//                    args[0] = entry.getColumnName();
//                    errorList.add(MessageFormat.format(bundle.getString("ERR_NameFieldContainsWhitespace"), args));
//                    continue;
//                }
//            }
//
//            // Validate that this field is an integer value.  Empty is acceptable
//            // as well (use required field flag to force non-empty fields.)
////            if(entry.isIntegerField()) {
////                if(Utils.notInteger(values[i])) {
////                    Object [] args = new Object [1];
////                    args[0] = entry.getColumnName();
////                    errorList.add(MessageFormat.format(bundle.getString("ERR_FieldMustBeInteger"), args));
////                    continue;
////                }
////            }
//        }
//
//        return errorList;
//    }

    public boolean requiredFieldsFilled() {
        boolean result = true;

        for(int i = 0; i < fieldEntries.size(); i++) {
            TableEntry entry = fieldEntries.get(i);
            if(entry.isRequiredField()) {
                if(!Utils.notEmpty(values[i])) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /** private class to allow easy binding between each text field in the array
     *  of JTextFields and the corresponding String in the array of values entered
     *  by the user.
     */
    private class TextFieldHandler extends KeyAdapter {
        private JTextField textField;
        private int controlIndex;

        public TextFieldHandler(JTextField tf, int index) {
            textField = tf;
            controlIndex = index;
        }

        public void keyReleased(KeyEvent evt) {
            values[controlIndex] = textField.getText();
            firePropertyChange(USER_DATA_CHANGED, null, null);
        }		
    }
}
