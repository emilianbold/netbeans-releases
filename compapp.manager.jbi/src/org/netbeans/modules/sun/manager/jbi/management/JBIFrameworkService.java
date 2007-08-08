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

package org.netbeans.modules.sun.manager.jbi.management;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Queries the runtime server to ensure that the JBI Container
 * is installed and is enabled. The JBI Container is installed as a
 * Lifecycle module on the server. This class also helps in configuring
 * the JBI Container at runtime. 
 *
 * @author Graj
 *
 */
public class JBIFrameworkService implements Serializable {
    
    /**
     * com.sun.jbi.home
     */
    public static final String JBI_CLASS_NAME_KEY = "class-name"; // NOI18N

    /**
     * com.sun.jbi.binding.proxy.connection
     */
    public static final String JBI_CLASSPATH_KEY = "classpath"; // NOI18N

    /**
     * disable-timeout-in-minutes - String - 30
     */
    public static final String DESCRIPTION_KEY = "description"; // NOI18N

    /**
     * enabled - boolean - true
     */
    public static final String ENABLED_KEY = "enabled"; // NOI18N

    /**
     * lb-enabled - boolean - false
     */
    public static final String IS_FAILURE_FATAL_KEY = "is-failure-fatal"; // NOI18N

    /**
     * ref - String - JBIFramework
     */
    public static final String LOAD_ORDER_KEY = "load-order"; // NOI18N

    /**
     * virtual-servers - String
     */
    public static final String NAME_KEY = "name"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_CONFIG_OBJECTNAME = "com.sun.appserv:name=JBIFramework,type=lifecycle-module,category=config"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String JBI_GETPROPERTYVALUE_OPERATION_NAME = "getPropertyValue"; // NOI18N
    public static final String JBI_SETPROPERTY_OPERATION_NAME = "setProperty"; // NOI18N
    private MBeanServerConnection serverConnection = null;
    

    /**
     * DOCUMENT ME!
     */
    private String className;

    /**
     * DOCUMENT ME!
     */
    private String classpath;

    /**
     * DOCUMENT ME!
     */
    private String description;

    /**
     * DOCUMENT ME!
     */
    private boolean enabled;

    /**
     * DOCUMENT ME!
     */
    private boolean failureFatal;

    /**
     * DOCUMENT ME!
     */
    private String loadOrder;

    /**
     * DOCUMENT ME!
     */
    private String name;
    
    boolean jbiFrameworkEnabled;
    
    boolean isUIMBeanRegistered;   


    /**
     *
     */
    public JBIFrameworkService(MBeanServerConnection connection) {
        super();
        this.serverConnection = connection;
        this.initialize();
    }

    /**
     * DOCUMENT ME!
     */
    void initialize() {
        try {
            this.jbiInitialize();
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MalformedObjectNameException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws InstanceNotFoundException DOCUMENT ME!
     * @throws MBeanException DOCUMENT ME!
     * @throws ReflectionException DOCUMENT ME!
     */
    void jbiInitialize() throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        QueryExp queryExpression = null;
        Set set = null;
        ObjectName objectName = null;
        objectName = new ObjectName(JBIFrameworkService.JBI_CONFIG_OBJECTNAME);
        ObjectName uiMBeanObjectName = new ObjectName(AdministrationService.ADMIN_SERVICE_OBJECTNAME);

        if (this.serverConnection != null) {
            try {
                isUIMBeanRegistered = this.serverConnection.isRegistered(uiMBeanObjectName);
            } catch (IOException ex) {
                // ignore
            }
            
            set = this.serverConnection.queryNames(objectName, queryExpression);

            Iterator iterator = set.iterator();

            if ((iterator != null) && (iterator.hasNext() == true)) {
                objectName = (ObjectName) iterator.next();
            }

            if (objectName != null) {
                
                this.className = (String) this.serverConnection.getAttribute(objectName, JBIFrameworkService.JBI_CLASS_NAME_KEY);
                this.description = (String) this.serverConnection.getAttribute(objectName, JBIFrameworkService.DESCRIPTION_KEY);
                String value = (String) this.serverConnection.getAttribute(objectName, JBIFrameworkService.ENABLED_KEY);
                if(value != null) {
                    if(value.equalsIgnoreCase("True") == true) { // NOI18N
                        this.enabled = true;
                    } else {
                        this.enabled = false;
                    }
                }
                this.classpath = (String) this.serverConnection.getAttribute(objectName, JBIFrameworkService.JBI_CLASSPATH_KEY);
                value = (String) this.serverConnection.getAttribute(objectName, JBIFrameworkService.IS_FAILURE_FATAL_KEY);
                if(value != null) {
                    if(value.equalsIgnoreCase("True") == true) { // NOI18N
                        this.failureFatal = true;
                    } else {
                        this.failureFatal = false;
                    }
                }
                this.loadOrder = (String) this.serverConnection.getAttribute(objectName, JBIFrameworkService.LOAD_ORDER_KEY);
                this.name = (String) this.serverConnection.getAttribute(objectName, JBIFrameworkService.NAME_KEY);
                this.jbiFrameworkEnabled = true;
            } else {
                this.jbiFrameworkEnabled = false;
                System.out.println("Cound not find the JBI Configuration MBean"); // NOI18N
            }
        } else {
            System.out.println("Could not connect to application server"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param objectName DOCUMENT ME!
     * @param paramObject DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws InstanceNotFoundException DOCUMENT ME!
     * @throws MBeanException DOCUMENT ME!
     * @throws ReflectionException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    Object invoke(ObjectName objectName, String paramObject) {
        String operationName = JBIFrameworkService.JBI_GETPROPERTYVALUE_OPERATION_NAME;
        Object resultObject = null;
        String[] params = {paramObject};
        String[] signature = {"java.lang.String"}; // NOI18N

        try {
            resultObject = (String) this.serverConnection.invoke(objectName, operationName, params, signature);
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return resultObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the serverConnection.
     */
    public MBeanServerConnection getServerConnection() {
        return this.serverConnection;
    }
    
    /*
    public String getDefaultLogPropertyValue() {
        String operationName = JBIFrameworkService.JBI_GETPROPERTYVALUE_OPERATION_NAME;
        ObjectName objectName = null;
        try {
             objectName = new ObjectName(JBIFrameworkService.JBI_CONFIG_OBJECTNAME);
        } catch (MalformedObjectNameException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        String resultObject = null;
        String[] params = {JBIFrameworkService.JBI_LOG_DEFAULT_PROPERTY_NAME};
        String[] signature = {"java.lang.String"}; // NOI18N

        try {
            resultObject = (String) this.serverConnection.invoke(objectName, operationName, params, signature);
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return resultObject;
    }
     
    public void setDefaultLogPropertyValue(String logLevelString) {
        String operationName = JBIFrameworkService.JBI_SETPROPERTY_OPERATION_NAME;
        ObjectName objectName = null;
        try {
             objectName = new ObjectName(JBIFrameworkService.JBI_CONFIG_OBJECTNAME);
        } catch (MalformedObjectNameException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        Attribute attribute = new Attribute(JBIFrameworkService.JBI_LOG_DEFAULT_PROPERTY_NAME, logLevelString);
        String resultObject = null;
        Object[] params = {attribute};
        String[] signature = {attribute.getClass().getName()};

        try {
            resultObject = (String) this.serverConnection.invoke(objectName, operationName, params, signature);
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }
    */
    
    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * @return Returns the classpath.
     */
    public String getClasspath() {
        return this.classpath;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @return Returns the failureFatal.
     */
    public boolean isFailureFatal() {
        return this.failureFatal;
    }

    /**
     * @return Returns the loadOrder.
     */
    public String getLoadOrder() {
        return this.loadOrder;
    }

    /**
     * @param loadOrder The loadOrder to set.
     */
//    public void setLoadOrder(String loadOrder) {
//        this.loadOrder = loadOrder;
//    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name The name to set.
     */
//    public void setName(String name) {
//        this.name = name;
//    }
    
    /**
     * @return Returns the jbiFrameworkEnabled.
     */
    public boolean isJbiFrameworkEnabled() {
        return this.jbiFrameworkEnabled && this.isUIMBeanRegistered && this.isEnabled();
    }

    /**
     * DOCUMENT ME!
     */
    public void printOut() {
        System.out.println("//////////////////////////////////////////////////////////////////"); // NOI18N
        System.out.println("//                 -- JBI Configuration --                      //"); // NOI18N
        System.out.println("//////////////////////////////////////////////////////////////////"); // NOI18N
        System.out.println("// " + JBIFrameworkService.DESCRIPTION_KEY + " is: " +this.getDescription()); // NOI18N
        System.out.println("// " + JBIFrameworkService.ENABLED_KEY + " is: " +this.isEnabled()); // NOI18N
        System.out.println("// " + JBIFrameworkService.IS_FAILURE_FATAL_KEY + " is: " +this.isFailureFatal()); // NOI18N
        System.out.println("// " + JBIFrameworkService.JBI_CLASS_NAME_KEY + " is: " + this.getClassName()); // NOI18N
        System.out.println("// " + JBIFrameworkService.JBI_CLASSPATH_KEY + " is: " +this.getClasspath()); // NOI18N
        System.out.println("// " + JBIFrameworkService.LOAD_ORDER_KEY + " is: " +this.getLoadOrder()); // NOI18N
        System.out.println("// " + JBIFrameworkService.NAME_KEY + " is: " +this.getName()); // NOI18N

        System.out.println("//////////////////////////////////////////////////////////////////"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static JBIFrameworkService getJBIConfiguration() {
        JBIFrameworkService configuration = null;
        return configuration;
    }
    
    /**
     * 
     * @param connection
     */
    public static void test(MBeanServerConnection connection) {
        /*
        String LOG_LEVEL_INFO_KEY = "INFO";
        String LOG_LEVEL_FINEST_KEY = "FINEST";
        String LOG_LEVEL_FINER_KEY = "FINER";
        String LOG_LEVEL_FINE_KEY = "FINE";
        String LOG_WARNING_INFO_KEY = "WARNING";
        String LOG_SEVERE_INFO_KEY = "SEVERE";
        String LOG_OFF_INFO_KEY = "OFF";
        */ 
        if(connection != null) {
            JBIFrameworkService service = new JBIFrameworkService(connection);
            service.printOut();
            if(service.isJbiFrameworkEnabled() == true) {
                System.out.println("**** JBI Framework is Enabled ****"); // NOI18N
            } else {
                System.out.println("**** JBI Framework is NOT Enabled ****"); // NOI18N
            }

            /*
            String level = service.getDefaultLogPropertyValue();
            service.setDefaultLogPropertyValue(LOG_SEVERE_INFO_KEY);
            */
        }       
    }
    


    /**
     * Run an ant script. Starts a separate process to do so. This prevents potential undesireable
     * interactions, but it also means it should only be used in 'low-frequency' situations to
     * avoid performance issues with continually creating new processes.
     *
     * @param args full path to the ant script file
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {
        String PROTOCOL_CLASS = "com.sun.enterprise.admin.jmx.remote.protocol"; // NOI18N
        String HTTP_AUTH_PROPERTY_NAME = "com.sun.enterprise.as.http.auth"; // NOI18N
        String DEFAULT_HTTP_AUTH_SCHEME = "BASIC"; // NOI18N
        String ADMIN_USER_ENV_PROPERTY_NAME = "USER"; // NOI18N
        String ADMIN_PASSWORD_ENV_PROPERTY_NAME = "PASSWORD"; // NOI18N
        String RTS_HTTP_CONNECTOR = "s1ashttp";         // NOI18N
        
        String hostName = null;
        String port = null;
        String userName = null;
        String password = null;

        hostName = "localhost"; // NOI18N
        port = "4848"; // NOI18N
        userName = "admin"; // NOI18N
        password = "adminadmin"; // NOI18N

        final Map<String, String> environment = new HashMap<String, String>();
        environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PROTOCOL_CLASS);
        environment.put(HTTP_AUTH_PROPERTY_NAME, DEFAULT_HTTP_AUTH_SCHEME);
        environment.put(ADMIN_USER_ENV_PROPERTY_NAME, userName);
        environment.put(ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);

        try {
            int portValue = new Integer(port).intValue();
            MBeanServerConnection connection = null;
            JMXServiceURL serviceURL = new JMXServiceURL(RTS_HTTP_CONNECTOR, hostName, portValue);
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, environment);
            connection = connector.getMBeanServerConnection();

        System.out.println("Connection Retrieved."+connection.toString()); // NOI18N
            JBIFrameworkService.test(connection);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Connection Failed "+ex.getMessage()); // NOI18N
        }
      
    }
}
