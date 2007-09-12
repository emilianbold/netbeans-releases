/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.wsdlextensions.jms.validator;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The format of the ConnectionURL is as follows:
 *   ConnectionURL := url[,url]*
 *   url := protocol://server:host/service[?options]
 *   protocol := mq, mqtcp, mqssl, http, https
 *   service  := jms, ssljms, connectRoot/tunnel
 *   options  := key=value[&key=value]*       
 *
 */
public class SunOneUrlParser extends ConnectionUrl {
    private SunOneConnectionUrl[] mConnectionUrls;
    
    /**
     * Protocol 1
     */
    public static final String PROT_MQ = "mq";
    /**
     * Protocol 2 
     */
    public static final String PROT_MQTCP = "mqtcp";
    /**
     * Protocol 3 
     */
    public static final String PROT_MQSSL = "mqssl";    
    /**
     * Protocol 4 
     */
    public static final String PROT_HTTP = "httpjms";
    /**
     * Protocol 5
     */
    public static final String PROT_HTTPS = "httpsjms";
    /**
     *  conection schema list
     */
    public static final String[] URL_PREFIXES = new String[] {
        PROT_MQ + "://",
        PROT_MQTCP + "://",
        PROT_MQSSL + "://",
        PROT_HTTP + "://",
        PROT_HTTPS + "://",        
    };
    /**
     *  connection schema list
     */    
    public static final String[] PROTOCOLS = new String[] {
        PROT_MQ,
        PROT_MQTCP,
        PROT_MQSSL,
        PROT_HTTP,
        PROT_HTTPS,        
    };
    
    /**
     * Constructor
     * 
     * @param s connection url string
     */
    public SunOneUrlParser(String s) {
        ArrayList urls = new ArrayList();
        for (StringTokenizer it = new StringTokenizer(s, ","); it.hasMoreTokens();) {
            String url = it.nextToken();
            urls.add(new SunOneConnectionUrl(url));
        }
        mConnectionUrls = (SunOneConnectionUrl[]) urls.toArray(new SunOneConnectionUrl[urls.size()]);
    }

    /**
     * @param  toAddTo  properties to be added to.
     * @throws  ValidationException
     *          if the URL validation failed.
     * @see com.stc.jmsjca.util.ConnectionUrl#getQueryProperties(java.util.Properties)
     */
    public void getQueryProperties(Properties toAddTo) throws ValidationException {
        SunOneConnectionUrl[] urls = getConnectionUrls();
        for (int i = 0; i < urls.length; i++) {
            urls[i].getQueryProperties(toAddTo);
        }
    }

    /**
     * Checks the validity of the URL; adjusts the port number if necessary
     * 
     * @throws  ValidationException
     *          if the URL validation failed.
     * @return boolean true if the url specified url object was changed by this
     *         validation
     */
    public boolean validate() throws ValidationException {
        
        if (mConnectionUrls.length ==0) {
            throw new ValidationException("URL should be a comma delimited set of URLs");            
        }        
        boolean protOk = true;        
        for (int j = 0; j < mConnectionUrls.length; j++) {
            SunOneConnectionUrl url = mConnectionUrls[j];
            protOk = false;
            for (int i = 0; i < PROTOCOLS.length; i++) {
                if (PROTOCOLS[i].equals(url.getProtocol())) {
                    protOk = true;
                    break;
                }
            }
            if (!protOk) {
                throw new ValidationException("Invalid protocol [" + url.getProtocol()
                    + "]: should be one of [" + Str.concat(PROTOCOLS, ", ") + "].");
            }
        }        
        return protOk;
    }    
    
    /**
     * Returns the parsers that constitute the URL
     * 
     * @return ConnectionUrl
     */
    public SunOneConnectionUrl[] getConnectionUrls() {
        return mConnectionUrls;
    }

    /**
     * Constructs a comma delimited string of schema://host:port/service
     * 
     * @return String
     * @throws  ValidationException
     *          if the URL validation failed.
     */
    public String getSunOneUrlSet() throws ValidationException {
        SunOneConnectionUrl[] urls = getConnectionUrls();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < urls.length; i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append(urls[i].getProtocol() + "://" + urls[i].getHost() + ":" + urls[i].getPort() + "/" + urls[i].getService());
        }
        return buf.toString();
    }
    
    /**
     * Constructs a comma delimited string of schema://host:port/admin or schema://host:port/ssladmin 
     * 
     * @return String
     * @throws  ValidationException
     *          if the URL validation failed.
     */
    public String getSunOneUrlAdminSet() throws ValidationException {
        SunOneConnectionUrl[] urls = getConnectionUrls();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < urls.length; i++) {
            if (i != 0) {
                buf.append(",");
            }            
            if ("jms".equals(urls[i].getService())) {
                buf.append(urls[i].getProtocol() + "://" + urls[i].getHost() + ":" + urls[i].getPort() + "/admin");                
            } else if ("jssljms".equals(urls[i].getService())) {
                buf.append(urls[i].getProtocol() + "://" + urls[i].getHost() + ":" + urls[i].getPort() + "/ssladmin");                                
            } else {
                buf.append(urls[i].getProtocol() + "://" + urls[i].getHost() + ":" + urls[i].getPort() + "/admin");                
            }
        }
        return buf.toString();
    }
    
}
