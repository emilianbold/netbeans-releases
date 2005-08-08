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
    
    /*
    private boolean mbeanItfImpl = false;
    private boolean keepPreRegParam = false; 
    private boolean implemNotifEmitter = false;
    private boolean genBrdCasterDeleg = false;
    private boolean genSeqNumber = false;
    
     */
    /** MBean constructor which is not wrapper and does not implement mbritf */
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
    
    /* MBean for mbeans which implement mbean reg itf */
    /*
    public MBean(String mBeanName, String mBeanType,
            String mBeanPackage, String mBeanComment,
            ArrayList<Attribute> mBeanAttributes,
            ArrayList<Operation> mBeanOperations,
            ArrayList<Notification> mBeanNotifications,
            boolean mbeanItfImpl, boolean keepPreRegParam) {
        this.mBeanName = mBeanName;
        this.mBeanType = mBeanType;
        this.mBeanPackage = mBeanPackage;
        this.mBeanComment = mBeanComment;
        this.mBeanAttributes = mBeanAttributes;
        this.mBeanOperations = mBeanOperations;
        this.mBeanNotifications = mBeanNotifications;
        
        this.mbeanItfImpl = mbeanItfImpl;
        this.keepPreRegParam = keepPreRegParam;
    }
    */
    /* MBean for mbeans which implements notification emitter itf */
    /*
    public MBean(String mBeanName, String mBeanType,
            String mBeanPackage, String mBeanComment,
            ArrayList<Attribute> mBeanAttributes,
            ArrayList<Operation> mBeanOperations,
            ArrayList<Notification> mBeanNotifications,
            boolean implemNotifEmitter,
            boolean genBrdCasterDeleg,
            boolean genSeqNumber) {
        this.mBeanName = mBeanName;
        this.mBeanType = mBeanType;
        this.mBeanPackage = mBeanPackage;
        this.mBeanComment = mBeanComment;
        this.mBeanAttributes = mBeanAttributes;
        this.mBeanOperations = mBeanOperations;
        this.mBeanNotifications = mBeanNotifications;
        
        this.setImplemNotifEmitter(implemNotifEmitter);
        this.setGenBrdCasterDeleg(genBrdCasterDeleg);
        this.setGenSeqNumber(genSeqNumber);
    }
    */
    /* Full featured MBean */
    /*
    public MBean(String mBeanName, String mBeanType,
            String mBeanPackage, String mBeanComment,
            ArrayList<Attribute> mBeanAttributes,
            ArrayList<Operation> mBeanOperations,
            ArrayList<Notification> mBeanNotifications,
            boolean mbeanItfImpl, boolean keepPreRegParam,
            boolean implemNotifEmitter,
            boolean genBrdCasterDeleg,
            boolean genSeqNumber) {
        this.mBeanName = mBeanName;
        this.mBeanType = mBeanType;
        this.mBeanPackage = mBeanPackage;
        this.mBeanComment = mBeanComment;
        this.mBeanAttributes = mBeanAttributes;
        this.mBeanOperations = mBeanOperations;
        this.mBeanNotifications = mBeanNotifications;
        
        this.mbeanItfImpl = mbeanItfImpl;
        this.keepPreRegParam = keepPreRegParam;
        
        this.setImplemNotifEmitter(implemNotifEmitter);
        this.setGenBrdCasterDeleg(genBrdCasterDeleg);
        this.setGenSeqNumber(genSeqNumber);
    }
    */
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
     * Returns the attribute list for the MBean
     * @return ArrayList<Attribute> the attribute list of the MBean
     */
    public ArrayList<Attribute> getMBeanAttributeList() {
        return mBeanAttributes;
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
     * Returns the operation list for the MBean
     * @return ArrayList<Attribute> the operation list of the MBean
     */
    public ArrayList<Operation> getMBeanOperationList() {
        return mBeanOperations;
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
     * Returns the notification list for the MBean
     * @return ArrayList<Notification> the notification list of the MBean
     */
    public ArrayList<Notification> getMBeanNotificationList() {
        return mBeanNotifications;
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

    /**
     * Return true if the class implements the mbean registration itf
     * @return boolean true if the class implements the mbean registration itf 
     */
    /*
    public boolean isMbeanItfImpl() {
        return mbeanItfImpl;
    }

    /**
     * Sets the caracteristic for this mbean to implement the mbean registration itf
     * @param mbeanItfImpl true if this mbean to implements the mbean registration itf
     */
    /*
    public void setMbeanItfImpl(boolean mbeanItfImpl) {
        this.mbeanItfImpl = mbeanItfImpl;
    }

    /**
     * Return true if the class keeps the preregistered parameter references
     * @return boolean true if the class keeps the preregistered parameter references
     */
    /*
    public boolean isKeepPreRegParam() {
        return keepPreRegParam;
    }
    
    /**
     * Sets the caracteristic for this mbean to keep the preregistered parameter references
     * @param keepPreRegParam true if this mbean keeps the preregistered parameter references
     */
    /*
    public void setKeepPreRegParam(boolean keepPreRegParam) {
        this.keepPreRegParam = keepPreRegParam;
    }

    public boolean isImplemNotifEmitter() {
        return implemNotifEmitter;
    }

    public void setImplemNotifEmitter(boolean implemNotifEmitter) {
        this.implemNotifEmitter = implemNotifEmitter;
    }

    public boolean isGenBrdCasterDeleg() {
        return genBrdCasterDeleg;
    }

    public void setGenBrdCasterDeleg(boolean genBrdCasterDeleg) {
        this.genBrdCasterDeleg = genBrdCasterDeleg;
    }

    public boolean isGenSeqNumber() {
        return genSeqNumber;
    }

    public void setGenSeqNumber(boolean genSeqNumber) {
        this.genSeqNumber = genSeqNumber;
    }
*/
}
