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
 * PersistenceManagerResource.java
 *
 * Created on September 17, 2003, 1:19 PM
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

import java.beans.*;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

/**
 *
 * @author  nityad
 */
public class PersistenceManagerResource extends Object implements java.io.Serializable{
    
    private String name;
    private String jndiName;
    private String factoryClass;
    private String datasourceJndiName;
    private String isEnabled;
    private String description;
    private NameValuePair[] extraParams;
    
    transient private PropertyChangeSupport propertySupport;
    
    /** Creates a new instance of PersistenceManagerResource */
    public PersistenceManagerResource() {
        propertySupport = new PropertyChangeSupport(this);
    }
    
    private void initPropertyChangeSupport(){
        if(propertySupport==null)
            propertySupport = new PropertyChangeSupport ( this );

    }
    
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
        propertySupport.removePropertyChangeListener (listener);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String value) {
        String oldValue = name;
        this.name = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("name", oldValue, name);//NOI18N
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
    
    public String getFactoryClass() {
        return factoryClass;
    }
    public void setFactoryClass(String value) {
        String oldValue = factoryClass;
        this.factoryClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("factoryClass", oldValue, factoryClass);//NOI18N
    }
    
    public String getDatasourceJndiName() {
        return datasourceJndiName;
    }
    public void setDatasourceJndiName(String value) {
        String oldValue = datasourceJndiName;
        this.datasourceJndiName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("datasourceJndiName", oldValue, datasourceJndiName);//NOI18N
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
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        String oldValue = description;
        this.description = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("description", oldValue, description);//NOI18N
    }
    
    public NameValuePair[] getExtraParams() {
        if(this.extraParams == null)
            this.extraParams = new NameValuePair[0];   
        return this.extraParams;
    }
    public void setExtraParams(Object[] value) {
        NameValuePair[] pairs = new NameValuePair[value.length];
        for (int i = 0; i < value.length; i++) {
            NameValuePair val = (NameValuePair)value[i];
            NameValuePair pair = new NameValuePair();
            pair.setParamName(val.getParamName());
            pair.setParamValue(val.getParamValue());
            pair.setParamDescription(val.getParamDescription());
            pairs[i] = pair;
        }
        NameValuePair[] oldValue = extraParams;
        this.extraParams = pairs;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("extraParams", oldValue, extraParams);//NOI18N
    } 
}
