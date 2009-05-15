/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTargetImpl;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class DeployOnSaveManager {

    public static enum DeploymentState {

        MODULE_NOT_DEPLOYED,

        MODULE_UPDATED,

        MODULE_HOT_SWAPPED,

        DEPLOYMENT_FAILED,

        SERVER_STATE_UNSUPPORTED
    }

    private static final Logger LOGGER = Logger.getLogger(DeployOnSaveManager.class.getName());

    private static final int DELAY = 300;

    private static final int PROGRESS_DELAY = 200;

    private static DeployOnSaveManager instance;

    private final WeakHashMap<J2eeModuleProvider, CompileOnSaveListener> compileListeners = new WeakHashMap<J2eeModuleProvider, CompileOnSaveListener>();

    private final WeakHashMap<J2eeModuleProvider, CopyOnSaveListener> copyListeners = new WeakHashMap<J2eeModuleProvider, CopyOnSaveListener>();

    /**
     * We need a custom thread factory because the default one stores the
     * ThreadGroup in constructor. If the group is destroyed in between
     * the submit throws IllegalThreadStateException.
     */
    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactory() {

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);

            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    });

    //private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    /**<i>GuardedBy("this")</i>*/
    private Map<J2eeModuleProvider, Set<Artifact>> toDeploy = new HashMap<J2eeModuleProvider, Set<Artifact>>();

    /**<i>GuardedBy("this")</i>*/
    private Map<J2eeModuleProvider, DeploymentState> lastDeploymentStates = new HashMap<J2eeModuleProvider, DeploymentState>();

    /**<i>GuardedBy("this")</i>*/
    private Future<?> current;

    private DeployOnSaveManager() {
        super();
    }

    public static synchronized DeployOnSaveManager getDefault() {
        if (instance == null) {
            instance = new DeployOnSaveManager();
        }
        return instance;
    }

    public void startListening(J2eeModuleProvider j2eeProvider) {
        synchronized (this) {
            if (compileListeners.containsKey(j2eeProvider)) {
                // this is due to EAR childs :(
                if (j2eeProvider instanceof J2eeApplicationProvider) {
                    stopListening(j2eeProvider);
                } else {
                    LOGGER.log(Level.FINE, "Already listening on {0}", j2eeProvider);
                    return;
                }
            }

            List<J2eeModuleProvider> providers = new ArrayList<J2eeModuleProvider>(4);
            providers.add(j2eeProvider);

            if (j2eeProvider instanceof J2eeApplicationProvider) {
                Collections.addAll(providers,
                        ((J2eeApplicationProvider) j2eeProvider).getChildModuleProviders());
            }

            // get all binary urls
            List<URL> urls = new ArrayList<URL>();
            for (J2eeModuleProvider provider : providers) {
                for (FileObject file : provider.getSourceFileMap().getSourceRoots()) {
                    URL url = URLMapper.findURL(file, URLMapper.EXTERNAL);
                    if (url != null) {
                        urls.add(url);
                    }
                }
            }

            // register CLASS listener
            CompileOnSaveListener listener = new CompileOnSaveListener(j2eeProvider, urls);
            for (URL url :urls) {
                BuildArtifactMapper.addArtifactsUpdatedListener(url, listener);
            }
            compileListeners.put(j2eeProvider, listener);

            // register WEB listener
            J2eeModuleProvider.DeployOnSaveSupport support = j2eeProvider.getDeployOnSaveSupport();
            if (support != null) {
                CopyOnSaveListener copyListener = new CopyOnSaveListener(j2eeProvider);
                support.addArtifactListener(copyListener);
                copyListeners.put(j2eeProvider, copyListener);
            }
        }
    }

    public void stopListening(J2eeModuleProvider j2eeProvider) {
        synchronized (this) {
            CompileOnSaveListener removed = compileListeners.remove(j2eeProvider);
            if (removed == null) {
                LOGGER.log(Level.FINE, "Not compile-listening on {0}", j2eeProvider);
            } else {
                for (URL url : removed.getRegistered()) {
                    BuildArtifactMapper.removeArtifactsUpdatedListener(url, removed);
                }
            }

            CopyOnSaveListener copyRemoved = copyListeners.remove(j2eeProvider);
            if (removed == null) {
                LOGGER.log(Level.FINE, "Not copy-listening on {0}", j2eeProvider);
            } else {
                J2eeModuleProvider.DeployOnSaveSupport support = j2eeProvider.getDeployOnSaveSupport();
                if (support != null) {
                    support.removeArtifactListener(copyRemoved);
                }
            }
        }
    }

    public static boolean isServerStateSupported(ServerInstance si) {
        return si.isRunning() && !si.isSuspended();
    }

    public void notifyInitialDeployment(J2eeModuleProvider provider) {
        synchronized (this) {
            if (compileListeners.containsKey(provider)) {
                // this is due to EAR childs :(
                if (provider instanceof J2eeApplicationProvider) {
                    startListening(provider);
                }
            }

            if (!lastDeploymentStates.containsKey(provider)) {
                lastDeploymentStates.put(provider, DeploymentState.MODULE_UPDATED);
            }
        }
    }

    public void submitChangedArtifacts(J2eeModuleProvider provider, Iterable<Artifact> artifacts) {
        assert provider != null;
        assert artifacts != null;

        synchronized (this) {
            Set<Artifact> preparedArtifacts = toDeploy.get(provider);
            if (preparedArtifacts == null) {
                preparedArtifacts = new HashSet<Artifact>();
                toDeploy.put(provider, preparedArtifacts);
            }
            for (Artifact artifact : artifacts) {
                preparedArtifacts.add(artifact);
            }

            boolean delayed = true;
            if (current != null && !current.isDone()) {
                // TODO interruption throws exception to user from lower levels :((
                // this is dummy interruption signal handling :(
                current.cancel(false);
                delayed = false;
            }

            current = EXECUTOR.submit(new DeployTask(delayed));
        }
    }

    private static final class CompileOnSaveListener implements ArtifactsUpdated {

        private final J2eeModuleProvider provider;

        private final List<URL> registered;

        public CompileOnSaveListener(J2eeModuleProvider provider, List<URL> registered) {
            this.provider = provider;
            this.registered = registered;
        }

        public List<URL> getRegistered() {
            return registered;
        }

        public void artifactsUpdated(Iterable<File> artifacts) {
            J2eeModuleProvider.DeployOnSaveClassInterceptor interceptor = provider.getDeployOnSaveClassInterceptor();
            Set<Artifact> realArtifacts = new HashSet<Artifact>();
            for (File file : artifacts) {
                if (file != null) {
                    Artifact a = Artifact.forFile(file);
                    if (interceptor != null) {
                        a = interceptor.convert(a);
                    }
                    realArtifacts.add(a);
                }
            }

            DeployOnSaveManager.getDefault().submitChangedArtifacts(provider, realArtifacts);
        }

    }

    private static final class CopyOnSaveListener implements ArtifactListener {

        private final J2eeModuleProvider provider;

        public CopyOnSaveListener(J2eeModuleProvider provider) {
            this.provider = provider;
        }

        public void artifactsUpdated(Iterable<Artifact> artifacts) {
            DeployOnSaveManager.getDefault().submitChangedArtifacts(provider, artifacts);
        }

    }

    private class DeployTask implements Runnable {

        private final boolean delayed;

        public DeployTask(boolean delayed) {
            this.delayed = delayed;
        }

        public void run() {
            if (delayed) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    return;
                }
            }

            LOGGER.log(Level.FINE, "Performing pending deployments");

            Map<J2eeModuleProvider, Set<Artifact>> deployNow;
            synchronized (DeployOnSaveManager.this) {
                if (toDeploy.isEmpty()) {
                    return;
                }

                deployNow = toDeploy;
                toDeploy = new HashMap<J2eeModuleProvider, Set<Artifact>>();
            }

            for (Map.Entry<J2eeModuleProvider, Set<Artifact>> entry : deployNow.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    continue;
                }
                notifyServer(entry.getKey(), entry.getValue());
            }
        }

        private void notifyServer(J2eeModuleProvider provider, Iterable<Artifact> artifacts) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                StringBuilder builder = new StringBuilder("Artifacts updated: [");
                for (Artifact artifact : artifacts) {
                    builder.append(artifact.getFile().getAbsolutePath()).append(",");
                }
                builder.setLength(builder.length() - 1);
                builder.append("]");
                LOGGER.log(Level.FINEST, builder.toString());
            }

            DeploymentState lastState;
            synchronized (this) {
                lastState = lastDeploymentStates.get(provider);
                if (lastState == null) {
                    lastState = DeploymentState.MODULE_NOT_DEPLOYED;
                }
            }

            DeploymentTargetImpl deploymentTarget = new DeploymentTargetImpl(provider, null);
            TargetServer server = new TargetServer(deploymentTarget);

            DeploymentState state;
            // DEPLOYMENT_FAILED - this can happen when metadata are invalid for example
            // SERVER_STATE_UNSUPPORTED - this can happen when server in suspended mode
            // null - app has not been deployed so far
            if (lastState == null || lastState == DeploymentState.DEPLOYMENT_FAILED
                    || (lastState == DeploymentState.SERVER_STATE_UNSUPPORTED
                        && isServerStateSupported(deploymentTarget.getServer().getServerInstance()))) {

                ProgressUI ui = new ProgressUI(NbBundle.getMessage(TargetServer.class,
                        "MSG_DeployOnSave", provider.getDeploymentName()), false);
                ui.start(Integer.valueOf(PROGRESS_DELAY));
                try {
                    TargetModule[] modules = server.deploy(ui, true);
                    if (modules == null || modules.length <= 0) {
                        state = DeploymentState.DEPLOYMENT_FAILED;
                    } else {
                        state = DeploymentState.MODULE_UPDATED;
                    }
                    // TODO start listening ?
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    state = DeploymentState.DEPLOYMENT_FAILED;
                } catch (ServerException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    state = DeploymentState.DEPLOYMENT_FAILED;
                } finally {
                    ui.finish();
                }
            // standard incremental deploy
            } else {
                state = server.notifyArtifactsUpdated(provider, artifacts);
            }

            String message = null;
            switch (state) {
                case MODULE_UPDATED:
                    message = NbBundle.getMessage(DeployOnSaveManager.class,
                            "MSG_DeployOnSave_Deployed", provider.getDeploymentName());
                    break;
                case DEPLOYMENT_FAILED:
                    message = NbBundle.getMessage(DeployOnSaveManager.class,
                            "MSG_DeployOnSave_Failed", provider.getDeploymentName());
                    break;
                case SERVER_STATE_UNSUPPORTED:
                    message = NbBundle.getMessage(DeployOnSaveManager.class,
                            "MSG_DeployOnSave_Unsupported", provider.getDeploymentName());
                    break;
                default:
                    message = null;
            }

            if (message != null) {
                StatusDisplayer.getDefault().setStatusText(message);
            }

            LOGGER.log(Level.FINE, "Deployment state {0}", state);
            synchronized (this) {
                lastDeploymentStates.put(provider, state);
            }
        }
    }
}
