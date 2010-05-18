/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.jmx;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which describes the structure of an MBean notification
 *
 */
public class MBeanNotification {

    private String notificationClass = ""; // NOI18N
    private String notificationDescription = ""; // NOI18N
    private List<MBeanNotificationType> notificationType = null;

    /**
     * Custom constructor
     * @param notificationClass the java class of the notification
     * @param notificationDescription the description of the notification
     * @param notificationType the list of the notification types for that 
     * notification
     */
    public MBeanNotification(String notificationClass, 
            String notificationDescription,
            List<MBeanNotificationType> notificationType) {
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
    public List<MBeanNotificationType> getNotificationTypeList() {
        return notificationType;
    }
    
    /**
     * Returns a string concat of all notification types for the current
     * notification; each one seperated by ","
     * @return String the string containing all notification types
     */
    public String getNotificationTypeClasses() {
        String notifTypeClass = ""; // NOI18N
        for (int i = 0; i < notificationType.size(); i++) {
            notifTypeClass += notificationType.get(i).getNotificationType();
            
            if (i < notificationType.size() -1)
                notifTypeClass += ","; // NOI18N
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
