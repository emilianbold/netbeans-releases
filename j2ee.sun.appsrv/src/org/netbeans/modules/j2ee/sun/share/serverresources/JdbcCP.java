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
 * JdbcCP.java
 *
 * Created on September 12, 2003, 5:39 PM
 */
package org.netbeans.modules.j2ee.sun.share.serverresources;

/**
 *
 * @author  nityad
 */
public class JdbcCP extends BaseResource implements java.io.Serializable{
    
    private String dsClass;
    private String resType;
    private String steadyPoolSize;
    private String maxPoolSize;
    private String maxWaitTimeMilli;
    private String poolResizeQty;
    private String idleIimeoutSecond;
    private String tranxIsoLevel;
    private String isIsoLevGuaranteed;
    private String isConnValidReq;
    private String connValidMethod;
    private String validationTableName;
    private String failAllConns;
    
    /** Creates a new instance of JdbcCP */
    public JdbcCP() {
    }
            
    public String getDsClass() {
        return dsClass;
    }
    public void setDsClass(String value) {
        String oldValue = dsClass;
        this.dsClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("dsClass", oldValue, dsClass);//NOI18N
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
    
    public String getSteadyPoolSize() {
        return steadyPoolSize;
    }
    public void setSteadyPoolSize(String value) {
        String oldValue = steadyPoolSize;
        this.steadyPoolSize = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("steadyPoolSize", oldValue, steadyPoolSize);//NOI18N
    }
    
    public String getMaxPoolSize() {
        return maxPoolSize;
    }
    public void setMaxPoolSize(String value) {
        String oldValue = maxPoolSize;
        this.maxPoolSize = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("maxPoolSize", oldValue, maxPoolSize);//NOI18N
    }
    
    public String getMaxWaitTimeMilli() {
        return maxWaitTimeMilli;
    }
    public void setMaxWaitTimeMilli(String value) {
        String oldValue = maxWaitTimeMilli;
        this.maxWaitTimeMilli = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("maxWaitTimeMilli", oldValue, maxWaitTimeMilli);//NOI18N
    }
    
    public String getPoolResizeQty() {
        return poolResizeQty;
    }
    public void setPoolResizeQty(String value) {
        String oldValue = poolResizeQty;
        this.poolResizeQty = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("poolResizeQty", oldValue, poolResizeQty);//NOI18N
    }
    
    public String getIdleIimeoutSecond() {
        return idleIimeoutSecond;
    }
    public void setIdleIimeoutSecond(String value) {
        String oldValue = idleIimeoutSecond;
        this.idleIimeoutSecond = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("idleIimeoutSecond", oldValue, idleIimeoutSecond);//NOI18N
    }
    
    public String getTranxIsoLevel() {
        return tranxIsoLevel;
    }
    public void setTranxIsoLevel(String value) {
        String oldValue = tranxIsoLevel;
        this.tranxIsoLevel = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("tranxIsoLevel", oldValue, tranxIsoLevel);//NOI18N
    }
    
    public String getIsIsoLevGuaranteed() {
        return isIsoLevGuaranteed;
    }
    public void setIsIsoLevGuaranteed(String value) {
        String oldValue = isIsoLevGuaranteed;
        this.isIsoLevGuaranteed = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isIsoLevGuaranteed", oldValue, isIsoLevGuaranteed);//NOI18N
    }
    
    public String getIsConnValidReq() {
        return isConnValidReq;
    }
    public void setIsConnValidReq(String value) {
        String oldValue = isConnValidReq;
        this.isConnValidReq = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("isConnValidReq", oldValue, isConnValidReq);//NOI18N
    }
    
    public String getConnValidMethod() {
        return connValidMethod;
    }
    public void setConnValidMethod(String value) {
        String oldValue = connValidMethod;
        this.connValidMethod = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("connValidMethod", oldValue, connValidMethod);//NOI18N
    }
    
    public String getValidationTableName() {
        return validationTableName;
    }
    public void setValidationTableName(String value) {
        String oldValue = validationTableName;
        this.validationTableName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("validationTableName", oldValue, validationTableName);//NOI18N
    }
    
    public String getFailAllConns() {
        return failAllConns;
    }
    public void setFailAllConns(String value) {
        String oldValue = failAllConns;
        this.failAllConns = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("failAllConns", oldValue, failAllConns);//NOI18N
    }
    
 }
