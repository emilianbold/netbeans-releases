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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardAction;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ConsolidationStrategy;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

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
                            DiscoveryManagerImpl.fixExcludedHeaderFiles(project, ImportProject.logger);
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
                return;
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
                    ImportProject.logger.log(Level.INFO, "Model parsing finished and ready to fixing of excluded header files for project {0}", aCsmProject); //NOI18N
                    if (csmProject.equals(aCsmProject)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        DiscoveryManagerImpl.listeners.remove(aCsmProject);
                        DiscoveryManagerImpl.fixExcludedHeaderFiles(DiscoveryWorker.this.project, ImportProject.logger);
                    }
                }
            };
            DiscoveryManagerImpl.listeners.put(csmProject,listener);
            CsmListeners.getDefault().addProgressListener(listener);
        }
    }

    public static void fixExcludedHeaderFiles(Project makeProject, Logger logger) {
        CsmModel model = CsmModelAccessor.getModel();
        if (!(model instanceof ModelImpl && makeProject != null)) {
            if (logger != null) {
                logger.log(Level.INFO, "Failed fixing of excluded header files for project {0}", makeProject); // NOI18N
            }
            return;
        }
        NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
        if (np == null) {
            if (logger != null) {
                logger.log(Level.INFO, "Not found make project for fixing of excluded header files for project {0}", makeProject); // NOI18N
            }
            return;
        }
        final CsmProject p = model.getProject(np);
        if (p == null) {
            if (logger != null) {
                logger.log(Level.INFO, "Not found model project for fixing of excluded header files for project {0}", np); // NOI18N
            }
            return;
        }
        if (logger != null) {
            logger.log(Level.INFO, "Start fixing of excluded header files for project {0}", p); // NOI18N
        }
        Set<String> needCheck = new HashSet<String>();
        Set<String> needAdd = new HashSet<String>();
        Map<String, Item> normalizedItems = DiscoveryManagerImpl.initNormalizedNames(makeProject);
        for (CsmFile file : p.getAllFiles()) {
            if (file instanceof FileImpl) {
                FileImpl impl = (FileImpl) file;
                NativeFileItem item = impl.getNativeFileItem();
                if (item == null) {
                    String path = impl.getAbsolutePath().toString();
                    item = normalizedItems.get(path);
                }
                boolean isLineDirective = false;
                if (item != null
                        && item.getLanguage() == Language.C_HEADER
                        && (p instanceof ProjectBase)) {
                    ProjectBase pb = (ProjectBase) p;
                    Set<CsmFile> parentFiles = pb.getParentFiles(file);
                    if (parentFiles.isEmpty()) {
                        isLineDirective = true;
                    }
                }
                if (item != null && np.equals(item.getNativeProject()) && item.isExcluded()) {
                    if (item instanceof Item) {
                        if (logger != null) {
                            logger.log(Level.FINE, "#fix excluded->included header for file {0}", impl.getAbsolutePath()); // NOI18N
                        }
                        ProjectBridge.setExclude((Item) item, false);
                        ProjectBridge.setHeaderTool((Item) item);
                        if (file.isHeaderFile()) {
                            needCheck.add(item.getAbsolutePath());
                        }
                    }
                } else if (isLineDirective && item != null && np.equals(item.getNativeProject()) && !item.isExcluded()) {
                    if (item instanceof Item) {
                        if (logger != null) {
                            logger.log(Level.FINE, "#fix included->excluded for file {0}", impl.getAbsolutePath()); // NOI18N
                        }
                        ProjectBridge.setExclude((Item) item, true);
                    }
                } else if (item == null) {
                    // It should be in project?
                    if (file.isHeaderFile()) {
                        String path = impl.getAbsolutePath().toString();
                        needAdd.add(path);
                    }
                }
            }
        }
        if (needCheck.size() > 0 || needAdd.size() > 0) {
            ProjectBridge bridge = new ProjectBridge(makeProject);
            if (bridge.isValid()) {
                if (needAdd.size() > 0) {
                    Map<String, Folder> prefferedFolders = bridge.prefferedFolders();
                    for (String path : needAdd) {
                        String name = path;
                        if (Utilities.isWindows()) {
                            path = path.replace('\\', '/'); // NOI18N
                        }
                        int i = path.lastIndexOf('/'); // NOI18N
                        if (i >= 0) {
                            String folderPath = path.substring(0, i);
                            Folder prefferedFolder = prefferedFolders.get(folderPath);
                            if (prefferedFolder == null) {
                                LinkedList<String> mkFolder = new LinkedList<String>();
                                while (true) {
                                    i = folderPath.lastIndexOf('/'); // NOI18N
                                    if (i > 0) {
                                        mkFolder.addLast(folderPath.substring(i + 1));
                                        folderPath = folderPath.substring(0, i);
                                        prefferedFolder = prefferedFolders.get(folderPath);
                                        if (prefferedFolder != null) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                if (prefferedFolder != null) {
                                    while (true) {
                                        if (mkFolder.isEmpty()) {
                                            break;
                                        }
                                        String segment = mkFolder.pollLast();
                                        prefferedFolder = prefferedFolder.addNewFolder(segment, segment, true, (Folder.Kind) null);
                                        folderPath += "/" + segment; // NOI18N
                                        prefferedFolders.put(folderPath, prefferedFolder);
                                    }
                                }
                            }
                            if (prefferedFolder != null) {
                                String relPath = bridge.getRelativepath(name);
                                Item item = bridge.getProjectItem(relPath);
                                if (item == null) {
                                    item = bridge.createItem(name);
                                    item = prefferedFolder.addItem(item);
                                }
                                if (item != null) {
                                    ProjectBridge.setHeaderTool(item);
                                    if (!MIMENames.isCppOrCOrFortran(item.getMIMEType())) {
                                        needCheck.add(path);
                                    }
                                    ProjectBridge.excludeItemFromOtherConfigurations(item);
                                }
                            }
                        }
                    }
                }
                if (needCheck.size() > 0) {
                    bridge.checkForNewExtensions(needCheck);
                }
            }
        }
        saveMakeConfigurationDescriptor(makeProject);
    }

    public static void saveMakeConfigurationDescriptor(Project lastSelectedProject) {
        ConfigurationDescriptorProvider pdp = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (makeConfigurationDescriptor != null) {
            makeConfigurationDescriptor.setModified();
            makeConfigurationDescriptor.save();
            makeConfigurationDescriptor.checkForChangedItems(lastSelectedProject, null, null);
        }
    }

    public static void writeDefaultVersionedConfigurations(Project lastSelectedProject) {
        ConfigurationDescriptorProvider pdp = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (makeConfigurationDescriptor != null) {
            makeConfigurationDescriptor.writeDefaultVersionedConfigurations();
        }
    }

    public static HashMap<String, Item> initNormalizedNames(Project makeProject) {
        HashMap<String, Item> normalizedItems = new HashMap<String, Item>();
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            if (makeConfigurationDescriptor != null) {
                for (Item item : makeConfigurationDescriptor.getProjectItems()) {
                    normalizedItems.put(item.getNormalizedPath(), item);
                }
            }
        }
        return normalizedItems;
    }
}
