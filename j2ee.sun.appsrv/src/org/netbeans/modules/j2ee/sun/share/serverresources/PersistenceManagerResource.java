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
 * PersistenceManagerResource.java
 *
 * Created on September 17, 2003, 1:19 PM
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

/**
 *
 * @author  nityad
 */
public class PersistenceManagerResource extends BaseResource implements java.io.Serializable{

    private String jndiName;
    private String factoryClass;
    private String datasourceJndiName;
    private String isEnabled;

    /** Creates a new instance of PersistenceManagerResource */
    public PersistenceManagerResource() {
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

}
