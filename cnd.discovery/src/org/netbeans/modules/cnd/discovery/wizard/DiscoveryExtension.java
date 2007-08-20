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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public class DiscoveryExtension implements IteratorExtension {
    
    /** Creates a new instance of DiscoveryExtension */
    public DiscoveryExtension() {
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
        if (isApplicableDwarfExecutable(descriptor)){
            return true;
        }
        return isApplicableDwarfFolder(descriptor);
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
        ProjectProxy proxy = new ProjectProxy(){
            public boolean createSubProjects() {
                return false;
            }
            public Object getProject() {
                return null;
            }
        };
        DiscoveryProvider provider = findProvider("dwarf-executable"); // NOI18N
        if (provider != null){
            provider.getProperty("executable").setValue(selectedExecutable); // NOI18N
            provider.getProperty("libraries").setValue(new String[0]); // NOI18N
            if (provider.canAnalyze(proxy)){
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
        ProjectProxy proxy = new ProjectProxy(){
            public boolean createSubProjects() {
                return false;
            }
            public Object getProject() {
                return null;
            }
        };
        DiscoveryProvider provider = findProvider("dwarf-folder"); // NOI18N
        if (provider != null){
            provider.getProperty("folder").setValue(rootFolder); // NOI18N
            if (provider.canAnalyze(proxy)){
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
        } else {
            return false;
        }
        SelectConfigurationPanel.buildModel(descriptor);
        return !descriptor.isInvokeProvider()
        && descriptor.getConfigurations() != null
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
    
    private DiscoveryProvider findProvider(String providerID){
        Lookup.Result<DiscoveryProvider> providers = Lookup.getDefault().lookup(new Lookup.Template<DiscoveryProvider>(DiscoveryProvider.class));
        for(DiscoveryProvider provider : providers.allInstances()){
            if (providerID.equals(provider.getID())) {
                provider.clean();
                return provider;
            }
        }
        return null;
    }

}
