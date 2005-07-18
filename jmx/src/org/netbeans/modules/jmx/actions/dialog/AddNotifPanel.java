/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.actions.AddNotifAction;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.table.NotificationTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel which is used to ask notifications to user.
 * @author  tl156378
 */
public class AddNotifPanel extends javax.swing.JPanel {
    
    /** class to add registration of MBean */
    private JavaClass currentClass;
    
    private MBeanNotificationTableModel notificationModel;
    private NotificationTable notificationTable;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    
    /**
     * Returns all the specified notifications by user.
     * @return <CODE>MBeanNotification[]</CODE> specified notifications by user.
     */
    public MBeanNotification[] getNotifications() {
        MBeanNotification[] notifs = 
                new MBeanNotification[notificationModel.getRowCount()];
        for (int i = 0; i < notificationModel.getRowCount(); i++)
            notifs[i] = notificationModel.getNotification(i);
        return notifs;
    }
     
    /** 
     * Creates new form RemoveAttrPanel.
     * @param  node  node selected when the Register Mbean action was invoked
     */
    public AddNotifPanel(Node node) {
        bundle = NbBundle.getBundle(AddNotifAction.class);
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        Resource rc = JavaModel.getResource(fo);
        currentClass = WizardHelpers.getJavaClass(rc,fo.getName());
        
        // init tags
        
        initComponents();
        
        notificationModel = new MBeanNotificationTableModel();
        notificationModel.setDefaultTypeValue(
                getMBeanClass().getName().toLowerCase() + WizardConstants.TYPE);
        notificationTable = new NotificationTable(this, notificationModel);
        notificationTable.setName("notificationTable"); // NOI18N
        notificationTable.setBorder(new javax.swing.border.EtchedBorder());
        jScrollPane1.setViewportView(notificationTable);
        
        removeButton.setEnabled(false);
        addButton.addActionListener(
                new AddTableRowListener(notificationTable, notificationModel,
                removeButton));
        removeButton.addActionListener(new RemTableRowListener(
                notificationTable, notificationModel, removeButton));
        
        // init labels
        Mnemonics.setLocalizedText(addButton,
                     bundle.getString("LBL_Button_AddNotification")); // NOI18N
        Mnemonics.setLocalizedText(removeButton,
                     bundle.getString("LBL_Button_RemoveNotification")); // NOI18N
        
    }
    
    private boolean isAcceptable() {
        return true;
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options 
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if specified attributes are correct.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_AddNotifAction.Title"); // NOI18N

        btnOK = new JButton(bundle.getString("LBL_OK")); // NOI18N
        btnOK.setEnabled(isAcceptable());
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor (
                        this,
                        title,
                        true,                       //modal
                        new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                        btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx(AddNotifAction.class),
                        (ActionListener) null
                ));
        
        if (returned == btnOK) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns the MBean class to add notifications.
     * @return <CODE>JavaClass</CODE> the MBean class
     */
    public JavaClass getMBeanClass() {
        return currentClass;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        buttonsPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(380, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        leftPanel.add(removeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        leftPanel.add(addButton, gridBagConstraints);

        buttonsPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(buttonsPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
}
