/*
 * EditServerListDialog.java
 *
 * Created on July 9, 2008, 7:24 AM
 */

package org.netbeans.modules.cnd.remote.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  gordonp
 */
public class EditServerListDialog extends JPanel implements ActionListener, PropertyChangeListener, ListSelectionListener {
    
    private DefaultListModel model;
    private int defaultIndex;
    private JButton ok;

    /** Creates new form EditServerListDialog */
    public EditServerListDialog(ServerUpdateCache cache) {
        initComponents();
        initListeners();
        initServerList(cache);
    }
    
    private void initListeners() {
        lstDevHosts.addListSelectionListener(this);
        btAddServer.addActionListener(this);
        btRemoveServer.addActionListener(this);
        btSetAsDefault.addActionListener(this);
    }
    
    private void initServerList(ServerUpdateCache cache) {
        model = new DefaultListModel();
        
        if (cache == null) {
            RemoteServerList registry = RemoteServerList.getInstance();
            for (String hkey : registry.getServerNames()) {
                model.addElement(hkey);
            }
            defaultIndex = registry.getDefaultIndex();
        } else {
            for (String hkey : cache.getHostKeyList()) {
                model.addElement(hkey);
            }
            defaultIndex = cache.getDefaultIndex();
        }
        lstDevHosts.setModel(model);
        lstDevHosts.setSelectedIndex(defaultIndex);
    }
    
    public String[] getHostKeyList() {
        String[] hklist = new String[model.getSize()];
        for (int i = 0; i < hklist.length; i++) {
            hklist[i] = (String) model.get(i);
        }
        return hklist;
    }
    
    public int getDefaultIndex() {
        return lstDevHosts.getSelectedIndex();
    }
    
    private void showAddServerDialog() {
        AddServerDialog dlg = new AddServerDialog();
        
        dlg.addPropertyChangeListener(AddServerDialog.PROP_VALID, this);
        ok = new JButton(NbBundle.getMessage(EditServerListDialog.class, "BTN_OK"));
        ok.setEnabled(dlg.isOkValid());
        DialogDescriptor dd = new DialogDescriptor((Object) dlg, NbBundle.getMessage(EditServerListDialog.class, "TITLE_AddNewServer"), true, 
                    new Object[] { ok, DialogDescriptor.CANCEL_OPTION },
                    DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == ok) {
            String entry = dlg.getLoginName() + '@' + dlg.getServerName();
            if (!model.contains(entry)) {
                model.addElement(entry);
                if (dlg.isDefault()) {
                    defaultIndex = model.getSize() - 1;
                    lstDevHosts.setSelectedIndex(defaultIndex);
                }
            }
        }
    }
    
    private void showPathMapper() {
        
    }

    /** Helps the AddServerDialog know when to enable/disable the OK button */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AddServerDialog.PROP_VALID)) {
            AddServerDialog dlg = (AddServerDialog) evt.getSource();
            ok.setEnabled(dlg.isOkValid());
        }
    }
    
    /** Enable/disable the Remove and Set As Default buttons */
    public void valueChanged(ListSelectionEvent evt) {
        int idx = lstDevHosts.getSelectedIndex();
        btRemoveServer.setEnabled(idx > 0);
        btSetAsDefault.setEnabled(idx != defaultIndex);
    }
    
    public void actionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        
        if (o instanceof JButton) {
            JButton b = (JButton) o;
            if (b.getActionCommand().equals("Add")) { // NOI18N
                showAddServerDialog();
            } else if (b.getActionCommand().equals("Remove")) { // NOI18N
                int idx = lstDevHosts.getSelectedIndex();
                if (idx > 0) {
                    model.remove(idx);
                    lstDevHosts.setSelectedIndex(model.size() > idx ? idx : idx - 1);
                }
            } else if (b.getActionCommand().equals("SetAsDefault")) { // NOI18N
                defaultIndex = lstDevHosts.getSelectedIndex();
                b.setEnabled(false);
            } else if (b.getActionCommand().equals("PathMapper")) { // NOI18N
                showPathMapper();
            }
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

        lbDevHosts = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDevHosts = new javax.swing.JList();
        btAddServer = new javax.swing.JButton();
        btRemoveServer = new javax.swing.JButton();
        btSetAsDefault = new javax.swing.JButton();
        btPathMapper = new javax.swing.JButton();

        lbDevHosts.setLabelFor(lstDevHosts);
        lbDevHosts.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_ServerList")); // NOI18N

        lstDevHosts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDevHosts.setSelectedIndex(0);
        jScrollPane1.setViewportView(lstDevHosts);

        btAddServer.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_AddServer")); // NOI18N
        btAddServer.setActionCommand("Add"); // NOI18N

        btRemoveServer.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_RemoveServer")); // NOI18N
        btRemoveServer.setEnabled(false);

        btSetAsDefault.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_SetAsDefault")); // NOI18N
        btSetAsDefault.setActionCommand("SetAsDefault"); // NOI18N
        btSetAsDefault.setEnabled(false);

        btPathMapper.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_PathMapper")); // NOI18N
        btPathMapper.setActionCommand("PathMapper"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(btSetAsDefault, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .add(btRemoveServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, btAddServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .add(btPathMapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)))
                    .add(lbDevHosts))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lbDevHosts)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(btAddServer)
                        .add(12, 12, 12)
                        .add(btRemoveServer)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btSetAsDefault)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btPathMapper))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddServer;
    private javax.swing.JButton btPathMapper;
    private javax.swing.JButton btRemoveServer;
    private javax.swing.JButton btSetAsDefault;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbDevHosts;
    private javax.swing.JList lstDevHosts;
    // End of variables declaration//GEN-END:variables
}
