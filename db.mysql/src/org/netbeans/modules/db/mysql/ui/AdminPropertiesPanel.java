/*
 * PropertiesPanel.java
 *
 * Created on February 15, 2008, 12:59 PM
 */

package org.netbeans.modules.db.mysql.ui;

import java.awt.Color;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.db.mysql.Installation;
import org.netbeans.modules.db.mysql.Installation.Command;
import org.netbeans.modules.db.mysql.InstallationSupport;
import org.netbeans.modules.db.mysql.MySQLOptions;
import org.netbeans.modules.db.mysql.ServerInstance;
import org.netbeans.modules.db.mysql.Utils;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Van Couvering
 */
public class AdminPropertiesPanel extends javax.swing.JPanel {
    MySQLOptions options = MySQLOptions.getDefault();
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
    

    private void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        
        String admin = getAdminPath();
        String start = getStartPath();
        String stop = getStopPath();
        
        if ( ! Utils.isValidExecutable(start, true)) {
            error = NbBundle.getMessage(AdminPropertiesPanel.class,
                    "AdminPropertiesPanel.MSG_InvalidStartPath");
        }
        
        if ( ! Utils.isValidExecutable(stop, true)) {
            error = NbBundle.getMessage(AdminPropertiesPanel.class,
                    "AdminPropertiesPanel.MSG_InvalidStopPath");
        }

        if ( (!Utils.isValidURL(admin, true))  && 
             (!Utils.isValidExecutable(admin, true))) {
            error = NbBundle.getMessage(AdminPropertiesPanel.class,
                    "AdminPropertiesPanel.MSG_InvalidAdminPath");
        }
        
        
        if (error != null) {
            messageLabel.setText(error);
            descriptor.setValid(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    
    /** If one command is updated, fill in the other values if it is for
     * a known installation.
     */
    private void fillInCommands() {
        
    }

    /** Creates new form PropertiesPanel */
    public AdminPropertiesPanel(ServerInstance server) {
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        this.setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        
        txtAdmin.getDocument().addDocumentListener(docListener);
        txtStart.getDocument().addDocumentListener(docListener);
        txtStop.getDocument().addDocumentListener(docListener);
        
        txtAdmin.setText(server.getAdminPath());
        txtAdminArgs.setText(server.getAdminArgs());
        txtStart.setText(server.getStartPath());
        txtStartArgs.setText(server.getStartArgs());
        txtStop.setText(server.getStopPath());
        txtStopArgs.setText(server.getStopArgs());
    }
    
    public String getAdminPath() {
        return txtAdmin.getText().trim();
    }
    
    public String getAdminArgs() {
        return txtAdminArgs.getText().trim();
    }
    
    public String getStartPath() {
        return txtStart.getText().trim();
    }
    
    public String getStartArgs() {
        return txtStartArgs.getText().trim();
    }
    
    public String getStopPath() {
        return txtStop.getText().trim();
    }
    
    public String getStopArgs() {
        return txtStopArgs.getText().trim();
    }

    public void setDialogDescriptor(DialogDescriptor desc) {
        this.descriptor = desc;
        validatePanel();
    }
    
    private void chooseFile(JTextField txtField) {
        JFileChooser chooser = new JFileChooser();
        
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        
        String path = txtField.getText().trim();
        if (path != null && path.length() > 0) {
            chooser.setSelectedFile(new File(path));
        }
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        txtField.setText(chooser.getSelectedFile().getAbsolutePath());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtAdmin = new javax.swing.JTextField();
        btnAdminBrowse = new javax.swing.JButton();
        txtAdminArgs = new javax.swing.JTextField();
        txtStart = new javax.swing.JTextField();
        btnStartBrowse = new javax.swing.JButton();
        txtStartArgs = new javax.swing.JTextField();
        txtStop = new javax.swing.JTextField();
        btnStopBrowse = new javax.swing.JButton();
        txtStopArgs = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        txtAdmin.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdmin.text")); // NOI18N
        txtAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAdminActionPerformed(evt);
            }
        });
        txtAdmin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAdminFocusLost(evt);
            }
        });

        btnAdminBrowse.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnAdminBrowse.text")); // NOI18N
        btnAdminBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminBrowseActionPerformed(evt);
            }
        });

        txtAdminArgs.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtAdminArgs.text")); // NOI18N

        txtStart.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStart.text")); // NOI18N
        txtStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStartActionPerformed(evt);
            }
        });
        txtStart.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtStartFocusLost(evt);
            }
        });

        btnStartBrowse.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStartBrowse.text")); // NOI18N
        btnStartBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartBrowseActionPerformed(evt);
            }
        });

        txtStartArgs.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStartArgs.text")); // NOI18N
        txtStartArgs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStartArgsActionPerformed(evt);
            }
        });

        txtStop.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStop.text")); // NOI18N
        txtStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStopActionPerformed(evt);
            }
        });

        btnStopBrowse.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStopBrowse.text")); // NOI18N
        btnStopBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopBrowseActionPerformed(evt);
            }
        });

        txtStopArgs.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.txtStopArgs.text")); // NOI18N

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        messageLabel.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.messageLabel.text")); // NOI18N

        jLabel1.setLabelFor(txtAdmin);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.jLabel2.text")); // NOI18N

        jLabel3.setLabelFor(txtStart);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.jLabel3.text")); // NOI18N

        jLabel4.setLabelFor(txtStop);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.jLabel4.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.jLabel5.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.jLabel6.text")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.jLabel7.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(352, 352, 352)
                .add(jLabel2)
                .add(279, 279, 279))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jLabel3)
                                .add(jLabel4)))
                        .add(layout.createSequentialGroup()
                            .add(78, 78, 78)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jLabel7)
                                .add(jLabel6))))
                    .add(jLabel5)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(txtStartArgs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(txtStopArgs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(txtStop, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(txtStart, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(txtAdminArgs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtAdmin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btnStartBrowse)
                    .add(btnAdminBrowse)
                    .add(btnStopBrowse))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(btnAdminBrowse)
                        .add(txtAdmin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel1))
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(txtAdminArgs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnStartBrowse)
                    .add(txtStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(txtStartArgs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(btnStopBrowse)
                    .add(txtStop, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(txtStopArgs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(messageLabel)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnAdminBrowse, jLabel4, jLabel5, jLabel6, jLabel7}, org.jdesktop.layout.GroupLayout.VERTICAL);

        btnAdminBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnAdminBrowse.AccessibleContext.accessibleDescription")); // NOI18N
        btnStartBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStartBrowse.AccessibleContext.accessibleDescription")); // NOI18N
        btnStopBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdminPropertiesPanel.class, "AdminPropertiesPanel.btnStopBrowse.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void btnAdminBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdminBrowseActionPerformed
    chooseFile(txtAdmin);
}//GEN-LAST:event_btnAdminBrowseActionPerformed

private void txtAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAdminActionPerformed

}//GEN-LAST:event_txtAdminActionPerformed

private void txtStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStopActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtStopActionPerformed

private void btnStartBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartBrowseActionPerformed
    chooseFile(txtStart);
}//GEN-LAST:event_btnStartBrowseActionPerformed

private void btnStopBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopBrowseActionPerformed
    chooseFile(txtStop);
}//GEN-LAST:event_btnStopBrowseActionPerformed

private void txtStartArgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStartArgsActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtStartArgsActionPerformed

private void txtAdminFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAdminFocusLost

}//GEN-LAST:event_txtAdminFocusLost

private void txtStartFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtStartFocusLost

}//GEN-LAST:event_txtStartFocusLost

private void txtStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStartActionPerformed
    /** TODO - nice idea, but no time to do in time for 6.1 
    String startPath = getStartPath();    
    
    Installation installation = InstallationSupport.findInstallationByCommand(
            startPath, Command.START);
    if ( installation != null ) {
        String[] command = installation.getAdminCommand();
        txtAdmin.setText(command[0]);
        txtAdminArgs.setText(command[1]);
        
        command = installation.getStopCommand();
        txtStop.setText(command[0]);
        txtStopArgs.setText(command[1]);
    }
     */

}//GEN-LAST:event_txtStartActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdminBrowse;
    private javax.swing.JButton btnStartBrowse;
    private javax.swing.JButton btnStopBrowse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField txtAdmin;
    private javax.swing.JTextField txtAdminArgs;
    private javax.swing.JTextField txtStart;
    private javax.swing.JTextField txtStartArgs;
    private javax.swing.JTextField txtStop;
    private javax.swing.JTextField txtStopArgs;
    // End of variables declaration//GEN-END:variables

}
