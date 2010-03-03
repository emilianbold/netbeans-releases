/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel.MyProgress;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension.class)
public class DiscoveryExtension implements IteratorExtension {
    
    /** Creates a new instance of DiscoveryExtension */
    public DiscoveryExtension() {
    }
    
    public Set<FileObject> createProject(WizardDescriptor wizard) throws IOException{
        return new ImportProject(wizard).create();
    }

    public void apply(WizardDescriptor wizard, Project project) throws IOException {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.setProject(project);
        DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(descriptor);
        generator.makeProject();
    }
    
    public void apply(Map<String, Object> map, Project project) throws IOException {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        descriptor.setProject(project);
        DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(descriptor);
        generator.makeProject();
    }


    public Map<String,Object> clone(WizardDescriptor wizard){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, wizard.getProperty("buildCommandWorkingDirTextField")); // NOI18N
        map.put(DiscoveryWizardDescriptor.BUILD_RESULT, wizard.getProperty("outputTextField")); // NOI18N
        map.put(DiscoveryWizardDescriptor.ADDITIONAL_LIBRARIES, wizard.getProperty("additionalLibraries")); // NOI18N
        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, wizard.getProperty("consolidationLevel")); // NOI18N
        return map;
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.clean();
    }
    
    public boolean isApplicable(DiscoveryDescriptor descriptor) {
        Progress progress = new MyProgress();
        progress.start(0);
        try {
            if (isApplicableDwarfExecutable(descriptor)){
                return true;
            } else if (isApplicableMakeLog(descriptor)){
                return true;
            }
            return isApplicableDwarfFolder(descriptor);
        } finally {
            progress.done();
        }
    }
    
    private boolean isApplicableDwarfExecutable(DiscoveryDescriptor descriptor){
        String selectedExecutable = descriptor.getBuildResult();
        if (selectedExecutable == null) {
            return false;
        }
        File file = new File(selectedExecutable);
        if (!file.exists()) {
            return false;
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = findProvider("dwarf-executable"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("executable").setValue(selectedExecutable); // NOI18N
            provider.getProperty("libraries").setValue(new String[0]); // NOI18N
            if (provider.canAnalyze(proxy)>0){
                descriptor.setProvider(provider);
                return true;
            }
        }
        return false;
    }

    private boolean isApplicableDwarfFolder(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return false;
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = findProvider("dwarf-folder"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("folder").setValue(rootFolder); // NOI18N
            if (provider.canAnalyze(proxy)>0){
                descriptor.setProvider(provider);
                return true;
            }
        }
        return false;
    }

    private boolean isApplicableMakeLog(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return false;
        }
        String logFile = descriptor.getBuildLog();
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = findProvider("make-log"); // NOI18N
        if (provider != null && provider.isApplicable(proxy)){
            provider.getProperty("make-log-file").setValue(logFile); // NOI18N
            if (provider.canAnalyze(proxy)>0){
                descriptor.setProvider(provider);
                return true;
            }
        }
        return false;
    }
    
    public boolean isApplicable(WizardDescriptor wizard) {
        String selectedExecutable = (String)wizard.getProperty("outputTextField"); // NOI18N
        String rootFolder = (String)wizard.getProperty("buildCommandWorkingDirTextField"); // NOI18N
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.setBuildResult(selectedExecutable);
        descriptor.setRootFolder(rootFolder);
        return isApplicable(descriptor);
    }
    
    public String getProviderID(WizardDescriptor wizard){
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        return descriptor.getProviderID();
    }
    
    public boolean canApply(DiscoveryDescriptor descriptor) {
        if (!isApplicable(descriptor)){
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
    
    public boolean canApply(WizardDescriptor wizard, Project project) {
        String selectedExecutable = (String)wizard.getProperty("outputTextField"); // NOI18N
        String additional = (String)wizard.getProperty("additionalLibraries"); // NOI18N
        String level = (String)wizard.getProperty("consolidationLevel"); // NOI18N
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.setBuildResult(selectedExecutable);
        descriptor.setAditionalLibraries(additional);
        descriptor.setLevel(level);
        descriptor.setProject(project);
        return canApply(descriptor);
    }

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

    private static class ProjectProxyImpl implements ProjectProxy {
            private DiscoveryDescriptor descriptor;
            private ProjectProxyImpl(DiscoveryDescriptor descriptor){
                this.descriptor = descriptor;
            }
            public boolean createSubProjects() {
                return false;
            }
            public Project getProject() {
                return null;
            }
            public String getMakefile() {
                return null;
            }
            public String getSourceRoot() {
                return descriptor.getRootFolder();
            }
            public String getExecutable() {
                return descriptor.getBuildResult();
            }
            public String getWorkingFolder() {
                return null;
            }
        };

}
