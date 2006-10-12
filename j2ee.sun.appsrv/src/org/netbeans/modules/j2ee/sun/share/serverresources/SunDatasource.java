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
 * SunDatasource.java
 *
 * Created on March 18, 2006, 5:56 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.serverresources;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.util.NbBundle;

/**
 *
 * @author Nitya Doraisamy
 */
public class SunDatasource implements Datasource{
    private String jndiName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private File resourceDir;
  
    private volatile int hash = -1;
    
    /** Creates a new instance of SunDatasource */
    public SunDatasource(String jndiName, String url, String username, String password, String driverClassName) { 
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
        return jndiName;
    }

    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (!(obj instanceof SunDatasource)){
            return false;
        }
        
        SunDatasource ds = (SunDatasource)obj;
        if (jndiName == null && ds.getJndiName() != null || jndiName != null && !jndiName.equals(ds.getJndiName())){
            return false;
        }
        if (url == null && ds.getUrl() != null || url != null && !url.equals(ds.getUrl())){
            return false;
        }
        if (username == null && ds.getUsername() != null || username != null && !username.equals(ds.getUsername())){
            return false;
        }
        if (password == null && ds.getPassword() != null || password != null && !password.equals(ds.getPassword())){
            return false;
        }
        if (driverClassName == null && ds.getDriverClassName() != null || driverClassName != null && !driverClassName.equals(ds.getDriverClassName())){
            return false;
        }
       
        return true;
    }
    
    public String toString() {
        return "[ " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_JNDI") + ": '" + jndiName + "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_URL") + ": '" + url +  "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_USER") + ": '" +  username +  "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_PASS") + ": '" + password +  "', " + // NOI18N
                NbBundle.getMessage(SunDatasource.class, "LBL_DRV") + ": '" + driverClassName +  "' ]"; // NOI18N
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

    public File getResourceDir() {
        return resourceDir;
    }

    public void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }
}
