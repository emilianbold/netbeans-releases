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
package org.netbeans.modules.cnd.remote.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.remote.ui.setup.CreateHostWizardIterator;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Mange the removeServer development hosts list.
 * 
 * @author  gordonp
 */
public class EditServerListDialog extends JPanel implements ActionListener, PropertyChangeListener, ListSelectionListener {

    private DefaultListModel model;
    private DialogDescriptor desc;
    private ServerRecord defaultRecord;
    private ProgressHandle phandle;
    private PropertyChangeSupport pcs;
    private boolean buttonsEnabled;
    private final ToolsCacheManager cacheManager;
    private final AtomicReference<ExecutionEnvironment> selectedEnv;

    private static final String CMD_ADD = "Add"; // NOI18N
    private static final String CMD_REMOVE = "Remove"; // NOI18N
    private static final String CMD_DEFAULT = "SetAsDefault"; // NOI18N
    private static final String CMD_PATHMAPPER = "PathMapper"; // NOI18N
    private static final String CMD_PROPERTIES = "Properties"; // NOI18N
    private static final String CMD_RETRY = "Retry"; // NOI18N

    public EditServerListDialog(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv) {
        this.cacheManager = cacheManager;
        initComponents();
        initServerList(cacheManager.getServerUpdateCache());
        desc = null;
        //lbReason.setText(" "); // NOI18N - this keeps the dialog from resizing
        tfReason.setEnabled(false); // setVisible(false);
        pbarStatusPanel.setVisible(false);
        this.selectedEnv = selectedEnv;
        initListeners();
    }

    private void initListeners() {
        lstDevHosts.addListSelectionListener(this);
        btAddServer.addActionListener(this);
        btRemoveServer.addActionListener(this);
        btSetAsDefault.addActionListener(this);
        btPathMapper.addActionListener(this);
        btProperties.addActionListener(this);
        btRetry.addActionListener(this);

        btAddServer.setActionCommand(CMD_ADD);
        btRemoveServer.setActionCommand(CMD_REMOVE);
        btSetAsDefault.setActionCommand(CMD_DEFAULT);
        btPathMapper.setActionCommand(CMD_PATHMAPPER);
        btProperties.setActionCommand(CMD_PROPERTIES);
        btRetry.setActionCommand(CMD_RETRY);

        pcs = new PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(this);
        setButtons(true);
        valueChanged(null);
    }

    private void initServerList(ServerUpdateCache cache) {
        model = new DefaultListModel();

        if (cache == null) {
            for (ServerRecord rec : ServerList.getRecords()) {
                model.addElement(rec);
            }
            defaultRecord = ServerList.getDefaultRecord();
        } else {
            for (ServerRecord rec : cache.getHosts()) {
                model.addElement(rec);
            }
            defaultRecord = cache.getDefaultRecord();
        }
        lstDevHosts.setModel(model);
        lstDevHosts.setSelectedValue(defaultRecord, false);
        lstDevHosts.setCellRenderer(new MyCellRenderer());
    }

    private void checkDefaultButton(RemoteServerRecord record) {
        final ExecutionEnvironment env = record.getExecutionEnvironment();
        // make fast checks just in the UI thread, in which we are called
        if (record.equals(defaultRecord)) {
            btSetAsDefault.setEnabled(false);
        } else if (!buttonsEnabled) {
            btSetAsDefault.setEnabled(false);
        } else if (env.isLocal()) {
            btSetAsDefault.setEnabled(true);
        } else {
            // need to check remote tool chains in a separate thread
            btSetAsDefault.setEnabled(false);
            final Runnable enabler = new Runnable() {
                @Override
                public void run() {
                    // check if it has already changed
                    ServerRecord curr = (ServerRecord) lstDevHosts.getSelectedValue();
                    if (curr != null && curr.getExecutionEnvironment().equals(env)) {
                        btSetAsDefault.setEnabled(true);
                    }
                }
            };
            Runnable checker = new Runnable() {
                @Override
                public void run() {
                    CompilerSetManager compilerSetManagerCopy = cacheManager.getCompilerSetManagerCopy(env, false);
                    if (!compilerSetManagerCopy.isEmpty()) {
                        SwingUtilities.invokeLater(enabler);
                    }
                }
            };
            RequestProcessor.getDefault().post(checker);
        }
    }

    private void revalidateRecord(final RemoteServerRecord record, String password, boolean rememberPassword) {
        if (!record.isOnline()) {
            record.resetOfflineState(); // this is a do-over
            setButtons(false);
            hideReason();
            phandle = ProgressHandleFactory.createHandle("");
            pbarStatusPanel.removeAll();
            pbarStatusPanel.add(ProgressHandleFactory.createProgressComponent(phandle), BorderLayout.CENTER);
            pbarStatusPanel.setVisible(true);
//            revalidate();
            phandle.start();
            // TODO: not good to use object's toString as resource key
            tfStatus.setText(NbBundle.getMessage(RemoteServerRecord.class, RemoteServerRecord.State.INITIALIZING.toString()));
            // move expensive operation out of EDT
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    record.init(pcs);
                    if (record.isOnline()) {
                        CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(record.getExecutionEnvironment(), false);
                        csm.initialize(false, true, null);
                    }
                    phandle.finish();
                    // back to EDT to work with Swing
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            pbarStatusPanel.setVisible(false);
                            setButtons(true);
                            valueChanged(null);
                        }
                    });
                }
            });
        }
    }

    private final class MyCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel out = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                ServerRecord rec = (ServerRecord) value;
                out.setText(rec.getDisplayName());
                if (rec.equals(getDefaultRecord())) {
                    out.setFont(out.getFont().deriveFont(Font.BOLD));
                }
            }
            return out;
        }
    }

    public void setDialogDescriptor(DialogDescriptor desc) {
        this.desc = desc;
    }

    public List<ServerRecord> getHosts() {
        List<ServerRecord> result = new ArrayList<ServerRecord>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            result.add((ServerRecord) model.get(i));
        }
        return result;
    }

    public ServerRecord getDefaultRecord() {
        return defaultRecord;
    }

    private void showPathMapper() {
        EditPathMapDialog.showMe((ServerRecord) lstDevHosts.getSelectedValue(), getHosts());
    }

    private void setButtons(boolean enable) {
        buttonsEnabled = enable;
        if (desc != null) {
            desc.setValid(enable);
        }
        btAddServer.setEnabled(enable);
        btAddServer.setEnabled(enable);
        btRemoveServer.setEnabled(enable);
        btPathMapper.setEnabled(enable);
        btSetAsDefault.setEnabled(enable);
        btRetry.setEnabled(enable);
        btProperties.setEnabled(enable);
    }

    /** Helps the AddServerDialog know when to enable/disable the OK button */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        String prop = evt.getPropertyName();
        if (source instanceof DialogDescriptor && prop.equals(DialogDescriptor.PROP_VALID)) {
            ((DialogDescriptor) source).setValid(false);
        }
    }

    /** Enable/disable the Remove and Set As Default buttons */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        RemoteServerRecord record = (RemoteServerRecord) lstDevHosts.getSelectedValue();
        if (record != null) {
            tfStatus.setText(record.getStateAsText());
            btRemoveServer.setEnabled(record.isRemote() && buttonsEnabled);            
            checkDefaultButton(record);
            btProperties.setEnabled(record.isRemote());
            btPathMapper.setEnabled(buttonsEnabled && record.isRemote() && record.getSyncFactory().isPathMappingCustomizable());
            if (!record.isOnline()) {
                showReason(record.getReason());
                btRetry.setEnabled(true);
            } else {
                hideReason();
                btRetry.setEnabled(false);
            }
            if (selectedEnv != null) {
                selectedEnv.set(record.getExecutionEnvironment());
            }
        } else {
            RemoteUtil.LOGGER.warning("ESLD.valueChanged: No selection in Dev Hosts list");
            if (selectedEnv != null) {
                selectedEnv.set(null);
            }
        }
    }

    private void showReason(String reason) {
        lbReason.setText(NbBundle.getMessage(EditServerListDialog.class, "LBL_Reason"));
        tfReason.setText(reason);
        tfReason.setEnabled(true); // setVisible(true);
    }

    private void hideReason() {
        //lbReason.setText(" "); // NOI18N
        tfReason.setEnabled(false); // setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Object o = evt.getSource();

        if (o instanceof JButton) {
            JButton b = (JButton) o;
            if (b.getActionCommand().equals(CMD_ADD)) {
                cacheManager.setHosts(getHosts());
                ServerRecord result = CreateHostWizardIterator.invokeMe(cacheManager);
                if (result != null) {
                    if (!model.contains(result)) {
                        model.addElement(result);
                        lstDevHosts.setSelectedValue(result, true);
                    }
                }
            } else if (b.getActionCommand().equals(CMD_REMOVE)) {
                ServerRecord rec2delete = (ServerRecord) lstDevHosts.getSelectedValue();
                int idx = lstDevHosts.getSelectedIndex();
                if (rec2delete != null) {
                    model.removeElement(rec2delete);
                    lstDevHosts.setSelectedIndex(model.size() > idx ? idx : idx - 1);
                    if (defaultRecord.equals(rec2delete)) {
                        defaultRecord = (ServerRecord) lstDevHosts.getSelectedValue();
                    }
                }
                lstDevHosts.repaint();

            } else if (b.getActionCommand().equals(CMD_DEFAULT)) {
                defaultRecord = (ServerRecord) lstDevHosts.getSelectedValue();
                b.setEnabled(false);
                lstDevHosts.repaint();
            } else if (b.getActionCommand().equals(CMD_PATHMAPPER)) {
                showPathMapper();
            } else if (b.getActionCommand().equals(CMD_PROPERTIES)) {
                RemoteServerRecord record = (RemoteServerRecord) lstDevHosts.getSelectedValue();
                if (record.isRemote()) {
                    if (HostPropertiesDialog.invokeMe(record)) {
                        lstDevHosts.repaint();
                    }
                    btPathMapper.setEnabled(buttonsEnabled && record.isRemote() && record.getSyncFactory().isPathMappingCustomizable());
                }
            } else if (b.getActionCommand().equals(CMD_RETRY)) {
                this.revalidateRecord(getSelectedRecord(), null, false);
            }
        }
    }

    private RemoteServerRecord getSelectedRecord() {
        // we know for sure it's a RemoteServerRecord, not just a ServerRecord
        return (RemoteServerRecord) lstDevHosts.getSelectedValue();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbDevHosts = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDevHosts = new javax.swing.JList();
        btAddServer = new javax.swing.JButton();
        btRemoveServer = new javax.swing.JButton();
        btSetAsDefault = new javax.swing.JButton();
        btPathMapper = new javax.swing.JButton();
        btProperties = new javax.swing.JButton();
        lbStatus = new javax.swing.JLabel();
        tfStatus = new javax.swing.JTextField();
        btRetry = new javax.swing.JButton();
        lbReason = new javax.swing.JLabel();
        tfReason = new javax.swing.JTextField();
        pbarStatusPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(419, 255));
        setLayout(new java.awt.GridBagLayout());

        lbDevHosts.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_ServerList").charAt(0));
        lbDevHosts.setLabelFor(lstDevHosts);
        lbDevHosts.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_ServerList")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(lbDevHosts, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 200));

        lstDevHosts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDevHosts.setMinimumSize(new java.awt.Dimension(200, 200));
        lstDevHosts.setSelectedIndex(0);
        jScrollPane1.setViewportView(lstDevHosts);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        btAddServer.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_AddServer").charAt(0));
        btAddServer.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_AddServer")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btAddServer, gridBagConstraints);

        btRemoveServer.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_RemoveServer").charAt(0));
        btRemoveServer.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_RemoveServer")); // NOI18N
        btRemoveServer.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btRemoveServer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btSetAsDefault, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_SetAsDefault")); // NOI18N
        btSetAsDefault.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btSetAsDefault, gridBagConstraints);

        btPathMapper.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_PathMapper").charAt(0));
        btPathMapper.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_PathMapper")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btPathMapper, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btProperties, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "EditServerListDialog.btProperties.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(btProperties, gridBagConstraints);

        lbStatus.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_Status").charAt(0));
        lbStatus.setLabelFor(tfStatus);
        lbStatus.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_Status")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(lbStatus, gridBagConstraints);

        tfStatus.setColumns(20);
        tfStatus.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 6);
        add(tfStatus, gridBagConstraints);

        btRetry.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_Retry").charAt(0));
        btRetry.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_Retry")); // NOI18N
        btRetry.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 0, 6);
        add(btRetry, gridBagConstraints);

        lbReason.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_Reason").charAt(0));
        lbReason.setLabelFor(lbReason);
        lbReason.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_Reason")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(lbReason, gridBagConstraints);

        tfReason.setEditable(false);
        tfReason.setPreferredSize(new java.awt.Dimension(40, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 6);
        add(tfReason, gridBagConstraints);

        pbarStatusPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(pbarStatusPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddServer;
    private javax.swing.JButton btPathMapper;
    private javax.swing.JButton btProperties;
    private javax.swing.JButton btRemoveServer;
    private javax.swing.JButton btRetry;
    private javax.swing.JButton btSetAsDefault;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbDevHosts;
    private javax.swing.JLabel lbReason;
    private javax.swing.JLabel lbStatus;
    private javax.swing.JList lstDevHosts;
    private javax.swing.JPanel pbarStatusPanel;
    private javax.swing.JTextField tfReason;
    private javax.swing.JTextField tfStatus;
    // End of variables declaration//GEN-END:variables
}
