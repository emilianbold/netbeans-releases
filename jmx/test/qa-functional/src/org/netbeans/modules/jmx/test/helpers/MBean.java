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
public class MBean {
    
    private String mBeanName = "";
    private String mBeanType = "";
    private String mBeanPackage = "";
    private String mBeanComment = "";
    private ArrayList<Attribute> mBeanAttributes = null;
    private ArrayList<Operation> mBeanOperations = null;
    private ArrayList<Notification> mBeanNotifications = null;
    
    public MBean(String mBeanName, String mBeanType,
            String mBeanPackage, String mBeanComment,
            ArrayList<Attribute> mBeanAttributes,
            ArrayList<Operation> mBeanOperations,
            ArrayList<Notification> mBeanNotifications) {
        this.mBeanName = mBeanName;
        this.mBeanType = mBeanType;
        this.mBeanPackage = mBeanPackage;
        this.mBeanComment = mBeanComment;
        this.mBeanAttributes = mBeanAttributes;
        this.mBeanOperations = mBeanOperations;
        this.mBeanNotifications = mBeanNotifications;
    }
 
    /**
     * Returns the name of the MBean
     * @return mBeanName the name of the MBean
     */
    public String getMBeanName() {
        return mBeanName;
    }
    
    /**
     * Returns the type of the MBean
     * @return mBeanType the type of the MBean
     */
    public String getMBeanType() {
        return mBeanType;
    }
    
    /**
     * Returns the package of the MBean
     * @return mBeanPackage the package of the MBean
     */
    public String getMBeanPackage() {
        return mBeanPackage;
    }
    
    /**
     * Returns the comment for the MBean
     * @return mBeanComment the comment of the MBean
     */
    public String getMBeanComment() {
        return mBeanComment;
    }
    
    /**
     * Returns an attribute for the MBean
     * @param index index of the attribute to return
     * @return mBeanAttributes the attribute at index of the MBean
     */
    public Attribute getMBeanAttribute(int index) {
        return mBeanAttributes.get(index);
    }
    
    /**
     * Returns an operation for the MBean
     * @param index index of the operation to return
     * @return mBeanOperations the operation at index of the MBean
     */
    public Operation getMBeanOperation(int index) {
        return mBeanOperations.get(index);
    }
    
    /**
     * Returns a notification for the MBean
     * @param index index of the notification to return
     * @return mBeanNotifications the notification at index of the MBean
     */
    public Notification getMBeanNotification(int index) {
        return mBeanNotifications.get(index);
    }
    
    /**
     * Returns the number of attributes of the MBean
     * @return mBeanAttributes the number of attributes of the MBean
     */
    public int getNumberOfAttributes() {
        if (mBeanAttributes != null)
            return mBeanAttributes.size();
        else
            return -1;
    }
    
    /**
     * Returns the number of operations of the MBean
     * @return mBeanOperations the number of operations of the MBean
     */
    public int getNumberOfOperations() {
        if (mBeanOperations != null)
            return mBeanOperations.size();
        else
            return -1;
    }
    
    /**
     * Returns the number of notifications of the MBean
     * @return mBeanNotifications the number of notifications of the MBean
     */
    public int getNumberOfNotifications() {
        if (mBeanNotifications != null)
            return mBeanNotifications.size();
        else
            return -1;
    }
}
