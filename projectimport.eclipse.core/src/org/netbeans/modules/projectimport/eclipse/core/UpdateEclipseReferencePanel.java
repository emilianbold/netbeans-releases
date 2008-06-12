/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.awt.Dialog;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;

/**
 *
 */
class UpdateEclipseReferencePanel extends javax.swing.JPanel implements DocumentListener {

    private DialogDescriptor dd;
    
    /** Creates new form UpdateEclipseReferencePanel */
    private UpdateEclipseReferencePanel(EclipseProjectReference reference) {
        initComponents();
        eclipseProjectTextField.setText(reference.getEclipseProjectLocation().getPath());
        eclipseProjectTextField.setEnabled(!reference.getEclipseProjectLocation().exists());
        browseProjectButton.setEnabled(!reference.getEclipseProjectLocation().exists());
        eclipseWorkspaceTextField.setText(reference.getEclipseWorkspaceLocation().getPath());
        eclipseWorkspaceTextField.setEnabled(!reference.getEclipseWorkspaceLocation().exists());
        browseWorkspaceButton.setEnabled(!reference.getEclipseWorkspaceLocation().exists());
    }

    public void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
        eclipseProjectTextField.getDocument().addDocumentListener(this);
        eclipseWorkspaceTextField.getDocument().addDocumentListener(this);
        updateStatus();
    }

    private void updateStatus() {
        String errorMsg = null;
        if (eclipseProjectTextField.isEnabled()) {
            if (!EclipseUtils.isRegularProject(eclipseProjectTextField.getText())) {
                errorMsg = "Eclipse project must be selected.";
            }
        }
        if (errorMsg == null && eclipseWorkspaceTextField.isEnabled()) {
            if (!EclipseUtils.isRegularWorkSpace(eclipseWorkspaceTextField.getText())) {
                errorMsg = "Eclipse wokrspace must be selected.";
            }
        }
        dd.setValid(errorMsg == null);
        error.setText(errorMsg == null ? " " : errorMsg);
    }


    public static boolean showEclipseReferenceResolver(EclipseProjectReference ref) {
        UpdateEclipseReferencePanel p = new UpdateEclipseReferencePanel(ref);
        DialogDescriptor dd = new DialogDescriptor (p, "Original Eclipse data cannot be found",
            true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        p.setDialogDescriptor(dd);
        Dialog dlg = DialogDisplayer.getDefault().createDialog (dd);
        dlg.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            ref.updateReference(
                    p.eclipseProjectTextField.isEnabled() ? p.eclipseProjectTextField.getText() : null,
                    p.eclipseWorkspaceTextField.isEnabled() ? p.eclipseWorkspaceTextField.getText() : null);
            return true;
        }
        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        eclipseProjectTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        eclipseWorkspaceTextField = new javax.swing.JTextField();
        browseProjectButton = new javax.swing.JButton();
        browseWorkspaceButton = new javax.swing.JButton();
        error = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.jLabel1.text")); // NOI18N

        eclipseProjectTextField.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.eclipseProjectTextField.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.jLabel2.text")); // NOI18N

        eclipseWorkspaceTextField.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.eclipseWorkspaceTextField.text")); // NOI18N

        browseProjectButton.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.browseProjectButton.text")); // NOI18N
        browseProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectButtonActionPerformed(evt);
            }
        });

        browseWorkspaceButton.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.browseWorkspaceButton.text")); // NOI18N
        browseWorkspaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseWorkspaceButtonActionPerformed(evt);
            }
        });

        error.setForeground(java.awt.Color.red);
        error.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(eclipseProjectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseProjectButton))
                            .add(layout.createSequentialGroup()
                                .add(eclipseWorkspaceTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseWorkspaceButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(browseProjectButton)
                    .add(eclipseProjectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(browseWorkspaceButton)
                    .add(eclipseWorkspaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(error)
                .add(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void browseProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle("Select Eclipse Project");
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        eclipseProjectTextField.setText(file.getAbsolutePath());
    }
}//GEN-LAST:event_browseProjectButtonActionPerformed

private void browseWorkspaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseWorkspaceButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle("Select Eclipse Workspace");
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        eclipseWorkspaceTextField.setText(file.getAbsolutePath());
    }
}//GEN-LAST:event_browseWorkspaceButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseProjectButton;
    private javax.swing.JButton browseWorkspaceButton;
    private javax.swing.JTextField eclipseProjectTextField;
    private javax.swing.JTextField eclipseWorkspaceTextField;
    private javax.swing.JLabel error;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    public void insertUpdate(DocumentEvent arg0) {
        updateStatus();
    }

    public void removeUpdate(DocumentEvent arg0) {
        updateStatus();
    }

    public void changedUpdate(DocumentEvent arg0) {
        
    }

}
