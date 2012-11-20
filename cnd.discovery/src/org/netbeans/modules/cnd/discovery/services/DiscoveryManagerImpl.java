/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardAction;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ConsolidationStrategy;
import org.netbeans.modules.cnd.discovery.wizard.api.support.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public final class DiscoveryManagerImpl {

    public static final String BUILD_LOG_KEY = "build-log"; //NOI18N 
    public static final String BUILD_EXEC_KEY = "exec-log"; //NOI18N 
    private static final RequestProcessor RP = new RequestProcessor("Discovery Manager Worker", 1); //NOI18N
    private static final Map<CsmProject, CsmProgressListener> listeners = new WeakHashMap<CsmProject, CsmProgressListener>();
    private static final Map<NativeProject, CsmProgressListener> listeners2 = new WeakHashMap<NativeProject, CsmProgressListener>();

    private DiscoveryManagerImpl() {
    }

    public static void projectBuilt(Project project, Map<String, Object> artifacts, boolean isIncremental) {
        RP.post(new DiscoveryWorker(project, artifacts, isIncremental));
    }

    public static void discoverHeadersByModel(final Project project) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                final NativeProject np = project.getLookup().lookup(NativeProject.class);
                ImportProject.logger.log(Level.INFO, "Post fixing of excluded header files for project {0}", np); //NOI18N
                CsmProgressListener listener = new CsmProgressAdapter() {

                    @Override
                    public void projectParsingFinished(CsmProject aCsmProject) {
                        ImportProject.logger.log(Level.INFO, "Model parsing finished and ready to fixing of excluded header files for project {0}", aCsmProject); //NOI18N
                        final CsmProject csmProject = CsmModelAccessor.getModel().getProject(np);
                        if (csmProject != null && csmProject.equals(aCsmProject)) {
                            CsmListeners.getDefault().removeProgressListener(this);
                            DiscoveryManagerImpl.listeners2.remove(np);
                            DiscoveryProjectGenerator.fixExcludedHeaderFiles(project, ImportProject.logger);
                        }
                    }
                };
                DiscoveryManagerImpl.listeners2.put(np, listener);
                CsmListeners.getDefault().addProgressListener(listener);
            }
        });
    }

    private static final class DiscoveryWorker implements Runnable {

        private final Project project;
        private final Map<String, Object> artifacts;
        private final boolean isIncremental;

        DiscoveryWorker(Project project, Map<String, Object> artifacts, boolean isIncremental) {
            this.project = project;
            this.artifacts = artifacts;
            this.isIncremental = isIncremental;
        }

        @Override
        public void run() {
            final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension == null) {
                return;
            }
            String artifact = (String) artifacts.get(BUILD_EXEC_KEY);
            if (artifact != null) {
                final Map<String, Object> map = new HashMap<String, Object>();
                map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, findRoot());
                map.put(DiscoveryWizardDescriptor.EXEC_LOG_FILE, artifact);
                map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, ConsolidationStrategy.FILE_LEVEL);
                if (isIncremental) {
                    map.put(DiscoveryWizardDescriptor.INCREMENTAL, Boolean.TRUE);
                }
                if (extension.canApply(map, project)) {
                    try {
                        postModelTask();
                        extension.apply(map, project);
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                return;
            }
            artifact = (String) artifacts.get(BUILD_LOG_KEY);
            if (artifact != null) {
                final Map<String, Object> map = new HashMap<String, Object>();
                map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, findRoot());
                map.put(DiscoveryWizardDescriptor.LOG_FILE, artifact);
                map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, ConsolidationStrategy.FILE_LEVEL);
                if (isIncremental) {
                    map.put(DiscoveryWizardDescriptor.INCREMENTAL, Boolean.TRUE);
                }
                if (extension.canApply(map, project)) {
                    try {
                        postModelTask();
                        extension.apply(map, project);
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }

        private String findRoot() {
            return DiscoveryWizardAction.findSourceRoot(project);
        }

        private void postModelTask() {
            final NativeProject np = project.getLookup().lookup(NativeProject.class);
            final CsmProject csmProject = CsmModelAccessor.getModel().getProject(np);
            if (csmProject == null) {
                ImportProject.logger.log(Level.INFO, "Can not post fix excluded header files; no associated CsmProject for {0}", np); //NOI18N
                return;
            }
            ImportProject.logger.log(Level.INFO, "Post fixing of excluded header files for project {0}", csmProject); //NOI18N
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject aCsmProject) {
                    ImportProject.logger.log(Level.INFO, "Model parsing finished and ready to fixing of excluded header files for model project {0}", aCsmProject); //NOI18N
                    if (csmProject.equals(aCsmProject)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        DiscoveryManagerImpl.listeners.remove(aCsmProject);
                        DiscoveryProjectGenerator.fixExcludedHeaderFiles(DiscoveryWorker.this.project, ImportProject.logger);
                    }
                }
            };
            DiscoveryManagerImpl.listeners.put(csmProject,listener);
            CsmListeners.getDefault().addProgressListener(listener);
        }
    }
}
