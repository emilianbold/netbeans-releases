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
package org.netbeans.modules.wag.manager.wizards;

import java.awt.Color;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import java.util.Arrays;
import javax.swing.*;
import org.netbeans.modules.wag.manager.model.WagSearchResult;
import org.netbeans.modules.wag.manager.model.WagSearchResults;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Enables searching for Web Services, via an URL, on the local file system
 * or in some uddiRegistry (UDDI)
 * @author Winston Prakash, cao
 */
public class AddSearchDlg extends JPanel implements ActionListener {

    private DialogDescriptor dlg = null;
    private Dialog dialog;
    private String defaultMsg;
    private WagSearchResults searchResults;

    public AddSearchDlg(WagSearchResults results) {
        initComponents();
        myInitComponents();
        this.searchResults = results;
    }

    private void setErrorMessage(String msg) {
        if (msg == null || msg.length() == 0) {
            errorLabel.setVisible(false);

            if (dlg != null) {
                dlg.setValid(true);
            }
        } else {
            errorLabel.setVisible(true);
            errorLabel.setText(msg);

            if (dlg != null) {
                if (msg.equals(defaultMsg)) {
                    dlg.setValid(true);
                } else {
                    dlg.setValid(false);
                }
            }
        }
    }

    private void checkValues() {
        setErrorMessage(null);
    }

    private void myInitComponents() {

        searchQueryTF.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                checkValues();
            }

            public void removeUpdate(DocumentEvent e) {
                checkValues();
            }

            public void changedUpdate(DocumentEvent e) {
                checkValues();
            }
        });

        maxResultsTF.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                checkValues();
            }

            public void removeUpdate(DocumentEvent e) {
                checkValues();
            }

            public void changedUpdate(DocumentEvent e) {
                checkValues();
            }
        });
    }

    public void displayDialog() {

        dlg = new DialogDescriptor(this, NbBundle.getMessage(AddSearchDlg.class, "LBL_RefineSearch"),
                true, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), this);

        //dlg.setOptions(new Object[]{addButton, cancelButton});
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dlg.setValid(false);
        dialog.setVisible(true);

        if (dlg.getValue() == DialogDescriptor.OK_OPTION) {
            addSearchResult();
        }
    }

    private void addSearchResult() {
        String query = searchQueryTF.getText().trim();
        int maxResults = Integer.parseInt(maxResultsTF.getText().trim());

        searchResults.addItems(Arrays.asList(new WagSearchResult(query, maxResults)));
    }

    /** XXX once we implement context sensitive help, change the return */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_add_websvcdb");
    }

    public void actionPerformed(ActionEvent evt) {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        queryLabel = new javax.swing.JLabel();
        searchQueryTF = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();
        errorLabel.setVisible(false);
        maxResultsLabel = new javax.swing.JLabel();
        maxResultsTF = new javax.swing.JTextField();

        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        queryLabel.setLabelFor(searchQueryTF);
        org.openide.awt.Mnemonics.setLocalizedText(queryLabel, org.openide.util.NbBundle.getMessage(AddSearchDlg.class, "LBL_SearchQuery")); // NOI18N

        searchQueryTF.setColumns(20);
        searchQueryTF.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchQueryTFMouseClicked(evt);
            }
        });

        errorLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        maxResultsLabel.setLabelFor(maxResultsTF);
        org.openide.awt.Mnemonics.setLocalizedText(maxResultsLabel, org.openide.util.NbBundle.getMessage(AddSearchDlg.class, "LBL_MaxResults")); // NOI18N

        maxResultsTF.setText("25");
        maxResultsTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxResultsTFActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(queryLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchQueryTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(maxResultsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(maxResultsTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(queryLabel)
                    .add(searchQueryTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxResultsLabel)
                    .add(maxResultsTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchQueryTFMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchQueryTFMouseClicked
        searchQueryTF.selectAll();
        searchQueryTF.setForeground(Color.BLACK);
}//GEN-LAST:event_searchQueryTFMouseClicked

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
// TODO add your handling code here:
}//GEN-LAST:event_formAncestorAdded

private void maxResultsTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxResultsTFActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_maxResultsTFActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel maxResultsLabel;
    private javax.swing.JTextField maxResultsTF;
    private javax.swing.JLabel queryLabel;
    private javax.swing.JTextField searchQueryTF;
    // End of variables declaration//GEN-END:variables
}
