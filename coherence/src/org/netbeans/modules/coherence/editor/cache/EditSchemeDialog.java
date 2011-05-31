/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.editor.cache;

import org.netbeans.modules.coherence.editor.cache.scheme.TransactionalSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.SchemePanelInterface;
import org.netbeans.modules.coherence.editor.cache.scheme.ReplicatedSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.ReadWriteBackingMapSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.ProxySchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.OverflowSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.OptimisticSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.NearSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.LocalSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.InvocationSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.ExternalSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.DistributedSchemePanel;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JPanel;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class EditSchemeDialog extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form EditSchemeDialog */
    public EditSchemeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initialise();
    }
    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public EditSchemeDialog(java.awt.Frame parent, boolean modal, List<Scheme> schemeNameList) {
        this(parent, modal);
        this.schemeList = schemeNameList;
        selectDefaultScheme();
    }
    /*
     * Properties
     */
    private List<Scheme> schemeList = null;
    private SchemePanelInterface[] panels = {new DistributedSchemePanel(), new ExternalSchemePanel(), new InvocationSchemePanel(),
                            new LocalSchemePanel(), new NearSchemePanel(), new OptimisticSchemePanel(), new OverflowSchemePanel(),
                            new ProxySchemePanel(), new ReadWriteBackingMapSchemePanel(), new ReplicatedSchemePanel(), new TransactionalSchemePanel()};
    /*
     * Methods
     */
    private void initialise() {
        cbSchemeType.setModel(getSchemeTypeModel());
    }

    private void selectDefaultScheme() {
        cbSchemeType.setSelectedIndex(0);
    }

    private ComboBoxModel getSchemeTypeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        if (panels != null) {
            model = new DefaultComboBoxModel(panels);
        }
        return model;
    }

    public Object getScheme() {
        return ((SchemePanelInterface)selectedSchemePanel).getScheme();
    }
    /*
     * =========================================================================
     * END: Custom Code
     * =========================================================================
     */

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
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
        cbSchemeType = new javax.swing.JComboBox();
        buttonPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        schemePanel = new javax.swing.JPanel();
        selectedSchemePanel = new javax.swing.JPanel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(EditSchemeDialog.class, "EditSchemeDialog.jLabel1.text")); // NOI18N

        cbSchemeType.setModel(getSchemeTypeModel());
        cbSchemeType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSchemeTypeActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(EditSchemeDialog.class, "EditSchemeDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText(org.openide.util.NbBundle.getMessage(EditSchemeDialog.class, "EditSchemeDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(355, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton))
        );

        buttonPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        schemePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(EditSchemeDialog.class, "EditSchemeDialog.schemePanel.border.title"))); // NOI18N

        selectedSchemePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(EditSchemeDialog.class, "EditSchemeDialog.selectedSchemePanel.border.title"))); // NOI18N

        javax.swing.GroupLayout selectedSchemePanelLayout = new javax.swing.GroupLayout(selectedSchemePanel);
        selectedSchemePanel.setLayout(selectedSchemePanelLayout);
        selectedSchemePanelLayout.setHorizontalGroup(
            selectedSchemePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 471, Short.MAX_VALUE)
        );
        selectedSchemePanelLayout.setVerticalGroup(
            selectedSchemePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 375, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout schemePanelLayout = new javax.swing.GroupLayout(schemePanel);
        schemePanel.setLayout(schemePanelLayout);
        schemePanelLayout.setHorizontalGroup(
            schemePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectedSchemePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        schemePanelLayout.setVerticalGroup(
            schemePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectedSchemePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(schemePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbSchemeType, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbSchemeType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(schemePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void cbSchemeTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSchemeTypeActionPerformed
        ((GroupLayout)schemePanel.getLayout()).replace(selectedSchemePanel, (JPanel)cbSchemeType.getSelectedItem());
        selectedSchemePanel = (JPanel)cbSchemeType.getSelectedItem();
        ((SchemePanelInterface)selectedSchemePanel).hideTitle();
        ((SchemePanelInterface)selectedSchemePanel).setSchemeList(schemeList);
    }//GEN-LAST:event_cbSchemeTypeActionPerformed

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EditSchemeDialog dialog = new EditSchemeDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox cbSchemeType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel schemePanel;
    private javax.swing.JPanel selectedSchemePanel;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;
}
