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
package org.netbeans.modules.jmx.mbeanwizard;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.GenericWizardPanel;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;
import javax.swing.event.*;

import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import org.netbeans.modules.jmx.MBeanNotification;

import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.jmx.mbeanwizard.table.NotificationTable;


/**
 *
 * MBean notification panel class: Manages the notifications the user wants the
 * mbean to emit
 *
 */
public class MBeanNotificationPanel extends JPanel {
    private boolean DEBUG = false;
    
    private NotificationTable notificationTable;
    private MBeanNotificationTableModel notificationModel;
    private NotificationsWizardPanel wiz;
    
    private JPanel ancestorPanel = null;
    
    /**
     * MBean Notification Panel constructor
     * @param wiz a wizard panel
     */
    public MBeanNotificationPanel(NotificationsWizardPanel wiz) {
        super(new BorderLayout());
        this.wiz = wiz;
        
        this.ancestorPanel = this;
        
        initComponents();
        String str = NbBundle.getMessage(MBeanNotificationPanel.class,
                "LBL_Notification_Panel");
        setName(str);
    }
    
    private void initJTable() {
        
        notificationModel = new MBeanNotificationTableModel();
        notificationTable = new NotificationTable(ancestorPanel, 
                notificationModel);
        notificationTable.setName("notificationTable");
    }
    
    private void initComponents() {
        initJTable();
        
        // defines a scrolol panel for the JTabel
        JScrollPane notifJTableScrollPanel = new JScrollPane(notificationTable);
        
        JButton notifAddJButton = new JButton();
        Mnemonics.setLocalizedText(notifAddJButton,
                NbBundle.getMessage(MBeanNotificationPanel.class,
                "BUTTON_add_notification"));//NOI18N
        JButton notifRemJButton = new JButton();
        Mnemonics.setLocalizedText(notifRemJButton,
                NbBundle.getMessage(MBeanNotificationPanel.class,
                "BUTTON_rem_notification"));//NOI18N
        
        notifAddJButton.setName("notifAddJButton");
        notifRemJButton.setName("notifRemJButton");
        
        //remove button should first be disabled
        notifRemJButton.setEnabled(false);
        
        notifAddJButton.addActionListener(
                new AddTableRowListener(notificationTable, notificationModel,
                notifRemJButton));
        notifRemJButton.addActionListener(new RemTableRowListener(
                notificationTable, notificationModel, notifRemJButton));
        
        // adds the buttons to this panel
        JPanel notifJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notifJPanel.add(notifAddJButton);
        notifJPanel.add(notifRemJButton);
        
        add(notifJTableScrollPanel, BorderLayout.CENTER);
        add(notifJPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Returns the notification table model
     * @return MBeanNotificationTableModel
     */
    public MBeanNotificationTableModel getNotificationModel() {
        return notificationModel;
    }
    
    /**
     * Method which sets the default type value to the notification type
     * @param defaultTypeValue the default value to set
     */
    public void setDefaultTypeValue(String defaultTypeValue) {
        notificationModel.setDefaultTypeValue(defaultTypeValue);
    }
    
    /**
     * The inner class which defines the wizard panel
     * descriptor with user data
     */
    public static class NotificationsWizardPanel extends GenericWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel {
        private MBeanNotificationPanel panel = null;
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish
         * Button to be always enabled
         * @return finish true if the panel can be the last one and enables 
         * the finish button
         */
        public boolean isFinishPanel() { return true;}
        
        /**
         * Method returning the corresponding panel; here the 
         * MBeanNotificationPanel
         * @return Component the panel
         */
        public Component getComponent() { return getPanel(); }
        
        private MBeanNotificationPanel getPanel() {
            if (panel == null) {
                panel = new MBeanNotificationPanel(this);
            }
            
            return panel;
        }
        
        /**
         * Method which reads the in the model already contained data
         * @param settings an object containing the contents of the 
         * notification table
         */
        public void readSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            String mbeanName = (String)
            wiz.getProperty(WizardConstants.PROP_MBEAN_NAME);
            String mbeanPackageName = (String)
            wiz.getProperty(WizardConstants.PROP_MBEAN_PACKAGE_NAME);
            String defaultTypeValue = "";
            if ((mbeanPackageName != null) && (mbeanName != null)) {
                if (mbeanPackageName.equals("")) {
                    defaultTypeValue = mbeanName + "." + "type";
                } else {
                    defaultTypeValue = mbeanPackageName + "." + mbeanName + "."
                            + "type";
                }
            }
            defaultTypeValue = defaultTypeValue.toLowerCase();
            getPanel().setDefaultTypeValue(defaultTypeValue);
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            //stores all values from the table in the model even with keyboard
            //navigation
            //the if block avoids the problem that the last entry is not taken
            //i.e we only do that if we are on the description TEXT field, not 
            //on the popup cell
            if (getPanel().notificationTable.getEditingColumn() == 
                    MBeanNotificationTableModel.IDX_NOTIF_DESCRIPTION)
                getPanel().notificationTable.editingStopped(
                        new ChangeEvent(this));
            
            // read the content of attributes table
            MBeanNotificationTableModel aModel = getPanel().notificationModel;
            int nbNotifs = aModel.size();
            
            wiz.putProperty(WizardConstants.PROP_NOTIF_NB, 
                    new Integer(nbNotifs).toString());
            
            for (int i = 0 ; i < nbNotifs ; i++) {
                //the current notification 
                MBeanNotification currentNotif = aModel.getNotification(i);
                wiz.putProperty(WizardConstants.PROP_NOTIF_CLASS + i,
                        currentNotif.getNotificationClass());
                
                wiz.putProperty(WizardConstants.PROP_NOTIF_DESCR + i,
                        currentNotif.getNotificationDescription());
                
                wiz.putProperty(WizardConstants.PROP_NOTIF_TYPE + i,
                        currentNotif.getNotificationTypeClasses());
            }
        }
        
        /**
         * Returns the current help context
         * @return HelpCtxt
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_app");
        }
    }
    
}
