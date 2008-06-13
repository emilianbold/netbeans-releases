/*
 * CreateDatabasePanel.java
 *
 * Created on February 15, 2008, 12:59 PM
 */

package org.netbeans.modules.db.mysql.ui;

import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.util.Utils;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.*;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.impl.SampleManager;
import org.netbeans.modules.db.mysql.impl.SampleManager.SampleName;
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
    final DatabaseServer server;
    private Color nbErrorForeground;

    private void validatePanel(String databaseName) {
        if (descriptor == null) {
            return;
        }
        
        String error = null;

        comboUsers.setEnabled(this.isGrantAccess());
                
        if ( Utils.isEmpty(databaseName) ) {
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
        
    public static DatabaseConnection showCreateDatabaseDialog(DatabaseServer server) {
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
                return null;
            }
            
            if ( Utils.isEmpty(panel.getDatabaseName())) {
                Utils.displayErrorMessage(
                    NbBundle.getMessage(CreateDatabasePanel.class,
                    "CreateDatabasePanel.MSG_EmptyDatabaseName"));
                continue;
            }
                        
            return createDatabase(panel.getServer(), panel.getDatabaseName(),
                        panel.getGrantUser());
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
     * @return the database connection to the newly created database or 
     *         <code>null</code> if a connection to the database was not created.
     */
    private static DatabaseConnection createDatabase(DatabaseServer server,
            String dbname, DatabaseUser grantUser) {
        
        boolean dbCreated = false;
        DatabaseConnection result = null;
        try {
            if ( ! checkDeleteExistingDatabase(server, dbname) ) {
                return null;
            }
            
            server.createDatabase(dbname);
            
            dbCreated = true;
            
            String user;
            if ( grantUser != null ) {
                server.grantFullDatabaseRights(dbname, grantUser);
                user = grantUser.getUser();
            } else {
                user = server.getUser();
            }
               
            result = createConnection(server, dbname, user);
            
            if ( result != null && SampleManager.isSampleName(dbname) ) {
                SampleManager.createSample(dbname, result);
            }
        } catch ( DatabaseException ex ) {
            displayCreateFailure(server, ex, dbname, dbCreated);
            return null;
        }
        
        return result;
    }
        
    private static void displayCreateFailure(DatabaseServer server,
            DatabaseException ex, String dbname, boolean dbCreated) {
        Utils.displayError(NbBundle.getMessage(CreateDatabasePanel.class,
                "CreateDatabasePanel.MSG_CreateFailed"), ex);

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
                server.dropDatabase(dbname);
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
            DatabaseServer server, String dbname) throws DatabaseException {
        if ( ! server.databaseExists(dbname)) {
            return true;
        }
             
       NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(CreateDatabasePanel.class, 
                        "CreateDatabasePanel.MSG_DatabaseAlreadyExists",
                    dbname),
                NbBundle.getMessage(CreateDatabasePanel.class,
                    "CreateDatabasePanel.STR_DatabaseExistsTitle"),
                NotifyDescriptor.YES_NO_OPTION);
        
        Object response =  DialogDisplayer.getDefault().notify(ndesc);
        
        if ( response == NotifyDescriptor.NO_OPTION ) {
            return false;
        } else {
            server.dropDatabase(dbname);
            return true;
        }
    }
    
    private static DatabaseConnection createConnection(
            DatabaseServer server, String dbname, String grantUser) {
        
        List<DatabaseConnection> conns = DatabaseUtils.
                findDatabaseConnections(server.getURL(dbname));
        if ( ! conns.isEmpty() ) {
            // We already have a connection, no need to create one
            return conns.get(0);
        }
        
        String user;
        
        if ( grantUser == null || grantUser.equals("")) {
            user = server.getUser();
        } else {
            user = grantUser;
        }
        
        String url = server.getURL(dbname);
        
        return ConnectionManager.getDefault().
            showAddConnectionDialogFromEventThread(
                DatabaseUtils.getJDBCDriver(), url, user, null);        
    }

    

    /** Creates new form CreateDatabasePanel */
    public CreateDatabasePanel(DatabaseServer server) {
        this.server = server;
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        
        comboDatabaseName.setModel(new DatabaseComboModel());
        
        comboDatabaseName.getEditor().getEditorComponent().addKeyListener(
            new KeyListener() {

            public void keyTyped(KeyEvent event) {
                // Get the actual key.  apparently getItem() doesn't return the current
                // string typed until *after* this event finishes :(
                String keyStr = Character.toString(event.getKeyChar()).trim();
                String dbname;
                if ( Utils.isEmpty(keyStr)) {
                    dbname = comboDatabaseName.getEditor().getItem().toString().trim();
                } else {
                    // We know the database name has at least this character in it
                    dbname = keyStr;
                }
                
                validatePanel(dbname);
            }

            public void keyPressed(KeyEvent event) {
            }

            public void keyReleased(KeyEvent event) {
            }
        });
                        
        comboUsers.setModel(new UsersComboModel(server));
        
        if ( comboUsers.getItemCount() == 0 ) {
            comboUsers.setVisible(false);
            chkGrantAccess.setVisible(false);
        } else {
            comboUsers.setSelectedIndex(0);
            setGrantAccess(false);
        }
                
        setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        messageLabel.setText(" ");        
    }


    private String getDatabaseName() {
        String dbname = (String)comboDatabaseName.getSelectedItem();
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
        validatePanel("");
    }
    
    private void setGrantAccess(boolean grant) {
        this.chkGrantAccess.setSelected(grant);
    }
    
    private boolean isGrantAccess() {
        return chkGrantAccess.isSelected();
    }
        
    private DatabaseServer getServer() {
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

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.messageLabel.text")); // NOI18N

        comboDatabaseName.setEditable(true);
        comboDatabaseName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboDatabaseName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboDatabaseNameItemStateChanged(evt);
            }
        });
        comboDatabaseName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboDatabaseNameActionPerformed(evt);
            }
        });
        comboDatabaseName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                comboDatabaseNameFocusLost(evt);
            }
        });
        comboDatabaseName.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                comboDatabaseNameInputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });
        comboDatabaseName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                comboDatabaseNameKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboDatabaseNameKeyPressed(evt);
            }
        });
        comboDatabaseName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                comboDatabaseNameMouseReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkGrantAccess, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.chkGrantAccess.text")); // NOI18N
        chkGrantAccess.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkGrantAccessItemStateChanged(evt);
            }
        });

        comboUsers.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(chkGrantAccess)
                        .add(18, 18, 18)
                        .add(comboUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(18, 18, 18)
                        .add(comboDatabaseName, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 344, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(comboDatabaseName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(chkGrantAccess)
                    .add(comboUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .add(22, 22, 22))
        );

        messageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.messageLabel.AccessibleContext.accessibleName")); // NOI18N
        chkGrantAccess.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.chkGrantAccess.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void comboDatabaseNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboDatabaseNameActionPerformed

}//GEN-LAST:event_comboDatabaseNameActionPerformed

private void comboDatabaseNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboDatabaseNameItemStateChanged
    validatePanel(evt.getItem().toString().trim());
}//GEN-LAST:event_comboDatabaseNameItemStateChanged

private void comboDatabaseNameMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_comboDatabaseNameMouseReleased

}//GEN-LAST:event_comboDatabaseNameMouseReleased

private void comboDatabaseNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboDatabaseNameKeyTyped

}//GEN-LAST:event_comboDatabaseNameKeyTyped

private void comboDatabaseNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comboDatabaseNameFocusLost

}//GEN-LAST:event_comboDatabaseNameFocusLost

private void chkGrantAccessItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkGrantAccessItemStateChanged
    if ( isGrantAccess() ) {
        comboUsers.setEnabled(true);
    } else {
        comboUsers.setEnabled(false);
    }
}//GEN-LAST:event_chkGrantAccessItemStateChanged

private void comboDatabaseNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboDatabaseNameKeyPressed

}//GEN-LAST:event_comboDatabaseNameKeyPressed

private void comboDatabaseNameInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_comboDatabaseNameInputMethodTextChanged

}//GEN-LAST:event_comboDatabaseNameInputMethodTextChanged



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkGrantAccess;
    private javax.swing.JComboBox comboDatabaseName;
    private javax.swing.JComboBox comboUsers;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables

    private static class DatabaseComboModel implements ComboBoxModel {
        static final SampleName[] SAMPLES = SampleName.values();
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
            return samplePrefix + SAMPLES[index].toString();
        }

        public void addListDataListener(ListDataListener listener) {
        }

        public void removeListDataListener(ListDataListener listener) {
        }
                
    }
    
    private static class UsersComboModel implements ComboBoxModel {
        final DatabaseServer server;

        ArrayList<DatabaseUser> users= new ArrayList<DatabaseUser>();
        DatabaseUser selected;
                
        public UsersComboModel(DatabaseServer server) {
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
                // This can be caused by permission problems.  Log the error
                // and continue with an empty user list
                LOGGER.log(Level.INFO, null, dbe);
                users.clear();
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
