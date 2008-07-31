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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.explorer.dlg;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.openide.util.NbBundle;
import javax.swing.border.EmptyBorder;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.modules.db.explorer.*;
import org.openide.awt.Mnemonics;

/**
* @author Slavek Psenicka
*/
public class LabeledTextFieldDialog {
    boolean result = false;
    Dialog dialog = null;
    Object combosel = null;
    JTextField field;
    JTextArea notesarea;
    JButton edButton;
    JLabel label;
    final String original_notes;
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    public LabeledTextFieldDialog(String notes) {
        String title = bundle.getString("RecreateTableRenameTable");
        String lab = bundle.getString("RecreateTableNewName");
        original_notes = notes;
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GroupLayout layout = new GroupLayout(pane);
            pane.setLayout(layout);

            // Title

            label = new JLabel();
            Mnemonics.setLocalizedText(label, lab);
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_RecreateTableNewNameA11yDesc"));  // NOI18N

            // Textfield

            field = new JTextField(35);
            field.getAccessibleContext().setAccessibleName(bundle.getString("ACS_RecreateTableNewNameTextFieldA11yName"));  // NOI18N
            field.setToolTipText(bundle.getString("ACS_RecreateTableNewNameTextFieldA11yDesc"));  // NOI18N

            // Descr.

            JLabel desc = new JLabel();
            Mnemonics.setLocalizedText(desc, bundle.getString("RecreateTableRenameNotes"));
            desc.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_RecreateTableRenameNotesA11yDesc"));  // NOI18N

            // Notes

            notesarea = new JTextArea(notes, 10, 50);
            notesarea.setEditable(false);
            notesarea.setLineWrap(true);
            notesarea.setWrapStyleWord(true);
            notesarea.setFont(label.getFont());
            notesarea.setBackground(label.getBackground()); // grey
            notesarea.setEnabled(false);
            notesarea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
            notesarea.getAccessibleContext().setAccessibleName(bundle.getString("ACS_RecreateTableTableScriptTextAreaA11yName"));  // NOI18N
            notesarea.setToolTipText(bundle.getString("ACS_RecreateTableTableScriptTextAreaA11yDesc"));  // NOI18N
            notesarea.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            JScrollPane spane = new JScrollPane(notesarea);
            
            // edit button

            edButton = new JButton();
            Mnemonics.setLocalizedText(edButton, bundle.getString("EditCommand")); // NOI18N
            edButton.setToolTipText(bundle.getString("ACS_EditCommandA11yDesc"));  // NOI18N

            // setup the layout
             
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(GroupLayout.LEADING)
                        .add(spane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                        .add(layout.createSequentialGroup()
                            .add(label)
                            .add(18, 18, 18)
                        .add(field, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
                    .add(GroupLayout.LEADING, desc)
                    .add(GroupLayout.LEADING, edButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(GroupLayout.BASELINE)
                        .add(label)
                        .add(field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(desc)
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(spane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(edButton)
                .addContainerGap())
            );

            edButton.addActionListener(new ActionListener() {
                private boolean noedit = true;
                public void actionPerformed(ActionEvent event) {
                    if(noedit) { // NOI18N
                        // set to edit 
                        noedit = false;
                        Mnemonics.setLocalizedText(edButton, bundle.getString("ReloadCommand"));
                        edButton.setToolTipText(bundle.getString("ACS_ReloadCommandA11yDesc"));  // NOI18N
                        notesarea.setEditable( true );
                        notesarea.setEnabled(true);
                        notesarea.setBackground(field.getBackground()); // white
                        notesarea.requestFocus();
                        field.setEditable( false );
                        field.setBackground(label.getBackground()); // grey
                    } else {
                        // reload script from file
                        noedit = true;
                        Mnemonics.setLocalizedText(edButton, bundle.getString("EditCommand"));
                        edButton.setToolTipText(bundle.getString("ACS_EditCommandA11yDesc"));  // NOI18N
                        notesarea.setText(original_notes);
                        notesarea.setEditable( false );
                        notesarea.setEnabled(false);
                        notesarea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
                        field.setEditable( true );
                        field.setBackground(notesarea.getBackground()); // grey
                        notesarea.setBackground(label.getBackground()); // white
                        field.requestFocus();
                    }
                }
            });

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION)
                        result = true;
                    else
                        result = false;;
                }
            };

            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_RecreateTableDialogA11yDesc"));

            DialogDescriptor descriptor = new DialogDescriptor(pane, title, true, listener);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null)
            dialog.setVisible(true);
        
        return result;
    }

    public String getStringValue() {
        return field.getText();
    }

    public String getEditedCommand() {
        return notesarea.getText();
    }

    public boolean isEditable() {
        return notesarea.isEditable();
    }

    public void setStringValue(String val) {
        field.setText(val);
    }
}
