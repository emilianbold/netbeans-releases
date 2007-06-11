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
 * AddressURL.java
 *
 * Created on October 10, 2006, 1:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.ftp.validator;

import org.netbeans.modules.wsdlextensions.ftp.FTPComponent;
import java.util.Collection;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author jfu
 */
public interface AddressURL {
    public static final String  FTP_URL_PLACEHOLDER = "ftp://[ftp_user]:[ftp_password]@[ftp_host]:[ftp_port]";
    public static final String  PROXY_URL_PLACEHOLDER = "[proxy_protocol]://[proxy_user]:[proxy_password]@[proxy_host]:[proxy_port]";
    public static final String FTP_URL_PREFIX = "ftp://";
    public static final String SOCKS4_URL_PREFIX = "socks4://";
    public static final String SOCKS5_URL_PREFIX = "socks5://";

    public static final String URL_LOGIN_HOST_DELIM = "@";
    public static final String URL_COLON_DELIM = ":";
    public static final String URL_PATH_DELIM = "/";

    public String getScheme();
    public void setScheme(String scheme);
    public String getUser();
    public void setUser(String user);
    public String getPassword();
    public void setPassword(String password);
    public String getHost();
    public void setHost(String host);
    public String getPort();
    public void setPort(String port);
    public boolean parse(Collection<ResultItem> results, Validator validator, FTPComponent target);
}
