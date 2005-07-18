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
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.MBeanNotificationType;
import org.openide.util.NbBundle;



/**
 * Class implementing the table model for the mbean notification type 
 * popup table
 * 
 */
public class NotificationTypeTableModel extends AbstractJMXTableModel{
    
    public static final int IDX_NOTIF_TYPE          = 0;
    private String defaultTypeValue = "";// NOI18N
    
    /**
     * Constructor
     * @param defaultTypeValue the default notification type value for 
     * the popup
     */
    public NotificationTypeTableModel(String defaultTypeValue)
    {
        super();
        bundle = NbBundle.getBundle(OperationParameterTableModel.class);
        data = new ArrayList();
        columnNames = new String[1];
        String sopn = bundle.getString("LBL_NotificationType");// NOI18N
        columnNames[IDX_NOTIF_TYPE]        = sopn;
        this.defaultTypeValue = defaultTypeValue;
    }
    
    /**
     * Creates a new MBean notification type
     * @return MBeanNotificationType the created notification type
     */
    public MBeanNotificationType createNewNotificationType() {
        
        return new MBeanNotificationType(defaultTypeValue);
    }
    
    /**
     * Returns a notification type according to his index
     * @param index the index of the notification type in the list
     * @return MBeanNotificationType the notification type
     */
    public MBeanNotificationType getNotificationType(int index) {
        return (MBeanNotificationType)data.get(index);
    }
    
    /**
     * Sets the notification type at index index to a new object
     * @param index the index
     * @param notifType the Notification type to set
     */
    public void setNotificationType(int index, 
                                    MBeanNotificationType notifType) {
        if (index < data.size()) {
            data.set(index, notifType);
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanNotificationType notifType = (MBeanNotificationType)data.get(row);
        switch(col) {
            case 0: return notifType.getNotificationType();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "NotificationTypeTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanNotificationType notifType = 
                    (MBeanNotificationType)data.get(rowIndex);
            switch(columnIndex) {
                case 0: notifType.setNotificationType((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanAttributeTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanNotificationType mbnt = createNewNotificationType();
        data.add(mbnt);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
}
