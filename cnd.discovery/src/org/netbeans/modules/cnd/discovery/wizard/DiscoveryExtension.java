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

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.projectimport.ImportExecutable;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.services.DiscoveryManagerImpl;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel.MyProgress;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.support.impl.DiscoveryProjectGeneratorImpl;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension.class)
public class DiscoveryExtension implements IteratorExtension, DiscoveryExtensionInterface {
    
    /** Creates a new instance of DiscoveryExtension */
    public DiscoveryExtension() {
    }

    @Override
    public void discoverArtifacts(Map<String, Object> map) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        Applicable applicable = isApplicable(descriptor, false);
        if (applicable != null) {
            if (applicable.isApplicable()) {
                descriptor.setCompilerName(applicable.getCompilerName());
                descriptor.setDependencies(applicable.getDependencies());
                descriptor.setSearchPaths(applicable.getSearchPaths());
                descriptor.setRootFolder(applicable.getSourceRoot());
                descriptor.setErrors(applicable.getErrors());
            } else {
                descriptor.setErrors(applicable.getErrors());
            }
        }
    }
    
    @Override
    public Set<FileObject> createProject(WizardDescriptor wizard) throws IOException{
        return new ImportProject(wizard).create();
    }

    @Override
    public void apply(Map<String, Object> map, Project project) throws IOException {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        descriptor.setProject(project);
        DiscoveryProjectGeneratorImpl generator = new DiscoveryProjectGeneratorImpl(descriptor);
        generator.makeProject();
    }

    public DiscoveryExtensionInterface.Applicable isApplicable(DiscoveryDescriptor descriptor, boolean findMain) {
        Progress progress = new MyProgress();
        progress.start(0);
        try {
            List<String> errors = new  ArrayList<String>();
            DiscoveryExtensionInterface.Applicable applicable;
            applicable = isApplicableExecLog(descriptor);
            if (applicable.isApplicable()){
                return applicable;
            }
            applicable = isApplicableDwarfExecutable(descriptor, findMain);
            if (applicable.isApplicable()){
                return applicable;
            }
            if (applicable.getErrors() != null) {
                errors.addAll(applicable.getErrors());
            }
            applicable = isApplicableMakeLog(descriptor);
            if (applicable.isApplicable()){
                return applicable;
            }
            if (applicable.getErrors() != null) {
                errors.addAll(applicable.getErrors());
            }
            applicable = isApplicableDwarfFolder(descriptor);
            if (applicable.isApplicable()){
                return applicable;
            }
            if (applicable.getErrors() != null) {
                errors.addAll(applicable.getErrors());
            }
            if (!errors.isEmpty()) {
                return ApplicableImpl.getNotApplicable(errors);
            } else {
                return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NoExecutable_NoBaseFolder"))); // NOI18N
            }
        } finally {
            progress.done();
        }
    }
    
    private DiscoveryExtensionInterface.Applicable isApplicableDwarfExecutable(DiscoveryDescriptor descriptor, boolean findMain){
        String selectedExecutable = descriptor.getBuildResult();
        if (selectedExecutable == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        FileSystem fileSystem = descriptor.getFileSystem();
        if (fileSystem == null) {
            fileSystem = FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
        }
        FileObject file = new FSPath(fileSystem, selectedExecutable).getFileObject();
        if (file == null || !file.isValid()) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundExecutable",selectedExecutable))); // NOI18N
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider("dwarf-executable"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("executable").setValue(selectedExecutable); // NOI18N
            provider.getProperty("libraries").setValue(new String[0]); // NOI18N
            provider.getProperty("filesystem").setValue(descriptor.getFileSystem()); // NOI18N
            ProviderProperty property = provider.getProperty("find_main");
            if (property != null) {
                if (findMain) {
                    property.setValue(Boolean.TRUE);
                } else {
                    property.setValue(Boolean.FALSE);
                }
            }
            Applicable canAnalyze = provider.canAnalyze(proxy);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            } else {
                if (canAnalyze.getErrors().size() > 0) {
                    return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                } else {
                    return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeExecutable",selectedExecutable))); // NOI18N
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }
    
    private DiscoveryExtensionInterface.Applicable  isApplicableDwarfFolder(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider("dwarf-folder"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("folder").setValue(rootFolder); // NOI18N
            Applicable canAnalyze = provider.canAnalyze(proxy);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            } else {
                if (canAnalyze.getErrors().size() > 0) {
                    return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                } else {
                    return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeFolder",rootFolder))); // NOI18N
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }

    private DiscoveryExtensionInterface.Applicable  isApplicableMakeLog(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        String logFile = descriptor.getBuildLog();
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider("make-log"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("make-log-file").setValue(logFile); // NOI18N
            Applicable canAnalyze = provider.canAnalyze(proxy);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            } else {
                if (canAnalyze.getErrors().size() > 0) {
                    return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                } else {
                    return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeBuildLog",logFile))); // NOI18N
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }
    
    private DiscoveryExtensionInterface.Applicable  isApplicableExecLog(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        String logFile = descriptor.getExecLog();
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider("exec-log"); // NOI18N
        if (provider != null) {
            provider.getProperty("exec-log-file").setValue(logFile); // NOI18N
            if (provider.isApplicable(proxy)){
                Applicable canAnalyze = provider.canAnalyze(proxy);
                if (canAnalyze.isApplicable()){
                    descriptor.setProvider(provider);
                    return canAnalyze;
                } else {
                    if (canAnalyze.getErrors().size() > 0) {
                        return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                    } else {
                        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeBuildLog",logFile))); // NOI18N
                    }
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }
    
    public DiscoveryExtensionInterface.Applicable isApplicable(Map<String,Object> map, Project project, boolean findMain) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        return isApplicable(descriptor, findMain);
    }
    
    public boolean canApply(DiscoveryDescriptor descriptor) {
        if (!isApplicable(descriptor, false).isApplicable()){
            return false;
        }
        String level = descriptor.getLevel();
        if (level == null || level.length() == 0){
            return false;
        }
        DiscoveryProvider provider = descriptor.getProvider();
        if (provider == null){
            return false;
        }
        if ("dwarf-executable".equals(provider.getID())){ // NOI18N
            String selectedExecutable = descriptor.getBuildResult();
            String additional = descriptor.getAditionalLibraries();
            provider.getProperty("executable").setValue(selectedExecutable); // NOI18N
            ProviderProperty property = provider.getProperty("find_main");
            if (property != null) {
                property.setValue(Boolean.TRUE);
            }
            if (additional != null && additional.length()>0){
                List<String> list = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(additional,";");  // NOI18N
                while(st.hasMoreTokens()){
                    list.add(st.nextToken());
                }
                provider.getProperty("libraries").setValue(list.toArray(new String[list.size()])); // NOI18N
            } else {
                provider.getProperty("libraries").setValue(new String[0]); // NOI18N
            }
        } else if ("dwarf-folder".equals(provider.getID())){ // NOI18N
            String rootFolder = descriptor.getRootFolder();
            provider.getProperty("folder").setValue(rootFolder); // NOI18N
        } else if ("make-log".equals(provider.getID())){ // NOI18N
            //String rootFolder = descriptor.getRootFolder();
            //provider.getProperty("folder").setValue(rootFolder); // NOI18N
        } else if ("exec-log".equals(provider.getID())){ // NOI18N
            //String rootFolder = descriptor.getRootFolder();
            //provider.getProperty("folder").setValue(rootFolder); // NOI18N
        } else {
            return false;
        }
        SelectConfigurationPanel.buildModel(descriptor);
        return !descriptor.isInvokeProvider()
            && descriptor.getConfigurations() != null && descriptor.getConfigurations().size() > 0
            && descriptor.getIncludedFiles() != null;
    }
    
    @Override
    public boolean canApply(Map<String, Object> map, Project project) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        descriptor.setProject(project);
        return canApply(descriptor);
    }
    
    @Override
    public void discoverProject(final Map<String, Object> map, final Project lastSelectedProject, ProjectKind projectKind) {
        ImportExecutable importer = new ImportExecutable(map, lastSelectedProject, projectKind);
        if (lastSelectedProject != null) {
            importer.process(this);
        }
    }

    @Override
    public void discoverHeadersByModel(Project project) {
        DiscoveryManagerImpl.discoverHeadersByModel(project);
    }

    private static class ProjectProxyImpl implements ProjectProxy {

        private DiscoveryDescriptor descriptor;

        private ProjectProxyImpl(DiscoveryDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public boolean createSubProjects() {
            return false;
        }

        @Override
        public Project getProject() {
            return null;
        }

        @Override
        public String getMakefile() {
            return null;
        }

        @Override
        public String getSourceRoot() {
            return descriptor.getRootFolder();
        }

        @Override
        public String getExecutable() {
            return descriptor.getBuildResult();
        }

        @Override
        public String getWorkingFolder() {
            return null;
        }

        @Override
        public boolean mergeProjectProperties() {
            return false;
        }
    };

}
