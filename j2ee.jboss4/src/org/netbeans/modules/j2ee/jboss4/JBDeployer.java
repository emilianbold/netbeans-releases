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
 */
package org.netbeans.modules.j2ee.jboss4;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossWeb;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.j2ee.jboss4.nodes.Util;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.jboss4.ide.JBDeploymentStatus;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.management.MBeanServerConnection;

import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBDeployer implements ProgressObject, Runnable {
    /** timeout for waiting for URL connection */
    private static final int TIMEOUT = 60000;

    private static final int POLLING_INTERVAL = 1000;

    private static final Logger LOGGER = Logger.getLogger(JBDeployer.class.getName());

    private final JBDeploymentManager dm;

    private File file;
    private String uri;
    private JBTargetModuleID mainModuleID;

    /** Creates a new instance of JBDeployer */
    public JBDeployer(String serverUri, JBDeploymentManager dm) {
        uri = serverUri;
        this.dm = dm;
    }


    public ProgressObject deploy(Target[] target, File file, File file2, String host, int port) {
        //PENDING: distribute to all targets!
        mainModuleID = new JBTargetModuleID(target[0], file.getName());

        try {
            String server_url = "http://" + host + ":" + port;

            if (file.getName().endsWith(".war")) {
                mainModuleID.setContextURL(server_url + JbossWeb.createGraph(file2).getContextRoot());
            } else if (file.getName().endsWith(".ear")) {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                FileObject appXml = jfs.getRoot().getFileObject("META-INF/application.xml");
                if (appXml != null) {
                    Application ear = DDProvider.getDefault().getDDRoot(appXml);
                    Module[] modules = ear.getModule();
                    for (int i = 0; i < modules.length; i++) {
                        JBTargetModuleID mod_id = new JBTargetModuleID(target[0]);
                        if (modules[i].getWeb() != null) {
                            mod_id.setContextURL(server_url + modules[i].getWeb().getContextRoot());
                        }
                        mainModuleID.addChild(mod_id);
                    }
                } else {
                    LOGGER.log(Level.INFO, "Cannot find file META-INF/application.xml in " + file); // NOI18N
                }
            }

        } catch(Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        this.file = file;
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public ProgressObject redeploy (TargetModuleID module_id[], File file, File file2) {
        //PENDING: distribute all modules!
        this.file = file;
        this.mainModuleID = (JBTargetModuleID) module_id[0];
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public void run() {

        String deployDir = InstanceProperties.getInstanceProperties(uri).getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
        FileObject foIn = FileUtil.toFileObject(file);
        FileObject foDestDir = FileUtil.toFileObject(new File(deployDir));
        String fileName = file.getName();

        File toDeploy = new File(deployDir + File.separator + fileName);
        if (toDeploy.exists()) {
            toDeploy.delete();
        }

        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        String msg = NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath());
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));


        try {
            URL deploymentUrl = toDeploy.toURI().toURL();
            Long previousDeployTime = getDeploymentTime(deploymentUrl);

            FileUtil.copyFile(foIn, foDestDir, fileName); // copy version
            TargetModuleID moduleID = mainModuleID;
            String webUrl = mainModuleID.getWebURL();
            if (webUrl == null) {
                TargetModuleID[] ch = mainModuleID.getChildTargetModuleID();
                if (ch != null) {
                    for (int i = 0; i < ch.length; i++) {
                        webUrl = ch [i].getWebURL();
                        if (webUrl != null) {
                            moduleID = ch[i];
                            break;
                        }
                    }
                }

            }
            if (webUrl != null) {
                URL url = new URL(webUrl);
                String waitingMsg = NbBundle.getMessage(JBDeployer.class, "MSG_Waiting_For_Url", url);
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));

                //wait until the url becomes active
                boolean ready = waitForUrlReady(moduleID, deploymentUrl, previousDeployTime, TIMEOUT);
                if (!ready) {
                    LOGGER.log(Level.INFO, "URL wait timeouted after " + TIMEOUT); // NOI18N
                }
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (MissingResourceException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, null, ex);
            // allow the thread to exit
        }

        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "Applicaton Deployed"));
    }

    /**
     * Waits until the url is ready. As a first attemp tries to ask the
     * deployer whether the application with given deploymentUrl is already
     * started. As a fallback it asks the jboss for the MBean of the
     * warfile (name of the war is expected to be <code>moduleID.getModuleID()</code>).
     */
    private boolean waitForUrlReady(TargetModuleID moduleID, URL deploymentUrl,
            Long previousDeploymentTime, long timeout) throws InterruptedException {

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted on wait enter"); // NOI18N
        }

        for (int i = 0, limit = (int) timeout / POLLING_INTERVAL;
                i < limit && !isApplicationReady(deploymentUrl, moduleID.getModuleID(), previousDeploymentTime, i == 0); i++) {

            Thread.sleep(POLLING_INTERVAL);
        }

        return isApplicationReady(deploymentUrl, moduleID.getModuleID(), previousDeploymentTime, false);
    }

    private Long getDeploymentTime(URL deploymentUrl) {
        assert deploymentUrl != null;

        try {
            Object info = dm.invokeMBeanOperation(new ObjectName("jboss.system:service=MainDeployer"), // NOI18N
                            "getDeployment", new Object[] {deploymentUrl}, new String[] {"java.net.URL"}); // NOI18N
            if (info == null) {
                return Long.MIN_VALUE;
            }

            Class infoClass = info.getClass();
            return infoClass.getDeclaredField("lastDeployed").getLong(info); // NOI18N
        } catch (Exception ex) {
            // pass through, return MIN_VALUE
            LOGGER.log(Level.FINE, null, ex);
        }
        return null;
    }

    private boolean isApplicationReady(URL deploymentUrl, String warName, Long previouslyDeployed,
            boolean initial) throws InterruptedException {

        assert deploymentUrl != null;
        assert warName != null;

        if (initial && previouslyDeployed == null) {
            // safety wait - avoids hitting previous content
            Thread.sleep(2000);
        }

        // Try JMX deployer first.
        try {
            Object info = dm.invokeMBeanOperation(new ObjectName("jboss.system:service=MainDeployer"), // NOI18N
                            "getDeployment", new Object[] {deploymentUrl} , new String[] {"java.net.URL"}); //NOI18N
            if (info != null) {
                Class infoClass = info.getClass();
                long lastDeployed = infoClass.getDeclaredField("lastDeployed").getLong(info); // NOI18N
                Object state = infoClass.getDeclaredField("state").get(info); // NOI18N
                Object requiredState = state.getClass().getDeclaredField("STARTED").get(null); // NOI18N
                return requiredState.equals(state)
                        && (previouslyDeployed == null || previouslyDeployed.longValue() != lastDeployed);
            }
        } catch (Exception ex) {
            // pass through, try the old way
            LOGGER.log(Level.FINE, null, ex);
        }

        // We will try the old way.
        try {
            ObjectName searchPattern = new ObjectName("jboss.web.deployment:war=" + warName + ",*"); // NOI18N
            MBeanServerConnection server = Util.getRMIServer(dm);
            return !server.queryMBeans(searchPattern, null).isEmpty();
        } catch (Exception ex) {
            // pass through, try the old way
            LOGGER.log(Level.INFO, null, ex);
        }

        return false;
    }

    // ----------  Implementation of ProgressObject interface
    private List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

    private DeploymentStatus deploymentStatus;

    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }

    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Stop is not supported"); // NOI18N
    }

    public boolean isStopSupported() {
        return false;
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Cancel is not supported"); // NOI18N
    }

    public boolean isCancelSupported() {
        return false;
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }

    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[] {mainModuleID};
    }

    public DeploymentStatus getDeploymentStatus() {
        synchronized (this) {
            return deploymentStatus;
        }
    }

    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);

        synchronized (this) {
            this.deploymentStatus = deploymentStatus;
        }

        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(evt);
        }
    }

}



