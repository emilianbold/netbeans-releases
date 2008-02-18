/*
 * SettingsPanel.java
 *
 * Created on February 15, 2008, 12:59 PM
 */

package org.netbeans.modules.db.mysql.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.*;
import org.netbeans.modules.db.mysql.SampleManager.Sample;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Dialog for creating a new MySQL database
 * 
 * @author  David Van Couvering
 */
public class CreateDatabasePanel extends javax.swing.JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(
            CreateDatabasePanel.class.getName());

    DialogDescriptor descriptor;
    private Color nbErrorForeground;

    
    private DocumentListener docListener = new DocumentListener() {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

    };
    
    private ActionListener actionListener = new ActionListener() {

        public void actionPerformed(ActionEvent arg0) {
            validatePanel();
        }
    };


    private void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        
        comboUsers.setEnabled(this.isGrantAccess());
        
        if ( getDatabaseName() == null || getDatabaseName().equals("")) {
            error = NbBundle.getMessage(CreateDatabasePanel.class,
                        "CreateDatabasePanel.MSG_SpecifyDatabase");
        }
        
        if (error != null) {
            messageLabel.setText(error);
            descriptor.setValid(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    public static boolean showCreateDatabase(ServerInstance server) {
        assert SwingUtilities.isEventDispatchThread();
        
        CreateDatabasePanel panel = new CreateDatabasePanel(server);
        String title = NbBundle.getMessage(CreateDatabasePanel.class, 
                "CreateDatabasePanel.LBL_CreateDatabaseTitle");

        DialogDescriptor desc = new DialogDescriptor(panel, title);
        panel.setDialogDescriptor(desc);

        for (;;) {                    
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            String acsd = NbBundle.getMessage(CreateDatabasePanel.class, 
                    "CreateDatabasePanel.ACSD_CreateDatabasePanel");
            dialog.getAccessibleContext().setAccessibleDescription(acsd);
            dialog.setVisible(true);
            dialog.dispose();

            if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                return false;
            }
            
            // TODO - Take action basied on dialog settings
            
            return true;
        }
    }
    

    /** Creates new form CreateDatabasePanel */
    public CreateDatabasePanel(ServerInstance server) {
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        
        comboDatabaseName.setModel(new DatabaseComboModel());
        comboDatabaseName.addActionListener(actionListener);
        
        comboUsers.setModel(new UsersComboModel(server));
        setGrantAccess(false);
        this.chkFullAccess.addActionListener(actionListener);
        
        setCreateConnection(true);

        
        setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        messageLabel.setText(" ");
        
    }


    private String getDatabaseName() {
        return (String)comboDatabaseName.getSelectedItem();
    }
    
    private String getGrantUser() {
        return (String)comboUsers.getSelectedItem();
    }
    
    private void setDialogDescriptor(DialogDescriptor desc) {
        this.descriptor = desc;
        validatePanel();
    }
    
    private void setGrantAccess(boolean grant) {
        this.chkFullAccess.setSelected(grant);
    }
    
    private boolean isGrantAccess() {
        return chkFullAccess.isSelected();
    }
    
    private void setCreateConnection(boolean create) {
        chkCreateConnection.setSelected(create);
    }
    
    private boolean isCreateConnection() {
        return chkCreateConnection.isSelected();
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        comboDatabaseName = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        chkFullAccess = new javax.swing.JCheckBox();
        comboUsers = new javax.swing.JComboBox();
        chkCreateConnection = new javax.swing.JCheckBox();

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        messageLabel.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "SettingsPanel.messageLabel.text")); // NOI18N

        comboDatabaseName.setEditable(true);
        comboDatabaseName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboDatabaseName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboDatabaseNameActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.jLabel1.text")); // NOI18N

        chkFullAccess.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.chkFullAccess.text")); // NOI18N

        comboUsers.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        chkCreateConnection.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.chkCreateConnection.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 432, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chkCreateConnection)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(jLabel1)
                            .add(18, 18, 18)
                            .add(comboDatabaseName, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(chkFullAccess)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(comboUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 235, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(comboDatabaseName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chkFullAccess)
                    .add(comboUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 13, Short.MAX_VALUE)
                .add(chkCreateConnection)
                .add(18, 18, 18)
                .add(messageLabel)
                .add(7, 7, 7))
        );
    }// </editor-fold>//GEN-END:initComponents

private void comboDatabaseNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboDatabaseNameActionPerformed

}//GEN-LAST:event_comboDatabaseNameActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkCreateConnection;
    private javax.swing.JCheckBox chkFullAccess;
    private javax.swing.JComboBox comboDatabaseName;
    private javax.swing.JComboBox comboUsers;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables

    private static class DatabaseComboModel implements ComboBoxModel {
        static final Sample[] SAMPLES = Sample.values();
        
        String selected = null;
        final ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

        public void setSelectedItem(Object item) {
            selected = (String)item;
        }

        public Object getSelectedItem() {
            return selected;
        }

        public int getSize() {
            return SAMPLES.length;
        }

        public Object getElementAt(int index) {
            return NbBundle.getMessage(CreateDatabasePanel.class, 
                    "CreateDatabasePanel.STR_SampleDatabase") + ": " + 
                    SAMPLES[index].toString();
        }

        public void addListDataListener(ListDataListener listener) {
        }

        public void removeListDataListener(ListDataListener listener) {
        }
                
    }
    
    private static class UsersComboModel implements ComboBoxModel {
        final ServerInstance server;

        List<String> users;
        String selected;
                
        public UsersComboModel(ServerInstance server) {
            this.server = server;
                        
            try {            
                users = server.getUsers();
                
                // Remove the root user, this user always has full access
                users.remove("root"); // NOI18N
            } catch ( DatabaseException dbe )  {
                LOGGER.log(Level.WARNING, null, dbe);
                Utils.displayError("CreateDatabasPanel.MSG_UnableToGetUsers", 
                        dbe);
                users = new ArrayList<String>();
            }
            
            assert users != null;
        }

        public void setSelectedItem(Object item) {
            selected = (String)item;
        }

        public Object getSelectedItem() {
            return selected;
        }

        public int getSize() {
            return users.size();
        }

        public Object getElementAt(int index) {
            return users.get(index);
        }

        public void addListDataListener(ListDataListener arg0) {
        }

        public void removeListDataListener(ListDataListener arg0) {
        }
    }
}
