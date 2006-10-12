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

package org.netbeans.modules.tomcat5.config;

import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * Tomcat datasource implementation
 *
 * @author sherold
 */
public class TomcatDatasource implements Datasource {
    
    private final String username;
    private final String url;
    private final String password;
    private final String jndiName;
    private final String driverClassName;
    private int hash;
    
    /**
     * Creates a new instance of TomcatDatasource
     */
    public TomcatDatasource(String username, String url, String password, String jndiName, String driverClassName) {
        this.username = username;
        this.url = url;
        this.password = password;
        this.jndiName = jndiName;
        this.driverClassName = driverClassName;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getDisplayName() {
        return jndiName + " [" + url + "]"; // NOI18N
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TomcatDatasource)) {
            return false;
        }
        TomcatDatasource ds = (TomcatDatasource)obj;
        if ((jndiName == null && ds.jndiName != null) || (jndiName != null && !jndiName.equals(ds.jndiName))) {
            return false;
        }
        if ((url == null && ds.url != null) || (url != null && !url.equals(ds.url))) {
            return false;
        }
        if ((username == null && ds.username != null) || (username != null && !username.equals(ds.username))) {
            return false;
        }
        if ((password == null && ds.password != null) || (password != null && !password.equals(ds.password))) {
            return false;
        }
        if ((driverClassName == null && ds.driverClassName != null) || (driverClassName != null && !driverClassName.equals(ds.driverClassName))) {
            return false;
        }
        return true;
    }
    
    public int hashCode() {
        if (hash == 0) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("TomcatDatasource [username=").append(username); // NOI18N
        sb.append(", url=").append(url); // NOI18N
        sb.append(", password=").append(password); // NOI18N
        sb.append(", jndiName=").append(jndiName); // NOI18N
        sb.append(", driverClassName=").append(driverClassName).append("]"); // NOI18N
        return sb.toString();
    }
    
}
