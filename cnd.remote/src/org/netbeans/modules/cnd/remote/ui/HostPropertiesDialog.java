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
            record.setRememberPassword(pane.rememberPassword.isSelected());
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
//            if (record.isX11forwardingPossible()) {
                boolean x11forwarding = pane.cbX11.isSelected();
                if (x11forwarding != record.getX11Forwarding()) {
                    record.setX11Forwarding(x11forwarding);
                    changed = true;
                }
//            }
            if (changed) {
                RemoteServerList.storePreferences(record);
                return true;
            }
        }
        return false;
    }


    /** Creates new form HostPropertiesDialog */
    @org.netbeans.api.annotations.common.SuppressWarnings("Se") // it's never serialized!
    private HostPropertiesDialog(RemoteServerRecord serverRecord) {
        this.serverRecord = serverRecord;
        initComponents();
        tfHost.setBackground(getBackground());
        tfPort.setBackground(getBackground());
        tfUser.setBackground(getBackground());
        tfName.setText(serverRecord.getDisplayName());
        tfHost.setText(serverRecord.getServerName());
        tfUser.setText(serverRecord.getUserName());
        rememberPassword.setSelected(serverRecord.isRememberPassword());
        tfPort.setText("" + serverRecord.getExecutionEnvironment().getSSHPort());
        SyncUtils.arrangeComboBox(cbSync, serverRecord.getExecutionEnvironment());
        cbSync.setSelectedItem(serverRecord.getSyncFactory());
        cbX11.setSelected(serverRecord.getX11Forwarding());
//        // if x11forwarding is set, but we consider it is unavailable,
//        // we should at least allow switching it off => || serverRecord.getX11Forwarding()
//        cbX11.setEnabled(serverRecord.isX11forwardingPossible() || serverRecord.getX11Forwarding());
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                tfName.requestFocus();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}
            @Override
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
        cbX11 = new javax.swing.JCheckBox();
        rememberPassword = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setFocusCycleRoot(true);

        lblHost.setLabelFor(tfHost);
        org.openide.awt.Mnemonics.setLocalizedText(lblHost, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblHost.text")); // NOI18N

        tfHost.setEditable(false);
        tfHost.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfHost.text")); // NOI18N

        lblPort.setLabelFor(tfPort);
        org.openide.awt.Mnemonics.setLocalizedText(lblPort, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblPort.text")); // NOI18N

        tfPort.setEditable(false);
        tfPort.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfPort.text")); // NOI18N

        lblUser.setLabelFor(tfUser);
        org.openide.awt.Mnemonics.setLocalizedText(lblUser, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblUser.text")); // NOI18N

        tfUser.setEditable(false);
        tfUser.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfUser.text")); // NOI18N

        lblName.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblName.text")); // NOI18N

        tfName.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfName.text")); // NOI18N

        lblSync.setLabelFor(cbSync);
        org.openide.awt.Mnemonics.setLocalizedText(lblSync, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblSync.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbX11, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.cbX11.text")); // NOI18N
        cbX11.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rememberPassword, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.rememberPassword.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUser)
                            .addComponent(lblHost))
                        .addGap(76, 76, 76)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfHost, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblPort)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfPort, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tfUser, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblName, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSync, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbX11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .addComponent(tfName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .addComponent(cbSync, javax.swing.GroupLayout.Alignment.TRAILING, 0, 288, Short.MAX_VALUE)
                            .addComponent(rememberPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHost)
                    .addComponent(tfHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPort))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(tfUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(rememberPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSync)
                    .addComponent(cbSync, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(cbX11))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbSync;
    private javax.swing.JCheckBox cbX11;
    private javax.swing.JLabel lblHost;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblSync;
    private javax.swing.JLabel lblUser;
    private javax.swing.JCheckBox rememberPassword;
    private javax.swing.JTextField tfHost;
    private javax.swing.JTextField tfName;
    private javax.swing.JTextField tfPort;
    private javax.swing.JTextField tfUser;
    // End of variables declaration//GEN-END:variables

}
