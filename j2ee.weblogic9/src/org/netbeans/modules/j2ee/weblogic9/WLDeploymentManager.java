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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.util.WLDebug;
import org.netbeans.modules.j2ee.weblogic9.util.WLOutputManager;
import org.netbeans.modules.j2ee.weblogic9.util.WLTailer;
import org.openide.util.NbBundle;


/**
 * Main class of the deployment process. This serves as a wrapper for the
 * server's DeploymentManager implementation, all calls are delegated to the
 * server's implementation, with the thread's context classloader updated
 * if necessary.
 *
 * @author Kirill Sorokin
 */
public class WLDeploymentManager implements DeploymentManager {

    private final WLDeploymentFactory factory;

    private DeploymentManager vendorDeploymentManager;

    private InstanceProperties instanceProperties;
    private String uri;
    private String username;
    private String password;
    private boolean isConnected;
    private String host;
    private String port;

    /** System process of the started WL server */
    private Process process;
    
    private WLOutputManager outputManager;

    /** Create connected DM */
    public WLDeploymentManager(WLDeploymentFactory factory, String uri,
            String username, String password, String host, String port) {
        this(factory, uri, username, password, host, port, true);
    }

    /** Create disconnected DM */
    public WLDeploymentManager(WLDeploymentFactory factory, String uri,
            String host, String port) {
        this(factory, uri, null, null, host, port, false);
    }

    protected WLDeploymentManager(WLDeploymentFactory factory, String uri,
            String username, String password, String host, String port, boolean isConnected) {
        this.factory = factory;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.isConnected = isConnected;
    }

    private DeploymentManager getDeploymentManager() throws DeploymentManagerCreationException {
        synchronized (factory) {
            if (vendorDeploymentManager == null) {
                if (isConnected) {
                    vendorDeploymentManager = factory.getVendorDeploymentManager(uri, username, password, host, port);
                } else {
                    vendorDeploymentManager = factory.getVendorDisconnectedDeploymentManager(uri);
                }

            }
            return vendorDeploymentManager;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Connection data methods
    ////////////////////////////////////////////////////////////////////////////

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Returns the stored server URI
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Returns the server host stored in the instance properties
     */
    public String getHost() {
        return host;
    }

    public String getUsername () {
        return getInstanceProperties().getProperty(InstanceProperties.USERNAME_ATTR);
    }

    public String getPassword () {
        return getInstanceProperties().getProperty(InstanceProperties.PASSWORD_ATTR);
    }

    /**
     * Returns the server port stored in the instance properties
     */
    public String getPort() {
        return port;
    }

    public boolean isLocal () {
        return new Boolean(getInstanceProperties().getProperty(WLPluginProperties.IS_LOCAL_ATTR)).booleanValue();
    }
    /**
     * Returns the InstanceProperties object for the current server instance
     */
    public InstanceProperties getInstanceProperties() {
        if (instanceProperties == null) {
            this.instanceProperties = InstanceProperties.getInstanceProperties(uri);

        }
        return instanceProperties;
    }

    /**
     * Set the <code>Process</code> of the started WL server.
     *
     * @param <code>Process</code> of the started WL server.
     */
    public synchronized void setServerProcess(Process process) {
        this.process = process;
    }

    /**
     * Return <code>Process</code> of the started WL server.
     *
     * @return <code>Process</code> of the started WL server, <code>null</code> if
     *         WL wasn't started by IDE.
     */
    public synchronized Process getServerProcess() {
        return process;
    }

    public WLOutputManager getOutputManager() {
        return outputManager;
    }

    public void setOutputManager(WLOutputManager outputManager) {
        this.outputManager = outputManager;
    }

    public ProgressObject distribute(Target[] target, File file, File file2)
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify(getClass(), "distribute(" + target + ", " + // NOI18N
                    file + ", " + file2 + ")");                        // NOI18N

        if (isLocal()) {
            //autodeployment version
            return new WLDeployer(uri).deploy(target, file, file2, getHost(), getPort());
        } else {
            //weblogic jsr88 version
            modifiedLoader();
            try {
                return new DelegatingProgressObject(getDeploymentManager().distribute(target, file, file2));
            } catch (DeploymentManagerCreationException ex) {
                return new FailedProgressObject(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                        NbBundle.getMessage(WLDeploymentManager.class, "MSG_Deployment_Failed"));
            } finally {
                originalLoader();
            }
        }
    }

    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }

    private ClassLoader swapLoader;

    private void modifiedLoader() {
        swapLoader = Thread.currentThread().getContextClassLoader();
        String serverRoot = getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        // if serverRoot is null, then we are in a server instance registration process, thus this call
        // is made from InstanceProperties creation -> WLPluginProperties singleton contains
        // install location of the instance being registered
        if (serverRoot == null)
            serverRoot = WLPluginProperties.getInstance().getInstallLocation();

        Thread.currentThread().setContextClassLoader(WLDeploymentFactory.getWLClassLoader(serverRoot));
    }
    private void originalLoader() {
        Thread.currentThread().setContextClassLoader(swapLoader);
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     *
     * @return a wrapper for the server's DeploymentConfiguration implementation
     */
    public DeploymentConfiguration createConfiguration(
        DeployableObject deployableObject) throws InvalidModuleException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject redeploy(TargetModuleID[] targetModuleID,
            InputStream inputStream, InputStream inputStream2)
            throws UnsupportedOperationException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("redeploy(" + targetModuleID + ", " +       // NOI18N
                    inputStream + ", " + inputStream2 + ")");          // NOI18N
        modifiedLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().redeploy(targetModuleID, inputStream, inputStream2));
        } catch (DeploymentManagerCreationException ex) {
            return new FailedProgressObject(ActionType.EXECUTE, CommandType.REDEPLOY,
                    NbBundle.getMessage(WLDeploymentManager.class, "MSG_Redeployment_Failed"));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject distribute(Target[] target, InputStream inputStream,
            InputStream inputStream2) throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("distribute(" + target + ", " +             // NOI18N
                    inputStream + ", " + inputStream2 + ")");          // NOI18N
        modifiedLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().distribute(target, inputStream, inputStream2));
        } catch (DeploymentManagerCreationException ex) {
            return new FailedProgressObject(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                    NbBundle.getMessage(WLDeploymentManager.class, "MSG_Deployment_Failed"));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject undeploy(TargetModuleID[] targetModuleID)
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("undeploy(" + targetModuleID + ")");        // NOI18N
        modifiedLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().undeploy(targetModuleID));
        } catch(DeploymentManagerCreationException ex) {
            return new FailedProgressObject(ActionType.EXECUTE, CommandType.UNDEPLOY,
                    NbBundle.getMessage(WLDeploymentManager.class, "MSG_Undeployment_Failed"));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject stop(TargetModuleID[] targetModuleID)
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("stop(" + targetModuleID + ")");            // NOI18N

        modifiedLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().stop(targetModuleID));
        } catch (DeploymentManagerCreationException ex) {
            return new FailedProgressObject(ActionType.EXECUTE, CommandType.STOP,
                    NbBundle.getMessage(WLDeploymentManager.class, "MSG_Application_Stop_Failed"));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject start(TargetModuleID[] targetModuleID)
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("start(" + targetModuleID + ")");           // NOI18N

        modifiedLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().start(targetModuleID));
        } catch (DeploymentManagerCreationException ex) {
            return new FailedProgressObject(ActionType.EXECUTE, CommandType.START,
                    NbBundle.getMessage(WLDeploymentManager.class, "MSG_Application_Start_Failed"));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getAvailableModules(ModuleType moduleType,
            Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getAvailableModules(" + moduleType +       // NOI18N
                    ", " + target + ")");                              // NOI18N

        modifiedLoader();
        try {
            TargetModuleID t[] = getDeploymentManager().getAvailableModules(moduleType, target);
            return t;
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType,
            Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getNonRunningModules(" + moduleType +      // NOI18N
                    ", " + target + ")");                              // NOI18N

        modifiedLoader();
        try {
            TargetModuleID t[] = getDeploymentManager().getNonRunningModules(moduleType, target);
            for (int i=0; i < t.length; i++) {
                System.out.println("non running module:" + t[i]);
            }
            return t;
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getRunningModules(ModuleType moduleType,
            Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getRunningModules(" + moduleType +         // NOI18N
                    ", " + target + ")");                              // NOI18N

        modifiedLoader();
        try {
            TargetModuleID t[] = getDeploymentManager().getRunningModules(moduleType, target);
            for (int i=0; i < t.length; i++) {
                System.out.println("running module:" + t[i]);
            }
            return t;
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file,
            File file2) throws UnsupportedOperationException,
            IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("redeploy(" + targetModuleID + ", " +       // NOI18N
                    file + ", " + file2 + ")");                        // NOI18N

        modifiedLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().redeploy(targetModuleID, file, file2));
        } catch (DeploymentManagerCreationException ex) {
            return new FailedProgressObject(ActionType.EXECUTE, CommandType.REDEPLOY,
                    NbBundle.getMessage(WLDeploymentManager.class, "MSG_Redeployment_Failed"));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void setDConfigBeanVersion(
            DConfigBeanVersionType dConfigBeanVersionType)
            throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isDConfigBeanVersionSupported(
            DConfigBeanVersionType dConfigBeanVersionType) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void release() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("release()");                               // NOI18N

        modifiedLoader();
        try {
            synchronized (factory) {
                if (vendorDeploymentManager != null) {
                    // delegate the call and clear the stored deployment manager
                    try {
                        vendorDeploymentManager.release();
                    }
                    catch (Exception e) {
                        Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
                    }
                    finally {
                        vendorDeploymentManager = null;
                    }
                }
            }
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isRedeploySupported() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("isRedeploySupported()");                   // NOI18N

        return false;
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Target[] getTargets() throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getTargets()");                            // NOI18N

        modifiedLoader();
        try {
            return getDeploymentManager().getTargets();
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader();
        }
    }

    private static final class FailedProgressObject implements ProgressObject {


        private final DeploymentStatus status;

        public FailedProgressObject(final ActionType action, final CommandType command,
                final String message) {

            status = new DeploymentStatus() {

                public ActionType getAction() {
                    return action;
                }

                public CommandType getCommand() {
                    return command;
                }

                public String getMessage() {
                    return message;
                }

                public StateType getState() {
                    return StateType.FAILED;
                }

                public boolean isCompleted() {
                    return false;
                }

                public boolean isFailed() {
                    return true;
                }

                public boolean isRunning() {
                    return false;
                }

            };
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        public DeploymentStatus getDeploymentStatus() {
            return status;
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return new TargetModuleID[0];
        }

        public boolean isCancelSupported() {
            return false;
        }

        public boolean isStopSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        public void stop() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        public void addProgressListener(ProgressListener arg0) {
            //nothing - object is in final state when constructed
        }

        public void removeProgressListener(ProgressListener arg0) {
            //nothing - object is in final state when constructed
        }

    }

    private static class DelegatingProgressObject implements ProgressObject, ProgressListener {

        private final ProgressObject original;

        private Vector listeners = new Vector();

        public DelegatingProgressObject(ProgressObject original) {
            this.original = original;
            original.addProgressListener(this);
        }

        public DeploymentStatus getDeploymentStatus() {
            return original.getDeploymentStatus();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return original.getResultTargetModuleIDs();
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
            return getClientConfiguration(targetModuleID);
        }

        public boolean isCancelSupported() {
            return original.isCancelSupported();
        }

        public void cancel() throws OperationUnsupportedException {
            original.cancel();
        }

        public boolean isStopSupported() {
            return original.isStopSupported();
        }

        public void stop() throws OperationUnsupportedException {
            original.stop();
        }

        public void addProgressListener(ProgressListener progressListener) {
            listeners.add(progressListener);
        }

        public void removeProgressListener(ProgressListener progressListener) {
            listeners.remove(progressListener);
        }

        public void handleProgressEvent(ProgressEvent progressEvent) {
            java.util.Vector targets = null;
            synchronized (this) {
                if (listeners != null) {
                    targets = (java.util.Vector) listeners.clone();
                }
            }

            if (targets != null) {
                for (int i = 0; i < targets.size(); i++) {
                    ProgressListener target = (ProgressListener)targets.elementAt(i);
                    target.handleProgressEvent(progressEvent);
                }
            }
        }

    }
}