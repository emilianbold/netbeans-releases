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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
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
import org.netbeans.modules.cnd.remote.mapper.HostMappingsAnalyzer;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
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

    private static final String ACTION_INLINE_EDITOR = "invokeInlineEditor";  //NOI18N
    private static final String ACTION_ESCAPE_TABLE = "escapeTable";  //NOI18N
    private static final String ACTION_TAB_IN_CELL = "tabInCell";  //NOI18N
    private static final String ACTION_SHIFT_TAB_IN_CELL = "shiftTabInCell";  //NOI18N

    public static boolean showMe(ServerRecord host, List<ServerRecord> hostList) {
        return showMe(host, null, hostList);
    }

    public static boolean showMe(ExecutionEnvironment execEnv, String pathToValidate) {
        return showMe(ServerList.get(execEnv), pathToValidate);
    }
    
    private static boolean showMe(ServerRecord host, String pathToValidate) {
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
    private final Map<ServerRecord, PathMapTableModel> cache = new HashMap<ServerRecord, PathMapTableModel>();
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
        addTableActions();

        tfHostName.setBackground(getBackground()); // otherwise it looks like editable on Mac
        tfHostName.setText(currentHost.getDisplayName());
        initTable();

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

    private void initTable(){
        tblPathMappings.getTableHeader().setPreferredSize(new Dimension(0, 20));
        tblPathMappings.getTableHeader().setFocusable(false);
        tblPathMappings.getTableHeader().setEnabled(false);
        //initRenderer();
        //tblPathMappings.getSelectionModel().addListSelectionListener(getListener1());
        tblPathMappings.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        //tableModel.addTableModelListener(getListener2());
        tblPathMappings.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), ACTION_INLINE_EDITOR); //NOI18N
        tblPathMappings.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_INLINE_EDITOR); //NOI18N
        tblPathMappings.getActionMap().put(ACTION_INLINE_EDITOR, getEditAction()); //NOI18N
        tblPathMappings.setSurrendersFocusOnKeystroke(true);
        tblPathMappings.setCellSelectionEnabled(false);
        tblPathMappings.setRowSelectionAllowed(true);
        tblPathMappings.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE); //NOI18N
        tblPathMappings.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); //NOI18N
        tblPathMappings.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_ESCAPE_TABLE);
        tblPathMappings.getActionMap().put(ACTION_ESCAPE_TABLE, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                EditPathMapDialog.this.btnOK.requestFocus();
            }
        });
    }

    private static RemotePathMap getRemotePathMap(ExecutionEnvironment host) {
        return RemotePathMap.getPathMap(host);
    }

    private void addTableActions() throws MissingResourceException {

        Action removeAction = new AbstractAction(NbBundle.getMessage(getClass(), "ACTION_Remove")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = tblPathMappings.getSelectedRows();
                if (rows.length > 0) {
                    DefaultTableModel model = (DefaultTableModel) tblPathMappings.getModel();
                    Arrays.sort(rows);
                    for (int i = rows.length - 1; i >= 0; i-- ) {
                        model.removeRow(rows[i]);
                    }
                }
            }
        };

        Action insertAction = new AbstractAction(NbBundle.getMessage(getClass(), "ACTION_Insert")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tblPathMappings.getSelectedRow();
                row = (row < 0) ? 0: row;
                DefaultTableModel model = (DefaultTableModel) tblPathMappings.getModel();
                model.insertRow(row, new Object[] {"", ""}); // NOI18N
            }
        };

        final JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(insertAction));
        menu.add(new JMenuItem(removeAction));

        class MenuListener extends MouseAdapter {
            private void showMenu(MouseEvent evt) {
                if (evt != null) {
                    int row = tblPathMappings.rowAtPoint(evt.getPoint());
                    if (row >= 0 && tblPathMappings.getSelectionModel().isSelectionEmpty()) {
                        tblPathMappings.getSelectionModel().setSelectionInterval(row, row);
                    }
                    menu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showMenu(evt);
                }
            }
            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showMenu(evt);
                }
            }
        }
        final MenuListener menuListener = new MenuListener();
        tblPathMappings.addMouseListener(menuListener);
    }

    private synchronized void initTableModel(final ServerRecord host) {
        PathMapTableModel tableModel = cache.get(host);
        if (tableModel == null) {
            enableControls(false, NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Loading"));
            handleProgress(true);
            tableModel = new PathMapTableModel();
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    final PathMapTableModel tm = prepareTableModel(host.getExecutionEnvironment());
                    cache.put(host, tm);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            if (tblPathMappings != null) {
                                handleProgress(false);
                                updatePathMappingsTable(tm, host.getExecutionEnvironment());
                                enableControls(true, "");
                            }
                        }
                    });
                }
            });            
            cache.put(host, tableModel);
        }
        updatePathMappingsTable(tableModel, host.getExecutionEnvironment());
    }

    private PathMapTableModel prepareTableModel(Map<String, String> pm) {
        PathMapTableModel tableModel = new PathMapTableModel();
        for (Map.Entry<String, String> entry : pm.entrySet()) {
            tableModel.addRow(new String[]{entry.getKey(), entry.getValue()});
        }
        if (tableModel.getRowCount() < 4) {
            // TODO: switch from JTable to a normal TableView
            for (int i = 4; i > tableModel.getRowCount(); i--) {
                tableModel.addRow(new String[]{null, null});
            }
        } else {
            tableModel.addRow(new String[]{null, null});
        }
        return tableModel;
    }

    private void enableControls(boolean value, String message) {
        btnOK.setEnabled(value);
        restore.setEnabled(value);
        tblPathMappings.setEnabled(value);
        txtError.setText(message);
    }

    private void updatePathMappingsTable(DefaultTableModel tableModel, ExecutionEnvironment env) {
        tblPathMappings.setModel(tableModel);
        tblPathMappings.getColumnModel().getColumn(0).setCellEditor(new PathCellEditor(ExecutionEnvironmentFactory.getLocal()));
        tblPathMappings.getColumnModel().getColumn(1).setCellEditor(new PathCellEditor(env));
    }

    private PathMapTableModel prepareTableModel(ExecutionEnvironment host) {
        Map<String, String> pm = getRemotePathMap(host).getMap();
        return prepareTableModel(pm);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPathMappings = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtExplanation = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtError = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfHostName = new javax.swing.JTextField();
        restore = new javax.swing.JButton();

        lblHostName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("EPMD_Hostname").charAt(0));
        lblHostName.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.lblHostName.text")); // NOI18N
        lblHostName.setFocusable(false);

        tblPathMappings.setModel(new PathMapTableModel());
        tblPathMappings.getTableHeader().setResizingAllowed(false);
        tblPathMappings.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblPathMappings);
        tblPathMappings.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_MappingsTable_AN")); // NOI18N
        tblPathMappings.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_MappingsTable_AD")); // NOI18N

        jScrollPane2.setBorder(null);
        jScrollPane2.setEnabled(false);
        jScrollPane2.setFocusable(false);

        txtExplanation.setBackground(getBackground());
        txtExplanation.setColumns(20);
        txtExplanation.setLineWrap(true);
        txtExplanation.setRows(4);
        txtExplanation.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.txtExplanation.text")); // NOI18N
        txtExplanation.setWrapStyleWord(true);
        txtExplanation.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        txtExplanation.setFocusable(false);
        jScrollPane2.setViewportView(txtExplanation);

        jScrollPane3.setBorder(null);
        jScrollPane3.setEnabled(false);
        jScrollPane3.setFocusable(false);

        txtError.setBackground(getBackground());
        txtError.setColumns(20);
        txtError.setForeground(new java.awt.Color(255, 0, 0));
        txtError.setLineWrap(true);
        txtError.setRows(4);
        txtError.setWrapStyleWord(true);
        txtError.setFocusable(false);
        jScrollPane3.setViewportView(txtError);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel1.setLabelFor(tblPathMappings);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_MappingsTable_AN")); // NOI18N

        tfHostName.setEditable(false);
        tfHostName.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.tfHostName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(restore, org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.restore.text")); // NOI18N
        restore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblHostName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfHostName, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(restore))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHostName)
                    .addComponent(tfHostName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(restore)
                .addContainerGap())
        );

        lblHostName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Hostname")); // NOI18N
        lblHostName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Host_AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void restoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreActionPerformed
        enableControls(false, NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Loading"));
        handleProgress(true);
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                HostMappingsAnalyzer an = new HostMappingsAnalyzer(currentHost.getExecutionEnvironment());
                final PathMapTableModel tm = prepareTableModel(an.getMappings());
                cache.put(currentHost, tm);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (tblPathMappings != null) {
                            handleProgress(false);
                            updatePathMappingsTable(tm, currentHost.getExecutionEnvironment());
                            enableControls(true, "");
                        }
                    }
                });
            }
        });
        

    }//GEN-LAST:event_restoreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblHostName;
    private javax.swing.JButton restore;
    private javax.swing.JTable tblPathMappings;
    private javax.swing.JTextField tfHostName;
    private javax.swing.JTextArea txtError;
    private javax.swing.JTextArea txtExplanation;
    // End of variables declaration//GEN-END:variables
    @Override
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

                @Override
                public void run() {
                    final String errors = validateMaps();
                    Runnable action = errors.length() == 0
                            ? new Runnable() {
                                @Override
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
                                @Override
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
        private final ExecutionEnvironment execEnv;

        public PathCellEditor(ExecutionEnvironment execEnv) {
            this.execEnv = execEnv;
            tfPath = new JTextField();
            btnBrowse = new JButton(NbBundle.getMessage(EditPathMapDialog.class, "BTN_Browse"));
            panel = new JPanel();
            //panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setLayout(new BorderLayout());
            
            tfPath.setBorder(BorderFactory.createEmptyBorder());//  getInsets() setInsets(new Insets(0, 0, 0, 0));
            //btnBrowse.setMaximumSize(btnBrowse.getMinimumSize());
            //btnBrowse.setBorder(BorderFactory.createLineBorder(Color.gray));
            btnBrowse.setPreferredSize(new java.awt.Dimension(20, btnBrowse.getMinimumSize().height));

            panel.add(tfPath, BorderLayout.CENTER);
            panel.add(btnBrowse, BorderLayout.EAST);
            btnBrowse.addActionListener(this);
            tfPath.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), ACTION_TAB_IN_CELL);
            tfPath.getActionMap().put(ACTION_TAB_IN_CELL, new AbstractAction(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    tfPath.setSelectionStart(0);
                    tfPath.setSelectionEnd(0);
                    btnBrowse.requestFocus();
                    btnBrowse.setSelected(true);
                }
            });
            btnBrowse.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK), ACTION_SHIFT_TAB_IN_CELL);
            btnBrowse.getActionMap().put(ACTION_SHIFT_TAB_IN_CELL, new AbstractAction(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    tfPath.setSelectionStart(0);
                    tfPath.setSelectionEnd(tfPath.getText().length());
                    btnBrowse.setSelected(false);
                    tfPath.requestFocus();
                }
            });
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String title = execEnv.isLocal() ?
                NbBundle.getMessage(EditPathMapDialog.class, "DIR_Choose_Title_Local") :
                NbBundle.getMessage(EditPathMapDialog.class, "DIR_Choose_Title_Remote", ServerList.get(execEnv).getDisplayName());
            JFileChooser fc = new FileChooserBuilder(execEnv).createFileChooser(tfPath.getText());
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setApproveButtonText(NbBundle.getMessage(EditPathMapDialog.class, "BTN_Choose"));
            fc.setDialogTitle(title);
            fc.setApproveButtonMnemonic(KeyEvent.VK_ENTER);
            if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                tfPath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }

        @Override
        public Object getCellEditorValue() {
            return tfPath.getText().trim();
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            tfPath.setText((String) value);
            return panel;
        }
    }

    private Action editAction;
    private Action getEditAction() {
        if (editAction == null) {
            editAction = new EditAction();
        }
        return editAction;
    }

    private static void autoEdit(JTable tab) {
        if (tab.editCellAt(tab.getSelectedRow(), tab.getSelectedColumn(), null) &&
                tab.getEditorComponent() != null) {
            if (tab.getSelectedColumn() == 0) {
                JPanel panel = (JPanel) tab.getEditorComponent();
                JTextField field = (JTextField) panel.getComponent(0);
                field.setCaretPosition(field.getText().length());
                field.requestFocusInWindow();
                field.selectAll();
            } else {
                JTextField field = (JTextField) tab.getEditorComponent();
                field.setCaretPosition(field.getText().length());
                field.requestFocusInWindow();
                field.selectAll();
            }
        }
    }

    private static class EditAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent ae) {
            autoEdit((JTable) ae.getSource());
        }
    }

    private static class PathMapTableModel extends DefaultTableModel {

        public PathMapTableModel() {
            super(new String[] {
                NbBundle.getMessage(EditPathMapDialog.class, "LocalPathColumnName"),
                NbBundle.getMessage(EditPathMapDialog.class, "RemotePathColumnName")
            }, 0);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }
}
