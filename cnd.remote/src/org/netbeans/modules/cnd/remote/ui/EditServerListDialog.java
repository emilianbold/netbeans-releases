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
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Mange the remove development hosts list.
 * 
 * @author  gordonp
 */
public class EditServerListDialog extends JPanel implements ActionListener, PropertyChangeListener, ListSelectionListener {
    
    private DefaultListModel model;
    private DialogDescriptor desc;
    private int defaultIndex;
    private JButton ok;
    private ProgressHandle phandle;
    private PropertyChangeSupport pcs;
    private boolean buttonsEnabled;
    private static Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N

    /** Creates new form EditServerListDialog */
    public EditServerListDialog(ServerUpdateCache cache) {
        initComponents();
        initListeners();
        initServerList(cache);
        desc = null;
        buttonsEnabled = true;
        pbarStatusPanel.setVisible(false);
    }
    
    private void initListeners() {
        lstDevHosts.addListSelectionListener(this);
        btAddServer.addActionListener(this);
        btRemoveServer.addActionListener(this);
        btSetAsDefault.addActionListener(this);
        btPathMapper.addActionListener(this);
        pcs = new PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(this);
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
    
    public void setDialogDescriptor(DialogDescriptor desc) {
        this.desc = desc;
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
    
    /**
     * Show the AddServerDialog. If the user adds a new server, we need to verify the connection
     * before we return. This is tricky because we're on the AWT-Event thread and need this thread
     * to process the password dialog. So we disable the OK buton in this dialog and start a progress
     * monitor and return. Oh ya, we also call RemoteServerList.get(entry) in a RequestProcessor thread
     * and monotor its status. When the initialization is complete, we kill the progress monitor and
     * re-enable the OK button.
     */
    private void showAddServerDialog() {
        assert desc != null : "Internal Error: Set DialogDescriptor before calling AddServer";
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
            String server = dlg.getServerName();
            final String entry;
            int pos = server.indexOf('@');
            if (pos == -1) {
                entry = dlg.getLoginName() + '@' + server;
            } else if (server.startsWith(dlg.getLoginName() + '@')) {
                entry = server;
            } else {
                return;
            }
            if (!model.contains(entry)) {
                model.addElement(entry);
                lstDevHosts.setSelectedValue(entry, true);
                final RemoteServerRecord record = (RemoteServerRecord) RemoteServerList.getInstance().get(entry);
                if (record.getState() == RemoteServerRecord.STATE_CANCELLED) {
                    record.setState(RemoteServerRecord.STATE_UNINITIALIZED); // this is a do-over
                }
                if (record.getState() == RemoteServerRecord.STATE_UNINITIALIZED) {
                    setButtons(false);
                    phandle = ProgressHandleFactory.createHandle("");
                    pbarStatusPanel.removeAll();
                    pbarStatusPanel.add(ProgressHandleFactory.createProgressComponent(phandle), BorderLayout.CENTER);
                    pbarStatusPanel.setVisible(true);
                    revalidate();
                    phandle.start();
                    tfStatus.setText(NbBundle.getMessage(RemoteServerRecord.class, "STATE_INITIALIZING"));
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            record.init(pcs);
                            tfStatus.setText(record.getStateAsText());
                            phandle.finish();
                            pbarStatusPanel.setVisible(false);
                            if (record.getState() == RemoteServerRecord.STATE_CANCELLED) {
                                pcs.firePropertyChange(new PropertyChangeEvent(record, RemoteServerRecord.PROP_STATE_CHANGED,
                                        null, RemoteServerRecord.STATE_CANCELLED));
                            }
                        }
                    });
                }
                
                if (dlg.isDefault()) {
                    defaultIndex = model.getSize() - 1;
                    lstDevHosts.setSelectedIndex(defaultIndex);
                }
            }
        }
    }
    
    private void showPathMapper() {
        EditPathMapDialog.showMe((String) lstDevHosts.getSelectedValue(), null);
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
    }

    /** Helps the AddServerDialog know when to enable/disable the OK button */
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        String prop = evt.getPropertyName();
        
        if (source instanceof AddServerDialog && prop.equals(AddServerDialog.PROP_VALID)) {
            AddServerDialog dlg = (AddServerDialog) evt.getSource();
            ok.setEnabled(dlg.isOkValid());
        } else if (source instanceof DialogDescriptor && prop.equals(DialogDescriptor.PROP_VALID)) {
            ((DialogDescriptor) source).setValid(false);
        } else if (source instanceof EditServerListDialog && prop.equals(RemoteServerRecord.PROP_STATE_CHANGED)) {
            Object state = evt.getNewValue();
            if (state == RemoteServerRecord.STATE_OFFLINE) {
                System.err.println("Offline");
            }
            setButtons(true);
        } else if (source instanceof RemoteServerRecord && prop.equals(RemoteServerRecord.PROP_STATE_CHANGED)) {
            Object state = evt.getNewValue();
            if (state == RemoteServerRecord.STATE_CANCELLED) {
                RemoteServerRecord record = (RemoteServerRecord) source;
                String hkey = record.getName();
                lstDevHosts.removeListSelectionListener(this);
                model.removeElement(hkey);
                lstDevHosts.addListSelectionListener(this);
            }
        }
    }
    
    /** Enable/disable the Remove and Set As Default buttons */
    public void valueChanged(ListSelectionEvent evt) {
        int idx = lstDevHosts.getSelectedIndex();
        if (idx >= 0) {
            String key = (String) lstDevHosts.getSelectedValue();
            RemoteServerRecord record = (RemoteServerRecord) RemoteServerList.getInstance().get(key);
            tfStatus.setText(record.getStateAsText());
            btRemoveServer.setEnabled(idx > 0 && buttonsEnabled);
            btSetAsDefault.setEnabled(idx != defaultIndex && buttonsEnabled);
            btPathMapper.setEnabled(!CompilerSetManager.LOCALHOST.equals(lstDevHosts.getSelectedValue()) && buttonsEnabled);
        } else {
            log.warning("ESLD.valueChanged: No selection in Dev Hosts list");
        }
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
        pbarArea = new javax.swing.JPanel();
        lbStatus = new javax.swing.JLabel();
        tfStatus = new javax.swing.JTextField();
        pbarStatusPanel = new javax.swing.JPanel();
        pbPlaceHolder = new javax.swing.JProgressBar();

        lbDevHosts.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_ServerList").charAt(0));
        lbDevHosts.setLabelFor(lstDevHosts);
        lbDevHosts.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_ServerList")); // NOI18N

        lstDevHosts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDevHosts.setSelectedIndex(0);
        jScrollPane1.setViewportView(lstDevHosts);

        btAddServer.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_AddServer").charAt(0));
        btAddServer.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_AddServer")); // NOI18N
        btAddServer.setActionCommand("Add"); // NOI18N

        btRemoveServer.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_RemoveServer").charAt(0));
        btRemoveServer.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_RemoveServer")); // NOI18N
        btRemoveServer.setEnabled(false);

        btSetAsDefault.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_SetAsDefault").charAt(0));
        btSetAsDefault.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_SetAsDefault")); // NOI18N
        btSetAsDefault.setActionCommand("SetAsDefault"); // NOI18N
        btSetAsDefault.setEnabled(false);

        btPathMapper.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_PathMapper").charAt(0));
        btPathMapper.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_PathMapper")); // NOI18N
        btPathMapper.setActionCommand("PathMapper"); // NOI18N

        lbStatus.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/remote/ui/Bundle").getString("MNEM_Status").charAt(0));
        lbStatus.setLabelFor(tfStatus);
        lbStatus.setText(org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_Status")); // NOI18N

        tfStatus.setEditable(false);

        pbarStatusPanel.setPreferredSize(new java.awt.Dimension(100, 24));
        pbarStatusPanel.setLayout(new java.awt.BorderLayout());

        pbPlaceHolder.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pbPlaceHolder.setPreferredSize(new java.awt.Dimension(0, 20));
        pbarStatusPanel.add(pbPlaceHolder, java.awt.BorderLayout.CENTER);

        org.jdesktop.layout.GroupLayout pbarAreaLayout = new org.jdesktop.layout.GroupLayout(pbarArea);
        pbarArea.setLayout(pbarAreaLayout);
        pbarAreaLayout.setHorizontalGroup(
            pbarAreaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pbarAreaLayout.createSequentialGroup()
                .add(lbStatus)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(tfStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 26, Short.MAX_VALUE)
                .add(pbarStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 122, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        pbarAreaLayout.setVerticalGroup(
            pbarAreaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pbarAreaLayout.createSequentialGroup()
                .add(pbarAreaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pbarAreaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(lbStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(tfStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(pbarStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pbarArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(btSetAsDefault, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                            .add(btRemoveServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, btAddServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                            .add(btPathMapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lbDevHosts))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(pbarArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddServer;
    private javax.swing.JButton btPathMapper;
    private javax.swing.JButton btRemoveServer;
    private javax.swing.JButton btSetAsDefault;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbDevHosts;
    private javax.swing.JLabel lbStatus;
    private javax.swing.JList lstDevHosts;
    private javax.swing.JProgressBar pbPlaceHolder;
    private javax.swing.JPanel pbarArea;
    private javax.swing.JPanel pbarStatusPanel;
    private javax.swing.JTextField tfStatus;
    // End of variables declaration//GEN-END:variables
}
