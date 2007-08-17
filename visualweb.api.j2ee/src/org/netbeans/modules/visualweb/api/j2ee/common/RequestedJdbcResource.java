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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.api.j2ee.common;

import java.io.File;
import java.util.List;

public class RequestedJdbcResource extends RequestedResource {
    private String driverClassName;
    private String url;
    private String username;
    private String password;    
    
    public RequestedJdbcResource(String name, String driverClassName,
                                 String url, 
                                 String username, String password) {
        super(name);
        this.driverClassName = (driverClassName == null)? null:
            new String(driverClassName);
        this.url = (url == null)? null: new String(url);
        this.username = (username == null)? null: new String(username);
        this.password = (password == null)? null: new String(password);        
    }


    public String getDriverClassName() {
        return driverClassName;
    }


    public void setDriverClassName(String driverClassName) {
        this.driverClassName = new String(driverClassName);
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = new String(url);
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = new String(username);
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = new String(password);
    }
  
}
