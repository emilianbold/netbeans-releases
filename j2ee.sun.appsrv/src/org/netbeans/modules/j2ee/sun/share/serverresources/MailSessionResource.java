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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * MailSessionResource.java
 *
 * Created on September 17, 2003, 2:38 PM
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

/**
 *
 * @author  nityad
 */
public class MailSessionResource extends BaseResource implements java.io.Serializable{

    private String jndiName;
    private String storeProt;
    private String storeProtClass;
    private String transProt;
    private String transProtClass;
    private String hostName;
    private String userName;
    private String fromAddr;
    private String isDebug;
    private String isEnabled;
        
    /** Creates a new instance of MailSessionResource */
    public MailSessionResource() {
    }
    
    public String getJndiName() {
        return jndiName;
    }
    public void setJndiName(String value) {
        String oldValue = jndiName;
        this.jndiName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("jndiName", oldValue, jndiName);//NOI18N
    }
    
    public String getStoreProt() {
        return storeProt;
    }
    public void setStoreProt(String value) {
        String oldValue = storeProt;
        this.storeProt = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("storeProt", oldValue, storeProt);//NOI18N
    }
    
    public String getStoreProtClass() {
        return storeProtClass;
    }
    public void setStoreProtClass(String value) {
        String oldValue = storeProtClass;
        this.storeProtClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("storeProtClass", oldValue, storeProtClass);//NOI18N
    }
    
    public String getTransProt() {
        return transProt;
    }
    public void setTransProt(String value) {
        String oldValue = transProt;
        this.transProt = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("transProt", oldValue, transProt);//NOI18N
    }
    
    public String getTransProtClass() {
        return transProtClass;
    }
    public void setTransProtClass(String value) {
        String oldValue = transProtClass;
        this.transProtClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("transProtClass", oldValue, transProtClass);//NOI18N
    }
    
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String value) {
        String oldValue = hostName;
        this.hostName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("hostName", oldValue, hostName);//NOI18N
    }
    
    public String getUserName() {
        return userName;
    }
    public void setUserName(String value) {
        String oldValue = userName;
        this.userName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("userName", oldValue, userName);//NOI18N
    }
    public String getFromAddr() {
        return fromAddr;
    }
    public void setFromAddr(String value) {
        String oldValue = fromAddr;
        this.fromAddr = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("fromAddr", oldValue, fromAddr);//NOI18N
    }
    
    public String getIsDebug() {
        return isDebug;
    }
    public void setIsDebug(String value) {
        String oldValue = isDebug;
        this.isDebug = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isDebug", oldValue, isDebug);//NOI18N
    }
    
    public String getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(String value) {
        String oldValue = isEnabled;
        this.isEnabled = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isEnabled", oldValue, isEnabled);//NOI18N
    }
            
}
