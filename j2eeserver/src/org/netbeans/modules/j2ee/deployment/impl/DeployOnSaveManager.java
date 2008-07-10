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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTargetImpl;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class DeployOnSaveManager {

    public static enum DeploymentState {
        MODULE_NOT_DEPLOYED,

        MODULE_UPDATED,

        DEPLOYMENT_FAILED,

        SERVER_STATE_UNSUPPORTED
    }

    private static final Logger LOGGER = Logger.getLogger(DeployOnSaveManager.class.getName());

    private static final int DELAY = 300;

    private static DeployOnSaveManager instance;

    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    /**<i>GuardedBy("this")</i>*/
    private Map<J2eeModuleProvider, Set<File>> toDeploy = new HashMap<J2eeModuleProvider, Set<File>>();

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

    public void submitChangedArtifacts(J2eeModuleProvider provider, Iterable<File> artifacts) {
        assert provider != null;
        assert artifacts != null;

        synchronized (this) {
            Set<File> files = toDeploy.get(provider);
            if (files == null) {
                files = new HashSet<File>();
                toDeploy.put(provider, files);
            }
            for (File artifact : artifacts) {
                files.add(artifact);
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

            Map<J2eeModuleProvider, Set<File>> deployNow;
            synchronized (DeployOnSaveManager.this) {
                if (toDeploy.isEmpty()) {
                    return;
                }

                deployNow = toDeploy;
                toDeploy = new HashMap<J2eeModuleProvider, Set<File>>();
            }

            for (Map.Entry<J2eeModuleProvider, Set<File>> entry : deployNow.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    continue;
                }
                notifyServer(entry.getKey(), entry.getValue());
            }
        }

        private void notifyServer(J2eeModuleProvider provider, Iterable<File> artifacts) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                StringBuilder builder = new StringBuilder("Artifacts updated: [");
                for (File file : artifacts) {
                    builder.append(file.getAbsolutePath()).append(",");
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
            // deployment failed so do the standard deploy
            // this can happen when metadata are invalid for example
            if (lastState == DeploymentState.DEPLOYMENT_FAILED) {
                ProgressUI ui = new ProgressUI(NbBundle.getMessage(TargetServer.class,
                        "MSG_DeployOnSave", provider.getDeploymentName()), false);
                ui.start(Integer.valueOf(0));
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

            LOGGER.log(Level.FINE, "Deployment state {0}", state);
            synchronized (this) {
                lastDeploymentStates.put(provider, state);
            }
        }
    }
}
