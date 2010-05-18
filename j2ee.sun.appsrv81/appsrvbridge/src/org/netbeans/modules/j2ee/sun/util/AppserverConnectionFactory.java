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
package org.netbeans.modules.j2ee.sun.util;

import java.io.IOException;
import javax.management.Attribute;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustAnyTrustManager;
import com.sun.appserv.management.config.ConfigDottedNames;


/**
 * Factory for creating the appropriate JMX connection to the Aplication Server
 * for management operations through the AMX API.
 */
public class AppserverConnectionFactory {
    
    /** Dotted name for the jmx connector port **/
    private static final String JMX_CONNECTOR_PORT_DOTTED_NAME = 
        "server.admin-service.jmx-connector.system.port";
    
    /** Dotted name for the security enabled token **/
    private static final String SECURITY_ENABLED_DOTTED_NAME = 
        "server.admin-service.jmx-connector.system.security-enabled";
    
    /** 
     * Constructor for AppserverConnectionFactory made private to avoid
     * anyone trying to instantiate an instance of this static class. This is
     * simply a good coding practice.
     */
    private AppserverConnectionFactory() {
    }
    
    
    /**
     * Returns an AMX connection for connecting to the Sun Java System
     * Application Server over SSL.
     *
     * @param host the name of the host on which the appserver is running
     * @param port the port on which the admin server is running
     * @param username username for logging into the admin server
     * @param password password for logging into the admin server
     *
     * @return an AppserverConnectionSource
     */
    public static AppserverConnectionSource getAppserverConnection(
            final String host, final int port, final String username, 
            final String password, final boolean isSecure) throws IOException {
        return getAppserverConnection(host, port, username, password,
            getDefaultTLSParams(isSecure), false);
    }
    
    
    /**
     * Returns an AMX connection for connecting to the Sun Java System
     * Application Server. 
     *
     * @param host the name of the host on which the appserver is running
     * @param port the port on which the admin server is running
     * @param username username for logging into the admin server
     * @param password password for logging into the admin server
     * @param forceNew boolean true/false to determine whether to force the 
     *                 creation of a new AppserverConnectionSource
     *
     * @return an AppserverConnectionSource
     */
    public static AppserverConnectionSource getAppserverConnection(
            final String host, final int port, final String username, 
            final String password, final boolean isSecure, boolean forceNew) 
                throws IOException {
        return getAppserverConnection(host, port, username, password, 
            getDefaultTLSParams(isSecure), forceNew);
    }

    
    /**
     * Returns an AMX connection for connecting to the Sun Java System
     * Application Server over SSL.
     *
     * @param host the name of the host on which the appserver is running
     * @param port the port on which the admin server is running
     * @param username username for logging into the admin server
     * @param password password for logging into the admin server
     * @param tlsParams SSL parameters for secure connection
     * @param forceNew boolean true/false to determine whether to force the 
     *                 creation of a new AppserverConnectionSource
     *
     * @return an AppserverConnectionSource
     */
    public static AppserverConnectionSource getAppserverConnection(
            final String host, final int port, final String username, 
            final String password, final TLSParams tlsParams, boolean forceNew) 
                throws IOException {
        return getRMIAppserverConnectionSource(host, port, username, password, 
                    tlsParams);
    }
  
    
    /**
     * Returns an AMX connection over HTTP for connecting to the Sun Java System
     * Application Server over SSL.
     *
     * @param host the name of the host on which the appserver is running
     * @param port the port on which the admin server is running
     * @param username username for logging into the admin server
     * @param password password for logging into the admin server
     *
     * @return an AppserverConnectionSource
     */
    public static AppserverConnectionSource getHTTPAppserverConnection(
            final String host, final int port, final String username, 
            final String password, final boolean isSecure) throws IOException {
        return getHTTPAppserverConnectionSource(host, port, username, password,
            getDefaultTLSParams(isSecure));
    }
    
    
    /**
     * Returns the AppserverConnectionSource connected to the RMI port running
     * on DAS. It's retrieved by creating an HTTP AppserverConnectionSource
     * that then uses dotted names for getting the port on which the JMX
     * Connector is running. After getting the appropriate RMI port, the 
     * AppserverConnectionSource running over RMI is created.
     *
     * @param host the name of the host on which the appserver is running
     * @param port the port on which the admin server is running
     * @param username username for logging into the admin server
     * @param password password for logging into the admin server 
     * @param tlsParams SSL parameters for secure connection
     *
     * @return the AppserverConnectionSource connected over RMI
     */
    static AppserverConnectionSource getRMIAppserverConnectionSource(
            final String host, final int port, final String username, 
            final String password, final TLSParams tlsParams) 
                throws IOException {
        AppserverConnectionSource httpConn = 
                getHTTPAppserverConnectionSource(host, port, username, password, 
                    tlsParams);
        return new AppserverConnectionSource(
            AppserverConnectionSource.PROTOCOL_RMI, host,
            getJMXConnectorPort(httpConn), username, password, tlsParams, null);
    }
    
    /**
     * Returns the AppserverConnectionSource running over HTTP. Currently, there
     * is no notification mechanism for HTTP due to its inherent stateless
     * nature. This method is used solely for getting the application server's 
     * JMX Connector port that is running over RMI. AppserverConnectionSource 
     * running over RMI is currently to only protocol supported for AMX. 
     *
     * @param host the name of the host on which the appserver is running
     * @param port the port on which the admin server is running
     * @param username username for logging into the admin server
     * @param password password for logging into the admin server 
     * @param tlsParams SSL parameters for secure connection
     *
     * @return the AppserverConnectionSource connected over HTTP
     */
    static AppserverConnectionSource getHTTPAppserverConnectionSource(
            final String host, final int port, final String username, 
            final String password, final TLSParams tlsParams) {
        return new AppserverConnectionSource(
            AppserverConnectionSource.PROTOCOL_HTTP, host, port, username,
            password, tlsParams, null);
    }
    
    /**
     * Returns the ConfigDottedNames object from an AppserverConnectionSource. 
     *
     * @param conn an AppserverConnectionSource
     *
     * @return the ConfigDottedNames AMX object
     */
    static ConfigDottedNames getConfigDottedNames(
            final AppserverConnectionSource conn) throws IOException {
        ConfigDottedNames names = null;
        names = conn.getDomainRoot().getConfigDottedNames();
        return names;
    }
    
    /**
     * Gets the port on which the JMXConnector is listening. This
     * operation is necessary in order for our continuing interaction with
     * our admin infrastructure to be through RMI, which is currently the only
     * protocol upon which the MBean API supports. 
     *
     * @param conn an AppserverConnectionSource
     *
     * @return the port on which the JMX Connector is listening.
     */
    static int getJMXConnectorPort(
            final AppserverConnectionSource conn) throws IOException {
       Attribute attr = 
            (Attribute)getAttributeFromConfigDottedNames(conn,
                JMX_CONNECTOR_PORT_DOTTED_NAME);
       return Integer.parseInt((String)attr.getValue());
    }
    
    /**
     * Returns true/false depending on whether or not the appserver is running
     * in secure mode.  
     *
     * @param conn an AppserverConnectionSource
     *
     * @return true/false determining whether or not security is enabled
     */
    static boolean isAppserverConnectionSecurityEnabled(
            final AppserverConnectionSource conn) throws IOException {
        Attribute attr = 
            (Attribute)getAttributeFromConfigDottedNames(conn,
                SECURITY_ENABLED_DOTTED_NAME);
       return Boolean.getBoolean((String)attr.getValue());
    }
    
    /**
     * Gets the Ojbect associated with the dotted name given for extraction of 
     * the value for name passed. 
     *
     * @param conn an AppserverConnectionSource
     *
     * @return a java.lang.Object containing dotted name value
     */
    static Object getAttributeFromConfigDottedNames(
            final AppserverConnectionSource conn, final String dottedName) 
                throws IOException{
        return getConfigDottedNames(conn).dottedNameGet(dottedName);
    }

    
    /**
     * Gets the default TLS params configuration. 
     *
     * @param isSecure Boolean value specifying whether the sever is secure or
     *        not.
     * @return Either null if server is not secure or an instance of a default
     *         TrustAnyTrustManager.
     */
    private static TLSParams getDefaultTLSParams(final boolean isSecure) {
        return (isSecure)
            ? new TLSParams(TrustAnyTrustManager.getInstanceArray(), null)
            : null;
    }
    
    
}






