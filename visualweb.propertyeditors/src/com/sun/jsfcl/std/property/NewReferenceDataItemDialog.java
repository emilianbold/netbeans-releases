/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.jsfcl.std.property;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import com.sun.jsfcl.std.reference.CompositeReferenceData;
import com.sun.jsfcl.std.reference.ReferenceDataItem;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NewReferenceDataItemDialog extends JPanel implements DocumentListener,
    UndoableEditListener, ActionListener {

    protected JButton cancelButton;
    protected JDialog dialog;
    protected JTextField nameTextField;
    protected JButton okButton;
    protected CompositeReferenceData referenceData;
    protected JCheckBox sameCheckBox;
    protected JTextField valueTextField;
    protected ReferenceDataItem newItem;

    public NewReferenceDataItemDialog(CompositeReferenceData referenceData) {

        this.referenceData = referenceData;
        initializeComponents();
    }

    public void actionPerformed(ActionEvent event) {

        Object source = event.getSource();
        if (source == sameCheckBox) {
            if (isValueSameAsName()) {
                valueTextField.setEditable(false);
            } else {
                valueTextField.setEditable(true);
                valueTextField.selectAll();
                valueTextField.requestFocusInWindow();
            }
            handleNameTextChanged();
        } else if (source == okButton) {
            handleOkButton();
        } else if (source == cancelButton) {
            if (dialog != null) {
                dialog.setVisible(false);
            }
        }
    }

    public void changedUpdate(DocumentEvent e) {

        if (e.getDocument() == nameTextField.getDocument()) {
            handleNameTextChanged();
        } else if (e.getDocument() == valueTextField.getDocument()) {
            handleValueTextChanged();
        }
    }

    protected void addDialogButtons() {

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        okButton = new JButton(BundleHolder.bundle.getMessage("OK")); //NOI18N
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        cancelButton = new JButton(BundleHolder.bundle.getMessage("Cancel")); //NOI18N
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(buttonPanel, gridBagConstraints);
    }

    public void handleNameTextChanged() {

        if (isValueSameAsName()) {
            valueTextField.setText(nameTextField.getText());
        }
    }

    public void handleValueTextChanged() {

    }

    protected void handleOkButton() {

        String name = nameTextField.getText().trim();
        String value = valueTextField.getText().trim();
        newItem = referenceData.getDefiner().newItem(name, value, null, false, true);
        newItem.setIsRemovable(true);
        String message;
        if (value.length() == 0) {
            message = BundleHolder.bundle.getMessage("newReferenceDataItemEmptyValueError"); //NOI18N
        } else if (referenceData.getItems().indexOf(newItem) >= 0) {
            message = BundleHolder.bundle.getMessage("newReferenceDataItemAlreadyExistError", value); //NOI18N
        } else {
            message = null;
        }
        if (message == null) {
            referenceData.add(newItem);
            dialog.setVisible(false);
        } else {
            newItem = null;
            JOptionPane.showMessageDialog(
                dialog,
                message);
        }
    }

    protected void initializeComponents() {
        GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        JLabel label = new JLabel();
        label.setText(BundleHolder.bundle.getMessage("newReferenceDataItemName")); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 5);
        add(label, gridBagConstraints);

        nameTextField = new JTextField();
        nameTextField.setColumns(20);
        nameTextField.getDocument().addDocumentListener(this);
        nameTextField.getDocument().addUndoableEditListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(nameTextField, gridBagConstraints);

        label = new JLabel();
        label.setText(BundleHolder.bundle.getMessage("newReferenceDataItemValue")); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 5);
        add(label, gridBagConstraints);

        valueTextField = new JTextField();
        valueTextField.setEditable(false);
        valueTextField.getDocument().addDocumentListener(this);
        valueTextField.getDocument().addUndoableEditListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(valueTextField, gridBagConstraints);

        sameCheckBox = new JCheckBox(BundleHolder.bundle.getMessage(
            "newReferenceDataItemSameCheckbox"), true);
        sameCheckBox.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        add(sameCheckBox, gridBagConstraints);

    }

    protected boolean isValueSameAsName() {

        return sameCheckBox.getModel().isSelected();
    }

    public void insertUpdate(DocumentEvent e) {

        if (e.getDocument() == nameTextField.getDocument()) {
            handleNameTextChanged();
        } else if (e.getDocument() == valueTextField.getDocument()) {
            handleValueTextChanged();
        }
    }

    public void removeUpdate(DocumentEvent e) {

        if (e.getDocument() == nameTextField.getDocument()) {
            handleNameTextChanged();
        } else if (e.getDocument() == valueTextField.getDocument()) {
            handleValueTextChanged();
        }
    }

    public ReferenceDataItem showDialog(JPanel parent) {

        addDialogButtons();
        Dialog parentDialog = (Dialog)parent.getRootPane().getParent();
        dialog = new JDialog(
            parentDialog,
            BundleHolder.bundle.getMessage("newReferenceDataItemPanelTitle",
            referenceData.getDisplayName()), // NOI18N
            true);
        dialog.setContentPane(this);
        dialog.pack();
        dialog.setLocationRelativeTo(dialog);
        nameTextField.requestFocusInWindow();
        dialog.show();
        dialog.dispose();
        dialog = null;
        okButton = cancelButton = null;
        return newItem;
    }

    public void undoableEditHappened(UndoableEditEvent e) {

        if (e.getSource() == nameTextField) {
            handleNameTextChanged();
        } else if (e.getSource() == valueTextField) {
            handleValueTextChanged();
        }
    }

}
