/*
 * EditPathMapDialog.java
 *
 * Created on 14 Июль 2008 г., 16:11
 */
package org.netbeans.modules.cnd.remote.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sergey Grinev
 */
public class EditPathMapDialog extends JPanel implements ActionListener {

    public static boolean showMe(String hkey, String pathToValidate) {
        EditPathMapDialog dlg = new EditPathMapDialog(hkey, pathToValidate);

        DialogDescriptor dd = new DialogDescriptor(dlg,
                NbBundle.getMessage(EditServerListDialog.class, "EditPathMapDialogTitle"),
                true, DialogDescriptor.OK_CANCEL_OPTION, null, dlg);
        dd.setClosingOptions(new Object[]{DialogDescriptor.CANCEL_OPTION});
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dlg.presenter = dialog;
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            dlg.applyChanges();
            return true;
        }
        return false;
    }
    private Dialog presenter;
    private String currentHkey;
    private DefaultComboBoxModel serverListModel;
    private final String pathToValidate;
    private final Map<String, DefaultTableModel> cache = new HashMap<String, DefaultTableModel>();

    /** Creates new form EditPathMapDialog */
    public EditPathMapDialog(String defaultHost, String pathToValidate) {
        this.pathToValidate = pathToValidate;
        currentHkey = defaultHost;
        serverListModel = new DefaultComboBoxModel();

        for (String hkey : RemoteServerList.getInstance().getServerNames()) {
            if (!CompilerSetManager.LOCALHOST.equals(hkey)) {
                serverListModel.addElement(hkey);
            }
        }

        initTableModel(currentHkey);
        initComponents();

        tblPathMappings.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        tblPathMappings.getTableHeader().setPreferredSize(new Dimension(0, 20));
        setColumnNames();

        cbHostsList.setSelectedItem(currentHkey);

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

    }

    private static RemotePathMap getRemotePathMap(String hkey) {
        return RemotePathMap.getMapper(hkey);
    }

    private void initTableModel(String hkey) {
        DefaultTableModel tableModel = cache.get(hkey);
        if (tableModel == null) {
            Map<String, String> pm = getRemotePathMap(hkey).getMap();
            tableModel = new DefaultTableModel(0, 2);
            for (String local : pm.keySet()) {
                tableModel.addRow(new String[]{local, pm.get(local)});
            }
            if (tableModel.getRowCount() < 4) { // TODO: switch from JTable to a normal TableView
                for (int i = 4; i > tableModel.getRowCount(); i--) {
                    tableModel.addRow(new String[]{null, null});
                }
            } else {
                tableModel.addRow(new String[]{null, null});
            }
            cache.put(hkey, tableModel);
        }

        if (tblPathMappings != null) {
            tblPathMappings.setModel(tableModel);
            setColumnNames();
        }
    }

    private void setColumnNames() {
        tblPathMappings.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(EditPathMapDialog.class, "LocalPathColumnName")); // NOI18N
        tblPathMappings.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(EditPathMapDialog.class, "RemotePathColumnName")); // NOI18N
    }

    /* package */ void applyChanges() {
        for (String hkey : cache.keySet()) {
            Map<String, String> map = new HashMap<String, String>();
            DefaultTableModel model = cache.get(hkey);
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
            getRemotePathMap(hkey).updatePathMap(map);
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

        tblPathMappings.setModel(cache.get(currentHkey));
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
        txtExplanation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jScrollPane2.setViewportView(txtExplanation);

        jScrollPane3.setBorder(null);

        txtError.setBackground(getBackground());
        txtError.setColumns(20);
        txtError.setLineWrap(true);
        txtError.setRows(4);
        txtError.setWrapStyleWord(true);
        txtError.setFocusable(false);
        jScrollPane3.setViewportView(txtError);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(lblHostName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cbHostsList, 0, 423, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
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
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        lblHostName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Hostname")); // NOI18N
        lblHostName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Host_AD")); // NOI18N
        cbHostsList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Hostname")); // NOI18N
        cbHostsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EPMD_Host_AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbHostsListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbHostsListItemStateChanged
    currentHkey = (String) cbHostsList.getSelectedItem();
    initTableModel(currentHkey);
}//GEN-LAST:event_cbHostsListItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbHostsList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblHostName;
    private javax.swing.JTable tblPathMappings;
    private javax.swing.JTextArea txtError;
    private javax.swing.JTextArea txtExplanation;
    // End of variables declaration//GEN-END:variables
    public void actionPerformed(ActionEvent e) {
        if (validateMaps()) {
            presenter.setVisible(false);
        }
    }

    private boolean validateMaps() {
        boolean isValid = true;
        ///txtErrors.setText("Validating mappings..."); //TODO
        DefaultTableModel model = cache.get(currentHkey);
        StringBuilder sb = new StringBuilder();
        boolean pathIsValidated = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            String local = (String) model.getValueAt(i, 0);
            String remote = (String) model.getValueAt(i, 1);
            if (local != null) {
                local = local.trim();
                if (local.length() > 0) {
                    if (!HostInfoProvider.getDefault().fileExists(CompilerSetManager.LOCALHOST, local)) {
                        isValid = false;
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
                    if (!HostInfoProvider.getDefault().fileExists(currentHkey, remote)) {
                        isValid = false;
                        sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_BadRemotePath", remote));
                    }
                }
            }
        }
        if (pathToValidate != null && !pathIsValidated) {
            isValid = false;
            sb.append(NbBundle.getMessage(EditPathMapDialog.class, "EPMD_PathNotResolved", pathToValidate));
        }
        txtError.setText(sb.toString());
        return isValid;
    }
}
