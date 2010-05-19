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
package org.netbeans.modules.sun.manager.jbi.management;

import com.sun.esb.management.api.administration.AdministrationService;
import com.sun.esb.management.api.configuration.ConfigurationService;
import com.sun.esb.management.api.deployment.DeploymentService;
import com.sun.esb.management.api.installation.InstallationService;
import com.sun.esb.management.api.notification.NotificationService;
import com.sun.esb.management.client.ManagementClient;
import com.sun.esb.management.client.ManagementClientFactory;
import com.sun.esb.management.common.ManagementRemoteException;
import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.modules.sun.manager.jbi.management.connectors.HTTPServerConnector;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.impl.PerformanceMeasurementServiceWrapperImpl;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.impl.RuntimeManagementServiceWrapperImpl;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstanceReader;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author jqian
 */
public class AppserverJBIMgmtController {

    // original MBeanServerConnection from NetBeans
    private MBeanServerConnection mBeanServerConnection;

    // MBeanServerConnection with classpath problem fixed
    private MBeanServerConnection myMBeanServerConnection;

    private ManagementClient managementClient;

    // cached services
    private AdministrationService administrationService;
    private DeploymentService deploymentService;
    private InstallationService installationService;
    private ConfigurationService configurationService;
    private PerformanceMeasurementServiceWrapper performanceMeasurementServiceWrapper;
    private RuntimeManagementServiceWrapper runtimeManagementServiceWrapper;
    private NotificationService notificationService;

    private boolean notificationServiceChecked;

    private String hostName;
    private String port;
    private String userName;
    private String password;

    public static final String SERVER_TARGET = "server";
    private static final String HOST_MBEAN_NAME =
            "com.sun.appserv:name=server,type=virtual-server,category=monitor,server=server"; // NOI18N
    private static final String HOST_ASADMIN_MBEAN_NAME =
            "com.sun.appserv:type=Host,host=__asadmin"; // NOI18N
    private static final String HTTP_PORT_MBEAN_NAME =
            "com.sun.appserv:type=http-listener,id=http-listener-1,config=server-config,category=config";  // NOI18N
    private static Logger logger = Logger.getLogger("org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController"); // NOI18N

    /**
     * Creates a new instance of AppserverJBIMgmtController
     */
    public AppserverJBIMgmtController(MBeanServerConnection mBeanServerConnection) {
        this.mBeanServerConnection = mBeanServerConnection;
        init();
        managementClient = new ManagementClient(myMBeanServerConnection, true);
    }

    public boolean isJBIFrameworkEnabled() {
        JBIFrameworkService service =
                new JBIFrameworkService(mBeanServerConnection);
        return service.isJbiFrameworkEnabled();
    }

    public AdministrationService getAdministrationService()
            throws ManagementRemoteException {
        if (administrationService == null) {
            administrationService = managementClient.getAdministrationService();
        }
        return administrationService;
    }

    public DeploymentService getDeploymentService()
            throws ManagementRemoteException {
        if (deploymentService == null) {
            deploymentService = managementClient.getDeploymentService();
        }
        return deploymentService;
    }

    public InstallationService getInstallationService()
            throws ManagementRemoteException {
        if (installationService == null) {
            installationService = managementClient.getInstallationService();
        }
        return installationService;
    }

    public ConfigurationService getConfigurationService()
            throws ManagementRemoteException {
        if (configurationService == null) {
            configurationService = managementClient.getConfigurationService();
        }
        return configurationService;
    }

    public NotificationService getNotificationService()
            throws ManagementRemoteException {

        if (!notificationServiceChecked && notificationService == null) {
            notificationServiceChecked = true;
            String rmiPortString = managementClient.getAdministrationService().getJmxRmiPort();

            if (password == null || password.equals("") || userName == null || userName.equals("")) { // NOI18N
                if (userName == null) {
                    userName = ""; // NOI18N
                }
                PasswordPanel passwordPanel = new PasswordPanel(userName);

                DialogDescriptor dd = new DialogDescriptor(passwordPanel, hostName);
                //passwordPanel.setPrompt(title);
                java.awt.Dialog dialog = DialogDisplayer.getDefault().createDialog( dd );
                dialog.setVisible(true);

                if (dd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
                    userName = passwordPanel.getUsername();
                    password = passwordPanel.getTPassword();
                }
            }

            try {
                notificationService =
                        //managementClient.getNotificationService(); // DOES NOT WORK
                        //ManagementClientFactory.getInstance(mBeanServerConnection, true).getNotificationService(); // DOES NOT WORK
                        //ManagementClientFactory.getInstance("localhost", 8686, "admin", "adminadmin").getNotificationService(); // WORKS
                        ManagementClientFactory.getInstance(hostName, Integer.parseInt(rmiPortString), userName, password).getNotificationService();
            } catch (ManagementRemoteException ex) {
                // #163645 The username and password in the .nbattrs file might
                // not be valid if the user has choosen non-default username and
                // password during installation. Let's give the user another
                // chance to specify the correct username and password.
                PasswordPanel passwordPanel = new PasswordPanel(userName);

                DialogDescriptor dd = new DialogDescriptor(passwordPanel, hostName);
                java.awt.Dialog dialog = DialogDisplayer.getDefault().createDialog( dd );
                dialog.setVisible(true);

                if (dd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
                    userName = passwordPanel.getUsername();
                    password = passwordPanel.getTPassword();
                }

                notificationService =
                        ManagementClientFactory.getInstance(hostName, Integer.parseInt(rmiPortString), userName, password).getNotificationService();
            }
        }
        return notificationService;
    }

    public PerformanceMeasurementServiceWrapper
            getPerformanceMeasurementServiceWrapper()
            throws ManagementRemoteException {
        if (performanceMeasurementServiceWrapper == null) {
            performanceMeasurementServiceWrapper =
                    new PerformanceMeasurementServiceWrapperImpl(
                    managementClient.getPerformanceMeasurementService());
        }
        return performanceMeasurementServiceWrapper;
    }

    public RuntimeManagementServiceWrapper getRuntimeManagementServiceWrapper()
            throws ManagementRemoteException {
        if (runtimeManagementServiceWrapper == null) {
            runtimeManagementServiceWrapper =
                    new RuntimeManagementServiceWrapperImpl(
                    managementClient.getRuntimeManagementService());
        }
        return runtimeManagementServiceWrapper;
    }

    private void init() {

        // The MBeanServerConnection we get from NetBeans
        // (org.netbeans.modules.j2ee.sun.util.PluginRequestInterceptor)
        // doesn't provide good error message when the RPC fails.
        // The wrapped one (com.sun.enterprise.admin.jmx.remote.internal.RemoteMBeanServerConnection)
        // doesn't do the job either.
        // See ControllerUtil.java in org.netbeans.modules.j2ee.sun.ide.controllers
        // in appserverplugin.

        String netBeansUserDir = System.getProperty("netbeans.user"); // NOI18N

        try {
            if (netBeansUserDir != null) {
                ServerInstance serverInstance = null;

                String settingsFileName =
                        netBeansUserDir + ServerInstanceReader.RELATIVE_FILE_PATH;
                File settingsFile = new File(settingsFileName);

                if (!settingsFile.exists()) {
                    logger.warning("The application server definition file " + // NOI18N
                            settingsFileName + " is missing."); // NOI18N
                } else {
                    ServerInstanceReader settings =
                            new ServerInstanceReader(settingsFileName);
                    List<ServerInstance> instances = settings.getServerInstances();
                    for (ServerInstance instance : instances) {
                        if (isCurrentInstance(instance)) {
                            serverInstance = instance;
                            break;
                        }
                    }

                    // If there is no match, and there is only one instance
                    // available, then use it.
                    if (serverInstance == null) {
                        if (instances.size() == 1) {
                            logger.warning("Could not find the server instance. Use the only instance available in " + settingsFileName + "."); // NOI18N
                            serverInstance = instances.get(0);
                        }
                    }

                    // If there is no match, and there is only one remote
                    // instance available, use it.
                    if (serverInstance == null) {
                        int remoteInstances = 0;
                        for (ServerInstance instance : instances) {
                            if (!instance.getHostName().equals("localhost")) { // NOI18N
                                remoteInstances++;
                            }
                        }

                        if (remoteInstances == 1) {
                            for (ServerInstance instance : instances) {
                                if (!instance.getHostName().equals("localhost")) { // NOI18N
                                    logger.warning("Could not find the server instance. Use the only remote instance defined in " + settingsFileName + "."); // NOI18N
                                    serverInstance = instance;
                                    break;
                                }
                            }
                        }
                    }

                    if (serverInstance != null) {
                        JBIClassLoader jbiClassLoader = new JBIClassLoader(serverInstance);

                        hostName = serverInstance.getHostName();
                        port = serverInstance.getAdminPort();
                        userName = serverInstance.getUserName();
                        password = serverInstance.getPassword();

                        HTTPServerConnector httpConnector = new HTTPServerConnector(
                                hostName, port, userName, password, jbiClassLoader);

                        myMBeanServerConnection = httpConnector.getConnection();
                    }
                }
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        if (myMBeanServerConnection == null) {
            // Fall back on the mBeanServerConnection provided by NetBeans
            try {
                logger.warning("Could not find the server instance. Falling back " + // NOI18N
                        "on the mBeanServerConnection provided by NetBeans."); // NOI18N
                myMBeanServerConnection = mBeanServerConnection;
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
    }

    private boolean isCurrentInstance(ServerInstance instance) {

        boolean isLocalHost = false;
        String instanceHost = instance.getHostName();
        if (instanceHost.equals("localhost")) { // NOI18N
            instanceHost = getHostName();
            isLocalHost = true;
        }

        try {
            ObjectName objectName = new ObjectName(HOST_MBEAN_NAME);

            String host = (String) mBeanServerConnection.getAttribute(objectName, "hosts-current");  // NOI18N

            // InetAddress's getCanonicalHostName() is a best-effort method
            // and doesn't work if the name service is not available. (cordova)
            // IP address is not reliable either.
            String instanceHostCanonicalHostName = InetAddress.getByName(instanceHost).getCanonicalHostName();
            String hostCanonicalHostName = InetAddress.getByName(host).getCanonicalHostName();
            String instanceHostAddress = InetAddress.getByName(instanceHost).getHostAddress();
            String hostAddress = InetAddress.getByName(host).getHostAddress();

            logger.fine("isCurrentInstance():");
            logger.fine("    isLocalHost? " + isLocalHost);
            logger.fine("    instanceHost=" + instanceHost +
                    " CanonicalHostName=" + instanceHostCanonicalHostName +
                    " IP=" + instanceHostAddress);
            logger.fine("            host=" + host +
                    " CanonicalHostName=" + hostCanonicalHostName +
                    " IP=" + hostAddress);
//            for (InetAddress addr : InetAddress.getAllByName(host)) {
//                logger.log(Level.FINE, "                   " +  addr.getHostAddress());
//            }

            if (instanceHostCanonicalHostName.equals(hostCanonicalHostName) ||
                    instanceHostAddress.equals(hostAddress)) {
                objectName = new ObjectName(HOST_ASADMIN_MBEAN_NAME);
                String appBase = (String) mBeanServerConnection.getAttribute(objectName, "appBase");    // NOI18N

                // For local domains, use instance LOCATION instead of url location  (#90749)
                String localInstanceLocation = instance.getLocation();
                assert localInstanceLocation != null;

                localInstanceLocation = localInstanceLocation.replace('\\', '/'); // NOI18N

                if (isLocalHost) {
                    logger.fine("    localInstanceLocation=" + localInstanceLocation);
                    logger.fine("                  appBase=" + appBase);
                }

                if (!isLocalHost ||
                        appBase.toLowerCase().startsWith(localInstanceLocation.toLowerCase()) &&
                        new File(localInstanceLocation).exists()) {
                    objectName = new ObjectName(HTTP_PORT_MBEAN_NAME);
                    String port = (String) mBeanServerConnection.getAttribute(objectName, "port");  // NOI18N
                    String instanceHttpPort = instance.getHttpPortNumber();

                    logger.fine("    instanceHttpPort=" + instanceHttpPort);
                    logger.fine("                port=" + port);

                    if (port.equals(instanceHttpPort)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String getHostName() {
        String hostName = null;
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostName = localMachine.getHostName();
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        }

        return hostName;
    }
}
