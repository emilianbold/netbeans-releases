/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * HostPropertiesDialog.java
 *
 * Created on Apr 28, 2009, 1:51:26 AM
 */

package org.netbeans.modules.cnd.remote.ui;

import java.awt.Dialog;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.sync.SyncUtils;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public class HostPropertiesDialog extends JPanel {

    private final ServerRecord serverRecord;

    public static boolean invokeMe(RemoteServerRecord record) {
        HostPropertiesDialog pane = new HostPropertiesDialog(record);
        DialogDescriptor dd = new DialogDescriptor(
                pane, NbBundle.getMessage(HostPropertiesDialog.class, "TITLE_HostProperties"),
                true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            String displayName = pane.tfName.getText();
            boolean changed = false;
            if (!displayName.equals(record.getDisplayName())) {
                record.setDisplayName(displayName);
                changed = true;
            }
            RemoteSyncFactory syncFactory = (RemoteSyncFactory) pane.cbSync.getSelectedItem();
            if (!syncFactory.equals(record.getSyncFactory())) {
                record.setSyncFactory(syncFactory);
                changed = true;
            }
            if (changed) {
                RemoteServerList.storePreferences(record);
                return true;
            }
        }
        return false;
    }


    /** Creates new form HostPropertiesDialog */
    private HostPropertiesDialog(ServerRecord serverRecord) {
        this.serverRecord = serverRecord;
        initComponents();
        tfHost.setBackground(getBackground());
        tfPort.setBackground(getBackground());
        tfUser.setBackground(getBackground());
        tfName.setText(serverRecord.getDisplayName());
        tfHost.setText(serverRecord.getServerName());
        tfUser.setText(serverRecord.getUserName());
        tfPort.setText("" + serverRecord.getExecutionEnvironment().getSSHPort());
        SyncUtils.arrangeComboBox(cbSync, serverRecord.getExecutionEnvironment());
        cbSync.setSelectedItem(serverRecord.getSyncFactory());
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                tfName.requestFocus();
            }
            public void ancestorRemoved(AncestorEvent event) {}
            public void ancestorMoved(AncestorEvent event) {}
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHost = new javax.swing.JLabel();
        tfHost = new javax.swing.JTextField();
        lblPort = new javax.swing.JLabel();
        tfPort = new javax.swing.JTextField();
        lblUser = new javax.swing.JLabel();
        tfUser = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        lblSync = new javax.swing.JLabel();
        cbSync = new javax.swing.JComboBox();

        setFocusCycleRoot(true);

        lblHost.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblHost.text")); // NOI18N

        tfHost.setEditable(false);
        tfHost.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfHost.text")); // NOI18N
        tfHost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfHostActionPerformed(evt);
            }
        });

        lblPort.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblPort.text")); // NOI18N

        tfPort.setEditable(false);
        tfPort.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfPort.text")); // NOI18N
        tfPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfPortActionPerformed(evt);
            }
        });

        lblUser.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblUser.text")); // NOI18N

        tfUser.setEditable(false);
        tfUser.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfUser.text")); // NOI18N
        tfUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUserActionPerformed(evt);
            }
        });

        lblName.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblName.text")); // NOI18N

        tfName.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfName.text")); // NOI18N
        tfName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfNameActionPerformed(evt);
            }
        });

        lblSync.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblSync.text")); // NOI18N

        cbSync.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblName)
                            .add(lblUser)
                            .add(lblHost))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(tfHost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(lblPort)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(tfPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(tfUser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                            .add(tfName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(lblSync)
                        .add(18, 18, 18)
                        .add(cbSync, 0, 263, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblHost)
                    .add(tfHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tfPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblPort))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblUser)
                    .add(tfUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(tfName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblSync)
                    .add(cbSync, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tfHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfHostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfHostActionPerformed

    private void tfUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUserActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_tfUserActionPerformed

    private void tfPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfPortActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_tfPortActionPerformed

    private void tfNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNameActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_tfNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbSync;
    private javax.swing.JLabel lblHost;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblSync;
    private javax.swing.JLabel lblUser;
    private javax.swing.JTextField tfHost;
    private javax.swing.JTextField tfName;
    private javax.swing.JTextField tfPort;
    private javax.swing.JTextField tfUser;
    // End of variables declaration//GEN-END:variables

}
