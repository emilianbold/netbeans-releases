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
