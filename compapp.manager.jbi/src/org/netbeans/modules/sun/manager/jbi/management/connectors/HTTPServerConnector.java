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
 * Created on Dec 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.sun.manager.jbi.management.connectors;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 *
 * @author Graj
 */
public class HTTPServerConnector extends ServerConnector implements
        Serializable {
    /**
     * DOCUMENT ME!
     */
    static final String PROTOCOL_CLASS = "com.sun.enterprise.admin.jmx.remote.protocol"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    static final String HTTP_AUTH_PROPERTY_NAME = "com.sun.enterprise.as.http.auth"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    static final String DEFAULT_HTTP_AUTH_SCHEME = "BASIC"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    static final String ADMIN_USER_ENV_PROPERTY_NAME = "USER"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    static final String ADMIN_PASSWORD_ENV_PROPERTY_NAME = "PASSWORD"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    static final String RTS_HTTP_CONNECTOR = "s1ashttp"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    transient MBeanServerConnection connection;

    private ClassLoader jbiClassLoader;

    /**
     * DOCUMENT ME!
     * 
     * @param hostNameParam
     * @param portParam
     * @param userNameParam
     * @param passwordParam
     */
    public HTTPServerConnector(String hostNameParam, String portParam,
            String userNameParam, String passwordParam,
            ClassLoader jbiClassLoader) {
        super(hostNameParam, portParam, userNameParam, passwordParam);

        this.jbiClassLoader = jbiClassLoader;

        try {
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param hostNameParam
     * @param portParam
     * @param userNameParam
     * @param passwordParam
     */
    public void setParameters(String hostNameParam, String portParam,
            String userNameParam, String passwordParam) {
        this.hostName = hostNameParam;
        this.port = portParam;
        this.userName = userNameParam;
        this.password = passwordParam;

        try {
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return Returns the connection.
     */
    public MBeanServerConnection getConnection() {
        return this.connection;
    }

    /**
     * This method returns the MBeanServerConnection to used to invoke the MBean
     * methods via HTTP connector.
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    public void initialize() throws Exception {
        boolean result = true;
        try {
            int portValue = new Integer(this.getPort()).intValue();
            this.connection = this.getMBeanServerConnection(this.getHostName(), 
                    portValue, this.getUserName(), this.getPassword());
        } catch (Exception exception) {
            throw exception;
        }
    }
    
    /**
     * Tests if MBean is registered.
     * @param MBeanServerConnection connection
     * @param objectNameString
     * @return true if registered, false if not
     * @throws MalformedObjectNameException, NullPointerException, IOException
     */
    boolean isMBeanRegistered(MBeanServerConnection connection, String objectNameString) 
            throws MalformedObjectNameException, NullPointerException, IOException {
        boolean result = false;
        ObjectName objectName = new ObjectName(objectNameString);
        result = connection.isRegistered(objectName);
        return result;
    }
    
    /**
     * Creates a new instance of JBIAdminCommands object First tries to
     * establish a HTTP connection. If that fails, tries to establish a HTTPS
     * connection, and if that fails tries to establish a JRMP Connection.
     * 
     * @param hostName
     * @param portNumber
     * @param userName
     * @param password
     * @return JBIAdminCommands object
     * @throws JBIRemoteException
     */
    public MBeanServerConnection getMBeanServerConnection(String hostName, 
            int portNumber,
            String userName, String password) throws Exception {

        MBeanServerConnection connection = null;
        boolean result = false;
        String ADMIN_SERVICE_OBJECTNAME = "com.sun.jbi:ServiceName=JbiAdminUiService,ComponentType=System"; // NOI18N

        // Try to obtain a HTTP connection
        try {
            connection = this.getMBeanServerConnection(hostName, portNumber,
                    userName, password, ConnectionType.HTTP);
            result = this.isMBeanRegistered(connection, ADMIN_SERVICE_OBJECTNAME);
        } catch (MalformedObjectNameException e) {
            connection = null;
        } catch (IOException e) {
            connection = null;
        } catch (RuntimeException runtimeException) {
            connection = null;
        } catch (Exception e) {
            connection = null;
        }

        if (connection == null) {
            // Try to obtain a HTTPS (secure) connection
            try {
                connection = this.getMBeanServerConnection(hostName,
                        portNumber, userName, password, ConnectionType.HTTPS);
                result = this.isMBeanRegistered(connection, ADMIN_SERVICE_OBJECTNAME);
            } catch (MalformedObjectNameException e) {
                connection = null;
            } catch (IOException e) {
                connection = null;
            } catch (RuntimeException runtimeException) {
                connection = null;
            } catch (Exception e) {
                connection = null;
            }
        }

        if (connection == null) {
            // Try to obtain a JRMP connection
            try {
                connection = this.getMBeanServerConnection(hostName,
                        portNumber, userName, password, ConnectionType.JRMP);
                result = this.isMBeanRegistered(connection, ADMIN_SERVICE_OBJECTNAME);
            } catch (MalformedObjectNameException e) {
                connection = null;
            } catch (IOException e) {
                connection = null;
            } catch (RuntimeException runtimeException) {
                connection = null;
            } catch (Exception e) {
                connection = null;
            }
        }
        return connection;
    }    

    /**
     * This method returns the MBeanServerConnection to used to invoke the MBean
     * methods via HTPP connector.
     * 
     * @param hostName -
     *            the hostName part of the URL. If null, defaults to the local
     *            hostName name, as determined by
     *            InetAddress.getLocalHost().getHostName(). If it is a numeric
     *            IPv6 address, it can optionally be enclosed in square brackets
     *            [].
     * @portNumber - the portNumber part of the URL.
     * @userName - the userName name for authenticating with MBeanServer
     * @password - the password for authenticating with MBeanServer
     * @return MBeanServerConnection
     * @throws Exception
     */
    protected MBeanServerConnection getMBeanServerConnection(String hostName,
            int portNumber, String userName, String password,
            ConnectionType type) throws Exception {
        if (type == ConnectionType.JRMP) {
            // Create a JMXMP connector client and
            // connect it to the JMXMP connector server
            // final JMXServiceURL url = new JMXServiceURL(null, hostName,
            // portNumber);
            // String urlString =
            // "service:jmx:rmi:///jndi/rmi://"+hostName+":"+portNumber+"/jmxri";
            String urlString = "service:jmx:rmi:///jndi/rmi://" + hostName
                    + ":" + portNumber + "/management/rmi-jmx-connector";
            return this.getMBeanServerConnection(urlString, userName, password);
        } else {
            final JMXServiceURL url = new JMXServiceURL(type.getProtocol(),
                    hostName, portNumber);
            final JMXConnector connector = JMXConnectorFactory.connect(url,
                    this.initEnvironment(userName, password));
            return connector.getMBeanServerConnection();
        }
    }

    /**
     * This method initialize the environment for creating the JMXConnector.
     * 
     * @return Map - HashMap of environemtn
     */
    private Map<String, Object> initEnvironment(String userName, String password) {
        final Map<String, Object> environment = new HashMap<String, Object>();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";

        environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER,
               jbiClassLoader);
        environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        environment.put("USER", userName);
        environment.put("PASSWORD", password);
        environment.put("com.sun.enterprise.as.http.auth", "BASIC");
        return (environment);
    }

    /**
     * This method returns the MBeanServerConnection to used to invoke the MBean
     * methods via HTTP connector.
     * 
     * @param url -
     *            service:jmx:rmi:///jndi/rmi://<hostName>:<portNumber>/management/rmi-jmx-connector
     * @userName - the userName name for authenticating with MBeanServer
     * @password - the password for authenticating with MBeanServer
     * @return MBeanServerConnection
     * @throws Exception
     */
    protected MBeanServerConnection getMBeanServerConnection(String urlString,
            String userName, String password) throws Exception {
        // Create a JMXMP connector client and
        // connect it to the JMXMP connector server
        // final JMXServiceURL url = new JMXServiceURL(urlString);
        // final JMXServiceURL url = new JMXServiceURL(null, hostName,
        // portNumber);
        final JMXServiceURL url = new JMXServiceURL(urlString);
        String[] credentials = new String[] { userName, password };
        Map<String, String[]> environment = new HashMap<String, String[]>();
        environment.put("jmx.remote.credentials", credentials);
        final JMXConnector connector = JMXConnectorFactory.connect(url,
                environment);
        return connector.getMBeanServerConnection();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param args
     *            DOCUMENT ME!
     */
    public static void main(String[] args) {
        String hostName = "localhost";
        String port = "";
//        port = "4848";
        port = "5651";
        String userName = "admin";
        String password = "adminadmin";
        
        HTTPServerConnector connector = new HTTPServerConnector(
                hostName, port, userName, password, null // NOI18N
        );

        if (connector.getConnection() != null) {
            System.out.println("Connection Retrieved." + connector.toString()); // NOI18N
            connector.printOut();
        } else {
            System.out.println("Connection Failed"); // NOI18N
        }
    }
}
