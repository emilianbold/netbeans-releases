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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel.MyProgress;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.RunDialogPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension.class)
public class DiscoveryExtension implements IteratorExtension, DiscoveryExtensionInterface {
    private static final RequestProcessor RP = new RequestProcessor(RunDialogPanel.class.getName(), 2);
    
    /** Creates a new instance of DiscoveryExtension */
    public DiscoveryExtension() {
    }
    
    @Override
    public Set<FileObject> createProject(WizardDescriptor wizard) throws IOException{
        return new ImportProject(wizard).create();
    }

    @Override
    public void apply(Map<String, Object> map, Project project) throws IOException {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        descriptor.setProject(project);
        DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(descriptor);
        generator.makeProject();
    }

    public DiscoveryExtensionInterface.Applicable isApplicable(DiscoveryDescriptor descriptor) {
        Progress progress = new MyProgress();
        progress.start(0);
        try {
            DiscoveryExtensionInterface.Applicable applicable = isApplicableDwarfExecutable(descriptor);
            if (applicable.isApplicable()){
                return applicable;
            }
            applicable = isApplicableMakeLog(descriptor);
            if (applicable.isApplicable()){
                return applicable;
            }
            return isApplicableDwarfFolder(descriptor);
        } finally {
            progress.done();
        }
    }
    
    private DiscoveryExtensionInterface.Applicable isApplicableDwarfExecutable(DiscoveryDescriptor descriptor){
        String selectedExecutable = descriptor.getBuildResult();
        if (selectedExecutable == null) {
            return ApplicableImpl.NotApplicable;
        }
        File file = new File(selectedExecutable);
        if (!file.exists()) {
            return ApplicableImpl.NotApplicable;
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = findProvider("dwarf-executable"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("executable").setValue(selectedExecutable); // NOI18N
            provider.getProperty("libraries").setValue(new String[0]); // NOI18N
            Applicable canAnalyze = provider.canAnalyze(proxy);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            }
        }
        return ApplicableImpl.NotApplicable;
    }

    private DiscoveryExtensionInterface.Applicable  isApplicableDwarfFolder(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return ApplicableImpl.NotApplicable;
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = findProvider("dwarf-folder"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("folder").setValue(rootFolder); // NOI18N
            Applicable canAnalyze = provider.canAnalyze(proxy);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            }
        }
        return ApplicableImpl.NotApplicable;
    }

    private DiscoveryExtensionInterface.Applicable  isApplicableMakeLog(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return ApplicableImpl.NotApplicable;
        }
        String logFile = descriptor.getBuildLog();
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = findProvider("make-log"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("make-log-file").setValue(logFile); // NOI18N
            Applicable canAnalyze = provider.canAnalyze(proxy);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            }
        }
        return ApplicableImpl.NotApplicable;
    }
    
    private DiscoveryExtensionInterface.Applicable isApplicable(Map<String,Object> map, Project project) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        return isApplicable(descriptor);
    }
    
    public boolean canApply(DiscoveryDescriptor descriptor) {
        if (!isApplicable(descriptor).isApplicable()){
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
    
    /*package-local*/ static DiscoveryProvider findProvider(String providerID){
        for(DiscoveryProvider provider : Lookup.getDefault().lookupAll(DiscoveryProvider.class)){
            if (providerID.equals(provider.getID())) {
                provider.clean();
                return provider;
            }
        }
        return null;
    }

    private static final List<CsmProgressListener> listeners = new ArrayList<CsmProgressListener>(1);

    private void openFunction(final String functionName, Project makeProject) {
        if (makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.getPlatformProject().equals(np)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        listeners.remove(this);
                        if (project instanceof ProjectBase) {
                            String from = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + ':' + functionName + '('; // NOI18N
                            Collection<CsmOffsetableDeclaration> decls = ((ProjectBase)project).findDeclarationsByPrefix(from);
                            for(CsmOffsetableDeclaration decl : decls){
                                CsmUtilities.openSource(decl);
                                break;
                            }
                        }
                    }
                }
            };
            listeners.add(listener);
            CsmListeners.getDefault().addProgressListener(listener);
        }
    }

    @Override
    public void discoverProject(final Map<String, Object> map, final Project lastSelectedProject, final String functionToOpen) {
        switchModel(false, lastSelectedProject);
        RP.post(new Runnable() {

            @Override
            public void run() {
                ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor(true);
                Applicable applicable = isApplicable(map, lastSelectedProject);
                if (applicable.isApplicable()) {
                    String preferredCompiler = applicable.getCompilerName();
                    resetCompilerSet(configurationDescriptor.getActiveConfiguration(), preferredCompiler);
                    if (canApply(map, lastSelectedProject)) {
                        try {
                            apply(map, lastSelectedProject);
                            configurationDescriptor.setModified();
                            configurationDescriptor.save();
                            configurationDescriptor.checkForChangedItems(lastSelectedProject, null, null);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                switchModel(true, lastSelectedProject);
                if (functionToOpen != null) {
                    openFunction(functionToOpen, lastSelectedProject);
                }
            }
        });
    }

    private void switchModel(boolean state, Project makeProject) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                ((ModelImpl) model).enableProject(np);
            } else {
                ((ModelImpl) model).disableProject(np);
            }
        }
    }

    private void resetCompilerSet(MakeConfiguration configuration, String preferredCompiler){
        if (configuration != null) {
            if (preferredCompiler != null && preferredCompiler.length()>2) {
                if (preferredCompiler.indexOf("GNU") >= 0 || // NOI18N
                    preferredCompiler.indexOf("gcc") >= 0 || // NOI18N
                    preferredCompiler.indexOf("g++") >= 0) { // NOI18N
                    configuration.getCompilerSet().setCompilerSetName(new StringConfiguration(null, "GNU")); // NOI18N
                } else if (preferredCompiler.indexOf("Sun") >= 0 || // NOI18N
                           preferredCompiler.indexOf("CC") >= 0 || // NOI18N
                           preferredCompiler.indexOf("cc") >= 0) { // NOI18N
                    configuration.getCompilerSet().setCompilerSetName(new StringConfiguration(null, "OracleSolarisStudio")); // NOI18N
                }
            }
        }
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
    };

}
