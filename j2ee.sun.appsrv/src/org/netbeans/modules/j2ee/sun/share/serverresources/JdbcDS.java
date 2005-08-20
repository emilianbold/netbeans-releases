/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * JdbcResource.java
 *
 * Created on September 16, 2003, 11:19 AM
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

/**
 *
 * @author  nityad
 */
public class JdbcDS extends BaseResource implements java.io.Serializable{
    
    private String jndiName;
    private String connPoolName;
    private String resType;
    private String isEnabled;
    
    /** Creates a new instance of JdbcResource */
    public JdbcDS() {
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
    
    public String getConnPoolName() {
        return connPoolName;
    }
    public void setConnPoolName(String value) {
        String oldValue = connPoolName;
        this.connPoolName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("connPoolName", oldValue, connPoolName);//NOI18N
    }
    
    public String getResType() {
        return resType;
    }
    public void setResType(String value) {
        String oldValue = resType;
        this.resType = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("resType", oldValue, resType);//NOI18N
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
