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
 * JDBCAddressURL.java
 *
 * Created on October 10, 2006, 1:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.jdbc.validator;

import org.netbeans.modules.wsdlextensions.jdbc.JDBCAddress;
import java.util.Collection;
import java.util.ResourceBundle;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;

/**
 *
 * @author jfu
 */
public class JDBCAddressURL implements AddressURL {
    
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.jdbc.validator.Bundle");
    
    private String scheme;
    private String driver;
    private String host;
    private String port;
	private String dns;
    
    private String url;
    
    public JDBCAddressURL(String url) {
        this.url = url;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public String getDriver() {
        return driver;
    }
    
    public void setDriver(String driver) {
        this.driver = driver;
    }
    
    public String getDNS() {
        return dns;
    }
    
    public void setDNS(String dns) {
        this.dns = dns;
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
    
    public boolean parse(Collection<Validator.ResultItem> results, Validator validator, JDBCAddress target) {
        // if missing
        if (url == null || url.trim().length() == 0 ) {
            results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("JDBCAddress.MISSING_JDBC_URL")));
            return false;
        }
        return true;

        // if still the place holder
     /*   if ( url.startsWith(JDBC_URL_PLACEHOLDER) ) {
            results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("JDBCAddress.REPLACE_JDBC_URL_PLACEHOLDER_WITH_REAL_URL")));
            return false;
        } */
        
     /*   if ( !url.startsWith(JDBC_URL_PREFIX) ) {
            results.add(
                    new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("JDBCAddress.INVALID_JDBC_URL_PREFIX") + url));
            return false;
        }*/
        
        /*
        scheme = "JDBC";
        if ( url.length() > JDBC_URL_PREFIX.length() ) {
            String rest = url.substring(JDBC_URL_PREFIX.length());
            if ( rest.indexOf(URL_PATH_DELIM) >= 0 ) {
                results.add(
                        new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        mMessages.getString("JDBCAddress.INVALID_JDBC_URL_PATH_NOT_ALLOWED") + url));
                return false;
            }
            int index = rest.indexOf(URL_LOGIN_HOST_DELIM);
            String up = null;
            String hp = null;
            if ( index >= 0 ) {
                // [user:password]@host[:port]
                if ( index == 0 ) {
                    // no user name, password
                    // e.g., jdbc://@host[:port]
                } else {
                    up = rest.substring(0, index);
                }
                if ( index < rest.length() - 1 )
                    hp = rest.substring(index+1);
                else {
                    // no content after @
                    results.add(
                            new Validator.ResultItem(validator,
                            Validator.ResultType.ERROR,
                            target,
                            mMessages.getString("JDBCAddress.MALFORMED_JDBC_URL") + url));
                    return false;
                }
            } else {
                // no @ char
                hp = rest;
            }
            // host[:port]
            if ( hp == null || hp.indexOf(URL_LOGIN_HOST_DELIM) >= 0 ) {
                results.add(
                        new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        mMessages.getString("JDBCAddress.MALFORMED_JDBC_URL") + url));
                return false;
            }
            
            if ( up != null ) {
                index = up.indexOf(URL_COLON_DELIM);
                if ( index >= 0 ) {
                    String[] s = up.split(":");
                    boolean malformed = false;
                    switch ( s.length ) {
                        case 1:
                            if ( index == 0 ) {
                                malformed = true;
                            }
                            else {
                                // no pass
                                driver = s[0];
                            }
                            break;
                        case 2:
                           // user = s[0];
                           // password = s[1];
						   driver = s[0];
                            break;
                        default:
                            malformed = true;
                    }
                    if ( malformed ) {
                        results.add(
                                new Validator.ResultItem(validator,
                                Validator.ResultType.ERROR,
                                target,
                                mMessages.getString("JDBCAddress.MALFORMED_JDBC_URL") + url));
                        return false;
                    }
                }
                else {
                    driver = up;
                }
            }
            if ( hp != null ) {
                index = hp.indexOf(URL_COLON_DELIM);
                if ( index >= 0 ) {
                    String[] s = hp.split(":");
                    boolean malformed = false;
                    switch ( s.length ) {
                        case 1:
                            if ( index == 0 ) {
                                malformed = true;
                            }
                            else {
                                // no port
                                host = s[0];
                            }
                            break;
                        case 2:
                            host = s[0];
                            port = s[1];
                            break;
                        default:
                            malformed = true;
                    }
                    if ( malformed ) {
                        results.add(
                                new Validator.ResultItem(validator,
                                Validator.ResultType.ERROR,
                                target,
                                mMessages.getString("JDBCAddress.MALFORMED_JDBC_URL") + url));
                        return false;
                    }
                    if ( port != null && port.trim().length() > 0 ) {
                        // must be a positive int
                        boolean goodPort = true;
                        try {
                            int pt = Integer.parseInt(port);
                            if ( pt <= 0 )
                                goodPort = false;
                        }
                        catch (Exception e) {
                            goodPort = false;
                        }
                        if ( !goodPort ) {
                            results.add(
                                    new Validator.ResultItem(validator,
                                    Validator.ResultType.ERROR,
                                    target,
                                    mMessages.getString("JDBCAddress.INVALID_PORT_IN_URL") + url));
                            return false;
                        }
                    }
                }
                else {
                    host = hp;
                }
            }
            if ( host == null || host.trim().length() == 0 ) {
                results.add(
                        new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        mMessages.getString("JDBCAddress.MALFORMED_JDBC_URL_HOST_REQUIRED") + url));
                return false;
            }
        } else {
            results.add(
                    new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("JDBCAddress.MALFORMED_JDBC_URL") + url));
            return false;
        }
        return true;
    }*/
    }
}
