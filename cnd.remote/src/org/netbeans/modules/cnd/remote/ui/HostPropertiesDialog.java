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
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.sync.SyncUtils;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.Validateable;
import org.netbeans.modules.nativeexecution.api.util.ValidationListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public class HostPropertiesDialog extends JPanel {

    private final ValidationListener validationListener;
    private final JButton ok;
    private final Validateable vpanel;

    public static boolean invokeMe(RemoteServerRecord record) {
        HostPropertiesDialog pane = new HostPropertiesDialog(record);

        Object[] buttons = new Object[]{
            pane.ok,
            DialogDescriptor.CANCEL_OPTION
        };

        DialogDescriptor dd = new DialogDescriptor(
                pane, NbBundle.getMessage(HostPropertiesDialog.class, "TITLE_HostProperties"),
                true, buttons, pane.ok, DialogDescriptor.DEFAULT_ALIGN, null, null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == pane.ok) {
            pane.vpanel.applyChanges();
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
        validationListener = new ValidationListenerImpl();

        initComponents();

        ok = new JButton("OK"); // NOI18N

        JPanel panel = ConnectionManager.getInstance().getConfigurationPanel(serverRecord.getExecutionEnvironment());
        vpanel = (panel instanceof Validateable) ? (Validateable) panel : null;

        if (vpanel != null) {
            vpanel.addValidationListener(validationListener);
        }

        cfgPanel.add(panel);
        tfName.setText(serverRecord.getDisplayName());
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
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
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

        jButton2 = new javax.swing.JButton();
        lblName = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        lblSync = new javax.swing.JLabel();
        cbSync = new javax.swing.JComboBox();
        cbX11 = new javax.swing.JCheckBox();
        cfgPanel = new javax.swing.JPanel();

        jButton2.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.jButton2.text")); // NOI18N

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setFocusCycleRoot(true);

        lblName.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblName.text")); // NOI18N

        tfName.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfName.text")); // NOI18N

        lblSync.setLabelFor(cbSync);
        org.openide.awt.Mnemonics.setLocalizedText(lblSync, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblSync.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbX11, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.cbX11.text")); // NOI18N
        cbX11.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cfgPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSync)
                    .addComponent(lblName))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                    .addComponent(cbSync, javax.swing.GroupLayout.Alignment.TRAILING, 0, 370, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(cfgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbX11, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(240, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cfgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSync)
                    .addComponent(cbSync, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbX11))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbSync;
    private javax.swing.JCheckBox cbX11;
    private javax.swing.JPanel cfgPanel;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblSync;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables

    private class ValidationListenerImpl implements ValidationListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() instanceof Validateable) {
                Validateable obj = (Validateable) e.getSource();
                final boolean isOK = !obj.hasProblem();
                Mutex.EVENT.readAccess(new Runnable() {

                    @Override
                    public void run() {
                        ok.setEnabled(isOK);
                    }
                });
            }
        }
    }
}
