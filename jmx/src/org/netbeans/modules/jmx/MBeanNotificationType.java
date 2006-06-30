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

package org.netbeans.modules.jmx;

/**
 * Class which describes the notification type structure
 *
 */
public class MBeanNotificationType {

    private String type = "";// NOI18N

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
