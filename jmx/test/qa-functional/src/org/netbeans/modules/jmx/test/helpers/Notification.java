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

package org.netbeans.modules.jmx.test.helpers;

import java.util.ArrayList;

/**
 *
 * @author an156382
 */
public class Notification {
    
    private String notificationClass = "";
    private String notificationComment = "";
    private ArrayList<NotificationType> notificationType = null;
    
    /** Creates a new instance of Notification */
    public Notification(String notificationClass, String notificationComment,
            ArrayList<NotificationType> notificationType) {
        this.notificationClass = notificationClass;
        this.notificationComment = notificationComment;
        this.notificationType = notificationType;
    }
    
    /**
     * Method which returns the class of the notification
     * @return notificationClass the class of the notification
     *
     */
    public String getNotificationClass() {
        return notificationClass;
    }
    
    /**
     * Method which returns the comment of the notification
     * @return notificationComment the comment of the notification
     *
     */
    public String getNotificationComment() {
        return notificationComment;
    }
    
    /**
     * Method which returns the type of the notification
     * @return notificationType the type of the notification
     *
     */
    public NotificationType getNotificationType(int index) {
        return notificationType.get(index);
    }
    
    /**
     * Method which returns the number of types of the notification
     * @return notificationType the number of types of the notification
     *
     */
    public int getNotificationTypeCount() {
        if (notificationType != null)
            return notificationType.size();
        else
            return -1;
    }
    
}
