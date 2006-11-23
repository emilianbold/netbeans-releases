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
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
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
    
    private SVNUrl svnUrl;
    private SVNRevision svnRevision;
    

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
        return password == null ? "" : password ;
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
    }

    public String getExternalCommand() {
        return externalCommand == null ? "" : externalCommand;
    }
    
    public SVNUrl getSvnUrl() throws MalformedURLException {
        if(svnUrl == null) {
            parseUrlString(url);
        }
        return svnUrl;
    }
    
    public SVNRevision getSvnRevision() throws MalformedURLException {
        if(svnRevision == null) {
            parseUrlString(url);
        }
        return svnRevision;        
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
        svnUrl = null;
        svnRevision = null;
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

    private void parseUrlString(String urlString) throws MalformedURLException {
        int idx = urlString.lastIndexOf('@');
        int hostIdx = urlString.indexOf("://");                         // NOI18N
        int firstSlashIdx = urlString.indexOf("/", hostIdx + 3);        // NOI18N
        if(idx < 0 || firstSlashIdx < 0 || idx < firstSlashIdx) {
            svnRevision = SVNRevision.HEAD;
        } else /*if (acceptRevision)*/ {
            if( idx + 1 < urlString.length()) {
                String revisionString = "";                             // NOI18N
                try {
                    revisionString = urlString.substring(idx + 1);
                    svnRevision = SvnUtils.getSVNRevision(revisionString);
                } catch (NumberFormatException ex) {
                    throw new MalformedURLException(NbBundle.getMessage(Repository.class, "MSG_Repository_WrongRevision", revisionString));
                }
            } else {
                svnRevision = SVNRevision.HEAD;
            }
            urlString = urlString.substring(0, idx);
        }    
        svnUrl = removeEmptyPathSegments(new SVNUrl(urlString));

    }
    
    private SVNUrl removeEmptyPathSegments(SVNUrl url) throws MalformedURLException {
        String[] pathSegments = url.getPathSegments();
        StringBuffer urlString = new StringBuffer();
        urlString.append(url.getProtocol());
        urlString.append("://");                                                // NOI18N
        urlString.append(SvnUtils.ripUserFromHost(url.getHost()));
        if(url.getPort() > 0) {
            urlString.append(":");                                              // NOI18N
            urlString.append(url.getPort());
        }
        boolean gotSegments = false;
        for (int i = 0; i < pathSegments.length; i++) {
            if(!pathSegments[i].trim().equals("")) {                            // NOI18N
                gotSegments = true;
                urlString.append("/");                                          // NOI18N
                urlString.append(pathSegments[i]);                
            }
        }
        try {
            if(gotSegments) {
                return new SVNUrl(urlString.toString());
            } else {
                return url;
            }
        } catch (MalformedURLException ex) {
            throw ex;
        }
    }
    
    public static String getString(RepositoryConnection rc) {
        SVNUrl url;
        try {        
            url = rc.getSvnUrl();
        } catch (MalformedURLException mue) {
            // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue); // should not happen
            return "";
        }        
        
        StringBuffer sb = new StringBuffer();        
        sb.append(url.toString());
        sb.append(RC_DELIMITER);
        sb.append(rc.getUsername());
        sb.append(RC_DELIMITER);
        sb.append(Scrambler.getInstance().scramble(rc.getPassword()));
        sb.append(RC_DELIMITER);
        sb.append(rc.getExternalCommand());
        sb.append(RC_DELIMITER);        
        ProxyDescriptor pd = rc.getProxyDescriptor();
        sb.append(pd != null && pd.getHost() != null     ? pd.getHost()                                       : "");
        sb.append(RC_DELIMITER);                        
        sb.append(pd != null && pd.getUserName() != null ? pd.getUserName()                                   : "");
        sb.append(RC_DELIMITER);                        
        sb.append(pd != null && pd.getPassword() != null ? Scrambler.getInstance().scramble(pd.getPassword()) : "");
        sb.append(RC_DELIMITER);        
        sb.append(pd != null && pd.getPort() != -1       ? pd.getPort()                                       : "");
        sb.append(RC_DELIMITER);        
        sb.append(pd != null && pd.getType() != -1       ? pd.getType()                                       : "");
        sb.append(RC_DELIMITER);
        return sb.toString();
    }
    
    public static RepositoryConnection parse(String str) {        
        String[] fields = str.split(RC_DELIMITER);
        
        int l = fields.length;
        
        ProxyDescriptor pd = null;
        String url          =           fields[0];
        String username     = l > 1 && !fields[1].equals("") ? fields[1] : null;
        String password     = l > 2 && !fields[2].equals("") ? Scrambler.getInstance().descramble(fields[2]) : null;
        String extCmd       = l > 3 && !fields[3].equals("") ? fields[3] : null;
        
        String pdHost       = l > 4 && !fields[4].equals("") ? fields[4] : null;
        String pdUsername   = l > 5 && !fields[5].equals("") ? fields[5] : null;
        String pdPassword   = l > 6 && !fields[6].equals("") ? Scrambler.getInstance().descramble(fields[6]) : null;
        int pdPort          = l > 7 && !fields[7].equals("") ? Integer.parseInt(fields[7]) : -1;
        int pdType          = l > 8 && !fields[8].equals("") ? Integer.parseInt(fields[8]) : -1;                        
        pd = new ProxyDescriptor(pdType, pdHost, pdPort, pdUsername, pdPassword);        
        
        return new RepositoryConnection(url, username, password, pd, extCmd);        
    }
    
}
