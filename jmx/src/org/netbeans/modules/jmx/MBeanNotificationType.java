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

/**
 * Class which describes the notification type structure
 * 
 */
public class MBeanNotificationType {
    
    private String type = "";
    
    /**
     * Default constructor
     */
    public MBeanNotificationType() {
        
    }
    
    /**
     * Constructor
     * @param type the notification type
     **/
    public MBeanNotificationType(String type) {
        this.type = type;
    }
    
    /**
     * Sets the notification type
     * @param notifType the notification type to set
     */
    public void setNotificationType(String notifType) {
        this.type = notifType;
    }
    
    /**
     * Method which returns a type of the notification
     * @return String a type of the notification
     *
     */
    public String getNotificationType() {
        return type;
    }
    
}
