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

package org.netbeans.modules.j2ee.common;

import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.util.NbBundle;


/**
 *
 * @author Libor Kotouc
 */
public final class DatasourceImpl implements Datasource {
    
    private String jndiName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String description;
    
    private volatile int hash = -1;
    
    public DatasourceImpl(String jndiName, String url, String username, String password, String driverClassName) {
        this.jndiName = jndiName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getDisplayName() {
        if (description == null) {
            //TODO implement some meaningful description
            description = getJndiName() + " [" + getUrl() + "]";
        }
        return description;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof DatasourceImpl))
            return false;
        
        DatasourceImpl ds = (DatasourceImpl)obj;
        if (jndiName == null && ds.getJndiName() != null || jndiName != null && !jndiName.equals(ds.getJndiName()))
            return false;
        if (url == null && ds.getUrl() != null || url != null && !url.equals(ds.getUrl()))
            return false;
        if (username == null && ds.getUsername() != null || username != null && !username.equals(ds.getUsername()))
            return false;
        if (password == null && ds.getPassword() != null || password != null && !password.equals(ds.getPassword()))
            return false;
        if (driverClassName == null && ds.getDriverClassName() != null || driverClassName != null && !driverClassName.equals(ds.getDriverClassName()))
            return false;
        
        return true;
    }
    
    public int hashCode() {
        if (hash == -1) {
            int result = 17;
            result += 37 * result + (jndiName == null ? 0 : jndiName.hashCode());
            result += 37 * result + (url == null ? 0 : url.hashCode());
            result += 37 * result + (username == null ? 0 : username.hashCode());
            result += 37 * result + (password == null ? 0 : password.hashCode());
            result += 37 * result + (driverClassName == null ? 0 : driverClassName.hashCode());
            
            hash = result;
        }
        
        return hash;
    }
    
    public String toString() {
        return "[ " + // NOI18N
                NbBundle.getMessage(DatasourceImpl.class, "LBL_DS_JNDI") + ": '" + jndiName + "', " + // NOI18N
                NbBundle.getMessage(DatasourceImpl.class, "LBL_DS_URL") + ": '" + url +  "', " + // NOI18N
                NbBundle.getMessage(DatasourceImpl.class, "LBL_DS_USER") + ": '" +  username +  "', " + // NOI18N
                NbBundle.getMessage(DatasourceImpl.class, "LBL_DS_PASS") + ": '" + password +  "', " + // NOI18N
                NbBundle.getMessage(DatasourceImpl.class, "LBL_DS_DRV") + ": '" + driverClassName + " ]"; // NOI18N
    }
}
