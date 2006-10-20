/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * Created on Dec 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.sun.manager.jbi.management.connectors;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * DOCUMENT ME!
 *
 * @author Graj TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class HTTPServerConnector extends ServerConnector implements Serializable {
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
    public HTTPServerConnector(
        String hostNameParam, String portParam, String userNameParam, String passwordParam,
            ClassLoader jbiClassLoader
    ) {
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
    public void setParameters(
        String hostNameParam, String portParam, String userNameParam, String passwordParam
    ) {
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
     * This method returns the MBeanServerConnection to used to invoke the MBean methods via HTPP
     * connector.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void initialize() throws Exception {
        boolean result = true;
        final Map<String, Object> environment = new HashMap<String, Object>();
        environment.put(
            JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, HTTPServerConnector.PROTOCOL_CLASS
        );
        environment.put(
            HTTPServerConnector.HTTP_AUTH_PROPERTY_NAME,
            HTTPServerConnector.DEFAULT_HTTP_AUTH_SCHEME
        );
        environment.put(HTTPServerConnector.ADMIN_USER_ENV_PROPERTY_NAME, this.getUserName());
        environment.put(HTTPServerConnector.ADMIN_PASSWORD_ENV_PROPERTY_NAME, this.getPassword());

        environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER, jbiClassLoader);
        
        try {
            int portValue = new Integer(this.getPort()).intValue();
            JMXServiceURL serviceURL = new JMXServiceURL(
                    HTTPServerConnector.RTS_HTTP_CONNECTOR, this.getHostName(), portValue
                );
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, environment);
            this.connection = connector.getMBeanServerConnection();
        } catch (Exception exception) {
            throw exception;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        HTTPServerConnector connector = new HTTPServerConnector(
                "GRajGX270.stc.com", "4848", "admin", "adminadmin", null // NOI18N
            );

        if (connector.getConnection() != null) {
            System.out.println("Connection Retrieved." + connector.toString()); // NOI18N
            connector.printOut();
        } else {
            System.out.println("Connection Failed"); // NOI18N
        }
    }
}
