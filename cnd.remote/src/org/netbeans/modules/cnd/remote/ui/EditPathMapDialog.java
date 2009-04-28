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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Sergey Grinev
 */
public class EditPathMapDialog extends JPanel implements ActionListener {

    public static boolean showMe(ServerRecord host, List<ServerRecord> hostList) {
        return showMe(host, null, hostList);
    }

    public static boolean showMe(ExecutionEnvironment execEnv, String pathToValidate) {
        return showMe(ServerList.get(execEnv), pathToValidate);
    }
    public static boolean showMe(ServerRecord host, String pathToValidate) {
        return showMe(host, pathToValidate, ServerList.getRecords());
    }

    private static boolean showMe(ServerRecord host, String pathToValidate, Collection<? extends ServerRecord> hostList) {
        JButton btnOK = new JButton(NbBundle.getMessage(EditPathMapDialog.class, "BTN_OK"));
        EditPathMapDialog dlg = new EditPathMapDialog(host, pathToValidate, hostList, btnOK);

        DialogDescriptor dd = new DialogDescriptor(dlg,
                NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialogTitle"),
                true, new Object[] { btnOK, DialogDescriptor.CANCEL_OPTION}, btnOK, DialogDescriptor.DEFAULT_ALIGN, null, dlg);
        dd.setClosingOptions(new Object[]{DialogDescriptor.CANCEL_OPTION});
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dlg.presenter = dialog;
        dialog.setVisible(true);
        if (dd.getValue() == btnOK) {
            dlg.applyChanges();
            return true;
        }
        return false;
    }

    private final JButton btnOK;
    private Dialog presenter;
    private ServerRecord currentHost;
    private DefaultComboBoxModel serverListModel;
    private final String pathToValidate;
    private final Map<ServerRecord, DefaultTableModel> cache = new HashMap<ServerRecord, DefaultTableModel>();
    private ProgressHandle phandle;

    /** Creates new form EditPathMapDialog */
    protected EditPathMapDialog(ServerRecord defaultHost, String pathToValidate, Collection<? extends ServerRecord> hostList, JButton btnOK) {
        this.btnOK = btnOK;
        this.pathToValidate = pathToValidate;
        currentHost = defaultHost;
        serverListModel = new DefaultComboBoxModel();

        for (ServerRecord host : hostList) {
            if (host.isRemote()) {
                serverListModel.addElement(host);
            }
        }

        initComponents();
        cbHostsList.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel out = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ServerRecord rec = (ServerRecord) value;
                out.setText(rec.getDisplayName());
                return out;
            }
        });

        tblPathMappings.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        tblPathMappings.getTableHeader().setPreferredSize(new Dimension(0, 20));
//        JTextField tmp = new JTextField();
//        tblPathMappings.setRowHeight(tmp.getPreferredSize().height);
        cbHostsList.setSelectedItem(currentHost);

        String explanationText;
        if (pathToValidate != null) {
            explanationText = NbBundle.getMessage(EditPathMapDialog.class, "EPMD_ExplanationWithPath", pathToValidate);
        } else {
            explanationText = NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Explanation");
        }
        txtExplanation.setText(explanationText);

        // bg color jdk bug fixup
        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            jScrollPane1.setOpaque(false);
            jScrollPane3.setOpaque(false);
        }

        initTableModel(currentHost);
    }

    private static RemotePathMap getRemotePathMap(ExecutionEnvironment host) {
        return RemotePathMap.getRemotePathMapInstance(host);
    }

    private synchronized void initTableModel(final ServerRecord host) {
        DefaultTableModel tableModel = cache.get(host);
        if (tableModel == null) {
            if (RemotePathMap.isReady(host.getExecutionEnvironment())) {
                tableModel = prepareTableModel(host.getExecutionEnvironment());
            } else {
                handleProgress(true);
                tableModel = new DefaultTableModel(0, 2);
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        final DefaultTableModel tm = prepareTableModel(host.getExecutionEnvironment());
                        cache.put(host, tm);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                if (tblPathMappings != null) {
                                    handleProgress(false);
                                    updatePathMappingsTable(tm);
                                    enableControls(true, "");
                                }
                            }
                        });
                    }
                });
                enableControls(false, NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Loading"));

            }
            cache.put(host, tableModel);
        }

        updatePathMappingsTable(tableModel);
    }

    private void enableControls(boolean value, String message) {
        btnOK.setEnabled(value);
        tblPathMappings.setEnabled(value);
        cbHostsList.setEnabled(value);
        txtError.setText(message);
    }

    private void updatePathMappingsTable(DefaultTableModel tableModel) {
        tblPathMappings.setModel(tableModel);
        tblPathMappings.getColumnModel().getColumn(0).setCellEditor(new PathCellEditor());
        setColumnNames();
    }

    private DefaultTableModel prepareTableModel(ExecutionEnvironment host) {
        Map<String, String> pm = getRemotePathMap(host).getMap();
        DefaultTableModel tableModel = new DefaultTableModel(0, 2);
        for (Map.Entry<String, String> entry : pm.entrySet()) {
            tableModel.addRow(new String[]{entry.getKey(), entry.getValue()});
        }
        if (tableModel.getRowCount() < 4) { // TODO: switch from JTable to a normal TableView
            for (int i = 4; i > tableModel.getRowCount(); i--) {
                tableModel.addRow(new String[]{null, null});
            }
        } else {
            tableModel.addRow(new String[]{null, null});
        }
        return tableModel;
    }

    private void setColumnNames() {
        tblPathMappings.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(EditPathMapDialog.class, "LocalPathColumnName")); // NOI18N
        tblPathMappings.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(EditPathMapDialog.class, "RemotePathColumnName")); // NOI18N
    }

    /* package */ void applyChanges() {
        for (ServerRecord host : cache.keySet()) {
            Map<String, String> map = new HashMap<String, String>();
            DefaultTableModel model = cache.get(host);
            for (int i = 0; i < model.getRowCount(); i++) {
                String local = (String) model.getValueAt(i, 0);
                String remote = (String) model.getValueAt(i, 1);
                if (local != null && remote != null) {
                    local = local.trim();
                    remote = remote.trim();
                    if (local.length() > 0 && remote.length() > 0) {
                        map.put(local, remote);
                    }
                }
            }
            getRemotePathMap(host.getExecutionEnvironment()).updatePathMap(map);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHostName = new javax.swing.JLabel();
        cbHostsList = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPathMappings = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtExplanation = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtError = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();

        lblHostName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("EPMD_Hostname").charAt(0));
        lblHostName.setLabelFor(cbHostsList);
        lblHostName.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.lblHostName.text")); // NOI18N
        lblHostName.setFocusable(false);

        cbHostsList.setModel(serverListModel);
        cbHostsList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbHostsListItemStateChanged(evt);
            }
        });

        tblPathMappings.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblPathMappings.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblPathMappings);
        tblPathMappings.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_MappingsTable_AN")); // NOI18N
        tblPathMappings.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_MappingsTable_AD")); // NOI18N

        jScrollPane2.setBorder(null);

        txtExplanation.setBackground(getBackground());
        txtExplanation.setColumns(20);
        txtExplanation.setLineWrap(true);
        txtExplanation.setRows(4);
        txtExplanation.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.txtExplanation.text")); // NOI18N
        txtExplanation.setWrapStyleWord(true);
        txtExplanation.setAutoscrolls(false);
        txtExplanation.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        txtExplanation.setFocusable(false);
        jScrollPane2.setViewportView(txtExplanation);

        jScrollPane3.setBorder(null);

        txtError.setBackground(getBackground());
        txtError.setColumns(20);
        txtError.setForeground(new java.awt.Color(255, 0, 0));
        txtError.setLineWrap(true);
        txtError.setRows(4);
        txtError.setWrapStyleWord(true);
        txtError.setFocusable(false);
        jScrollPane3.setViewportView(txtError);

        jPanel1.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblHostName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cbHostsList, 0, 391, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblHostName)
                    .add(cbHostsList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 118, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        lblHostName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Hostname")); // NOI18N
        lblHostName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Host_AD")); // NOI18N
        cbHostsList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Hostname")); // NOI18N
        cbHostsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Host_AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbHostsListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbHostsListItemStateChanged
    currentHost = (ServerRecord) cbHostsList.getSelectedItem();
    initTableModel(currentHost);
}//GEN-LAST:event_cbHostsListItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbHostsList;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblHostName;
    private javax.swing.JTable tblPathMappings;
    private javax.swing.JTextArea txtError;
    private javax.swing.JTextArea txtExplanation;
    // End of variables declaration//GEN-END:variables
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOK) {
            if (cache.get(currentHost).getRowCount() == 0) {
                // fast handle vacuous case
                presenter.setVisible(false);
                return;
            }
            handleProgress(true);
            enableControls(false, NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Validating"));
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final String errors = validateMaps();
                    Runnable action = errors.length() == 0
                            ? new Runnable() {
                                public void run() {
                                    try {
                                        //this is done to don't scare user with red note if validateMaps() was fast
                                        Thread.sleep(500);
                                    } catch (InterruptedException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                    presenter.setVisible(false);
                                }
                            }
                            : new Runnable() {
                                public void run() {
                                    handleProgress(false);
                                    enableControls(true, errors);
                                }
                            };
                    SwingUtilities.invokeLater(action);
                }
            });
        }
    }

    private void handleProgress(boolean start) {
        if (start) {
            phandle = ProgressHandleFactory.createHandle("");
            jPanel1.add(ProgressHandleFactory.createProgressComponent(phandle), BorderLayout.NORTH);
            jPanel1.setVisible(true);
            phandle.start();
        } else {
            phandle.finish();
            jPanel1.setVisible(false);
            jPanel1.removeAll();
        }
    }

    private String validateMaps() {
        DefaultTableModel model = cache.get(currentHost);
        StringBuilder sb = new StringBuilder();
        boolean pathIsValidated = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            String local = (String) model.getValueAt(i, 0);
            String remote = (String) model.getValueAt(i, 1);
            if (local != null) {
                local = local.trim();
                if (local.length() > 0) {
                    if (!HostInfoProvider.fileExists(ExecutionEnvironmentFactory.getLocal(), local)) {
                        sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_BadLocalPath", local));
                    }
                    if (pathToValidate != null && !pathIsValidated) {
                        if (remote != null && RemotePathMap.isSubPath(local, pathToValidate)) {
                            pathIsValidated = true;
                            //TODO: real path mapping validation (create file, check from both sides, etc)
                        }
                    }
                }
            }
            if (remote != null) {
                remote = remote.trim();
                if (remote.length() > 0) {
                    if (!HostInfoProvider.fileExists(currentHost.getExecutionEnvironment(), remote)) {
                        sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_BadRemotePath", remote));
                    }
                }
            }
        }
        if (pathToValidate != null && !pathIsValidated) {
            sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_PathNotResolved", pathToValidate));
        }
        return sb.toString();
    }

    private static class PathCellEditor extends AbstractCellEditor 
            implements TableCellEditor, ActionListener {

        private final JPanel panel;
        private final JTextField tfPath;
        private final JButton btnBrowse;

        public PathCellEditor() {
            tfPath = new JTextField();
            btnBrowse = new JButton(NbBundle.getMessage(EditPathMapDialog.class, "BTN_Browse"));
            panel = new JPanel();
            //panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setLayout(new BorderLayout());
            
            tfPath.setBorder(BorderFactory.createEmptyBorder());//  getInsets() setInsets(new Insets(0, 0, 0, 0));
            //btnBrowse.setMaximumSize(btnBrowse.getMinimumSize());
            btnBrowse.setBorder(BorderFactory.createLineBorder(Color.gray));

            panel.add(tfPath, BorderLayout.CENTER);
            panel.add(btnBrowse, BorderLayout.EAST);
            btnBrowse.addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            File file = new File(tfPath.getText());
            JFileChooser fc = new JFileChooser(file);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setApproveButtonText(NbBundle.getMessage(EditPathMapDialog.class, "BTN_Choose"));
            fc.setDialogTitle(NbBundle.getMessage(EditPathMapDialog.class, "DIR_Choose_Title"));
            fc.setApproveButtonMnemonic(KeyEvent.VK_ENTER);
            if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                tfPath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }

        public Object getCellEditorValue() {
            return tfPath.getText().trim();
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            tfPath.setText((String) value);
            return panel;
        }
    }

}
