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
 *  
 * $Id$
 */
package org.netbeans.installer.sandbox.download.proxy;

import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;

/**
 * A representation of a proxy. Connections are expected to use proxies registered
 * in DownloadManager to alter their way to connect to remote machines (if
 * supported/requried). Unlike the standard <code>java.net.Proxy</code>, this class
 * provides support for encapsulating the required username/password credentials as
 * well as the list of hosts for which this proxy should be bypassed.
 *
 * @author Kirill Sorokin
 */
public class Proxy {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The symbol with which the host names are separated in the string list of
     * hosts for which the proxy should be bypassed.
     */
    private static final String NON_PROXY_HOSTS_SEPARATOR =
            "\\|"; // NOI18N
    
    /**
     * Resource bundle key name - the name of the SOCKS proxy type.
     */
    private static final String KEY_TYPE_SOCKS =
            "Proxy.ProxyType.socks"; // NOI18N
    
    /**
     * Resource bundle key name - the name of the HTTP proxy type.
     */
    private static final String KEY_TYPE_HTTP =
            "Proxy.ProxyType.http"; // NOI18N
    
    /**
     * Resource bundle key name - the name of the HTTPS proxy type.
     */
    private static final String KEY_TYPE_HTTPS =
            "Proxy.ProxyType.https"; // NOI18N
    
    /**
     * Resource bundle key name - the name of the FTP proxy type.
     */
    private static final String KEY_TYPE_FTP =
            "Proxy.ProxyType.ftp"; // NOI18N
    
    /**
     * Resource bundle key name - warning message yelded when the port of a
     * proxy cannot be parsed.
     */
    private static final String KEY_CANNOT_PARSE_PORT =
            "Proxy.warning.cannotParsePort"; // NOI18N
    
    /**
     * Resource bundle key name - warning message yelded when either the host or the
     * port of a proxy is null (i.e. is not set).
     */
    private static final String KEY_NULL_HOST_OR_PORT =
            "Proxy.warning.nullHostOrPort"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    /**
     * Constructs a <code>Proxy</code> using the values of the supplied system
     * properties. It is used in the <code>DownloadManager</code> to register all the
     * proxies defined by the system (useful when the application is started via JNLP
     * or an applet.
     *
     * @param type The type of proxy to be constructed.
     * @param hostProperty The name of the system property which holds the proxy
     *      host.
     * @param portProperty The name of the system property which holds the proxy
     *      port.
     * @param nonProxyHostsProperty The name of the system property which holds the
     *      list of hosts for which the proxy should be bypassed.
     * @return The constructed <code>Proxy</code> or <code>null</code> if something
     *      did not allow to parse the data correctly.
     */
    public static Proxy parseProxy(ProxyType type, String hostProperty,
            String portProperty, String nonProxyHostsProperty) {
        String host = System.getProperty(hostProperty);
        String port = System.getProperty(portProperty);
        
        // if either the host or the port are null (i.e. not set), it does not make
        // any sense to continue parsing - issue a warning
        if ((host != null) && (port != null)) {
            try {
                String nonProxyHosts = System.getProperty(nonProxyHostsProperty);
                String[] nonProxyHostsArray;
                if (nonProxyHosts != null) {
                    nonProxyHostsArray = nonProxyHosts.split(NON_PROXY_HOSTS_SEPARATOR);
                } else {
                    nonProxyHostsArray = new String[0];
                }
                
                int intPort = Integer.parseInt(port);
                
                nonProxyHosts = "";
                for (int i = 0; i < nonProxyHostsArray.length; i++) {
                    nonProxyHostsArray[i] = nonProxyHostsArray[i].replace(".", "\\.").replace("*", ".*");
                    
                    nonProxyHosts += nonProxyHostsArray[i];
                    if (i != nonProxyHostsArray.length - 1) {
                        nonProxyHosts += ", ";
                    }
                }
                
                
                LogManager.log(ErrorLevel.WARNING, "        ... proxy successfully parsed -- " + host + ":" + port + ", exceptions: " + nonProxyHosts);
                
                return new Proxy(type, host, intPort, nonProxyHostsArray);
            } catch (NumberFormatException e) {
                LogManager.log(ErrorLevel.WARNING, "        ... cannot parse the port (" + portProperty + " = " + port + ") of the supplied system proxy");
            }
        } else {
            LogManager.log(ErrorLevel.WARNING, "        ... either the host (" + hostProperty + ") or the port (" + portProperty + ") of the supplied system proxy were not defined");
        }
        
        return null;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The type of this proxy.
     */
    private ProxyType myType;
    
    /**
     * The host of this proxy.
     */
    private String myHost;
    
    /**
     * The port of this proxy.
     */
    private int myPort;
    
    /**
     * The username which should be used to authenticate with this proxy.
     */
    private String myUsername;
    
    /**
     * The password which should be used to authenticate with this proxy.
     */
    private String myPassword;
    
    /**
     * The list of hosts for which this proxy should be bypassed. Each entry can be a
     * RE pattern, to facilitate describing a group of hosts.
     */
    private String[] myNonProxyHosts;
    
    /**
     * Constructs a new instance of <code>Proxy</code>. The username, password and
     * the list of hosts for which to bypass this proxy are set to null, null and
     * an empty array correspondingly.
     *
     * @param type The type of this proxy.
     * @param host The host of this proxy.
     * @param port The port of this proxy.
     */
    public Proxy(ProxyType type, String host, int port) {
        myType = type;
        
        myHost = host;
        myPort = port;
        
        myUsername = null;
        myPassword = null;
        
        myNonProxyHosts = new String[0];
    }
    
    
    /**
     * Constructs a new instance of <code>Proxy</code>. The username and password
     * are set to null.
     *
     * @param type The type of this proxy.
     * @param host The host of this proxy.
     * @param port The port of this proxy.
     * @param nonProxyHosts The list of hosts for which this proxy should be
     *      bypassed.
     */
    public Proxy(ProxyType type, String host, int port, String[] nonProxyHosts) {
        this(type, host, port);
        
        myNonProxyHosts = nonProxyHosts;
    }
    
    /**
     * Constructs a new instance of <code>Proxy</code>.
     *
     * @param type The type of this proxy.
     * @param host The host of this proxy.
     * @param port The port of this proxy.
     * @param username The username to be used to authenticate with this proxy.
     * @param password The password to be used to authenticate with this proxy.
     * @param nonProxyHosts The list of hosts for which this proxy should be
     *      bypassed.
     */
    public Proxy(ProxyType type, String host, int port, String username,
            String password, String[] nonProxyHosts) {
        this(type, host, port, nonProxyHosts);
        
        myUsername = username;
        myPassword = password;
    }
    
    /**
     * Getter for the <code>myType</code> property.
     *
     * @return The type of this proxy.
     */
    public ProxyType getType() {
        return myType;
    }
    
    /**
     * Getter for the <code>myHost</code> property.
     *
     * @return The host of this proxy.
     */
    public String getHost() {
        return myHost;
    }
    
    /**
     * Getter for the <code>myPort</code> property.
     *
     * @return The port of this proxy.
     */
    public int getPort() {
        return myPort;
    }
    
    /**
     * Getter for the <code>myUsername</code> property.
     *
     * @return The username to be used to authenticate with this proxy.
     */
    public String getUsername() {
        return myUsername;
    }
    
    /**
     * Getter for the <code>myPassword</code> property.
     *
     * @return The password to be used to authenticate with this proxy.
     */
    public String getPassword() {
        return myPassword;
    }
    
    /**
     * Getter for the <code>myNonProxyHosts</code> property.
     *
     * @return The list of hosts for which this proxy should be bypassed.
     */
    public String[] getNonProxyHosts() {
        return myNonProxyHosts;
    }
    
    /**
     * Checks whether the given host should be reached via the proxy or the proxy
     * should be bypassed.
     *
     * @param host The host to check.
     * @return <code>true</code> is the proxy should be bypassed, <code>false</code>
     *      otherwise.
     */
    public boolean skipProxyForHost(String host) {
        for (String pattern: myNonProxyHosts) {
            if (host.matches(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * An enumeration depicting all possible proxy types. Currently four types are 
     * supported: SOCKS, HTTP, HTTPS, FTP.
     *
     * @author Kirill Sorokin
     */
    public static enum ProxyType {
        SOCKS(ResourceUtils.getString(Proxy.class, KEY_TYPE_SOCKS)),
        HTTP(ResourceUtils.getString(Proxy.class, KEY_TYPE_HTTP)),
        HTTPS(ResourceUtils.getString(Proxy.class, KEY_TYPE_HTTPS)),
        FTP(ResourceUtils.getString(Proxy.class, KEY_TYPE_FTP));
        
        /**
         * The string representation of this proxy type.
         */
        private String myName;
        
        /**
         * Constructs a new <code>ProxyType</code> with the given type name. The 
         * constructor is obviously private.
         * 
         * @param name The string representation of this type.
         */
        private ProxyType(String name) {
            myName = name;
        }
        
        /**
         * {@inheritDoc}
         */
        public String toString() {
            return myName;
        }
    }
}
