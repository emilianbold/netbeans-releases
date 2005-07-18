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

package org.netbeans.modules.jmx;

import java.util.ArrayList;

/**
 * Class which describes the structure of an MBean notification
 *
 */
public class MBeanNotification {
    
    private String notificationClass = "";
    private String notificationDescription = "";
    private ArrayList<MBeanNotificationType> notificationType = null;
    
    /**
     * Custom constructor
     * @param notificationClass the java class of the notification
     * @param notificationDescription the description of the notification
     * @param notificationType the list of the notification types for that 
     * notification
     */
    public MBeanNotification(String notificationClass, 
            String notificationDescription,
            ArrayList<MBeanNotificationType> notificationType) {
        this.notificationClass = notificationClass;
        this.notificationDescription = notificationDescription;
        this.notificationType = notificationType;
    }
    
    /**
     * Sets the notification class
     * @param notifClass the notification class to set
     */
    public void setNotificationClass(String notifClass) {
        this.notificationClass = notifClass;
    }
    
    /**
     * Method which returns the class of the notification
     * @return String the class of the notification
     */
    public String getNotificationClass() {
        return notificationClass;
    }
    
    /**
     * Sets the notification description
     * @param notifDescr the notification description to set
     */
    public void setNotificationDescription(String notifDescr) {
        this.notificationDescription = notifDescr;
    }
    
    /**
     * Method which returns the description of the notification
     * @return String the description of the notification
     *
     */
    public String getNotificationDescription() {
        return notificationDescription;
    }
    
    /**
     * Adds a notification type to the notification type list
     * @param notifType the notification type to add to the list
     */
    public void addNotificationType(MBeanNotificationType notifType) {
        notificationType.add(notifType);
    }
    
    /**
     * Removes a notification type from the list
     * @param notifType the notification type to remove from the list
     */
    public void removeNotificationType(MBeanNotificationType notifType) {
        notificationType.remove(notifType);
    }
    
    /**
     * Removes a notification type by it's index in the list
     * @param index the index of the notification type to remove
     */
    public void removeNotificationType(int index) {
        notificationType.remove(index);
    }
    
    /**
     * Method which returns the a notification type by it's index in the list
     * @return MBeanNotificationType the notification type
     * @param index the index of the notification type to return
     */
    public MBeanNotificationType getNotificationType(int index) {
        return notificationType.get(index);
    }
    
    /**
     * Sets the notification type list of this notification
     * @param array array of notification types
     */
    public void setNotificationTypeList(ArrayList<MBeanNotificationType> 
            array) {
        notificationType = array;
    }
    
    /**
     * Returns the whole notification type list for the current notification
     * @return ArrayList<MBeanNotificationType> the notification type list
     */
    public ArrayList<MBeanNotificationType> getNotificationTypeList() {
        return notificationType;
    }
    
    /**
     * Returns a string concat of all notification types for the current
     * notification; each one seperated by ","
     * @return String the string containing all notification types
     */
    public String getNotificationTypeClasses() {
        String notifTypeClass = "";
        for (int i = 0; i < notificationType.size(); i++) {
            notifTypeClass += notificationType.get(i).getNotificationType();
            
            if (i < notificationType.size() -1)
                notifTypeClass += ",";
        }
        return notifTypeClass;
    }
    
    /**
     * Method which returns the number of types of the current notification
     * Returns -1 if null
     * @return int the number of types of the notification
     *
     */
    public int getNotificationTypeCount() {
        if (notificationType != null)
            return notificationType.size();
        else
            return -1;
    }
}
