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
package org.netbeans.modules.compapp.jbiserver;

import java.awt.Dialog;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import org.netbeans.modules.compapp.projects.jbi.JbiActionProvider;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.NoSelectedServerWarning;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.sun.manager.jbi.management.JBIClassLoader;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class JbiManager {

    public static final String URL_ATTR = "url"; // NOI18N
    public static final String HOSTNAME_ATTR = "hostname"; // NOI18N
    public static final String PORT_ATTR = "port"; // NOI18N
    public static final String USERNAME_ATTR = "username"; // NOI18N
    public static final String PASSWORD_ATTR = "password"; // NOI18N
    /** Mapping from ServerInstance to JbiClassLoader instances. */
    private static Map<String, JBIClassLoader> loaderMap;

    public static JBIClassLoader getJBIClassLoader(String serverInstance) {
        if (loaderMap == null) {
            loaderMap = new HashMap<String, JBIClassLoader>();
        }

        JBIClassLoader loader = loaderMap.get(serverInstance);
        if (loader == null) {
            try {
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstance);

                if (isAppServer(platform)) {

                    loader = new JBIClassLoader(new Empty().getClass().getClassLoader());

                    File[] roots = platform.getPlatformRoots();
                    for (int i = 0; i < roots.length; i++) {
                        File root = roots[i];
                        String rootPath = root.getAbsolutePath();

                        File f = new File(rootPath + "/lib/appserv-rt.jar");    // NOI18N
                        if (f.exists()) {
                            loader.addURL(f);
                            break;
                        }
                    }

                    loaderMap.put(serverInstance, loader);
                }
            } catch (Exception ex) {
                loader = null;
                ex.printStackTrace(System.out);
            }
        }
        return loader;
    }

    public static boolean isRunningAppServer(JbiProject project) {
        if (project == null) {
            return false;
        }

        JbiProjectProperties projectProperties = project.getProjectProperties();
        String serverInstance = (String) projectProperties.get(JbiProjectProperties.J2EE_SERVER_INSTANCE);
        return isRunningAppServer(serverInstance);
    }

    public static boolean isRunningAppServer(String serverInstance) {
        if (serverInstance == null) {
            return false;
        }

        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstance);

        if (!isAppServer(platform)) {
            return false;
        }

        // Can't get the ServerInstance directly from the platform! Get the info from layer.

        Properties properties = getServerInstanceProperties(serverInstance);

        String url = properties.getProperty(URL_ATTR);
        String userName = properties.getProperty(USERNAME_ATTR);
        String password = properties.getProperty(PASSWORD_ATTR);

        String plugin = Deployment.getDefault().getServerID(serverInstance);

        if (url != null && userName != null && password != null && plugin != null) {
            StartServer startServer = getStartServer(plugin, url, userName, password);
            if (startServer != null) {
                return startServer.isRunning();
            }
        }
        return false;
    }

    /**
     * Starts the given App Server if it is not running. Optionally, blocks
     * the current thread until the server is ready.
     * <p>
     * This should not be called from the Event Dispatch Thread.
     * 
     * @param serverInstanceID  server instance ID
     * @param block     if <code>true</code>, block the current thread 
     *                  until the server is ready; <code>false</code> otherwise.
     */
    public static void startServer(JbiProject project, boolean block) {        
        JbiProjectProperties projectProperties =
                project.getProjectProperties();
        String serverInstanceID = (String) projectProperties.get(
                JbiProjectProperties.J2EE_SERVER_INSTANCE);
        assert serverInstanceID != null;
        startServer(serverInstanceID, block);
    }

    /**
     * Starts the given App Server if it is not running. Optionally, blocks
     * the current thread until the server is ready.      
     * <p>
     * This should not be called from the Event Dispatch Thread.
     * 
     * @param serverInstanceID  server instance ID
     * @param block     if <code>true</code>, block the current thread 
     *                  until the server is ready; <code>false</code> otherwise.
     */
    public static void startServer(String serverInstanceID, boolean block) {
        
        assert !SwingUtilities.isEventDispatchThread() : 
            "This should not be called from the EDT."; // NOI18N
                
        if (isRunningAppServer(serverInstanceID)) {
            return;
        }

        org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = null;

        try {
            inst = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        } catch (Exception e) {
            // NPE from command line deployment.
            // Fine. Not supporting auto server start if deployed from command line.
            return;
        }

        if (inst == null) {
            System.out.println("Bad target server ID: " + serverInstanceID);
            return;
        }

        ServerString server = new ServerString(inst);

        org.netbeans.modules.j2ee.deployment.impl.ServerInstance serverInstance =
                server.getServerInstance();
        if (server == null || serverInstance == null) {
            System.out.println("Make sure a target server is set in project properties.");
        }

        // Currently it is not possible to select target to which modules will
        // be deployed. Lets use the first one.
        // (This will start the server if the server is not running.)
        ServerTarget targets[] = serverInstance.getTargets();

        if (block) {
            while (!isRunningAppServer(serverInstanceID)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            // Wait some additional time for the JBI framework to get ready.
            // This is trying to make sure when the app server is started 
            // on demand when deploying a CompApp project, the state of the   
            // old SA on the JBI runtime is correct. (#131236)
            try {
                Thread.sleep(5000); 
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static boolean isSelectedServer(JbiProject project) {
        
        AntProjectHelper antProjectHelper = project.getAntProjectHelper();
        String instance = antProjectHelper.getStandardPropertyEvaluator().
                getProperty(JbiProjectProperties.J2EE_SERVER_INSTANCE);

        if ((instance == null) || !JbiManager.isAppServer(instance)) {
            String[] serverIDs = JbiManager.getAppServers();

            if (serverIDs.length < 1) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(JbiActionProvider.class,
                        "MSG_NoInstalledServerError"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return false;
            }

            NoSelectedServerWarning panel = new NoSelectedServerWarning(serverIDs);

            Object[] options = new Object[]{
                DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(NoSelectedServerWarning.class,
                    "CTL_NoSelectedServerWarning_Title"), // NOI18N 
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);

            if (desc.getValue() != options[0]) {
                // cancel
                return false;
            } else {
                instance = panel.getSelectedInstance();
                if (instance != null) {
                    JbiProjectProperties projectProperties = project.getProjectProperties();
                    projectProperties.put(JbiProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    projectProperties.store();
                }
            }

            dlg.dispose();
        }

        if (instance == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(JbiActionProvider.class,
                    "MSG_NoSelectedServerError"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
//        } else if (!JbiManager.isRunningAppServer(instance)) {
//            NotifyDescriptor d =
//                    new NotifyDescriptor.Message(
//                    NbBundle.getMessage(
//                    JbiActionProvider.class, "MSG_NoRunningServerError" // NOI18N
//                    ),
//                    NotifyDescriptor.ERROR_MESSAGE);
//            DialogDisplayer.getDefault().notify(d);
//            return false;
        }

        return true;
    }

    public static Properties getServerInstanceProperties(String serverInstance) {
        if (serverInstance == null) {
            return null;
        }

        Properties properties = new Properties();

        FileSystem defaultFileSystem = Repository.getDefault().getDefaultFileSystem();
        FileObject dir = defaultFileSystem.findResource("/J2EE/InstalledServers");  // NOI18N
        FileObject[] ch = dir.getChildren();
        String plugin = Deployment.getDefault().getServerID(serverInstance);
        for (int i = 0; i < ch.length; i++) {
            FileObject file = ch[i];
            String url = (String) ch[i].getAttribute(URL_ATTR);
            if (url != null && url.equals(serverInstance)) {
                String userName = (String) file.getAttribute(USERNAME_ATTR);
                String password = (String) file.getAttribute(PASSWORD_ATTR);

                String tmp = url.substring(url.lastIndexOf("::") + 2);  // NOI18N
                String hostName = tmp.substring(0, tmp.indexOf(":"));   // NOI18N
                String port = tmp.substring(tmp.indexOf(":") + 1);  // NOI18N

                properties.put(URL_ATTR, url);
                properties.put(HOSTNAME_ATTR, hostName);
                properties.put(PORT_ATTR, port);
                properties.put(USERNAME_ATTR, userName);
                properties.put(PASSWORD_ATTR, password);

                break;
            }
        }
        return properties;
    }

    private static StartServer getStartServer(String plugin,
            String url, String userName, String password) {
        try {
            FileSystem defaultFileSystem = Repository.getDefault().getDefaultFileSystem();
            FileObject file = defaultFileSystem.findResource(
                    "/J2EE/DeploymentPlugins/" + plugin + "/Factory.instance"); // NOI18N

            DataObject dob = DataObject.find(file);

            InstanceCookie cookie = (InstanceCookie) dob.getCookie(InstanceCookie.class);

            DeploymentFactory deploymentFactory =
                    (DeploymentFactory) cookie.instanceCreate();
            DeploymentManager deploymentManager =
                    deploymentFactory.getDeploymentManager(url, userName, password);


            file = defaultFileSystem.findResource(
                    "J2EE/DeploymentPlugins/" + plugin + "/OptionalFactory.instance");  // NOI18N

            dob = DataObject.find(file);

            cookie = (InstanceCookie) dob.getCookie(InstanceCookie.class);

            OptionalDeploymentManagerFactory deploymentManagerFactory =
                    (OptionalDeploymentManagerFactory) cookie.instanceCreate();

            StartServer startServer = deploymentManagerFactory.getStartServer(deploymentManager);

            return startServer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isGlassFish(File candidate) {
        //now test for AS 9 (J2EE 5.0) which should work for this plugin
        File as9 = new File(candidate.getAbsolutePath() +
                "/lib/dtds/sun-web-app_2_5-0.dtd");  // NOI18N
        return as9.exists();
    }

    public static boolean appServerInstalled() {
        Deployment deployment = Deployment.getDefault();
        String[] serverInstanceIDs = deployment.getServerInstanceIDs();
        //String[] serverNames = new String[serverInstanceIDs.length];
        //String[] serverURLs = new String[serverInstanceIDs.length];
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            J2eePlatform platform = deployment.getJ2eePlatform(serverInstanceIDs[i]);
            if (platform != null) {
                if (isAppServer(platform)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String[] getAppServers() {
        Deployment deployment = Deployment.getDefault();
        String[] serverInstanceIDs = deployment.getServerInstanceIDs();
        ArrayList arr = new ArrayList();
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            // This is slow if the server instance is remote!
            J2eePlatform platform = deployment.getJ2eePlatform(serverInstanceIDs[i]);
            if (platform != null) {
                if (isAppServer(platform)) {
                    arr.add(serverInstanceIDs[i]);
                }
            }
        }
        return (String[]) arr.toArray(new String[arr.size()]);
    }

    public static boolean isAppServer(String id) {
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(id);
        // On Windows, the path should be case insensitive. 
        // An issue has been filed against j2eeserver at 
        // http://www.netbeans.org/issues/show_bug.cgi?id=84063
        if (platform != null) {
            return isAppServer(platform);
        }
        return false;
    }

    static boolean isAppServer(J2eePlatform platform) {
        File[] cps = platform.getClasspathEntries();
        for (int j = 0; j < cps.length; j++) {
            String path = cps[j].getPath();
            if ((path.indexOf("javaee.jar") > 0) || // NOI18N
                    (path.indexOf("j2ee.jar") > 0)) { // NOI18N
                return true;
            }
        }
        return false;
    }

    /**
     * Used to get the netbeans classload of this class.
     *
     */
    static class Empty {
        //  empty...
    }
}
