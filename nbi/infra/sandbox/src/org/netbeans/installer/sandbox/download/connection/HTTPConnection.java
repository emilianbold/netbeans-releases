/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.sandbox.download.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.installer.sandbox.download.Download;
import org.netbeans.installer.sandbox.download.DownloadManager;
import org.netbeans.installer.sandbox.download.DownloadOptions;
import org.netbeans.installer.sandbox.download.proxy.Proxy;
import org.netbeans.installer.sandbox.download.proxy.Proxy.ProxyType;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.HTTPException;

/**
 *
 * @author Kirill Sorokin
 */
public class HTTPConnection extends Connection {
    private URL url;
    private long offset;
    private long length;
    
    private DownloadOptions options;
    
    private String host;
    private int port;
    private String file;
    private String parent;
    private String referer;
    
    private String requestStatusLine;
    private Map<String, String> requestFields;
    private String requestHeader;
    
    private String authorizationString;
    private String proxyAuthorizationString;
    
    private int responseCode;
    private String responseStatusLine;
    private Map<String, String> responseFields;
    private String responseHeader;
    
    private int numberOfRedirects;
    
    private Socket socket;
    private PushbackInputStream socketInput;
    private OutputStream socketOutput;
    
    public HTTPConnection(URL anURL, long anOffset, long aLength, DownloadOptions someOptions) {
        // save the initial connection properties
        offset = anOffset;
        length = aLength;
        
        options = someOptions;
        
        // parse the url
        parseURL(anURL);
    }
    
    public void open() throws IOException {
        boolean socksProxyUsed = false;
        boolean httpProxyUsed = false;
        boolean retryDirectly = false;
        
        List<Proxy> httpProxies = 
                DownloadManager.getInstance().getProxies(ProxyType.HTTP);
        List<Proxy> socksProxies = 
                DownloadManager.getInstance().getProxies(ProxyType.SOCKS);
        
        if (!options.getBoolean(DownloadOptions.IGNORE_PROXIES)) {
            if ((httpProxies.size() == 0) && (socksProxies.size() > 0)) {
                socksProxyUsed = true;
                System.setProperty("socksProxyHost", socksProxies.get(0).getHost());
                System.setProperty("socksProxyPort", 
                        Integer.toString(socksProxies.get(0).getPort()));
            }
            if (httpProxies.size() > 0) {
                Proxy httpProxy = httpProxies.get(0);
                
                if (!httpProxy.skipProxyForHost(host)) {
                    httpProxyUsed = true;
                }
            }
        }
        
        try {
            if (httpProxyUsed) {
                socket = new Socket(httpProxies.get(0).getHost(), 
                        httpProxies.get(0).getPort());
            } else {
                socket = new Socket(host, port);
            }
        } catch (IOException e) {
            if (socksProxyUsed) {
                ErrorManager.notify(ErrorLevel.WARNING, "Could not connect through SOCKS proxy", e);
                retryDirectly = true;
            } else {
                throw e;
            }
        }
        
        if (socksProxyUsed) {
            System.getProperties().remove("socksProxyHost");
            System.getProperties().remove("socksProxyPort");
        }
        
        if (retryDirectly) {
            socket = new Socket(host, port);
        }
        
        socketOutput = socket.getOutputStream();
        socketInput = new PushbackInputStream(socket.getInputStream(), 1024);
        
        // init the request header
        requestFields = new HashMap<String, String>();
        
        // init the response header
        responseFields = new HashMap<String, String>();
        
        sendRequestHeader();
        readResponseHeader();
    }
    
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            
            socket = null;
        }
    }
    
    public int read(byte[] buffer) throws IOException {
        ensureAvailability();
        
        return socketInput.read(buffer);
    }
    
    public int available() throws IOException {
        return socketInput.available();
    }
    
    public boolean supportsRanges() {
        for (String key: responseFields.keySet()) {
            if (key.equalsIgnoreCase("Accept-Ranges") && responseFields.get(key).contains("bytes")) {
                return true;
            }
        }
        
        return false;
    }
    
    public long getContentLength() {
        for (String key: responseFields.keySet()) {
            if (key.equalsIgnoreCase("Content-Length")) {
                try {
                    return Integer.parseInt(responseFields.get(key));
                } catch (NumberFormatException e) {
                    ErrorManager.notify(ErrorLevel.DEBUG, "Invalid content length received", e);
                }
            }
        }
        
        return -1;
    }
    
    public Date getModificationDate() {
        for (String key: responseFields.keySet()) {
            if (key.equalsIgnoreCase("Last-Modified")) {
                try {
                    return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US).parse(responseFields.get(key));
                } catch (ParseException e) {
                    try {
                        return new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US).parse(responseFields.get(key));
                    } catch (ParseException ex) {
                        try {
                            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(responseFields.get(key));
                        } catch (ParseException exe) {
                            ErrorManager.notify(ErrorLevel.DEBUG, "Faield to parse the modification date", e);
                        }
                    }
                }
            }
        }
        
        return new Date();
    }
    
    private void parseURL(URL anURL) {
        url     = anURL;
        host    = url.getHost();
        port    = (url.getPort() == UNDEFINED_PORT) ? url.getDefaultPort() : url.getPort();
        file    = url.getFile().equals("") ? URL_PATH_SEPARATOR : url.getFile();
        parent  = file.substring(0, file.lastIndexOf(URL_PATH_SEPARATOR) + 1);
        referer = url.getProtocol() + "://" + host + ":" + port + parent;
    }
    
    private void sendRequestHeader() throws IOException {
        boolean httpProxyUsed = 
                DownloadManager.getInstance().getProxies(ProxyType.HTTP).size() > 0;
        
        // init the request header
        if (!httpProxyUsed) {
            requestStatusLine = "GET " + file + " HTTP/1.1";
        } else {
            requestStatusLine = "GET " + url + " HTTP/1.1";
        }
        
        requestFields = new HashMap<String, String>();
        requestFields.put("User-Agent", "NetBeans Installer Download Manager");
        requestFields.put("Accept", "*/*");
        requestFields.put("Referer", referer);
        requestFields.put("Host", host);
        requestFields.put("Pragma", "no-cache");
        requestFields.put("Cache-Control", "no-cache");

        if ((offset != Download.ZERO_OFFSET) || (length != Download.UNDEFINED_LENGTH)) {
            String range = "bytes=" + offset + "-";
            if (length != Download.UNDEFINED_LENGTH) {
                range += offset + length;
            }
            requestFields.put("Range", range);
        }
        
        if (authorizationString != null) {
            requestFields.put("Authorization", authorizationString);
        }
        if (proxyAuthorizationString != null) {
            requestFields.put("Proxy-Authorization", proxyAuthorizationString);
        }
        
        // construct the request string
        String header = requestStatusLine;
        
        for (String field: requestFields.keySet()) {
            header += StringUtils.CRLF + field + ": " + requestFields.get(field);
        }
        
        header += StringUtils.CRLFCRLF;
        
            socketOutput.write(header.getBytes("UTF-8"));
        
        requestHeader = header;
    }
    
    private void readResponseHeader() throws IOException {
        boolean headerRead = false;
        byte[] buffer = new byte[1024];
        
        responseHeader = "";
        
        while (!headerRead) {
            ensureAvailability();
            
            int read = socketInput.read(buffer);
            String string = new String(buffer, 0, read, "ISO-8859-1");
            
            int endIndex = string.indexOf(StringUtils.CRLF + StringUtils.CRLF);
            
            if (endIndex != -1) {
                socketInput.unread(buffer, endIndex + 4, read - endIndex - 4);
                string = string.substring(0, endIndex + 4);
                headerRead = true;
            }
            
            responseHeader += string;
        }
        
        String[] lines = StringUtils.rightTrim(responseHeader).split(StringUtils.CRLF);
        
        responseStatusLine = lines[0];
        responseCode = Integer.parseInt(responseStatusLine.split(" ")[1]);
        
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(":");
            
            String name = StringUtils.rightTrim(lines[i].substring(0, colon)).toLowerCase();
            String value = StringUtils.leftTrim(lines[i].substring(colon + 1));
            
            responseFields.put(name, value);
        }
        
        // if the response code is from OK family - continue
        if ((responseCode >= 200) && (responseCode < 300)) {
            return;
        }
        
        // otherwise go into a more detailed analysis
        switch (responseCode) {
            case 301:
            case 302:
            case 303:
                if (numberOfRedirects <= 5) {
                    parseURL(new URL(responseFields.get("location")));
                    open();
                    return;
                } else {
                    throw new HTTPException("Maximum number of redirects exceeded");
                }
            case 401:
                if (authorizationString == null) {
                    constructAuthorizationString();
                    open();
                    return;
                } else {
                    throw new HTTPException("Authorization failed");
                }
            case 407:
                if (proxyAuthorizationString == null) {
                    constructProxyAuthorizationString();
                    open();
                    return;
                } else {
                    throw new HTTPException("Proxy Authorization failed");
                }
            default:
                throw new HTTPException("Unsupported response header: " + responseStatusLine);
        }
    }
    
    private void ensureAvailability() throws IOException {
        for (int timeout = 0; timeout <= connectionTimeout; timeout += DELAY) {
            if (socketInput.available() > 0) {
                return;
            }
            
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        
        throw new IOException("Connection timed out.");
    }
    
    private void constructAuthorizationString() throws HTTPException {
        String challenge = responseFields.get("WWW-Authenticate");
        
        if (challenge.startsWith("Basic")) {
            String username = options.getString(DownloadOptions.USERNAME);
            String password = options.getString(DownloadOptions.PASSWORD);
            
            if ((username == null) || (password == null)) {
                throw new HTTPException("Authorization required, while either username or password were not supplied");
            } else {
                try {
                    authorizationString = "Basic " + StringUtils.base64Encode(username + ":" + password);
                } catch (UnsupportedEncodingException e) {
                    throw new HTTPException("Cannot build the authorization string", e);
                }
            }
        } else {
            throw new HTTPException("Unsupported authorization scheme");
        }
    }
    
    private void constructProxyAuthorizationString() throws HTTPException {
        String challenge = responseFields.get("Proxy-Authenticate");
        
        if (challenge.startsWith("Basic")) {
            List<Proxy> httpProxies = 
                DownloadManager.getInstance().getProxies(ProxyType.HTTP);
                
            if (httpProxies.size() == 0) {
                throw new HTTPException("Proxy authorization was required, while no proxies were registered");
            }
            
            String username = httpProxies.get(0).getUsername();
            String password = httpProxies.get(0).getPassword();
            
            if ((username == null) || (password == null)) {
                throw new HTTPException("Proxy authorization required, while either username or password were not set");
            } else {
                try {
                    authorizationString = "Basic " + StringUtils.base64Encode(username + ":" + password);
                } catch (UnsupportedEncodingException e) {
                    throw new HTTPException("Cannot build the authorization string", e);
                }
            }
        } else {
            throw new HTTPException("Unsupported authorization scheme");
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final int DELAY = 25;
    private static final int UNDEFINED_PORT = -1;
    private static final String URL_PATH_SEPARATOR = "/";
}
