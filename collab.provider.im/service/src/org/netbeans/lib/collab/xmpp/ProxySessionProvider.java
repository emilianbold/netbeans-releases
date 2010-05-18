/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.xmpp;

import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.RegistrationListener;
import org.netbeans.lib.collab.SecureRegistrationListener;
import org.netbeans.lib.collab.SecureSessionListener;
import org.netbeans.lib.collab.util.StringUtility;

import java.security.cert.X509Certificate;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * This session provider is used to connect to an XMPP server via
 * a proxy server.  In this version, it can be used for web proxies
 * and SOCKS V5 proxies.
 * <p>
 * The service URL used in the getSession() method must inidicate the
 * XMPP server information (host and port) as well as the proxy server
 * information (host, port, protocol, authentication credentials, and
 * keepalive behavior).  The format is as follows:
 *
 * <ul>
 * <i>protocol</i>://<i>proxy-host</i>[:<i>proxy-port</i>]?<i>attrs</i>
 * </ul>
 * The default port is 5222.  The attributes are provided using the
 * usual URL query syntax.  Available attributes are 
 * <ul>
 * <li>service=<i>xmpp-host</i>[:<i>xmpp-port</i>]  (mandatory)</li> 
 * <li>authname=<i>name</i>  (proxy credential)</li> 
 * <li>password=<i>password</i>  (proxy credential)</li> 
 * <li>keepalive=<i>seconds</i>  (keepalive period in seconds)</li> 
 * </ul>
 * Example:
 * <ul>
 * socks://sprox.example.com?service=jabber.org&authname=bob&password=secret
 *</ul>
 *
 * HTTP/HTTPS tunneling is defined by the following specification:
 * <ul>
 *  <a href="http://www.web-cache.com/Writings/Internet-Drafts/draft-luotonen-web-proxy-tunneling-01.txt">
http://www.web-cache.com/Writings/Internet-Drafts/draft-luotonen-web-proxy-tunneling-01.txt
 * </a>
 * </ul>
 * SOCKS V5 tunneling is defined by the following specifications:
 * <ul>
 *  <li><a href="http://www.ietf.org/rfc/rfc1928.txt">RFC 1928</a></li>
 *  <li><a href="http://www.ietf.org/rfc/rfc1929.txt">RFC 1929</a></li>
 * </ul>

 * The service URL passed getSession must contain both the IM server and 
 * http proxy information.  The format is as follows:
 * <ul>
 * <tt>http(s)://https-host:https-port?service=xmpp-host:xmpp-port</tt>
 * </ul>
 * 
 * @author Jacques Belissent
 * 
 */
public class ProxySessionProvider extends XMPPSessionProvider 
{

    public final static String KEEPALIVE_ATTR = "keepalive";
    public final static String AUTHNAME_ATTR  = "authname";
    public final static String PASSWORD_ATTR  = "password";
    public final static String SERVICE_ATTR   = "service";
    public final static String USE_SSL_ATTR   = "usessl"; 

 
    protected XMPPSession createSession(String serviceUrl,
                                        String destination,
                                        String loginName, 
                                        String password, 
                                        int loginType, 
                                        CollaborationSessionListener csl) 
        throws CollaborationException 
    {
        java.net.URI uri = null;
        try {
            uri = new java.net.URI(serviceUrl);
        } catch (java.net.URISyntaxException use) {
            throw new CollaborationException(use);
        }
        Map attributes = parseQuery(uri.getQuery());

        if (attributes.containsKey(KEEPALIVE_ATTR)) {
            startKeepAlive(Long.parseLong((String)attributes.get(KEEPALIVE_ATTR)) * 1000);
        }

        StreamSourceCreator ssc = null;
        if (uri.getScheme().equalsIgnoreCase("socks")) {
            ssc = new Socks5StreamSourceCreator(csl, uri, attributes);
        } else if (uri.getScheme().equalsIgnoreCase("http") ||
                   uri.getScheme().equalsIgnoreCase("https")) {
            ssc = new HTTPStreamSourceCreator(csl, uri, attributes);
        }

        if (ssc != null) {
            return new XMPPSession(this,
                                   (String)attributes.get(SERVICE_ATTR),
                                   destination, loginName, password, 
                                   loginType, csl, 
                                   ssc);
        } else { 
            throw new CollaborationException("Unsupported protocol: " + uri.getScheme());
        }
    }

    private Map parseQuery(String query) {

        HashMap attributes = new HashMap();
        if (query != null) {
            for (StringTokenizer st = new StringTokenizer(query, "&");
                 st.hasMoreTokens(); ) {
                String avp = st.nextToken();
                int eqi = avp.indexOf("=");
                if (eqi > 0 && eqi < avp.length()) {
                    try {
                        String val =
                            URLDecoder.decode(avp.substring(eqi + 1),
                                              "UTF-8");
                        attributes.put(avp.substring(0, eqi), val);
                    } catch (UnsupportedEncodingException uee) {
                        // never happens with UTF8
                    }
                }
            }
        }
	return attributes;
    }
     
     protected long getKeepAliveInterval() {
        //override since i look at keepalive in url and decide
        return 0;
    }

    protected String getDomain(String loginName, String serviceURL) {
	String server = null;
        java.net.URI uri = null;
        try {
            uri = new java.net.URI(serviceURL);
	    Map attributes = parseQuery(uri.getQuery());
	    server = (String)attributes.get(SERVICE_ATTR);
	    if (null == server){
		server = uri.getHost();
            }
        } catch (java.net.URISyntaxException use) {
	    // Should not happen , this has already been validated ...
            throw new IllegalArgumentException("URI is invalid : " + serviceURL + " , message : " + use.toString());
        }

	return super.getDomain(loginName , server);
    }



}
