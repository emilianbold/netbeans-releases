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
/*
 * FTPAddressURL.java
 *
 * Created on October 10, 2006, 1:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.ftp.validator;

import org.netbeans.modules.wsdlextensions.ftp.FTPComponent;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.xml.xam.spi.Validator;

/**
 *
 * @author jfu
 */
public class FTPAddressURL implements AddressURL {
    private String scheme;
    private String user;
    private String password;
    private String host;
    private String port;
    
    private String url;
    
    public FTPAddressURL(String url) {
        this.url = url;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public boolean parse(Collection<Validator.ResultItem> results, Validator validator, FTPComponent target) {
        // if missing
        if (url == null || url.trim().length() == 0 ) {
            results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPAddress.MISSING_FTP_URL")));
            return false;
        }

        // do not parse it if contains env vars
        if ( Util.hasMigrationEnvVar(url) )
            return true;
        
        // if still the place holder
        if ( url.startsWith(FTP_URL_PLACEHOLDER) ) {
            results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPAddress.REPLACE_FTP_URL_PLACEHOLDER_WITH_REAL_URL")));
            return false;
        }
        
        if ( !url.startsWith(FTP_URL_PREFIX) ) {
            results.add(
                    new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPAddress.INVALID_FTP_URL_PREFIX", new Object[] {url})));
            return false;
        }
        scheme = "ftp";
        if ( url.length() > FTP_URL_PREFIX.length() ) {
            String rest = url.substring(FTP_URL_PREFIX.length());
            if ( rest.indexOf(URL_PATH_DELIM) >= 0 ) {
                results.add(
                        new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPAddress.INVALID_FTP_URL_PATH_NOT_ALLOWED", new Object[] {url})));
                return false;
            }
            
            int l = rest.trim().length();
            int i = 0;
            StringBuffer cur = new StringBuffer();
            int at = 0;
            int col = 0;
            List comps = new Vector();
            while ( i < l ) {
                char c = rest.charAt(i);
                switch ( c ) {
                    case '\\':
                        if ( i + 1 < l ) {
                            cur.append(rest.charAt(i+1));
                            i = i + 2;
                        }
                        else {
                            cur.append(c);
                            i++;
                        }
                        break;
                    case ':':
                        col++;
                        if ( col > 1 || cur.length() == 0 /* :password and :port are invalid */) {
                            // in each part: either user:password
                            // or host:port, there can be at most 1
                            // ':' delimiter;
                            results.add(
                                    new Validator.ResultItem(validator,
                                    Validator.ResultType.ERROR,
                                    target,
                                    Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url})));
                            return false;
                        }
                        comps.add(cur.toString());
                        cur = new StringBuffer();
                        i++;
                        break;
                    case '@':
                        at++;
                        if ( at > 1 ) {
                            // at most 1 '@' as delimiter;
                            results.add(
                                    new Validator.ResultItem(validator,
                                    Validator.ResultType.ERROR,
                                    target,
                                    Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url})));
                            return false;
                        }
                        // previously collected belongs to user_password
                        comps.add(cur.toString());
                        cur = new StringBuffer();
                        col = 0;
                        switch ( comps.size() ) {
                            case 1:
                                this.user = (String)comps.get(0);
                                break;
                            case 2:
                                this.user = (String)comps.get(0);
                                this.password = (String)comps.get(1);
                                break;
                            default:
                                results.add(
                                        new Validator.ResultItem(validator,
                                        Validator.ResultType.ERROR,
                                        target,
                                        Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url})));
                                return false;
                        }
                        comps = new Vector();
                        i++;
                        break;
                    default:
                        cur.append(c);
                        i++;
                }
            }
            
            if ( cur != null && cur.length() > 0 )
                comps.add(cur.toString());

            switch ( comps.size() ) {
                case 1:
                    this.host = (String)comps.get(0);
                    break;
                case 2:
                    this.host = (String)comps.get(0);
                    this.port = (String)comps.get(1);
                    boolean goodPort = true;
                    if ( port != null && port.trim().length() > 0 ) {
                        // must be a positive int
                        try {
                            int pt = Integer.parseInt(port);
                            if ( pt <= 0 )
                                goodPort = false;
                        }
                        catch (Exception e) {
                            goodPort = false;
                        }
                    }

                    if ( !goodPort ) {
                        results.add(
                                new Validator.ResultItem(validator,
                                Validator.ResultType.ERROR,
                                target,
                                Util.getMessage("FTPAddress.INVALID_PORT_IN_URL", new Object[] {url})));
                        return false;
                    }
                    
                    break;
                default:
                    results.add(
                            new Validator.ResultItem(validator,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url})));
                    return false;
            }
            
            if ( host == null || host.trim().length() == 0 ) {
                results.add(
                        new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPAddress.MALFORMED_FTP_URL_HOST_REQUIRED", new Object[] {url})));
                return false;
            }
        } else {
            results.add(
                    new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url})));
            return false;
        }
        return true;
    }
    
    public boolean parse(Vector results, FTPComponent target) {
        // if missing
        if (url == null || url.trim().length() == 0 ) {
            results.add(Util.getMessage("FTPAddress.MISSING_FTP_URL"));
            return false;
        }

        // do not parse it if contains env vars
        if ( Util.hasMigrationEnvVar(url) )
            return true;
        
        // if still the place holder
        if ( url.startsWith(FTP_URL_PLACEHOLDER) ) {
            results.add(Util.getMessage("FTPAddress.REPLACE_FTP_URL_PLACEHOLDER_WITH_REAL_URL"));
            return false;
        }
        
        if ( !url.startsWith(FTP_URL_PREFIX) ) {
            results.add(Util.getMessage("FTPAddress.INVALID_FTP_URL_PREFIX", new Object[] {url}));
            return false;
        }
        scheme = "ftp";
        if ( url.length() > FTP_URL_PREFIX.length() ) {
            String rest = url.substring(FTP_URL_PREFIX.length());
            if ( rest.indexOf(URL_PATH_DELIM) >= 0 ) {
                results.add(Util.getMessage("FTPAddress.INVALID_FTP_URL_PATH_NOT_ALLOWED", new Object[] {url}));
                return false;
            }
            
            int l = rest.trim().length();
            int i = 0;
            StringBuffer cur = new StringBuffer();
            int at = 0;
            int col = 0;
            List comps = new Vector();
            while ( i < l ) {
                char c = rest.charAt(i);
                switch ( c ) {
                    case '\\':
                        if ( i + 1 < l ) {
                            cur.append(rest.charAt(i+1));
                            i = i + 2;
                        }
                        else {
                            cur.append(c);
                            i++;
                        }
                        break;
                    case ':':
                        col++;
                        if ( col > 1 || cur.length() == 0 /* :password and :port are invalid */) {
                            // in each part: either user:password
                            // or host:port, there can be at most 1
                            // ':' delimiter;
                            results.add(Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url}));
                            return false;
                        }
                        comps.add(cur.toString());
                        cur = new StringBuffer();
                        i++;
                        break;
                    case '@':
                        at++;
                        if ( at > 1 ) {
                            // at most 1 '@' as delimiter;
                            results.add(Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url}));
                            return false;
                        }
                        // previously collected belongs to user_password
                        comps.add(cur.toString());
                        cur = new StringBuffer();
                        col = 0;
                        switch ( comps.size() ) {
                            case 1:
                                this.user = (String)comps.get(0);
                                break;
                            case 2:
                                this.user = (String)comps.get(0);
                                this.password = (String)comps.get(1);
                                break;
                            default:
                                results.add(Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url}));
                                return false;
                        }
                        comps = new Vector();
                        i++;
                        break;
                    default:
                        cur.append(c);
                        i++;
                }
            }
            
            if ( cur != null && cur.length() > 0 )
                comps.add(cur.toString());

            switch ( comps.size() ) {
                case 1:
                    this.host = (String)comps.get(0);
                    break;
                case 2:
                    this.host = (String)comps.get(0);
                    this.port = (String)comps.get(1);
                    boolean goodPort = true;
                    if ( port != null && port.trim().length() > 0 ) {
                        // must be a positive int
                        try {
                            int pt = Integer.parseInt(port);
                            if ( pt <= 0 )
                                goodPort = false;
                        }
                        catch (Exception e) {
                            goodPort = false;
                        }
                    }

                    if ( !goodPort ) {
                        results.add(Util.getMessage("FTPAddress.INVALID_PORT_IN_URL", new Object[] {url}));
                        return false;
                    }
                    
                    break;
                default:
                    results.add(Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url}));
                    return false;
            }
            
            if ( host == null || host.trim().length() == 0 ) {
                results.add(Util.getMessage("FTPAddress.MALFORMED_FTP_URL_HOST_REQUIRED", new Object[] {url}));
                return false;
            }
        } else {
            results.add(Util.getMessage("FTPAddress.MALFORMED_FTP_URL", new Object[] {url}));
            return false;
        }
        return true;
    }
}
