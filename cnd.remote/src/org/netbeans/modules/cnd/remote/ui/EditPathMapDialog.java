/*
 * EditPathMapDialog.java
 *
 * Created on 14 Июль 2008 г., 16:11
 */
package org.netbeans.modules.cnd.remote.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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
public class EditPathMapDialog extends JPanel {

    public static boolean showMe(String hkey, String pathToValidate) {
        EditPathMapDialog dlg = new EditPathMapDialog(hkey, pathToValidate);

        DialogDescriptor dd = new DialogDescriptor(dlg,
                NbBundle.getMessage(EditServerListDialog.class, "EditPathMapDialogTitle"),
                true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            dlg.applyChanges();
            return true;
        }
        return false;
    }
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

        tblPathMappings.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tblPathMappings.getTableHeader().setPreferredSize(new Dimension(0, 20));
        setColumnNames();

        cbHostsList.setSelectedItem(currentHkey);

        String explanationText;
        if (pathToValidate != null) {
            explanationText = NbBundle.getMessage(EditPathMapDialog.class, "EPMP_ExplanationWithPath", pathToValidate);
        } else {
            explanationText = NbBundle.getMessage(EditPathMapDialog.class, "EPMP_Explanation");
        }
        txtExplanation.setText(explanationText);
    }

    private static RemotePathMap getRemotePathMap(String hkey) {
        return (RemotePathMap) RemotePathMap.getMapper(hkey);
    }

    private void initTableModel(String hkey) {
        DefaultTableModel tableModel = cache.get(hkey);
        if (tableModel == null) {
            RemotePathMap pm = getRemotePathMap(hkey);
            tableModel = new DefaultTableModel(0, 2);
            for (String local : pm.keySet()) {
                tableModel.addRow(new String[]{local, pm.get(local)});
            }
            if (tableModel.getRowCount() < 8) { // TODO: switch from JTable to a normal TableView
                for (int i = 8; i > tableModel.getRowCount(); i--) {
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
                        //TODO: path existence validation
                        //TODO: path correspondence validation
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
        txtExplanation = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtErrors = new javax.swing.JTextPane();
        btnTestValidation = new javax.swing.JButton();

        lblHostName.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.lblHostName.text")); // NOI18N

        cbHostsList.setModel(serverListModel);
        cbHostsList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbHostsListItemStateChanged(evt);
            }
        });

        tblPathMappings.setModel(cache.get(currentHkey));
        tblPathMappings.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblPathMappings);

        jScrollPane2.setBorder(null);

        txtExplanation.setBackground(new java.awt.Color(240, 240, 240));
        txtExplanation.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        txtExplanation.setEditable(false);
        txtExplanation.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.txtExplanation.text")); // NOI18N
        jScrollPane2.setViewportView(txtExplanation);

        jScrollPane3.setBorder(null);

        txtErrors.setBackground(new java.awt.Color(240, 240, 240));
        txtErrors.setBorder(null);
        jScrollPane3.setViewportView(txtErrors);

        btnTestValidation.setText(org.openide.util.NbBundle.getMessage(EditPathMapDialog.class, "EditPathMapDialog.btnTestValidation.text")); // NOI18N
        btnTestValidation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestValidationActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblHostName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cbHostsList, 0, 307, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, btnTestValidation))
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
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnTestValidation)
                .add(11, 11, 11))
        );
    }// </editor-fold>//GEN-END:initComponents

private void cbHostsListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbHostsListItemStateChanged
    currentHkey = (String) cbHostsList.getSelectedItem();
    initTableModel(currentHkey);
}//GEN-LAST:event_cbHostsListItemStateChanged

private void btnTestValidationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestValidationActionPerformed
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
                    sb.append("Local path \"" + local + "\" doesn't exist.\n");
                }
                if (pathToValidate != null && !pathIsValidated) {
                    if (remote != null && RemotePathMap.isSubPath(local, pathToValidate)) {
                        pathIsValidated = true;
                    }
                }
            }
        }
        if (remote != null) {
            remote = remote.trim();
            if (remote.length() > 0) {
                if (!HostInfoProvider.getDefault().fileExists(currentHkey, remote)) {
                    sb.append("Remote path \"" + remote + "\" doesn't exist.\n");
                }
            }
        }
    }
    if (pathToValidate != null && !pathIsValidated) {
        sb.append("Requested path \"" + pathToValidate + "\" is not resolved by those mappings.\n");
    }
    if (sb.length() == 0) {
        sb.append("No errors.");
    }
    txtErrors.setText(sb.toString());
}//GEN-LAST:event_btnTestValidationActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnTestValidation;
    private javax.swing.JComboBox cbHostsList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblHostName;
    private javax.swing.JTable tblPathMappings;
    private javax.swing.JTextPane txtErrors;
    private javax.swing.JTextPane txtExplanation;
    // End of variables declaration//GEN-END:variables
}