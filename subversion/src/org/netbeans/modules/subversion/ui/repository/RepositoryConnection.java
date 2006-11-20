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
package org.netbeans.modules.subversion.ui.repository;

import java.net.MalformedURLException;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.modules.subversion.config.Scrambler;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryConnection {
    
    private static final String RC_DELIMITER = "~=~";
    
    private String url;
    private String username;
    private String password;
    private ProxyDescriptor proxyDescriptor;
    private String externalCommand;
    

    public RepositoryConnection(RepositoryConnection rc) {
        this(rc.url, rc.username, rc.password, rc.proxyDescriptor, rc.externalCommand);
    }
    
    public RepositoryConnection(String url) {
        this(url, null, null, null, null);
    }
            
    public RepositoryConnection(String url, String username, String password,
                                ProxyDescriptor proxyDescriptor,
                                String externalCommand) {
        this.setUrl(url);
        this.setUsername(username);
        this.setPassword(password);
        this.setProxyDescriptor(proxyDescriptor);
        this.setExternalCommand(externalCommand);
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username == null ? "" : username;
    }

    public String getPassword() {
        return password== null ? "" : password ;
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
    }

    public String getExternalCommand() {
        return externalCommand == null ? "" : externalCommand;
    }
    
    public SVNUrl getSvnUrl() {
        try {
            return new SVNUrl(url);
        }
        catch (MalformedURLException ex) {
            return null;
        }        
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;   
        }            
        if (getClass() != o.getClass()) {
            return false;
        }            
        
        final RepositoryConnection test = (RepositoryConnection) o;

        if (this.url != test.url && this.url != null && !this.url.equals(test.url)) {
            return false;
        }        
        return true;
    }
    
    public int hashCode() {
        int hash = 3;

        hash = 61 * hash + (this.url != null ? this.url.hashCode()
                                             : 0);        
        return hash;
    }

    void setUrl(String url) {
        this.url = url;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setPassword(String password) {
        this.password = password;
    }

    void setProxyDescriptor(ProxyDescriptor proxyDescriptor) {
        this.proxyDescriptor = proxyDescriptor;
    }

    void setExternalCommand(String externalCommand) {
        this.externalCommand = externalCommand;
    }

    public String toString() {
        return url;
    }
        
    public static String getString(RepositoryConnection rc) {
        StringBuffer sb = new StringBuffer();        
        sb.append(rc.getUrl());
        sb.append(RC_DELIMITER);
        sb.append(rc.getUsername());
        sb.append(RC_DELIMITER);
        sb.append(Scrambler.getInstance().scramble(rc.getPassword()));
        sb.append(RC_DELIMITER);
        sb.append(rc.getExternalCommand());
        sb.append(RC_DELIMITER);        
        ProxyDescriptor pd = rc.getProxyDescriptor();
        sb.append(pd != null ? pd.getHost() : "");
        sb.append(RC_DELIMITER);        
        sb.append(pd != null ? pd.getUserName() : "");
        sb.append(RC_DELIMITER);                        
        sb.append(pd != null ? Scrambler.getInstance().scramble(pd.getPassword()) : "");
        sb.append(RC_DELIMITER);        
        sb.append(pd != null ? pd.getPort() : "");
        sb.append(RC_DELIMITER);        
        sb.append(pd != null ? pd.getType() : "");
        sb.append(RC_DELIMITER);
        return sb.toString();
    }
    
    public static RepositoryConnection parse(String str) {
        String[] fields = str.split(RC_DELIMITER);
        
        int l = fields.length;
        
        ProxyDescriptor pd = null;
        String url          =         fields[0];
        String username     = l > 1 ? fields[1] : "";
        String password     = l > 2 ? Scrambler.getInstance().descramble(fields[2]) : "";
        String extCmd       = l > 3 ? fields[3] : "";
        
        String pdHost       = l > 4 ? fields[4] : "";
        String pdUsername   = l > 5 ? fields[5] : "";
        String pdPassword   = l > 6 ? Scrambler.getInstance().descramble(fields[6]) : "";
        int pdPort          = l > 7 ? Integer.parseInt(fields[7]) : -1;
        int pdType          = l > 8 ? Integer.parseInt(fields[8]) : -1;                        
        pd = new ProxyDescriptor(pdType, pdHost, pdPort, pdUsername, pdPassword);        
        
        return new RepositoryConnection(url, username, password, pd, extCmd);        
    }
    
}
