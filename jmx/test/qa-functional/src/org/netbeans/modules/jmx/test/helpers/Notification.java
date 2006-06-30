/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
