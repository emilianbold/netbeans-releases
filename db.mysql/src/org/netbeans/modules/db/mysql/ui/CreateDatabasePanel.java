/*
 * CreateDatabasePanel.java
 *
 * Created on February 15, 2008, 12:59 PM
 */

package org.netbeans.modules.db.mysql.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.*;
import org.netbeans.modules.db.mysql.SampleManager.Sample;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    final ServerInstance server;
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
    
    public static boolean showCreateDatabaseDialog(ServerInstance server) {
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

            // The user cancelled
            if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                return false;
            }
                        
            return createDatabase(panel.getServer(), panel.getDatabaseName(),
                        panel.getGrantUser(), panel.isCreateConnection());
        }
    }

    /**
     * Create a database based on settings of the dialog
     * 
     * @param server the ServerInstance we are working with
     * @param dbname the name of the database
     * @param grantUser the user name to grant full access to or null if 
     *      no grant is desired
     * @param createConnection
     *      set to true if the user wants to create and register a
     *      DatabaseConnection for this database in the Database Explorer
     * 
     * @return true to indicate the dialog can be exited, false to indicate
     *      that the dialog should stay displayed (e.g. we want the user
     *      to be able to try again).
     */
    private static boolean createDatabase(ServerInstance server,
            String dbname, DatabaseUser grantUser, 
            boolean createConnection) {
        
        boolean dbCreated = false;
        try {
            if ( ! checkDeleteExistingDatabase(server, dbname) ) {
                return true;
            }
            
            server.createDatabase(dbname);
            
            dbCreated = true;
            
            // TODO - Create sample schema and tables
            
            if ( grantUser != null ) {
                server.grantFullDatabaseRights(dbname, grantUser);
            }
            
            // TODO - Create database connection            
        } catch ( DatabaseException ex ) {
            displayCreateFailure(server, ex, dbname, dbCreated);
            return false;
        }
        
        return true;
    }
    
    private static void displayCreateFailure(ServerInstance server,
            DatabaseException ex, String dbname, boolean dbCreated) {
        Utils.displayError("CreateDatabasePanel.MSG_CreateFailed", ex);

        if ( dbCreated ) {
            NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(CreateDatabasePanel.class,
                        "CreateDatabasePanel.MSG_DeleteCreatedDatabase",
                        dbname),
                    NbBundle.getMessage(CreateDatabasePanel.class,
                        "CreateDatabasePanel.STR_DeleteCreatedDatabaseTitle"),
                    NotifyDescriptor.YES_NO_OPTION);
            
            Object response = DialogDisplayer.getDefault().notify(ndesc);
            
            if ( response == NotifyDescriptor.YES_OPTION ) {
                try {
                    server.dropDatabase(dbname);
                } catch ( DatabaseException exc ) {
                    Utils.displayError(
                        NbBundle.getMessage(CreateDatabasePanel.class, 
                           "CreateDatabasePanel.MSG_DeleteFailed"), 
                        ex);
                }
            }
        }
    }

    
    /**
     * Check to see if a database exists, and drop it if the user wants
     * to.
     * 
     * @return true if it's OK to continue or false to cancel
     */
    private static boolean checkDeleteExistingDatabase(
            ServerInstance server, String dbname) throws DatabaseException {
        if ( ! server.databaseExists(dbname)) {
            return true;
        }
             
       NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(CreateDatabasePanel.class, 
                        "CreateDatabasePanel.MSG_DatabaseAlreadyExists",
                    dbname),
                NbBundle.getMessage(CreateDatabasePanel.class,
                    "CreateDatabasePanel.STR_DatabaseExistsTitle"),
                NotifyDescriptor.YES_NO_CANCEL_OPTION);
        
        Object response =  DialogDisplayer.getDefault().notify(ndesc);
        
        if ( response == NotifyDescriptor.CANCEL_OPTION ) {
            return false;
        } else if ( response == NotifyDescriptor.YES_OPTION) {
            server.dropDatabase(dbname);
        }
        
        return true;        
    }
    

    /** Creates new form CreateDatabasePanel */
    public CreateDatabasePanel(ServerInstance server) {
        this.server = server;
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        
        comboDatabaseName.setModel(new DatabaseComboModel());
        comboDatabaseName.addActionListener(actionListener);
        
        comboUsers.setModel(new UsersComboModel(server));
        
        if ( comboUsers.getItemCount() == 0 ) {
            comboUsers.setVisible(false);
            chkGrantAccess.setVisible(false);
        } else {
            comboUsers.setSelectedIndex(0);
            setGrantAccess(false);
            chkGrantAccess.addActionListener(actionListener);
        }
        
        setCreateConnection(true);

        
        setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        messageLabel.setText(" ");
        
    }


    private String getDatabaseName() {
        String dbname = (String)comboDatabaseName.getEditor().getItem();
        if ( dbname != null ) {
            dbname = dbname.trim();
        }
        return dbname;
    }
    
    private DatabaseUser getGrantUser() {
        return (DatabaseUser)comboUsers.getSelectedItem();
    }
    
    private void setDialogDescriptor(DialogDescriptor desc) {
        this.descriptor = desc;
        validatePanel();
    }
    
    private void setGrantAccess(boolean grant) {
        this.chkGrantAccess.setSelected(grant);
    }
    
    private boolean isGrantAccess() {
        return chkGrantAccess.isSelected();
    }
    
    private void setCreateConnection(boolean create) {
        chkCreateConnection.setSelected(create);
    }
    
    private boolean isCreateConnection() {
        return chkCreateConnection.isSelected();
    }
    
    private ServerInstance getServer() {
        return server;
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
        chkGrantAccess = new javax.swing.JCheckBox();
        comboUsers = new javax.swing.JComboBox();
        chkCreateConnection = new javax.swing.JCheckBox();

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        messageLabel.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.messageLabel.text")); // NOI18N

        comboDatabaseName.setEditable(true);
        comboDatabaseName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboDatabaseName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboDatabaseNameActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.jLabel1.text")); // NOI18N

        chkGrantAccess.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.chkGrantAccess.text")); // NOI18N

        comboUsers.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        chkCreateConnection.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.chkCreateConnection.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 344, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chkCreateConnection)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(jLabel1)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(comboDatabaseName, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(chkGrantAccess)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(comboUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(comboDatabaseName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(9, 9, 9)
                .add(chkCreateConnection)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comboUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chkGrantAccess))
                .add(18, 18, 18)
                .add(messageLabel))
        );

        messageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.messageLabel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void comboDatabaseNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboDatabaseNameActionPerformed

}//GEN-LAST:event_comboDatabaseNameActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkCreateConnection;
    private javax.swing.JCheckBox chkGrantAccess;
    private javax.swing.JComboBox comboDatabaseName;
    private javax.swing.JComboBox comboUsers;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables

    private static class DatabaseComboModel implements ComboBoxModel {
        static final Sample[] SAMPLES = Sample.values();
        static final String samplePrefix =
                NbBundle.getMessage(CreateDatabasePanel.class, 
                    "CreateDatabasePanel.STR_SampleDatabase") + ": ";
        
        String selected = null;
        final ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

        public void setSelectedItem(Object item) {
            selected = (String)item;
        }

        public Object getSelectedItem() {
            if ( selected != null && selected.startsWith(samplePrefix)) {
                // trim off the "Sample database: " string
                return selected.replace(samplePrefix, "");
            } else if ( selected != null ) {
                return selected; 
            } else {
                return "";
            }
        }

        public int getSize() {
            return SAMPLES.length;
        }

        public Object getElementAt(int index) {
            return  samplePrefix + SAMPLES[index].toString();
        }

        public void addListDataListener(ListDataListener listener) {
        }

        public void removeListDataListener(ListDataListener listener) {
        }
                
    }
    
    private static class UsersComboModel implements ComboBoxModel {
        final ServerInstance server;

        ArrayList<DatabaseUser> users= new ArrayList<DatabaseUser>();
        DatabaseUser selected;
                
        public UsersComboModel(ServerInstance server) {
            this.server = server;
                        
            try {            
                users.addAll(server.getUsers());
                
                // Remove the root user, this user always has full access
                DatabaseUser rootUser = null;
                for ( DatabaseUser user : users ) {
                    if ( user.getUser() != null && user.getUser().equals("root")) {
                        // Note the user.  We can't remove it while iterating,
                        // that's a concurrent modification...
                        rootUser = user;
                        break;
                    }
                }
                
                if ( rootUser != null ) {
                    users.remove(rootUser);
                }
            } catch ( DatabaseException dbe )  {
                LOGGER.log(Level.WARNING, null, dbe);
                Utils.displayError("CreateDatabasePanel.MSG_UnableToGetUsers", 
                        dbe);
            }
        }

        public void setSelectedItem(Object item) {
            selected = (DatabaseUser)item;
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
