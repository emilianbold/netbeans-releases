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

import java.util.Properties;
import java.util.StringTokenizer;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * A generic parser for the following pattern:
 * protocol://host[:port][/service][/path]
 * service := 
 * path := file?query | ?query | file
 */
public class SunOneConnectionUrl extends ConnectionUrl {
    private String mUrl;
    private boolean mParsed;
    private String mProtocol;
    private int mPort;
    private String mHost;
    private String mService;    
    private String mPath;
    private String mFile;
    private String mQuery;

    /**
     * Constructor
     *
     * @param url String
     */
    public SunOneConnectionUrl(String url) {
        mUrl = url;
    }

    private void parse() throws ValidationException {
        if (mParsed) {
            return;
        }

        String r = mUrl;
        mParsed = true;

        // Protocol
        int i = r.indexOf("://");
        if (i < 0) {
            throw new ValidationException ("Invalid URL [" + mUrl + "]: no protocol specified");
        }
        mProtocol = r.substring(0, i);
        r = r.substring(i + "://".length());

        // host[:port]
        i = r.indexOf('/');
        if (i < 0) {
            i = r.indexOf('?');
        }
        String server = i >= 0 ? r.substring(0, i) : r;
        r = i >= 0 ? r.substring(i) : "";

        i = server.indexOf(':');
        if (i >= 0) {
            mHost = server.substring(0, i);
            String port = server.substring(i + 1);
            if (port.length() > 0) {
                mPort = Integer.parseInt(port);
            } else {
                mPort = -1;
            }
        } else {
            mHost = server;
            mPort = -1;
        }
        
        // service
        int tunnel = r.indexOf("/tunnel");            
        if (r.length() > 0 && tunnel != -1) {
            mService = r.substring(0, tunnel + "/tunnel".length());       
            r = r.substring(tunnel + "/tunnel".length());
        } else if (r.length() > 0 && r.startsWith("/ssljms")) {
            mService = "ssljms";
            r = r.substring("/ssljms".length());            
        } else if (r.length() > 0 && r.startsWith("/jms")) {
            mService = "jms";
            r = r.substring("jms".length());            
        } else {
            mService = "";            
        }
        
        // path
        if (r.length() > 0) {
            mFile = r.substring(0);
        } else {
            mFile = "";
        }

        // file
        if (!r.startsWith("/")) {
            mPath = "";
        } else {
            i = r.indexOf('?');
            if (i >= 0) {
                mPath = r.substring(0, i);
                r = r.substring(i);
            } else {
                mPath = r;
                r = "";
            }
        }

        // query
        if (r.startsWith("?")) {
            mQuery = r.substring(1);
        }
    }

    /**
     * Changes the port
     *
     * @param port int
     */
    public void setPort(int port) throws ValidationException{
        parse();
        mPort = port;
        mUrl = null;
    }

    /**
     * Changes the server portion
     * 
     * @param host String 
     */
    public void setHost(String host) throws ValidationException {
       parse();
       mHost = host;
       mUrl = null;
    }

    /**
     * Changes the server connection service
     * 
     * @param host String 
     */
    public void setService(String service) throws ValidationException {
       parse();
       mService = service;
       mUrl = null;
    }
    
    /**
     * Returns the URL in full string form
     *
     * @return String
     */
    public String toString() {
        if (mUrl == null) {
            StringBuffer url = new StringBuffer();
            url.append(mProtocol).append("://").append(mHost);
            if (mPort != -1) {
                url.append(":").append(mPort);
            }
            if (!"".equals(mService)) {
                url.append("/").append(mService);
            }            
            url.append(mFile);
            mUrl = url.toString();
        }
        return mUrl;
    }

    /**
     * Gets the protocol name of this <code>URL</code>.
     *
     * @return  the protocol of this <code>URL</code>.
     */
    public String getProtocol() throws ValidationException {
        parse();
        return mProtocol;
    }

    /**
     * Gets the host name of this <code>URL</code>
     *
     * @return  the host name of this <code>URL</code>.
     */
    public String getHost() throws ValidationException {
        parse();
        return mHost;
    }

    /**
     * Gets the port number of this <code>URL</code>.
     *
     * @return  the port number, or -1 if the port is not set
     */
    public int getPort() throws ValidationException {
        parse();
        return mPort;
    }

    /**
     * Gets the connection service of this <code>URL</code>.
     *
     * @return  the connection service name
     */
    public String getService() throws ValidationException {
        parse();
        return mService;
    }
    
    /**
     * Gets the file name of this <code>URL</code>.
     * The returned file portion will be
     * the same as <CODE>getPath()</CODE>, plus the concatenation of
     * the value of <CODE>getQuery()</CODE>, if any. If there is
     * no query portion, this method and <CODE>getPath()</CODE> will
     * return identical results.
     *
     * @return  the file name of this <code>URL</code>,
     * or an empty string if one does not exist
     */
    public String getFile() throws ValidationException {
        parse();
        return mFile;
    }

    /**
     * Gets the path part of this <code>URL</code>.
     *
     * @return  the path part of this <code>URL</code>, or an
     * empty string if one does not exist
     */
    public String getPath() throws ValidationException {
        parse();
        return mPath;
    }

    /**
     * Gets the query part of this <code>URL</code>.
     *
     * @return  the query part of this <code>URL</code>,
     * or <CODE>null</CODE> if one does not exist
     */
    public String getQuery() throws ValidationException {
        parse();
        return mQuery;
    }

    /**
     * Extracts the key value pairs from the query string
     *
     * @param toAddTo Properties key-value pairs will be added to this properties set
     */
    public void getQueryProperties(Properties toAddTo) throws ValidationException {
        if (mUrl == null) {
            return;
        }
        String q = getQuery();
        getQueryProperties(q, toAddTo);
    }

    /**
     * Tool function: queries a query string and adds the key/value pairs to the specified
     * properties map
     *
     * @param q String
     * @param toAddTo Properties
     */
    public static void getQueryProperties(String q, Properties toAddTo) throws ValidationException {
        if (q == null || q.length() == 0) {
            return;
        }
        for (StringTokenizer iter = new StringTokenizer(q, "&");
            iter.hasMoreElements();/*-*/) {
            String pair = (String) iter.nextToken();
            int split = pair.indexOf('=');
            if (split <= 0) {
                throw new ValidationException("Invalid pair [" + pair
                    + "] in query string [" + q + "]");
            } else {
                String key = pair.substring(0, split);
                String value = pair.substring(split + 1);
                try {
                    key = URLDecoder.decode(key, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new ValidationException("Invalid encoding in [" + pair
                        + "] in query string [" + q + "]",
                        e);
                }
                toAddTo.setProperty(key, value);
            }
        }
    }
}
