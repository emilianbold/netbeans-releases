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

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.mbeanwizard.table.NotificationTypePopupTable;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author an156382
 */
public class NotificationTypePopup extends AbstractPopup{
    
    private int editedRow;
    private MBeanNotificationTableModel notifTableModel;
    
    public NotificationTypePopup(JPanel ancestorPanel, MBeanNotificationTableModel notifTableModel,
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
        
        addJButton.addActionListener(new AddTableRowListener(popupTable,popupTableModel,removeJButton));
        addJButton.setName("addNotifTypeJButton");
        removeJButton.addActionListener(new RemTableRowListener(popupTable,popupTableModel,removeJButton));
        
        closeJButton.addActionListener(new ClosePopupButtonListener(this,textFieldToFill));
    closeJButton.setName("closeNotifTypeJButton");
                
        definePanels(new JButton[] {addJButton,
                removeJButton,
                closeJButton
        },
                popupTable);
    }
    
    protected void readSettings() {
        
        if(notifTableModel.size() != 0) {
            String tmp = (String) notifTableModel.getValueAt(editedRow,
                    MBeanNotificationTableModel.IDX_NOTIF_TYPE);
            String[] stringValues = tmp.split(",");
            if (!((stringValues.length == 1) && (stringValues[0].equals("")))) {
                for (int i = 0; i < stringValues.length; i++) {
                    String toStore = stringValues[i].trim();
                    popupTableModel.addRow();
                    popupTableModel.setValueAt(toStore, i, 
                            NotificationTypeTableModel.IDX_NOTIF_TYPE);
                }
            }
            removeJButton.setEnabled(popupTableModel.getRowCount() > 0);
            
        }
    }
    
    public String storeSettings() {
        
        String notifType = "";
        String notifTypeString = "";
        
        popupTable.editingStopped(new ChangeEvent(this));
        
        for (int i = 0; i < popupTableModel.size(); i++) {
            notifType = (String)popupTableModel.getValueAt(i, NotificationTypeTableModel.IDX_NOTIF_TYPE);
            
            if (notifType != "") {
                if (notifTypeString != "") {
                    notifTypeString += "," + notifType;
                } else {
                    notifTypeString += notifType;
                }
            }
        }
        notifTableModel.setValueAt(notifTypeString, editedRow, MBeanNotificationTableModel.IDX_NOTIF_TYPE);
        
        
        return notifTypeString;
    }
}
