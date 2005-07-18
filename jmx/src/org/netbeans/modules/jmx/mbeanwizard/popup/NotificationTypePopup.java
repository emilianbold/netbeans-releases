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

package org.netbeans.modules.jmx.mbeanwizard.popup;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.NotificationTypeTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.ClosePopupButtonListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.mbeanwizard.table.NotificationTypePopupTable;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.MBeanNotificationType;

/**
 * Class implementing the notification popup window
 *
 */
public class NotificationTypePopup extends AbstractPopup{
    
    private int editedRow;
    private MBeanNotificationTableModel notifTableModel;
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel of the popup window; here the 
     * wizard
     * @param notifTableModel the table model of the notification table in 
     * the wizard
     * @param textField the textfield to fill with the information from 
     * the popup
     * @param editedRow the current edited row in the wizard notification table
     */
    public NotificationTypePopup(JPanel ancestorPanel, 
            MBeanNotificationTableModel notifTableModel,
            JTextField textField, int editedRow) {
        
        super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor());
        
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        
        this.notifTableModel = notifTableModel;
        this.textFieldToFill = textField;
        this.editedRow = editedRow;
        
        initJTable();
        initComponents();
        readSettings();
        
        setDimensions(NbBundle.getMessage(NotificationTypePopup.class,
                "LBL_NotificationType_Popup"));
    }
    
    protected void initJTable() {
        
        popupTableModel = new NotificationTypeTableModel(
                notifTableModel.getDefaultTypeValue());
        popupTable = new NotificationTypePopupTable(popupTableModel);
        popupTable.setName("notifPopupTable");
    }
    
    protected void initComponents() {
        
        addJButton = instanciatePopupButton(NotificationTypePopup.class,
                "LBL_Notification_addType");
        removeJButton = instanciatePopupButton(NotificationTypePopup.class,
                "LBL_Notification_remType");
        closeJButton = instanciatePopupButton(NotificationTypePopup.class,
                "LBL_Notification_close");
        
        addJButton.addActionListener(
            new AddTableRowListener(popupTable,popupTableModel,removeJButton));
        addJButton.setName("addNotifTypeJButton");
        removeJButton.addActionListener(
            new RemTableRowListener(popupTable,popupTableModel,removeJButton));
        removeJButton.setName("remNotifTypeJButton");
        closeJButton.addActionListener(new ClosePopupButtonListener(
                this,textFieldToFill));
        closeJButton.setName("closeNotifTypeJButton");
        
        definePanels(new JButton[] {addJButton,
                removeJButton,
                closeJButton
        },
                popupTable);
    }
    
    protected void readSettings() {
        if(notifTableModel.size() != 0) {
            //get the notification type list for the current notification
            ArrayList<MBeanNotificationType> notifTypes =
                    (ArrayList<MBeanNotificationType>)
          notifTableModel.getNotification(editedRow).getNotificationTypeList();
            
            
            for (int i = 0; i < notifTypes.size(); i++) {
                popupTableModel.addRow();
                //copy the notification type i from the panel model
                //to the popup model for the current notification
                ((NotificationTypeTableModel)
                                popupTableModel).setNotificationType(i, 
                                                            notifTypes.get(i));
            }
            removeJButton.setEnabled(popupTableModel.getRowCount() > 0);
        }
    }
    
    /**
     * Inhertited from superclass
     */
    public String storeSettings() {
        
        //stores all values from the table in the model even with keyboard
        //navigation
        popupTable.editingStopped(new ChangeEvent(this));
        
        String notifTypeString = "";
        String notifTypeContent = "";
        ArrayList<MBeanNotificationType> mbnt = 
                new ArrayList<MBeanNotificationType>();
        
        for (int i = 0 ; i < popupTableModel.size(); i++) {
            //gets the current notification type in the popup
            MBeanNotificationType notifType =
                ((NotificationTypeTableModel)
                popupTableModel).getNotificationType(i);
            
            notifTypeContent = notifType.getNotificationType();
            
            if (notifTypeContent != "")
                notifTypeString += notifTypeContent;
            
            if (i < popupTableModel.size() -1)
                notifTypeString += ",";
            
            // fills the arraylist with the exceptions to store
            mbnt.add(notifType);
        }
        
        //copy back the notification types from the popup to the panel model
        notifTableModel.getNotification(editedRow).setNotificationTypeList(mbnt);
        
        return notifTypeString;
    }
}
