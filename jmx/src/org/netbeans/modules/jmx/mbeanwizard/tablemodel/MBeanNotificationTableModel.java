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
package org.netbeans.modules.jmx.mbeanwizard.tablemodel;

import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.MBeanNotificationType;


/**
 * Class implementing the table model for the mbean notification table
 *
 */
public class MBeanNotificationTableModel extends AbstractJMXTableModel {
    public static final int IDX_NOTIF_CLASS                = 0;
    public static final int IDX_NOTIF_DESCRIPTION          = 1;
    public static final int IDX_NOTIF_TYPE                 = 2;
    private String defaultTypeValue = "";// NOI18N
    
    /**
     * Constructor
     */
    public MBeanNotificationTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanNotificationTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[3];
        
        String sc = bundle.getString("LBL_NotificationClass");// NOI18N
        String sd = bundle.getString("LBL_NotificationDescription");// NOI18N
        String st = bundle.getString("LBL_NotificationType");// NOI18N
        
        columnNames[IDX_NOTIF_CLASS]        = sc;
        columnNames[IDX_NOTIF_DESCRIPTION]  = sd;
        columnNames[IDX_NOTIF_TYPE]         = st;
    }
    
    /**
     * Sets the default notification type value
     * @param defaultValue value to be set
     */
    public void setDefaultTypeValue(String defaultValue) {
        this.defaultTypeValue = defaultValue;
    }
    
    /**
     * Gets the default notification type value
     * @return String the default notification type value
     */
    public String getDefaultTypeValue() {
        return defaultTypeValue;
    }
    
    /**
     * Instantiates a new notification; called when a line is added to the
     * table
     * @return MBeanNotification the created notification
     */
    public MBeanNotification createNewNotification() {
        
        return new MBeanNotification(
                WizardConstants.NOTIFICATION,
                WizardConstants.NOTIF_DESCR_DEFVALUE,
                new ArrayList<MBeanNotificationType>());
    }
    
    /**
     * Returns the notification at index index
     * @return MBeanNotification the notification at index index
     */
    public MBeanNotification getNotification(int index) {
        return (MBeanNotification)data.get(index);
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanNotification notif = (MBeanNotification)data.get(row);
        switch(col) {
            case 0: return notif.getNotificationClass();
            case 1: return notif.getNotificationDescription();
            case 2: return notif.getNotificationTypeList();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanMethodTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanNotification notif = (MBeanNotification)data.get(rowIndex);
            switch(columnIndex) {
                case 0: notif.setNotificationClass((String)aValue);
                break;
                case 1: notif.setNotificationDescription((String)aValue);
                break;
                case 2: notif.setNotificationTypeList(
                        (ArrayList<MBeanNotificationType>)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanMethodTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanNotification mbn = createNewNotification();
        data.add(mbn);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
