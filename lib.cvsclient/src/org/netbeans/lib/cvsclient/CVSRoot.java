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
 * Software is Rami Ojares. Portions created by Rami Ojares are Copyright (C) 2003.
 * All Rights Reserved.
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
 *
 * Contributor(s): Rami Ojares
 */

package org.netbeans.lib.cvsclient;

import java.io.*;
import java.util.*;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.ConnectionFactory;

/**
    <p>
        CVSRoot represents the cvsroot that identifies the cvs repository's location
        and the means to get to it. We use following definition of cvsroot:
    </p>

    <code>
        [:method:][[user][:password]@][hostname[:[port]]]/path/to/repository
    </code>

    <p>
        When the method is not defined, we treat it as local or ext method
        depending on whether the hostname is present or not.
        This gives us two different formats:
    </p>
    
    <h4>1. Local format</h4>
    <code>[:method:]/path/to/repository</code> or
    <code>(:local:|:fork:)anything<code>
    
    <h4>2. Server format</h4>
    <code>
        [:method:][[user][:password]@]hostname[:[port]]/path/to/repository
    </code>
    
    <p>
        There are currently 6 different methods that are implemented by 3 different connection classes.
        <ul>
            <li>:local:, :fork: & no-method --> LocalConnection (LOCAL_FORMAT)</li>
            <li>:server: & :ext: --> SSH2Connection (SERVER_FORMAT)</li>
            <li>:pserver: --> PServerConnection (SERVER_FORMAT)</li>
        </li>
        gserver and kserver are not included. Environment variables are not used (like CVS_RSH).
    </p>
    <p>
        local and no-method work like fork. They start the cvs server program on
        the local machine thus using the remote protocol on the local machine.
        According to Cederqvist fork's relation to local is:
        "In other words it does pretty much the same thing as :local:,
        but various quirks, bugs and the like are those of the remote CVS rather
        than the local CVS." 
    </p>
    <p>
        server is using ssh. According to Cederqvist it would use an internal RSH
        client but since it is not known what this exactly means it just uses ssh.
        Note ssh is able only to use ssh protocol version 2 which is recommended anyways.
    </p>
    <p>
        Note that cvsroot is case sensitive so remember to write the method in lowercase.
        You can succesfully construct a cvsroot that has a different method but
        ConnectionFactory will be unable to create a connection for such CVSRoot. 
    </p>
    <p>
        CVSRoot object keeps the cvsroot in components that are
        <ul>
            <li>method</li>
            <li>user</li>
            <li>password</li>
            <li>host</li>
            <li>port</li>
            <li>repository</li>
        </ul>
        You can change these components through setters.
        When you ask fo the cvsroot string representation it is constructed based
        on the current values of the components. The returned cvsroot never contains
        the password for security reasons.
        Also "no-method" is always represented as local method.
    </p>
*/
public class CVSRoot {
    
    /** A constant representing the "local" connection method. */
    public static final String METHOD_LOCAL = "local"; // NOI18N
    /** A constant representing the "fork" connection method. */
    public static final String METHOD_FORK = "fork"; // NOI18N
    /** A constant representing the "server" connection method. */
    public static final String METHOD_SERVER = "server"; // NOI18N
    /** A constant representing the "pserver" connection method. */
    public static final String METHOD_PSERVER = "pserver"; // NOI18N
    /** A constant representing the "ext" connection method. */
    public static final String METHOD_EXT = "ext"; // NOI18N
    
    // the connection method. no-method is represented by null
    // the value is interned for fast comparisons
    private String method;
    // user (default = null)
    private String username;
    // password (default = null)
    private String password;
    // hostname (default = null)
    private String hostname;
    // port (default = 0) 0 means that port is not used and the protocol will use default protocol
    private int port;
    // repository as string representation
    private String repository;

    /**
     * Parse the CVSROOT string into CVSRoot object.
     * The CVSROOT string must be of the form
     * [:method:][[user][:password]@][hostname:[port]]/path/to/repository
     */
    public static CVSRoot parse(String cvsroot) throws IllegalArgumentException {
        return new CVSRoot(cvsroot);
    }
    
    /**
     * Construct CVSRoot from Properties object.
     * The names are exactly the same as the attribute names in this class.
     */
    public static CVSRoot parse(Properties props) throws IllegalArgumentException {
        return new CVSRoot(props);
    }
	
    /**
     * This constructor allows to construct CVSRoot from Properties object.
     * The names are exactly the same as the attribute names in this class.
     */
    protected CVSRoot(Properties props) throws IllegalArgumentException {

        String mtd = props.getProperty("method");
        if (mtd != null) {
            this.method = mtd.intern();
        }

        // host & port
        this.hostname = props.getProperty("hostname");
        
        if (this.hostname.length() == 0)
            this.hostname = null;
        //this.localFormat = this.hostname == null || this.hostname.length() == 0;
        
        if (this.hostname != null) {
            
            // user & password (they are always optional)
            this.username = props.getProperty("username");
            this.password = props.getProperty("password");
            
            // host & port
            // We already have hostname
            try {
                int p = Integer.parseInt(props.getProperty("port"));
                if (p > 0)
                    this.port = p;
                else
                    throw new IllegalArgumentException("The port is not a positive number.");
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("The port is not a number: '"+props.getProperty("port")+"'.");
            }
        }

        // and the most important which is repository
        String r = props.getProperty("repository");
        if (r == null)
            throw new IllegalArgumentException("Repository is obligatory.");
        else
            this.repository = r;
    }
	
    /**
     * Breaks the string representation of cvsroot into it's components:
     *
     * The valid format (from the cederqvist) is:
     *
     * :method:[[user][:password]@]hostname[:[port]]/path/to/repository
     *
     * Also parse alternative format from WinCVS, which stores connection
     * parameters such as username and hostname in method options:
     *
     * :method[;option=arg...]:other_connection_data
     *
     * e.g. :pserver;username=anonymous;hostname=localhost:/path/to/repository
     *
     * For CVSNT compatability it also supports following local repository path format
     *  
     *  driveletter:path\\path\\path
     *
     */
    protected CVSRoot(String cvsroot) throws IllegalArgumentException {
        
        int colonPosition = 0;
        boolean localFormat;
        if (cvsroot.startsWith(":") == false) {
            
            // no method mentioned guess it using heuristics
            
            localFormat = cvsroot.startsWith("/");
            if (localFormat == false) {
                if (cvsroot.indexOf(':') == 1 && cvsroot.indexOf('\\') == 2) {
                    //#67504 it looks like windows drive  => local
                    method = METHOD_LOCAL;
                    repository = cvsroot;
                    return;
                }                
                colonPosition = cvsroot.indexOf(':');
                if (colonPosition < 0) {
                    // No colon => server format, but there must be a '/' in the middle
                    int slash = cvsroot.indexOf('/');
                    if (slash < 0) {
                        throw new IllegalArgumentException("CVSROOT must be an absolute pathname.");
                    }
                    method = METHOD_SERVER;
                } else {
                    method = METHOD_EXT;
                }
                colonPosition = 0;
            } else {
                method = METHOD_LOCAL;
            }
        } else {
            // connection method is given so parse it

            colonPosition = cvsroot.indexOf(':', 1);
            if (colonPosition < 0)
                throw new IllegalArgumentException("The connection method does not end with ':'.");
            int methodNameEnd = colonPosition;
            int semicolonPosition = cvsroot.indexOf(";", 1);

            if (semicolonPosition != -1 && semicolonPosition < colonPosition) {
                // method has options
                methodNameEnd = semicolonPosition;
                String options = cvsroot.substring(semicolonPosition +1, colonPosition);
                StringTokenizer tokenizer = new StringTokenizer(options, "=;");
			    while (tokenizer.hasMoreTokens()) {
					String option = tokenizer.nextToken();
                    if (tokenizer.hasMoreTokens() == false) {
                        throw new IllegalArgumentException("Undefined " + option  + " option value.");
                    }
                    String value = tokenizer.nextToken();
                    if ("hostname".equals(option)) {  // NOI18N
                        hostname = value;
                    } else if ("username".equals(option)) { // NOI18N
                        username = value;
                    } else if ("password".equals(option)) { // NOI18N
                        password = value;
                    } if ("port".equals(option)) { // NOI18N
                        try {
                            port = Integer.parseInt(value, 10);
                        } catch (NumberFormatException ex) {
                            throw new IllegalArgumentException("Port option must be number.");
                        }
                    }
				}
            }

            this.method = cvsroot.substring(1, methodNameEnd).intern();

            //#65742 read E!e>2.0 workdirs 
            if ("extssh".equals(method)) {  // NOI18N
                method = METHOD_EXT;
            }
            // CVSNT supports :ssh;ver=2:username@cvs.sf.net:/cvsroot/xoops
            if ("ssh".equals(method)) { // NOI18N
                method = METHOD_EXT;
            }
            colonPosition++;
            // Set local format in case of :local: or :fork: methods.
            localFormat = isLocalMethod(this.method);
        }
        
        if (localFormat) {
            // everything after method is repository in local format
            this.repository = cvsroot.substring(colonPosition);
        } else {
            /* So now we parse SERVER_FORMAT
            :method:[[user][:password]@]hostname[:[port]]/reposi/tory
            ALGORITHM:
            - find the first '@' character
                - not found:
                    - find the first ':'
                        - not found
                            - find the first '/'
                                - not found
                                    - exception
                                - found
                                    - parse hostname/path/to/repository
                        - found
                            - parse rest
                - found
                    - find the following ':' character
                        - not found
                            - exception
                        - found
                            parse rest
            */
            
            int startSearch = cvsroot.substring(colonPosition).lastIndexOf('@');
            if (startSearch != -1) startSearch += colonPosition;
            if (startSearch < 0) startSearch = colonPosition;
            String userPasswdHost;
            int pathBegin = -1;
            int hostColon = cvsroot.indexOf(':', startSearch);
            if (hostColon == -1) {
                pathBegin = cvsroot.indexOf('/', startSearch);
                if (pathBegin < 0) {
                    throw new IllegalArgumentException("cvsroot " + cvsroot + " is malformed, host name is missing.");
                } else {
                    userPasswdHost = cvsroot.substring(colonPosition, pathBegin);
                }
            } else {
                userPasswdHost = cvsroot.substring(colonPosition, hostColon);
            }

            int at = userPasswdHost.lastIndexOf('@');
            if (at == -1) {
                // there is no user or password, only hostname before port
                if (userPasswdHost.length() > 0) {
                    this.hostname = userPasswdHost;
                }
            }
            else {
                // there is user, password or both before hostname
                // up = username, password or both
                String up = userPasswdHost.substring(0, at);
                if (up.length() > 0) {
                    int upDivider = up.indexOf(':');
                    if (upDivider != -1) {
                        this.username = up.substring(0, upDivider);
                        this.password = up.substring(upDivider+1);
                    }
                    else {
                        this.username = up;
                    }
                }

                // hostname
                this.hostname = userPasswdHost.substring(at+1);
            }
            
            if (hostname == null || hostname.length() == 0) {
                throw new IllegalArgumentException("Didn't specify hostname in CVSROOT '"+cvsroot+"'.");
            }

            /*
            Now we are left with port (optional) and repository after hostColon
            pr = possible port and repository
            */
            if (hostColon > 0) {
                String pr = cvsroot.substring(hostColon+1);
                int index = 0;
                int port = 0;
                char c;
                while (pr.length() > index && Character.isDigit(c = pr.charAt(index))) {
                    int d = Character.digit(c, 10);
                    port = port*10 + d;
                    index++;
                }
                this.port = port;
                if (index > 0) pr = pr.substring(index);
                if (pr.startsWith(":")) {  // NOI18N
                    pr = pr.substring(1);
                }
                this.repository = pr;
            } else {
                this.port = 0;
                this.repository = cvsroot.substring(pathBegin);
            }
        }
    }
    
    /**
     * Test whether this cvsroot describes a local connection or remote connection.
     * The connection is local if and only if the host name is <code>null</code>.
     * E.g. for local or fork methods.
     */
    public boolean isLocal() {
        return hostname == null;
    }
    
    /**
        <ul>
            <li>
                <code>LOCAL_FORMAT --> :method:/reposi/tory</code>
                <br/>
                "no method" is always represented internally as null
            </li>
            <li>
                <code>SERVER_FORMAT --> :method:user@hostname:[port]/reposi/tory</code>
                <br/>
                Password is never included in cvsroot string representation. Use getPassword to get it.
            </li>
        </ul>
    */
    
    public String toString() {
		
        if (this.hostname == null) {
            if (this.method == null)
                return this.repository;
            
            return ":" + this.method + ":" + this.repository;
        } else {
        
            StringBuffer buf = new StringBuffer();
            
            if (this.method != null) {
                buf.append(':');
                buf.append(this.method);
                buf.append(':');
            }
            
            // don't put password in cvsroot
            if (this.username != null) {
                buf.append(this.username);
                buf.append('@');
            }
            
            // hostname
            buf.append(this.hostname);
            buf.append(':');
            
            // port
            if (this.port > 0)
                buf.append(this.port);
            
            // repository
            buf.append(this.repository);
            
            return buf.toString();
        }
    }
	
    /**
    <p>
        With this method it is possible to compare how close two CVSRoots are to each other. The possible values are:
    </p>
    
    <ul>
        <li>-1 = not compatible - if none of the below match</li>
        <li>0 = when equals(..) returns true</li>
        <li>1 = refers to same repository on the same machine using same method on same port and same user</li>
        <li>2 = refers to same repository on the same machine using same method</li>
        <li>3 = refers to same repository on the same machine</li>
    </ul>
	*/
    public int getCompatibilityLevel(CVSRoot compared) {
		
        if (equals(compared))
            return 0;
        
        
        boolean sameRepository = isSameRepository(compared);
        boolean sameHost = isSameHost(compared);
        boolean sameMethod = isSameMethod(compared);
        boolean samePort = isSamePort(compared);
        boolean sameUser = isSameUser(compared);
        
        if (sameRepository && sameHost && sameMethod && samePort && sameUser)
            return 1;
        else if (sameRepository && sameHost && sameMethod)
            return 2;
        else if (sameRepository && sameHost)
            return 3;
        else
            return -1;
    }
    
    private boolean isSameRepository(CVSRoot compared) {
        if (this.repository.equals(compared.repository)) {
            return true;
        }
        try {
            if (
                (new File(this.repository)).getCanonicalFile().equals(
                    new File(compared.repository).getCanonicalFile()
                )
            )
                return true;
            else
                return false;
        }
        catch (IOException ioe) {
            // something went wrong when invoking getCanonicalFile() so return false
            return false;
        }
    }
    
    private boolean isSameHost(CVSRoot compared) {
        String comparedHostName = compared.getHostName();
        if (this.hostname == comparedHostName) {
            return true;
        }
        if (this.hostname != null) {
            return this.hostname.equalsIgnoreCase(comparedHostName);
        } else {
            return false;
        }
    }
    
    private boolean isSameMethod(CVSRoot compared) {
        if (this.method == null)
            if (compared.getMethod() == null)
                return true;
            else
                return false;
        else if (this.method.equals(compared.getMethod()))
            return true;
        else
            return false;
    }
    
    private boolean isSamePort(CVSRoot compared) {
        if (this.isLocal() == compared.isLocal())
            if (this.isLocal())
                return true;
            else if (this.port == compared.getPort())
                return true;
            else {
                try {
                    Connection c1 = ConnectionFactory.getConnection(this);
                    Connection c2 = ConnectionFactory.getConnection(compared);
                    // Test actual ports used by the connections.
                    // This is necessary in case that port in CVSRoot is zero and the conection is using some default port number.
                    return c1.getPort() == c2.getPort();
                } catch (IllegalArgumentException iaex) {
                    return false;
                }
            }
        else
            return false;
    }
    
    private boolean isSameUser(CVSRoot compared) {
        String user = compared.getUserName();
        if (user == getUserName()) return true;
        if (user != null) {
            return user.equals(getUserName());
        }
        return false;
    }
    
    /**
     * CVSRoots are equal if their toString representations are equal.
     * This puts some extra pressure on the toString method that should be defined very precisely.
     */
    public boolean equals(Object o) {
        // This should be null safe, right?
        if (!(o instanceof CVSRoot))
            return false;
        
        CVSRoot compared = (CVSRoot) o;
        
        if (toString().equals(compared.toString()))
            return true;
        
        return false;
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
	
    /**
     * Get the format of this cvsroot.
     * @return Either {@link #LOCAL_FORMAT} or {@link #SERVER_FORMAT}.
     *
    public int getUrlFormat() {
        return urlFormat;
    }
     */
    
    /**
     * Get the connection method.
     * @return The connection method or <code>null</code> when no method is defined.
     */
    public String getMethod() {
        return method;
    }
    
    /**
    setting the method has effects on other components.
    The method might change
    - urlFormat
    - username and password
    - hostname/port
    If urlFormat becomes LOCAL_FORMAT then username, password and hostname are set to null and port to 0.
    If urlFormat becomes SERVER_FORMAT then hostname must not be null.
    */
    protected void setMethod(String method) {
        if (method != null) {
            this.method = method.intern();
        } else {
            method = null;
        }
        if (isLocalMethod(method)) {
            this.username = null;
            this.password = null;
            this.hostname = null;
            this.port = 0;
        }
        else {
            if (this.hostname == null)
                throw new IllegalArgumentException("Hostname must not be null when setting a remote method.");
        }
    }
    
    // test whether the method is "local" or "fork"
    private boolean isLocalMethod(String method) {
        return METHOD_LOCAL == method || METHOD_FORK == method;
    }
    
    /**
     * Get the user name.
     * @return The user name or code>null</code> when the user name is not defined.
     */
    public String getUserName() {
        return username;
    }
    /**
     * Set the user name.
     * @param username The user name.
     */
    protected void setUserName(String username) {
        this.username = username;
    }
    
    /**
     * Get the password.
     * @return The password or <code>null</code> when the password is not defined.
     */
    public String getPassword() {
        return this.password;
    }
    /**
     * Set the password.
     * @param password The password
     */
    protected void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Get the host name.
     * @return The host name or <code>null</code> when the host name is
     *         not defined
     */
    public String getHostName() {
        return this.hostname;
    }
    /**
     * Set the host name.
     * @param hostname The host name or <code>null</code> when the host name is
     *        not defined.
     */
    protected void setHostName(String hostname) {
        this.hostname = hostname;
    }
    
    /**
     * Get the port number.
     * @return The port number or zero when the port is not defined.
     */
    public int getPort() {
        return this.port;
    }
    /**
     * Set the port number.
     * @param port The port number or zero when the port is not defined.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * Get the repository.
     * @return The repository. This is never <code>null</code>.
     */
    public String getRepository() {
        return repository;
    }
    /**
     * Set the repository.
     * @param repository The repository. Must not be <code>null</code>.
     */
    protected void setRepository(String repository) {
        if (repository == null) {
            throw new IllegalArgumentException("The repository must not be null.");
        }
        this.repository = repository;
    }

}
