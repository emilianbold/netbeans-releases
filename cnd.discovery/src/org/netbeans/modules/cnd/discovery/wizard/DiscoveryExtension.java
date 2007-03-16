/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    
    public void uninitialize(WizardDescriptor wizard) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.clean();
    }

    public boolean isApplicable(DiscoveryDescriptor descriptor) {
        String selectedExecutable = descriptor.getBuildResult();
        if (selectedExecutable == null) {
            return false;
        }
        File file = new File(selectedExecutable);
        if (!file.exists()) {
            return false;
        }
        DiscoveryProvider provider = descriptor.getProvider();
        if (provider== null){
            provider = findProvider();
        }
        if (provider == null){
            return false;
        }
        provider.getProperty("executable").setValue(selectedExecutable);// NOI18N
        provider.getProperty("libraries").setValue(new String[0]);// NOI18N
        descriptor.setProvider(provider);
        return provider.canAnalyze(new ProjectProxy(){
            public boolean createSubProjects() {
                return false;
            }

            public Object getProject() {
                return null;
            }
        });
    }
    
    public boolean isApplicable(WizardDescriptor wizard) {
        String selectedExecutable = (String)wizard.getProperty("outputTextField"); // NOI18N
        if (selectedExecutable == null) {
            return false;
        }
        File file = new File(selectedExecutable);
        if (!file.exists()) {
            return false;
        }
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.setBuildResult(selectedExecutable);
        return isApplicable(descriptor);
    }

    public boolean canApply(DiscoveryDescriptor descriptor) {
        String level = descriptor.getLevel();
        if (level == null || level.length() == 0){
            return false;
        }
        String selectedExecutable = descriptor.getBuildResult();
        String additional = descriptor.getAditionalLibraries();

        DiscoveryProvider provider = descriptor.getProvider();
        if (provider== null){
            provider = findProvider();
        }
        if (provider == null){
            return false;
        }
        provider.getProperty("executable").setValue(selectedExecutable);// NOI18N
        if (additional != null && additional.length()>0){
            List<String> list = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(additional,";"); // NOI18N
            while(st.hasMoreTokens()){
                list.add(st.nextToken());
            }
            provider.getProperty("libraries").setValue(list.toArray(new String[list.size()]));// NOI18N
        } else {
            provider.getProperty("libraries").setValue(new String[0]);// NOI18N
        }
        descriptor.setProvider(provider);
        SelectConfigurationPanel.buildModel(descriptor);
        return !descriptor.isInvokeProvider()
            && descriptor.getConfigurations() != null
            && descriptor.getIncludedFiles() != null;
    }
    
    public boolean canApply(WizardDescriptor wizard, Project project) {
        String selectedExecutable = (String)wizard.getProperty("outputTextField"); // NOI18N
        if (selectedExecutable == null) {
            return false;
        }
        File file = new File(selectedExecutable);
        if (!file.exists()) {
            return false;
        }
        String additional = (String)wizard.getProperty("additionalLibraries"); // NOI18N
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        String level = (String)wizard.getProperty("consolidationLevel"); // NOI18N
        descriptor.setBuildResult(selectedExecutable);
        descriptor.setAditionalLibraries(additional);
        descriptor.setLevel(level);
        descriptor.setProject(project);
        return canApply(descriptor);
    }
    
    private DiscoveryProvider findProvider(){
        Lookup.Result providers = Lookup.getDefault().lookup(new Lookup.Template(DiscoveryProvider.class));
        for(Object p : providers.allInstances()){
            DiscoveryProvider provider = (DiscoveryProvider)p;
            if ("dwarf-executable".equals(provider.getID())) {// NOI18N
                provider.clean();
                return provider;
            }
        }
        return null;
    }
}
