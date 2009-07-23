/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.olddeploy;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.netbeans.modules.j2ee.weblogic9.WLBaseDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLTargetModuleID;
import org.openide.util.NbBundle;

/**
 *
 * @author petrjiricka
 */
public class WLOldDeploymentManager extends WLBaseDeploymentManager{
    private String username;
    private String password;
    private boolean isConnected;

    private static final Logger LOGGER = Logger.getLogger(WLOldDeploymentManager.class.getName());

    /** Create connected DM */
    public WLOldDeploymentManager(WLDeploymentFactory factory, String uri,
            String username, String password, String host, String port) {
        this(factory, uri, username, password, host, port, true);
    }

    /** Create disconnected DM */
    public WLOldDeploymentManager(WLDeploymentFactory factory, String uri,
            String host, String port) {
        this(factory, uri, null, null, host, port, false);
    }

    private WLOldDeploymentManager(WLDeploymentFactory factory, String uri,
            String username, String password, String host, String port, boolean isConnected) {
        super(factory, uri, host, port);
        this.username = username;
        this.password = password;
        this.isConnected = isConnected;
    }

    private static DeploymentManager getVendorDeploymentManager(String uri, String username, String password, String host, String port) throws DeploymentManagerCreationException {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDM, uri:" + uri + " username:" + username + " password:" + password + " host:" + host + " port:" + port);
        }
        DeploymentManagerCreationException dmce = null;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            String serverRoot = InstanceProperties.getInstanceProperties(uri).getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            // if serverRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> WLPluginProperties singleton contains
            // install location of the instance being registered
            if (serverRoot == null) {
                serverRoot = WLPluginProperties.getInstance().getInstallLocation();
            }
            ClassLoader loader = getWLClassLoader(serverRoot);
            Thread.currentThread().setContextClassLoader(loader);
            Class helperClazz = Class.forName("weblogic.deploy.api.tools.SessionHelper", false, loader);
            //NOI18N
            Method m = helperClazz.getDeclaredMethod("getDeploymentManager", new Class[]{String.class, String.class, String.class, String.class});
            // NOI18N
            Object o = m.invoke(null, new Object[]{host, port, username, password});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                return (DeploymentManager) o;
            } else {
                dmce = new DeploymentManagerCreationException("Instance created by weblogic is not DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException("Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(e);
        } catch (NoClassDefFoundError err) {
            dmce = new DeploymentManagerCreationException("Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(err);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
        throw dmce;
    }

    private static DeploymentManager getVendorDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        DeploymentManagerCreationException dmce = null;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            String serverRoot = InstanceProperties.getInstanceProperties(uri).getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            // if serverRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> WLPluginProperties singleton contains
            // install location of the instance being registered
            if (serverRoot == null) {
                serverRoot = WLPluginProperties.getInstance().getInstallLocation();
            }
            ClassLoader loader = getWLClassLoader(serverRoot);
            Thread.currentThread().setContextClassLoader(loader);
            Class helperClazz = Class.forName("weblogic.deploy.api.tools.SessionHelper", false, loader);
            //NOI18N
            Method m = helperClazz.getDeclaredMethod("getDisconnectedDeploymentManager", new Class[]{});
            // NOI18N
            Object o = m.invoke(null, new Object[]{});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                return (DeploymentManager) o;
            } else {
                dmce = new DeploymentManagerCreationException("Instance created by weblogic is not disconnected DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException("Cannot create weblogic disconnected DeploymentManager instance.");
            dmce.initCause(e);
        } catch (NoClassDefFoundError err) {
            dmce = new DeploymentManagerCreationException("Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(err);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
        throw dmce;
    }
    private DeploymentManager vendorDeploymentManager;

    private DeploymentManager getDeploymentManager() throws DeploymentManagerCreationException {
        synchronized (factory) {
            if (vendorDeploymentManager == null) {
                if (isConnected) {
                    vendorDeploymentManager = new SafeDeploymentManager(getVendorDeploymentManager(getURI(), username, password, getHost(), getPort()));
                } else {
                    vendorDeploymentManager = new SafeDeploymentManager(getVendorDisconnectedDeploymentManager(getURI()));
                }
            }
            return vendorDeploymentManager;
        }
    }

    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        //autodeployment
        return new WLDeployer(getURI()).deploy(target, file, file2, getHost(), getPort());
    }

    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().redeploy(targetModuleID, inputStream, inputStream2));
        } catch (DeploymentManagerCreationException ex) {
            return new FinishedProgressObject(ActionType.EXECUTE, CommandType.REDEPLOY, NbBundle.getMessage(WLOldDeploymentManager.class, "MSG_Redeployment_Failed"), null, true);
        } finally {
            originalLoader(original);
        }
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().distribute(target, inputStream, inputStream2));
        } catch (DeploymentManagerCreationException ex) {
            return new FinishedProgressObject(ActionType.EXECUTE, CommandType.DISTRIBUTE, NbBundle.getMessage(WLOldDeploymentManager.class, "MSG_Deployment_Failed"), null, true);
        } finally {
            originalLoader(original);
        }
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().undeploy(targetModuleID));
        } catch (DeploymentManagerCreationException ex) {
            return new FinishedProgressObject(ActionType.EXECUTE, CommandType.UNDEPLOY, NbBundle.getMessage(WLOldDeploymentManager.class, "MSG_Undeployment_Failed"), null, true);
        } finally {
            originalLoader(original);
        }
    }

    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().stop(targetModuleID));
        } catch (DeploymentManagerCreationException ex) {
            return new FinishedProgressObject(ActionType.EXECUTE, CommandType.STOP, NbBundle.getMessage(WLOldDeploymentManager.class, "MSG_Application_Stop_Failed"), null, true);
        } finally {
            originalLoader(original);
        }
    }

    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        TargetModuleID[] serverIds = getServerTargetModuleIds(targetModuleID);
        if (serverIds.length == 0) {
            //can't do anything with autodeployed apps
            return new FinishedProgressObject(ActionType.EXECUTE, CommandType.START, NbBundle.getMessage(WLOldDeploymentManager.class, "MSG_Application_Started_Already"), targetModuleID, false);
        }
        ClassLoader original = modifyLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().start(serverIds));
        } catch (DeploymentManagerCreationException ex) {
            return new FinishedProgressObject(ActionType.EXECUTE, CommandType.START, NbBundle.getMessage(WLOldDeploymentManager.class, "MSG_Application_Start_Failed"), null, true);
        } finally {
            originalLoader(original);
        }
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            TargetModuleID[] t = getDeploymentManager().getAvailableModules(moduleType, target);
            return t;
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader(original);
        }
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            TargetModuleID[] t = getDeploymentManager().getNonRunningModules(moduleType, target);
            return t;
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader(original);
        }
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            TargetModuleID[] t = getDeploymentManager().getRunningModules(moduleType, target);
            return t;
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader(original);
        }
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            return new DelegatingProgressObject(getDeploymentManager().redeploy(targetModuleID, file, file2));
        } catch (DeploymentManagerCreationException ex) {
            return new FinishedProgressObject(ActionType.EXECUTE, CommandType.REDEPLOY, NbBundle.getMessage(WLOldDeploymentManager.class, "MSG_Redeployment_Failed"), null, true);
        } finally {
            originalLoader(original);
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isRedeploySupported() {
        return false;
    }

    private TargetModuleID[] getServerTargetModuleIds(TargetModuleID[] modules) {
        List<TargetModuleID> serverIds = new LinkedList<TargetModuleID>();
        for (TargetModuleID module : modules) {
            if (module instanceof WLTargetModuleID) {
                continue;
            }
            serverIds.add(module);
        }
        return serverIds.toArray(new TargetModuleID[serverIds.size()]);
    }

    public void release() {
        ClassLoader original = modifyLoader();
        try {
            synchronized (factory) {
                if (vendorDeploymentManager != null) {
                    // delegate the call and clear the stored deployment manager
                    try {
                        vendorDeploymentManager.release();
                    } catch (Exception e) {
                        Logger.getLogger("global").log(Level.INFO, null, e);
                    } finally {
                        vendorDeploymentManager = null;
                    }
                }
            }
        } finally {
            originalLoader(original);
        }
    }

    public Target[] getTargets() throws IllegalStateException {
        ClassLoader original = modifyLoader();
        try {
            return getDeploymentManager().getTargets();
        } catch (DeploymentManagerCreationException ex) {
            throw new IllegalStateException(ex);
        } finally {
            originalLoader(original);
        }
    }

    private static final class FinishedProgressObject implements ProgressObject {

        private final TargetModuleID[] moduleIds;
        private final DeploymentStatus status;

        public FinishedProgressObject(final ActionType action, final CommandType command, final String message, final TargetModuleID[] moduleIds, final boolean failed) {
            this.moduleIds = moduleIds == null ? new TargetModuleID[0] : moduleIds;
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
                    return failed ? StateType.FAILED : StateType.COMPLETED;
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
            throw new UnsupportedOperationException("Not supported.");
        }

        public DeploymentStatus getDeploymentStatus() {
            return status;
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return moduleIds;
        }

        public boolean isCancelSupported() {
            return false;
        }

        public boolean isStopSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void stop() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void addProgressListener(ProgressListener arg0) {
        }

        public void removeProgressListener(ProgressListener arg0) {
        }
    }

    private static class DelegatingProgressObject implements ProgressObject, ProgressListener {

        private final ProgressObject original;
        private List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

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
            return original.getClientConfiguration(targetModuleID);
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
            for (ProgressListener target : listeners) {
                target.handleProgressEvent(progressEvent);
            }
        }
    }

    private static final class SafeDeploymentManager implements DeploymentManager {

        private final DeploymentManager delegate;

        public SafeDeploymentManager(DeploymentManager delegate) {
            this.delegate = delegate;
        }

        public synchronized ProgressObject undeploy(TargetModuleID[] arg0) throws IllegalStateException {
            return delegate.undeploy(arg0);
        }

        public synchronized ProgressObject stop(TargetModuleID[] arg0) throws IllegalStateException {
            return delegate.stop(arg0);
        }

        public synchronized ProgressObject start(TargetModuleID[] arg0) throws IllegalStateException {
            return delegate.start(arg0);
        }

        public synchronized void setLocale(Locale arg0) throws UnsupportedOperationException {
            delegate.setLocale(arg0);
        }

        public synchronized void setDConfigBeanVersion(DConfigBeanVersionType arg0) throws DConfigBeanVersionUnsupportedException {
            delegate.setDConfigBeanVersion(arg0);
        }

        public synchronized void release() {
            delegate.release();
        }

        public synchronized ProgressObject redeploy(TargetModuleID[] arg0, InputStream arg1, InputStream arg2) throws UnsupportedOperationException, IllegalStateException {
            return delegate.redeploy(arg0, arg1, arg2);
        }

        public synchronized ProgressObject redeploy(TargetModuleID[] arg0, File arg1, File arg2) throws UnsupportedOperationException, IllegalStateException {
            return delegate.redeploy(arg0, arg1, arg2);
        }

        public synchronized boolean isRedeploySupported() {
            return delegate.isRedeploySupported();
        }

        public synchronized boolean isLocaleSupported(Locale arg0) {
            return delegate.isLocaleSupported(arg0);
        }

        public synchronized boolean isDConfigBeanVersionSupported(DConfigBeanVersionType arg0) {
            return delegate.isDConfigBeanVersionSupported(arg0);
        }

        public synchronized Target[] getTargets() throws IllegalStateException {
            return delegate.getTargets();
        }

        public synchronized Locale[] getSupportedLocales() {
            return delegate.getSupportedLocales();
        }

        public synchronized TargetModuleID[] getRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
            return delegate.getRunningModules(arg0, arg1);
        }

        public synchronized TargetModuleID[] getNonRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
            return delegate.getNonRunningModules(arg0, arg1);
        }

        public synchronized Locale getDefaultLocale() {
            return delegate.getDefaultLocale();
        }

        public synchronized DConfigBeanVersionType getDConfigBeanVersion() {
            return delegate.getDConfigBeanVersion();
        }

        public synchronized Locale getCurrentLocale() {
            return delegate.getCurrentLocale();
        }

        public synchronized TargetModuleID[] getAvailableModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
            return delegate.getAvailableModules(arg0, arg1);
        }

        public synchronized ProgressObject distribute(Target[] arg0, ModuleType arg1, InputStream arg2, InputStream arg3) throws IllegalStateException {
            return delegate.distribute(arg0, arg1, arg2, arg3);
        }

        public synchronized ProgressObject distribute(Target[] arg0, InputStream arg1, InputStream arg2) throws IllegalStateException {
            return delegate.distribute(arg0, arg1, arg2);
        }

        public synchronized ProgressObject distribute(Target[] arg0, File arg1, File arg2) throws IllegalStateException {
            return delegate.distribute(arg0, arg1, arg2);
        }

        public synchronized DeploymentConfiguration createConfiguration(DeployableObject arg0) throws InvalidModuleException {
            return delegate.createConfiguration(arg0);
        }
    }

}
