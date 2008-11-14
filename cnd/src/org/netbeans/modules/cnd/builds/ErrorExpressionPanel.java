/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.builds;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.settings.MakeSettings;
//import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *  Custom editor for ErrorExpression property of make execution.
 */
public final class ErrorExpressionPanel extends javax.swing.JPanel
					/*implements EnhancedCustomPropertyEditor*/ {

    /** Reference to ErrorDescriptionPropertyEditor */
    private final ErrorExpressionEditor editor;

    static final long serialVersionUID =-2763818133650482979L;

    /**
     *  Initializes the Form
     *  @param ed an ErrorDescriptionPropertyEditor instance
     */
    public ErrorExpressionPanel(ErrorExpressionEditor ed) {
        editor = ed;
        // make a copy of the current value
        descriptions = (ErrorExpression) ed.getValue();

        initComponents();

        // localize components
        setBorder(new CompoundBorder(new EmptyBorder(new Insets(2, 2, 2, 2)),
                       new TitledBorder(new EtchedBorder(),
		       getString("CTL_Error_description_name"))));

        addButton.setText(getString("CTL_Add"));
        addButton.setToolTipText(getString("CTL_Add_tip"));
        removeButton.setText(getString("CTL_Remove"));
        removeButton.setToolTipText(getString("CTL_Remove_tip"));
        changeButton.setText(getString("CTL_Change"));
        changeButton.setToolTipText(getString("CTL_Change_tip"));
        presetNameLabel.setText(getString("CTL_Preset_label"));
        errorDescriptionLabel.setText(getString("CTL_Error_label"));
        filePositionLabel.setText(getString("CTL_File_label"));
        linePositionLabel.setText(getString("CTL_Line_label"));
        columnPositionLabel.setText(getString("CTL_Column_label"));
        descriptionPositionLabel.setText(getString("CTL_Description_label"));

        ListSelectionListener l = new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent ev) {
		if (internalListChange) {
		    return;
		}

		int sel = errorDescriptions.getSelectedIndex();
		if (sel < 0) {
		    return;
		}
		descriptions = editor.getExpressions()[sel];
		updateFields();
		updateButtons();
	    }
        };
        errorDescriptions.addListSelectionListener(l);
        updateList();
        updateFields();
        updateButtons();

        HelpCtx.setHelpIDString(this, ErrorExpressionPanel.class.getName());
    }

    /**
     *  Gettero for the property value that is result of the CustomPropertyEditor.
     *
     *  @return The property value
     *  @throws IllegalStateException when the custom property editor does not
     *   represent valid property value (and thus it should not be set)
     * FIXUP: EnhancedCustomPropertyEditor is now deprecated so this method will not get called anymore.
     * FIXUP: See TargetEditor how to rewrite...
     */
    public Object getPropertyValue() throws IllegalStateException {
        return descriptions;
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();

        if (d.width < 400) {
            d.width = 400;
        }

        return d;
    }

    private void updateList() {
        ErrorExpression[] exprs = editor.getExpressions();
        ErrorExpression sel = descriptions;

        String[] strings = new String [exprs.length];
        int selIndex = -1;
        for (int i = 0; i < exprs.length; i++) {
            strings[i] = "";//exprs[i].getName(); // FIXUP - TRUNK - THP // NOI18N
            //if (exprs[i].getName().equals(sel.getName())) selIndex = i; // FIXUP - TRUNK - THP
        }

        internalListChange = true;
        errorDescriptions.setListData(strings);
        internalListChange = false;
        if (selIndex != -1) {
            errorDescriptions.setSelectedIndex(selIndex);
	}

    }

    private void updateFields() {
        ErrorExpression current = descriptions;
        //presetNameField.setText(current.getName()); // FIXUP - TRUNK - THP
        //errorDescriptionField.setText(current.getErrorExpression()); // FIXUP - TRUNK - THP
        //filePositionField.setText(String.valueOf(current.getFilePos())); // FIXUP - TRUNK - THP
        //linePositionField.setText(String.valueOf(current.getLinePos())); // FIXUP - TRUNK - THP
        //columnPositionField.setText(String.valueOf(current.getColumnPos())); // FIXUP - TRUNK - THP
        //descriptionPositionField.setText(String.valueOf(current.getDescriptionPos())); // FIXUP - TRUNK - THP
    }

    private void updateButtons() {
        boolean enabled = errorDescriptions.getSelectedIndex()!= -1;
        
        if (enabled) {
            enabled = !isReadOnly(descriptions);
        }
        removeButton.setEnabled(enabled);
        changeButton.setEnabled(enabled);
        addButton.setEnabled(!"".equals(presetNameField.getText())); // NOI18N
    }

    private boolean isReadOnly(ErrorExpression sel) {
        if (sel.equals(MakeSettings.SUN_COMPILERS)) {
            return true;
	}
        if (sel.equals(MakeSettings.GNU_COMPILERS)) {
            return true;
	}

        return false;
    }


    /**
     *  This method is called from within the constructor to initialize the dialog.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 0)));
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;

        descriptionPanel = new javax.swing.JPanel ();
        descriptionPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;

        presetNameLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (presetNameLabel, gridBagConstraints2);

        presetNameField = new javax.swing.JTextField ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        descriptionPanel.add (presetNameField, gridBagConstraints2);

        errorDescriptionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (errorDescriptionLabel, gridBagConstraints2);

        errorDescriptionField = new javax.swing.JTextField ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.ipadx = 20;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        descriptionPanel.add (errorDescriptionField, gridBagConstraints2);

        filePositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (filePositionLabel, gridBagConstraints2);

        filePositionField = new javax.swing.JTextField ();
        filePositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (filePositionField, gridBagConstraints2);

        linePositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (linePositionLabel, gridBagConstraints2);

        linePositionField = new javax.swing.JTextField ();
        linePositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (linePositionField, gridBagConstraints2);

        columnPositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (columnPositionLabel, gridBagConstraints2);

        columnPositionField = new javax.swing.JTextField ();
        columnPositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (columnPositionField, gridBagConstraints2);

        descriptionPositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (descriptionPositionLabel, gridBagConstraints2);

        descriptionPositionField = new javax.swing.JTextField ();
        descriptionPositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (descriptionPositionField, gridBagConstraints2);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        add (descriptionPanel, gridBagConstraints1);

        buttonsPanel = new javax.swing.JPanel ();
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 8, 0, 0)));
        buttonsPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;

        addButton = new javax.swing.JButton ();
        addButton.setEnabled (false);
        addButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             addButtonActionPerformed (evt);
                                         }
                                     }
                                    );
        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (2, 2, 2, 5);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.SOUTH;
        buttonsPanel.add (addButton, gridBagConstraints3);

        removeButton = new javax.swing.JButton ();
        removeButton.setEnabled (false);
        removeButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                removeButtonActionPerformed (evt);
                                            }
                                        }
                                       );
        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (2, 2, 2, 5);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints3.weighty = 1.0;
        buttonsPanel.add (removeButton, gridBagConstraints3);

        changeButton = new javax.swing.JButton ();
        changeButton.setEnabled (false);
        changeButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             changeButtonActionPerformed (evt);
                                         }
                                     }
                                    );
        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (2, 2, 2, 5);
        buttonsPanel.add (changeButton, gridBagConstraints3);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        add (buttonsPanel, gridBagConstraints1);

        presetsScroll = new javax.swing.JScrollPane ();

        errorDescriptions = new javax.swing.JList ();
        errorDescriptions.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                errorDescriptionsMouseClicked(evt);
            }
        });

        presetsScroll.setViewportView (errorDescriptions);
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (presetsScroll, gridBagConstraints1);

    }//GEN-END:initComponents

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        synchronized (editor) {//GEN-HEADEREND:event_removeButtonActionPerformed
            java.util.Collection exprs = editor.getExpressionsVector ();
            int pos = errorDescriptions.getSelectedIndex ();
            exprs.remove (descriptions);
            if (pos >= exprs.size()) {
                pos = exprs.size() - 1;
            }
            if (pos >= 0) {
                descriptions = editor.getExpressions ()[pos];
            } else {
                descriptions = null;
            }
            updateList ();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void changeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        if ("".equals (presetNameField.getText ())) { // NOI18N
            return;
        }
        ErrorExpression expr = descriptions;
        int fPos = 0;
        int lPos = 0;
        int cPos = 0;
        int dPos = 0;
        try {
            fPos = Integer.parseInt (filePositionField.getText ());
            lPos = Integer.parseInt (linePositionField.getText ());
            cPos = Integer.parseInt (columnPositionField.getText ());
            dPos = Integer.parseInt (descriptionPositionField.getText ());
        } catch (NumberFormatException ex) { // ignored
            return; // [PENDING - notify user]
        }
        //expr.setName(presetNameField.getText()); // FIXUP - TRUNK - THP
        //expr.setErrorExpression(errorDescriptionField.getText()); // FIXUP - TRUNK - THP
        //expr.setFilePos(fPos); // FIXUP - TRUNK - THP
        //expr.setLinePos(lPos); // FIXUP - TRUNK - THP
        //expr.setColumnPos(cPos); // FIXUP - TRUNK - THP
        //expr.setDescriptionPos(dPos); // FIXUP - TRUNK - THP
        updateList ();
    }//GEN-LAST:event_changeButtonActionPerformed

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if ("".equals (presetNameField.getText ())) {// NOI18N
            return;
        }
        ErrorExpression[] exprsAr = editor.getExpressions();
	/* // FIXUP - TRUNK - THP
        for (int i = 0; i < exprsAr.length; i++)
            if (exprsAr[i].getName ().equals (presetNameField.getText ()))
                return;
	*/ // FIXUP - TRUNK - THP

        ErrorExpression expr = null;
        try {
            expr = new ErrorExpression (
                       presetNameField.getText (),
                       errorDescriptionField.getText (),
                       Integer.parseInt (filePositionField.getText ()),
                       Integer.parseInt (linePositionField.getText ()),
                       Integer.parseInt (columnPositionField.getText ()),
                       Integer.parseInt (descriptionPositionField.getText ())
                   );
        } catch (NumberFormatException ex) { // ignored
            return; // [PENDING - notify user]
        }
        synchronized (editor) {
            java.util.Collection<ErrorExpression> exprs = editor.getExpressionsVector();
            int pos = errorDescriptions.getSelectedIndex();
            exprs.add(expr);
            descriptions = expr;
            updateList();
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void errorDescriptionsMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_errorDescriptionsMouseClicked
        // Add your handling code here:
        //    int index = errorDescriptions.locationToIndex(evt.getPoint());
        //    setSelected(index);
    }//GEN-LAST:event_errorDescriptionsMouseClicked



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane presetsScroll;
    private javax.swing.JLabel presetNameLabel;
    private javax.swing.JTextField presetNameField;
    private javax.swing.JLabel errorDescriptionLabel;
    private javax.swing.JTextField errorDescriptionField;
    private javax.swing.JLabel filePositionLabel;
    private javax.swing.JTextField filePositionField;
    private javax.swing.JLabel linePositionLabel;
    private javax.swing.JTextField linePositionField;
    private javax.swing.JLabel columnPositionLabel;
    private javax.swing.JTextField columnPositionField;
    private javax.swing.JLabel descriptionPositionLabel;
    private javax.swing.JTextField descriptionPositionField;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton changeButton;
    private javax.swing.JList errorDescriptions;
    // End of variables declaration//GEN-END:variables

    private ErrorExpression descriptions;
    private boolean internalListChange = false;

    /** Getter for resource string.
    */
    private static String getString (String res) {
        return NbBundle.getBundle (ErrorExpressionPanel.class).getString (res);
    }
}

/*
* Log
*/
